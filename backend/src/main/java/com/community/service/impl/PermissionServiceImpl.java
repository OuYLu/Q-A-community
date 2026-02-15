package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.dto.PermissionSaveDTO;
import com.community.dto.PermissionQueryDTO;
import com.community.entity.Permission;
import com.community.entity.Role;
import com.community.entity.RolePermission;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.community.mapper.PermissionMapper;
import com.community.mapper.RoleMapper;
import com.community.mapper.RolePermissionMapper;
import com.community.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleMapper roleMapper;

    @Override
    public PageInfo<Permission> listAll(PermissionQueryDTO query) {
        String name = query == null ? null : query.getName();
        Integer pageNum = query == null || query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query == null || query.getPageSize() == null ? 10 : query.getPageSize();
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Permission::getName, name);
        }
        wrapper.orderByDesc(Permission::getCreatedAt);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(this.list(wrapper));
    }

    @Override
    @Transactional
    public Permission create(PermissionSaveDTO dto) {
        Permission exist = this.getOne(new LambdaQueryWrapper<Permission>()
            .eq(Permission::getCode, dto.getCode())
            .last("LIMIT 1"));
        if (exist != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "权限编码已存在");
        }

        Permission permission = new Permission();
        permission.setCode(dto.getCode());
        permission.setName(dto.getName());
        permission.setType(dto.getType());
        permission.setPathOrApi(dto.getPathOrApi());
        permission.setMethod(dto.getMethod());
        permission.setCreatedAt(LocalDateTime.now());
        this.save(permission);
        return permission;
    }

    @Override
    @Transactional
    public Permission update(Long id, PermissionSaveDTO dto) {
        Permission permission = this.getById(id);
        if (permission == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "权限不存在");
        }

        if (StringUtils.hasText(dto.getCode()) && !dto.getCode().equals(permission.getCode())) {
            Permission exist = this.getOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getCode, dto.getCode())
                .last("LIMIT 1"));
            if (exist != null) {
                throw new BizException(ResultCode.BAD_REQUEST, "权限编码已存在");
            }
        }

        permission.setCode(dto.getCode());
        permission.setName(dto.getName());
        permission.setType(dto.getType());
        permission.setPathOrApi(dto.getPathOrApi());
        permission.setMethod(dto.getMethod());
        this.updateById(permission);
        return permission;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Permission permission = this.getById(id);
        if (permission == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "权限不存在");
        }

        List<RolePermission> refs = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
            .eq(RolePermission::getPermissionId, id));
        if (!refs.isEmpty()) {
            List<Long> roleIds = refs.stream()
                .map(RolePermission::getRoleId)
                .distinct()
                .toList();
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            String roleNames = roles.stream()
                .map(role -> role.getName() == null ? role.getCode() : role.getName() + "(" + role.getCode() + ")")
                .collect(Collectors.joining(", "));
            String message = StringUtils.hasText(roleNames)
                ? "权限已被角色引用，无法删除： " + roleNames
                : "权限已被角色引用，无法删除";
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }

        this.removeById(id);
    }

    @Override
    public Permission getById(Long id) {
        Permission permission = super.getById(id);
        if (permission == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "权限不存在");
        }
        return permission;
    }
}
