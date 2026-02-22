package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.CmsReportHandleDTO;
import com.community.dto.CmsReportPageQueryDTO;
import com.community.service.CmsReportAdminService;
import com.community.vo.CmsReportDetailVO;
import com.community.vo.CmsReportPageItemVO;
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
@RequestMapping("/api/admin/cms/report")
@RequiredArgsConstructor
@Tag(name = "Admin CMS Report")
public class AdminCmsReportController {
    private final CmsReportAdminService cmsReportAdminService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('menu:content:report','content:report:manage','op:report:manage')")
    @Operation(summary = "CMS report page")
    public Result<PageInfo<CmsReportPageItemVO>> page(@ModelAttribute CmsReportPageQueryDTO query) {
        return Result.success(cmsReportAdminService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('menu:content:report','content:report:manage','op:report:manage')")
    @Operation(summary = "CMS report detail")
    public Result<CmsReportDetailVO> detail(@PathVariable Long id) {
        return Result.success(cmsReportAdminService.detail(id));
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAnyAuthority('menu:content:report','content:report:manage','op:report:manage')")
    @Operation(summary = "CMS report handle")
    public Result<Void> handle(@PathVariable Long id, @Valid @RequestBody CmsReportHandleDTO dto) {
        cmsReportAdminService.handle(id, dto);
        return Result.success(null);
    }

    @PostMapping("/{id}/to-audit")
    @PreAuthorize("hasAnyAuthority('menu:content:report','content:report:manage','op:report:manage')")
    @Operation(summary = "Transfer report to audit queue")
    public Result<Void> toAudit(@PathVariable Long id) {
        cmsReportAdminService.toAudit(id);
        return Result.success(null);
    }
}
