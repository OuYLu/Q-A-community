package com.community.service;

import com.community.domain.permission.Permission;
import com.community.domain.role.Role;
import com.community.dto.admin.*;

import java.util.List;
import java.util.Set;

public interface AdminRbacService {
    List<Permission> listPermissions(String keyword);

    Role createRole(String code, String name, String description);

    List<Role> listRoles(String keyword);

    Role updateRole(Long roleId, String name, String description);

    void deleteRole(Long roleId);

    RolePermissionResponse getRolePermissions(Long roleId, boolean group);

    void updateRolePermissions(Long roleId, List<String> permissionCodes);

    Set<String> getUserRoleCodes(Long userId);

    void updateUserRoles(Long userId, List<String> roleCodes);

    PageResult<UserListItemDTO> pageUsers(String keyword, int page, int pageSize, String roleCode);

    UserDetailDTO getUserDetail(Long userId);

    void updateUserStatus(Long userId, Integer status);

    UserDetailDTO createUser(UserCreateRequest request);

    void resetUserPassword(Long userId, String newPassword);
}
