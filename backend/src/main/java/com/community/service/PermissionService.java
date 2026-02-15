package com.community.service;

import com.community.dto.PermissionSaveDTO;
import com.community.dto.PermissionQueryDTO;
import com.community.entity.Permission;
import com.github.pagehelper.PageInfo;

public interface PermissionService {
    PageInfo<Permission> listAll(PermissionQueryDTO query);

    Permission create(PermissionSaveDTO dto);

    Permission update(Long id, PermissionSaveDTO dto);

    void delete(Long id);

    Permission getById(Long id);
}
