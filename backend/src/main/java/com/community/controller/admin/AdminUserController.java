package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.AdminCreateStaffDTO;
import com.community.dto.ExpertReviewDTO;
import com.community.dto.PermissionSaveDTO;
import com.community.dto.RoleCreateDTO;
import com.community.dto.RolePermUpdateDTO;
import com.community.dto.RoleUpdateDTO;
import com.community.entity.ExpertApply;
import com.community.entity.Permission;
import com.community.entity.Role;
import com.community.service.ExpertAdminService;
import com.community.service.PermissionService;
import com.community.service.RoleService;
import com.community.service.UserService;
import com.community.vo.UserVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin User")
public class AdminUserController {
    private final UserService userService;
    private final ExpertAdminService expertAdminService;
    private final PermissionService permissionService;
    private final RoleService roleService;

    @PostMapping("/staff")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "Create staff", description = "Admin only")
    public Result<UserVO> createStaff(@Valid @RequestBody AdminCreateStaffDTO dto) {
        return Result.success(userService.createStaff(dto));
    }

    @GetMapping("/expert/apply/list")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "List expert applications", description = "Admin only")
    public Result<PageInfo<ExpertApply>> list(@RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(expertAdminService.listApplies(status, pageNum, pageSize));
    }

    @PostMapping("/expert/apply/review")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "Review expert application", description = "Admin only")
    public Result<Void> review(@Valid @RequestBody ExpertReviewDTO dto) {
        expertAdminService.review(dto);
        return Result.success(null);
    }

    @GetMapping("/perm/list")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "Permission list", description = "Permission management")
    public Result<PageInfo<Permission>> listPermissions(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(permissionService.listAll(pageNum, pageSize));
    }

    @PostMapping("/perm")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "Create permission", description = "Permission management")
    public Result<Permission> createPermission(@Valid @RequestBody PermissionSaveDTO dto) {
        return Result.success(permissionService.create(dto));
    }

    @PutMapping("/perm/{id}")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "Update permission", description = "Permission management")
    public Result<Permission> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionSaveDTO dto) {
        return Result.success(permissionService.update(id, dto));
    }

    @DeleteMapping("/perm/{id}")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "Delete permission", description = "Permission management")
    public Result<Void> deletePermission(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/role/list")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "Role list", description = "Role management")
    public Result<PageInfo<Role>> listRoles(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(roleService.listRoles(pageNum, pageSize));
    }

    @PostMapping("/role")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "Create role", description = "Role management")
    public Result<Role> createRole(@Valid @RequestBody RoleCreateDTO dto) {
        return Result.success(roleService.create(dto));
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "Update role name", description = "Role management")
    public Result<Role> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        return Result.success(roleService.updateName(id, dto));
    }

    @DeleteMapping("/role/{id}")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "Delete role", description = "Role management")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/role/{id}/perm/list")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "Role permissions", description = "Role management")
    public Result<PageInfo<Permission>> listRolePermissions(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(roleService.listRolePermissions(id, pageNum, pageSize));
    }

    @PutMapping("/role/perm")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "Update role permissions", description = "Role management")
    public Result<Void> updateRolePermissions(@Valid @RequestBody RolePermUpdateDTO dto) {
        roleService.updateRolePermissions(dto);
        return Result.success(null);
    }
}
