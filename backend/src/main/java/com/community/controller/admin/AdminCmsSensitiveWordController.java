package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.CmsSensitiveWordBatchStatusDTO;
import com.community.dto.CmsSensitiveWordQueryDTO;
import com.community.dto.CmsSensitiveWordSaveDTO;
import com.community.entity.CmsSensitiveWord;
import com.community.service.CmsSensitiveWordAdminService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cms/sensitive-word")
@RequiredArgsConstructor
@Tag(name = "后台敏感词管理")
public class AdminCmsSensitiveWordController {
    private final CmsSensitiveWordAdminService sensitiveWordAdminService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "敏感词列表")
    public Result<PageInfo<CmsSensitiveWord>> list(@ModelAttribute CmsSensitiveWordQueryDTO query) {
        return Result.success(sensitiveWordAdminService.list(query));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "新增敏感词")
    public Result<CmsSensitiveWord> create(@Valid @RequestBody CmsSensitiveWordSaveDTO dto) {
        return Result.success(sensitiveWordAdminService.create(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "敏感词详情")
    public Result<CmsSensitiveWord> detail(@PathVariable Long id) {
        return Result.success(sensitiveWordAdminService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "更新敏感词")
    public Result<CmsSensitiveWord> update(@PathVariable Long id, @Valid @RequestBody CmsSensitiveWordSaveDTO dto) {
        return Result.success(sensitiveWordAdminService.update(id, dto));
    }

    @PutMapping("/enable/batch")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "批量启用敏感词")
    public Result<Void> batchEnable(@Valid @RequestBody CmsSensitiveWordBatchStatusDTO dto) {
        sensitiveWordAdminService.batchEnable(dto.getIds());
        return Result.success(null);
    }

    @PutMapping("/disable/batch")
    @PreAuthorize("hasAnyAuthority('menu:content:audit','content:audit:manage','op:audit:manage')")
    @Operation(summary = "批量禁用敏感词")
    public Result<Void> batchDisable(@Valid @RequestBody CmsSensitiveWordBatchStatusDTO dto) {
        sensitiveWordAdminService.batchDisable(dto.getIds());
        return Result.success(null);
    }
}

