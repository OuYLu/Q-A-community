package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BizException;
import com.community.common.result.ErrorCode;
import com.community.domain.permission.Permission;
import com.community.domain.role.Role;
import com.community.domain.role.RolePermission;
import com.community.domain.user.User;
import com.community.domain.user.UserRole;
import com.community.dto.admin.PageResult;
import com.community.dto.admin.PermissionGroupDTO;
import com.community.dto.admin.PermissionItemDTO;
import com.community.dto.admin.RolePermissionResponse;
import com.community.dto.admin.UserCreateRequest;
import com.community.dto.admin.UserDetailDTO;
import com.community.dto.admin.UserListItemDTO;
import com.community.mapper.PermissionMapper;
import com.community.mapper.RoleMapper;
import com.community.mapper.RolePermissionMapper;
import com.community.mapper.UserRoleMapper;
import com.community.service.AdminRbacService;
import com.community.service.PasswordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminRbacServiceImpl implements AdminRbacService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private com.community.mapper.UserMapper userMapper;

    @Autowired
    private PasswordService passwordService;

    @Override
    public List<Permission> listPermissions(String keyword) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Permission::getCode, keyword)
                    .or()
                    .like(Permission::getName, keyword);
        }
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public Role createRole(String code, String name, String description) {
        Role exist = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, code));
        if (exist != null) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "角色 code 已存在");
        }
        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setCreatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    @Override
    public List<Role> listRoles(String keyword) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Role::getCode, keyword)
                    .or()
                    .like(Role::getName, keyword);
        }
        return roleMapper.selectList(wrapper);
    }

    @Override
    public Role updateRole(Long roleId, String name, String description) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "角色不存在");
        }
        role.setName(name);
        role.setDescription(description);
        roleMapper.updateById(role);
        return role;
    }

    @Override
    public void deleteRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "角色不存在");
        }
        Long cnt = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId));
        if (cnt != null && cnt > 0) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "角色已被分配给用户，无法删除");
        }
        roleMapper.deleteById(roleId);
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
    }

    @Override
    public RolePermissionResponse getRolePermissions(Long roleId, boolean group) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "角色不存在");
        }
        List<RolePermission> relations = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        if (relations.isEmpty()) {
            return new RolePermissionResponse(List.of(), List.of(), group ? List.of() : null);
        }
        List<Long> permIds = relations.stream().map(RolePermission::getPermissionId).toList();
        List<Permission> permissions = permissionMapper.selectBatchIds(permIds);

        List<String> codes = permissions.stream().map(Permission::getCode).toList();
        List<PermissionItemDTO> items = permissions.stream().map(p -> {
            PermissionItemDTO dto = new PermissionItemDTO();
            dto.setId(p.getId());
            dto.setCode(p.getCode());
            dto.setName(p.getName());
            dto.setDescription(null);
            dto.setType(p.getType());
            dto.setPathOrApi(p.getPathOrApi());
            dto.setMethod(p.getMethod());
            return dto;
        }).toList();

        List<PermissionGroupDTO> groups = null;
        if (group) {
            groups = buildGroups(items);
        }
        return new RolePermissionResponse(codes, items, groups);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<String> permissionCodes) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "角色不存在");
        }
        if (permissionCodes == null) {
            permissionCodes = List.of();
        }
        List<Permission> permissions = permissionMapper.selectList(new LambdaQueryWrapper<Permission>().in(Permission::getCode, permissionCodes));
        if (permissions.size() != permissionCodes.size()) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "存在无效的权限 code");
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        LocalDateTime now = LocalDateTime.now();
        for (Permission p : permissions) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(p.getId());
            rp.setCreatedAt(now);
            rolePermissionMapper.insert(rp);
        }
    }

    @Override
    public Set<String> getUserRoleCodes(Long userId) {
        List<UserRole> relations = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (relations.isEmpty()) {
            return Set.of();
        }
        List<Long> roleIds = relations.stream().map(UserRole::getRoleId).toList();
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream().map(Role::getCode).collect(Collectors.toSet());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long userId, List<String> roleCodes) {
        if (roleCodes == null) {
            roleCodes = List.of();
        }
        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>().in(Role::getCode, roleCodes));
        if (roles.size() != roleCodes.size()) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "存在无效的角色 code");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        LocalDateTime now = LocalDateTime.now();
        for (Role r : roles) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(r.getId());
            ur.setCreatedAt(now);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    public PageResult<UserListItemDTO> pageUsers(String keyword, int page, int pageSize, String roleCode) {
        if (page <= 0) page = 1;
        if (pageSize <= 0) pageSize = 10;

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getUsername, keyword);
        }
        if (roleCode != null && !roleCode.isBlank()) {
            Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode));
            if (role == null) {
                return new PageResult<>(0, List.of());
            }
            List<UserRole> urs = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, role.getId()));
            if (urs.isEmpty()) {
                return new PageResult<>(0, List.of());
            }
            List<Long> userIds = urs.stream().map(UserRole::getUserId).toList();
            wrapper.in(User::getId, userIds);
        }

        PageHelper.startPage(page, pageSize);
        List<User> users = userMapper.selectList(wrapper);
        PageInfo<User> pageInfo = new PageInfo<>(users);

        List<UserListItemDTO> list = users.stream().map(u -> {
            UserListItemDTO dto = new UserListItemDTO();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setNickname(u.getNickname());
            dto.setStatus(u.getStatus());
            dto.setCreatedAt(u.getCreatedAt());
            return dto;
        }).toList();
        return new PageResult<>(pageInfo.getTotal(), list);
    }

    @Override
    public UserDetailDTO getUserDetail(Long userId) {
        com.community.domain.user.User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return toDetailDTO(user);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        com.community.domain.user.User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetailDTO createUser(UserCreateRequest request) {
        boolean exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())) > 0;
        if (exists) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "用户名已存在");
        }
        boolean phoneExists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone())) > 0;
        if (phoneExists) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "手机号已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setPassword(passwordService.hash(request.getPassword()));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        if (request.getRoleCodes() != null && !request.getRoleCodes().isEmpty()) {
            updateUserRoles(user.getId(), request.getRoleCodes());
        }
        return toDetailDTO(user);
    }

    @Override
    public void resetUserPassword(Long userId, String newPassword) {
        com.community.domain.user.User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        user.setPassword(passwordService.hash(newPassword));
        userMapper.updateById(user);
    }

    private List<PermissionGroupDTO> buildGroups(List<PermissionItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        HashMap<String, String> moduleNameMap = new HashMap<>();
        moduleNameMap.put("user", "用户管理");
        moduleNameMap.put("role", "角色管理");
        moduleNameMap.put("perm", "权限管理");
        moduleNameMap.put("admin", "系统管理");

        var grouped = items.stream().collect(Collectors.groupingBy(item -> {
            String code = item.getCode();
            if (code == null || !code.contains(":")) {
                return "other";
            }
            return code.split(":", 2)[0];
        }));

        return grouped.entrySet().stream().map(e -> {
            PermissionGroupDTO g = new PermissionGroupDTO();
            g.setModuleKey(e.getKey());
            g.setModuleName(moduleNameMap.getOrDefault(e.getKey(), e.getKey()));
            g.setChildren(e.getValue());
            return g;
        }).toList();
    }

    private UserDetailDTO toDetailDTO(com.community.domain.user.User user) {
        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setNickname(user.getNickname());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
