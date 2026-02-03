package com.community.controller.admin;
import com.community.common.Result;
import com.community.dto.AdminCreateStaffDTO;
import com.community.dto.ExpertReviewDTO;
import com.community.entity.ExpertApply;
import com.community.service.ExpertAdminService;
import com.community.service.UserService;
import com.community.vo.UserVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class AdminUserController {
    private final UserService userService;
    private final ExpertAdminService expertAdminService;

    @PostMapping("/staff")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "创建运营人员", description = "管理员创建运营人员")
    public Result<UserVO> createStaff(@Valid @RequestBody AdminCreateStaffDTO dto) {
        return Result.success(userService.createStaff(dto));
    }

    @GetMapping("/expert/apply/list")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "查看专家列表", description = "仅限管理员查看专家列表")
    public Result<PageInfo<ExpertApply>> list(@RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(expertAdminService.listApplies(status, pageNum, pageSize));
    }

    @PostMapping("/expert/apply/review")
    @PreAuthorize("hasAuthority('rbac:user:manage')")
    @Operation(summary = "审核专家资质", description = "仅限管理员审核专家资质")
    public Result<Void> review(@Valid @RequestBody ExpertReviewDTO dto) {
        expertAdminService.review(dto);
        return Result.success(null);
    }
}
