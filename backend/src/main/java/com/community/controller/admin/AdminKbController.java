package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.KbCategorySaveDTO;
import com.community.dto.KbCategoryStatusDTO;
import com.community.dto.KbEntryPageQueryDTO;
import com.community.dto.KbEntrySaveDTO;
import com.community.dto.KbEntryStatusDTO;
import com.community.service.KbAdminService;
import com.community.vo.KbCategoryTreeVO;
import com.community.vo.KbEntryDetailVO;
import com.community.vo.KbEntryPageItemVO;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/kb")
@RequiredArgsConstructor
@Tag(name = "后台知识库管理")
public class AdminKbController {
    private final KbAdminService kbAdminService;

    @GetMapping("/category/tree")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "知识库分类树")
    public Result<List<KbCategoryTreeVO>> categoryTree() {
        return Result.success(kbAdminService.categoryTree());
    }

    @PostMapping("/category")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "创建知识库分类")
    public Result<Map<String, Long>> createCategory(@Valid @RequestBody KbCategorySaveDTO dto) {
        return Result.success(Map.of("id", kbAdminService.createCategory(dto)));
    }

    @PutMapping("/category/{id}")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "更新知识库分类")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody KbCategorySaveDTO dto) {
        kbAdminService.updateCategory(id, dto);
        return Result.success(null);
    }

    @PutMapping("/category/{id}/status")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "更新知识库分类状态")
    public Result<Void> updateCategoryStatus(@PathVariable Long id, @Valid @RequestBody KbCategoryStatusDTO dto) {
        kbAdminService.updateCategoryStatus(id, dto);
        return Result.success(null);
    }

    @GetMapping("/entry/page")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "知识库条目分页")
    public Result<PageInfo<KbEntryPageItemVO>> entryPage(@ModelAttribute KbEntryPageQueryDTO query) {
        return Result.success(kbAdminService.entryPage(query));
    }

    @GetMapping("/entry/{id}")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "知识库条目详情")
    public Result<KbEntryDetailVO> entryDetail(@PathVariable Long id) {
        return Result.success(kbAdminService.entryDetail(id));
    }

    @PostMapping("/entry")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "创建知识库条目")
    public Result<Map<String, Long>> createEntry(@Valid @RequestBody KbEntrySaveDTO dto) {
        return Result.success(Map.of("id", kbAdminService.createEntry(dto)));
    }

    @PutMapping("/entry/{id}")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "更新知识库条目")
    public Result<Void> updateEntry(@PathVariable Long id, @Valid @RequestBody KbEntrySaveDTO dto) {
        kbAdminService.updateEntry(id, dto);
        return Result.success(null);
    }

    @PutMapping("/entry/{id}/status")
    @PreAuthorize("hasAuthority('op:kb:manage')")
    @Operation(summary = "更新知识库条目状态")
    public Result<Void> updateEntryStatus(@PathVariable Long id, @Valid @RequestBody KbEntryStatusDTO dto) {
        kbAdminService.updateEntryStatus(id, dto);
        return Result.success(null);
    }
}
