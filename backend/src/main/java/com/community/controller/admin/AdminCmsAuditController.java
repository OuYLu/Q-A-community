package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.CmsAuditBatchReviewDTO;
import com.community.dto.CmsAuditPageQueryDTO;
import com.community.dto.CmsAuditReviewDTO;
import com.community.service.CmsAuditAdminService;
import com.community.vo.CmsAuditDetailVO;
import com.community.vo.CmsAuditPageItemVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cms/audit")
@RequiredArgsConstructor
@Tag(name = "Admin CMS Audit")
public class AdminCmsAuditController {
    private final CmsAuditAdminService cmsAuditAdminService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "CMS audit page")
    public Result<PageInfo<CmsAuditPageItemVO>> page(@ModelAttribute CmsAuditPageQueryDTO query) {
        return Result.success(cmsAuditAdminService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "CMS audit detail")
    public Result<CmsAuditDetailVO> detail(@PathVariable Long id) {
        return Result.success(cmsAuditAdminService.detail(id));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "CMS audit review")
    public Result<Void> review(@PathVariable Long id, @Valid @RequestBody CmsAuditReviewDTO dto) {
        cmsAuditAdminService.review(id, dto);
        return Result.success(null);
    }

    @PostMapping("/batch-review")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "CMS audit batch review")
    public Result<Void> batchReview(@Valid @RequestBody CmsAuditBatchReviewDTO dto) {
        cmsAuditAdminService.batchReview(dto);
        return Result.success(null);
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "CMS audit reopen")
    public Result<Void> reopen(@PathVariable Long id) {
        cmsAuditAdminService.reopen(id);
        return Result.success(null);
    }
}
