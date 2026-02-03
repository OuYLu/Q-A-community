package com.community.service;

import com.community.dto.PermissionSaveDTO;
import com.community.entity.Permission;
import com.github.pagehelper.PageInfo;

public interface PermissionService {
    PageInfo<Permission> listAll(int pageNum, int pageSize);

    Permission create(PermissionSaveDTO dto);

    Permission update(Long id, PermissionSaveDTO dto);

    void delete(Long id);
}
