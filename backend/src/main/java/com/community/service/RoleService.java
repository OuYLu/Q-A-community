package com.community.service;

import com.community.dto.RoleCreateDTO;
import com.community.dto.RolePermUpdateDTO;
import com.community.dto.RoleUpdateDTO;
import com.community.entity.Permission;
import com.community.entity.Role;
import com.github.pagehelper.PageInfo;

public interface RoleService {
    PageInfo<Role> listRoles(int pageNum, int pageSize);

    Role create(RoleCreateDTO dto);

    Role updateName(Long id, RoleUpdateDTO dto);

    void delete(Long id);

    PageInfo<Permission> listRolePermissions(Long roleId, int pageNum, int pageSize);

    void updateRolePermissions(RolePermUpdateDTO dto);
}
