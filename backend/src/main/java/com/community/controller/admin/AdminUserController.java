package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.AdminCreateStaffDTO;
import com.community.dto.ExpertApplyQueryDTO;
import com.community.dto.ExpertReviewDTO;
import com.community.dto.ExpertStatusUpdateDTO;
import com.community.dto.ExpertUserQueryDTO;
import com.community.dto.PermissionSaveDTO;
import com.community.dto.PermissionQueryDTO;
import com.community.dto.RoleCreateDTO;
import com.community.dto.RolePermUpdateDTO;
import com.community.dto.RoleQueryDTO;
import com.community.dto.RoleUpdateDTO;
import com.community.dto.UserQueryDTO;
import com.community.dto.UserStatusDTO;
import com.community.dto.UserUpdateDTO;
import com.community.entity.ExpertApply;
import com.community.entity.Permission;
import com.community.entity.Role;
import com.community.service.ExpertAdminService;
import com.community.service.ExpertUserService;
import com.community.service.PermissionService;
import com.community.service.RoleService;
import com.community.service.UserAdminService;
import com.community.service.UserService;
import com.community.vo.ExpertManageVO;
import com.community.vo.ExpertApplyDetailVO;
import com.community.vo.UserVO;
import com.community.vo.UserManageVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
@Tag(name = "后台用户管理")
public class AdminUserController {
    private final UserService userService;
    private final UserAdminService userAdminService;
    private final ExpertAdminService expertAdminService;
    private final ExpertUserService expertUserService;
    private final PermissionService permissionService;
    private final RoleService roleService;

    @PostMapping("/staff")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "创建员工", description = "仅管理员")
    public Result<UserVO> createStaff(@Valid @RequestBody AdminCreateStaffDTO dto) {
        return Result.success(userService.createStaff(dto));
    }

    @GetMapping("/expert/apply/list")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "专家申请列表", description = "仅管理员")
    public Result<PageInfo<ExpertApply>> list(@ModelAttribute ExpertApplyQueryDTO query) {
        return Result.success(expertAdminService.listApplies(query));
    }

    @GetMapping("/expert/apply/user/{userId}")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "按用户编号查看专家申请详情", description = "仅管理员")
    public Result<ExpertApplyDetailVO> getExpertApplyDetail(@PathVariable Long userId) {
        return Result.success(expertAdminService.getDetailByUserId(userId));
    }

    @PostMapping("/expert/apply/review")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "审核专家申请", description = "仅管理员")
    public Result<Void> review(@Valid @RequestBody ExpertReviewDTO dto) {
        expertAdminService.review(dto);
        return Result.success(null);
    }

    @GetMapping("/perm/list")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "权限列表", description = "权限管理")
    public Result<PageInfo<Permission>> listPermissions(@ModelAttribute PermissionQueryDTO query) {
        return Result.success(permissionService.listAll(query));
    }

    @PostMapping("/perm")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "创建权限", description = "权限管理")
    public Result<Permission> createPermission(@Valid @RequestBody PermissionSaveDTO dto) {
        return Result.success(permissionService.create(dto));
    }

    @PutMapping("/perm/{id}")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "更新权限", description = "权限管理")
    public Result<Permission> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionSaveDTO dto) {
        return Result.success(permissionService.update(id, dto));
    }

    @GetMapping("/perm/{id}")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "权限详情", description = "权限管理")
    public Result<Permission> getPermission(@PathVariable Long id) {
        return Result.success(permissionService.getById(id));
    }

    @DeleteMapping("/perm/{id}")
    @PreAuthorize("hasAuthority('rbac:perm:manage')")
    @Operation(summary = "删除权限", description = "权限管理")
    public Result<Void> deletePermission(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/role/list")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "角色列表", description = "角色管理")
    public Result<PageInfo<Role>> listRoles(@ModelAttribute RoleQueryDTO query) {
        return Result.success(roleService.listRoles(query));
    }

    @PostMapping("/role")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "创建角色", description = "角色管理")
    public Result<Role> createRole(@Valid @RequestBody RoleCreateDTO dto) {
        return Result.success(roleService.create(dto));
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "更新角色名称", description = "角色管理")
    public Result<Role> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        return Result.success(roleService.updateName(id, dto));
    }

    @GetMapping("/role/{id}")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "角色详情", description = "角色管理")
    public Result<Role> getRole(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @DeleteMapping("/role/{id}")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "删除角色", description = "角色管理")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/role/{id}/perm/list")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "角色权限", description = "角色管理")
    public Result<PageInfo<Permission>> listRolePermissions(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(roleService.listRolePermissions(id, pageNum, pageSize));
    }

    @PutMapping("/role/perm")
    @PreAuthorize("hasAuthority('rbac:role:manage')")
    @Operation(summary = "更新角色权限", description = "角色管理")
    public Result<Void> updateRolePermissions(@Valid @RequestBody RolePermUpdateDTO dto) {
        roleService.updateRolePermissions(dto);
        return Result.success(null);
    }

    @GetMapping("/user/list")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "用户列表", description = "用户管理")
    public Result<PageInfo<UserManageVO>> listUsers(@ModelAttribute UserQueryDTO query) {
        return Result.success(userAdminService.listManageableUsers(query));
    }

    @GetMapping("/expert/user/list")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "专家用户列表", description = "用户管理")
    public Result<PageInfo<ExpertManageVO>> listExpertUsers(@ModelAttribute ExpertUserQueryDTO query) {
        return Result.success(expertUserService.listExperts(query));
    }

    @GetMapping("/expert/user/{id}")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "专家用户详情", description = "用户管理")
    public Result<ExpertManageVO> getExpertUser(@PathVariable Long id) {
        return Result.success(expertUserService.getExpertDetail(id));
    }

    @GetMapping("/expert/user/{id}/detail")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "按用户编号查看专家用户详情", description = "仅管理员")
    public Result<ExpertApplyDetailVO> getExpertUserDetail(@PathVariable Long id) {
        return Result.success(expertAdminService.getDetailByUserId(id));
    }

    @PutMapping("/expert/user/{id}/status")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "更新专家状态", description = "用户管理")
    public Result<Void> updateExpertStatus(@PathVariable Long id, @Valid @RequestBody ExpertStatusUpdateDTO dto) {
        expertUserService.updateExpertStatus(id, dto);
        return Result.success(null);
    }

    @PutMapping("/user/{id}")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "更新用户", description = "用户管理")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userAdminService.updateUser(id, dto);
        return Result.success(null);
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "用户详情", description = "用户管理")
    public Result<UserManageVO> getUser(@PathVariable Long id) {
        return Result.success(userAdminService.getUserDetail(id));
    }

    @PutMapping("/user/{id}/status")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "更新用户状态", description = "用户管理")
    public Result<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO dto) {
        userAdminService.updateStatus(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "删除用户", description = "用户管理")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userAdminService.deleteUser(id);
        return Result.success(null);
    }
}
