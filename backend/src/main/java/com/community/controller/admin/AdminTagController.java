package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.QaTagBatchStatusDTO;
import com.community.dto.QaTagQueryDTO;
import com.community.dto.QaTagSaveDTO;
import com.community.entity.QaTag;
import com.community.service.QaTagAdminService;
import com.community.vo.TagDetailExtraVO;
import com.community.vo.TagUsageTrendPointVO;
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
@Tag(name = "Admin Tag")
public class AdminTagController {
    private final QaTagAdminService qaTagAdminService;

    @GetMapping("/tag/list")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Tag list", description = "Operation tag management")
    public Result<PageInfo<QaTag>> list(@ModelAttribute QaTagQueryDTO query) {
        return Result.success(qaTagAdminService.list(query));
    }

    @PostMapping("/tag")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Create tag", description = "Operation tag management")
    public Result<QaTag> create(@Valid @RequestBody QaTagSaveDTO dto) {
        return Result.success(qaTagAdminService.create(dto));
    }

    @GetMapping("/tag/{id}")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Tag detail", description = "Operation tag management")
    public Result<QaTag> get(@PathVariable Long id) {
        return Result.success(qaTagAdminService.getById(id));
    }

    @GetMapping("/tag/{id}/detail-extra")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Tag detail extra", description = "Recent 10 questions and question manage link")
    public Result<TagDetailExtraVO> detailExtra(@PathVariable Long id) {
        return Result.success(qaTagAdminService.getDetailExtra(id));
    }

    @GetMapping("/tag/{id}/usage-trend")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Tag usage trend", description = "Reference trend in recent days")
    public Result<List<TagUsageTrendPointVO>> usageTrend(@PathVariable Long id,
                                                         @RequestParam(required = false) Integer days) {
        return Result.success(qaTagAdminService.usageTrend(id, days));
    }

    @PutMapping("/tag/{id}")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Update tag", description = "Operation tag management")
    public Result<QaTag> update(@PathVariable Long id, @Valid @RequestBody QaTagSaveDTO dto) {
        return Result.success(qaTagAdminService.update(id, dto));
    }

    @DeleteMapping("/tag/{id}")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Delete tag", description = "Operation tag management")
    public Result<Void> delete(@PathVariable Long id) {
        qaTagAdminService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/tag/enable/batch")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Batch enable tags", description = "Operation tag management")
    public Result<Void> batchEnable(@Valid @RequestBody QaTagBatchStatusDTO dto) {
        qaTagAdminService.batchEnable(dto.getIds());
        return Result.success(null);
    }

    @PutMapping("/tag/disable/batch")
    @PreAuthorize("hasAuthority('op:tag:manage')")
    @Operation(summary = "Batch disable tags", description = "Operation tag management")
    public Result<Void> batchDisable(@Valid @RequestBody QaTagBatchStatusDTO dto) {
        qaTagAdminService.batchDisable(dto.getIds());
        return Result.success(null);
    }
}
