package com.community.controller.admin;

import com.community.common.annotation.RequirePermission;
import com.community.common.result.Result;
import com.community.domain.permission.Permission;
import com.community.domain.role.Role;
import com.community.dto.admin.*;
import com.community.service.AdminRbacService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminRbacController {

    @Autowired
    private AdminRbacService adminRbacService;

    // A. 权限列表
    @GetMapping("/permissions")
    @RequirePermission(value = "admin:permission:read", name = "查看权限")
    public Result<List<Permission>> listPermissions(@RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(adminRbacService.listPermissions(keyword));
    }

    // B. 角色 CRUD
    @PostMapping("/roles")
    @RequirePermission(value = "admin:role:create", name = "创建角色")
    public Result<Role> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return Result.success(adminRbacService.createRole(request.getCode(), request.getName(), request.getDescription()));
    }

    @GetMapping("/roles")
    @RequirePermission(value = "admin:role:read", name = "查看角色")
    public Result<List<Role>> listRoles(@RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(adminRbacService.listRoles(keyword));
    }

    @PutMapping("/roles/{roleId}")
    @RequirePermission(value = "admin:role:update", name = "修改角色")
    public Result<Role> updateRole(@PathVariable("roleId") Long roleId,
                                   @Valid @RequestBody RoleUpdateRequest request) {
        return Result.success(adminRbacService.updateRole(roleId, request.getName(), request.getDescription()));
    }

    @DeleteMapping("/roles/{roleId}")
    @RequirePermission(value = "admin:role:delete", name = "删除角色")
    public Result<String> deleteRole(@PathVariable("roleId") Long roleId) {
        adminRbacService.deleteRole(roleId);
        return Result.success("删除成功");
    }

    // C. 角色权限
    @GetMapping("/roles/{roleId}/permissions")
    @RequirePermission(value = "admin:role:read", name = "查看角色权限")
    public Result<RolePermissionResponse> getRolePermissions(@PathVariable("roleId") Long roleId,
                                                             @RequestParam(value = "group", required = false, defaultValue = "false") boolean group) {
        return Result.success(adminRbacService.getRolePermissions(roleId, group));
    }

    @PutMapping("/roles/{roleId}/permissions")
    @RequirePermission(value = "admin:role:grant", name = "分配角色权限")
    public Result<String> updateRolePermissions(@PathVariable("roleId") Long roleId,
                                              @Valid @RequestBody UpdateRolePermissionsRequest request) {
        adminRbacService.updateRolePermissions(roleId, request.getPermissionCodes());
        return Result.success("分配角色权限成功");
    }

    // D. 用户角色分配
    @GetMapping("/users/{userId}/roles")
    @RequirePermission(value = "admin:user:assign-role", name = "查看用户角色")
    public Result<Set<String>> getUserRoles(@PathVariable("userId") Long userId) {
        return Result.success(adminRbacService.getUserRoleCodes(userId));
    }

    @PutMapping("/users/{userId}/roles")
    @RequirePermission(value = "admin:user:assign-role", name = "分配用户角色")
    public Result<String> updateUserRoles(@PathVariable("userId") Long userId,
                                        @Valid @RequestBody UpdateUserRolesRequest request) {
        adminRbacService.updateUserRoles(userId, request.getRoleCodes());
        return Result.success("分配用户角色成功");
    }

    // 用户列表
    @GetMapping("/users")
    @RequirePermission(value = "admin:user:read", name = "查看用户列表")
    public Result<PageResult<UserListItemDTO>> listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "roleCode", required = false) String roleCode) {
        return Result.success(adminRbacService.pageUsers(keyword, page, pageSize, roleCode));
    }

    @GetMapping("/users/{userId}")
    @RequirePermission(value = "admin:user:read", name = "查看用户详情")
    public Result<UserDetailDTO> getUser(@PathVariable("userId") Long userId) {
        return Result.success(adminRbacService.getUserDetail(userId));
    }

    @PutMapping("/users/{userId}/status")
    @RequirePermission(value = "admin:user:update", name = "冻结/解冻用户")
    public Result<String> updateUserStatus(@PathVariable("userId") Long userId,
                                           @Valid @RequestBody UpdateUserStatusRequest request) {
        adminRbacService.updateUserStatus(userId, request.getStatus());
        return Result.success("更新用户状态成功");
    }

    @PostMapping("/users")
    @RequirePermission(value = "admin:user:create", name = "创建用户")
    public Result<UserDetailDTO> createUser(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(adminRbacService.createUser(request));
    }

    @PutMapping("/users/{userId}/password")
    @RequirePermission(value = "admin:user:update", name = "重置用户密码")
    public Result<String> resetPassword(@PathVariable("userId") Long userId,
                                        @Valid @RequestBody ResetUserPasswordRequest request) {
        adminRbacService.resetUserPassword(userId, request.getNewPassword());
        return Result.success("重置密码成功");
    }
}
