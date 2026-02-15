package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.dto.RoleCreateDTO;
import com.community.dto.RolePermUpdateDTO;
import com.community.dto.RoleQueryDTO;
import com.community.dto.RoleUpdateDTO;
import com.community.entity.Permission;
import com.community.entity.Role;
import com.community.entity.RolePermission;
import com.community.entity.UserRole;
import com.community.mapper.PermissionMapper;
import com.community.mapper.RoleMapper;
import com.community.mapper.RolePermissionMapper;
import com.community.mapper.UserRoleMapper;
import com.community.service.RoleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public PageInfo<Role> listRoles(RoleQueryDTO query) {
        String name = query == null ? null : query.getName();
        Integer pageNum = query == null || query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Role::getName, name);
        }
        wrapper.orderByDesc(Role::getCreatedAt);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(this.list(wrapper));
    }

    @Override
    @Transactional
    public Role create(RoleCreateDTO dto) {
        Role exist = this.getOne(new LambdaQueryWrapper<Role>()
            .eq(Role::getCode, dto.getCode())
            .last("LIMIT 1"));
        if (exist != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "角色编码已存在");
        }

        Role role = new Role();
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setCreatedAt(LocalDateTime.now());
        this.save(role);
        return role;
    }

    @Override
    @Transactional
    public Role updateName(Long id, RoleUpdateDTO dto) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "角色不存在");
        }
        role.setName(dto.getName());
        this.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "角色不存在");
        }

        UserRole bound = userRoleMapper.selectOne(new LambdaQueryWrapper<UserRole>()
            .eq(UserRole::getRoleId, id)
            .last("LIMIT 1"));
        if (bound != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "角色已被用户绑定，无法删除");
        }

        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
            .eq(RolePermission::getRoleId, id));
        this.removeById(id);
    }

    @Override
    public PageInfo<Permission> listRolePermissions(Long roleId, int pageNum, int pageSize) {
        if (roleId == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "roleId is required");
        }
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(permissionMapper.selectByRoleId(roleId));
    }

    @Override
    @Transactional
    public void updateRolePermissions(RolePermUpdateDTO dto) {
        if (dto.getRoleId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "roleId is required");
        }
        Role role = this.getById(dto.getRoleId());
        if (role == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "角色不存在");
        }

        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
            .eq(RolePermission::getRoleId, dto.getRoleId()));

        if (CollectionUtils.isEmpty(dto.getPermissionIds())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long permId : dto.getPermissionIds()) {
            if (permId == null) {
                continue;
            }
            RolePermission rp = new RolePermission();
            rp.setRoleId(dto.getRoleId());
            rp.setPermissionId(permId);
            rp.setCreatedAt(now);
            rolePermissionMapper.insert(rp);
        }
    }

    @Override
    public Role getById(Long id) {
        Role role = super.getById(id);
        if (role == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "角色不存在");
        }
        return role;
    }
}
