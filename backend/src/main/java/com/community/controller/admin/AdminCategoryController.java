package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.QaCategoryQueryDTO;
import com.community.dto.QaCategorySaveDTO;
import com.community.entity.QaCategory;
import com.community.service.QaCategoryAdminService;
import com.community.vo.CategoryListVO;
import com.community.vo.CategoryTreeVO;
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
@Tag(name = "后台分类管理")
public class AdminCategoryController {
    private final QaCategoryAdminService qaCategoryAdminService;

    @GetMapping("/category/list")
    @Operation(summary = "分类列表", description = "分类管理操作")
    public Result<PageInfo<CategoryListVO>> list(@ModelAttribute QaCategoryQueryDTO query) {
        return Result.success(qaCategoryAdminService.list(query));
    }

    @GetMapping("/category/tree")
    @Operation(summary = "分类树懒加载列表", description = "按父级ID懒加载分类树")
    public Result<List<CategoryTreeVO>> tree(@RequestParam(required = false) Long parentId) {
        return Result.success(qaCategoryAdminService.listTreeLazy(parentId));
    }

    @PostMapping("/category")
    @PreAuthorize("hasAuthority('op:category:manage')")
    @Operation(summary = "创建分类", description = "分类管理操作")
    public Result<QaCategory> create(@Valid @RequestBody QaCategorySaveDTO dto) {
        return Result.success(qaCategoryAdminService.create(dto));
    }

    @GetMapping("/category/{id}")
    @PreAuthorize("hasAuthority('op:category:manage')")
    @Operation(summary = "分类详情", description = "分类管理操作")
    public Result<QaCategory> get(@PathVariable Long id) {
        return Result.success(qaCategoryAdminService.getById(id));
    }

    @PutMapping("/category/{id}")
    @PreAuthorize("hasAuthority('op:category:manage')")
    @Operation(summary = "更新分类", description = "分类管理操作")
    public Result<QaCategory> update(@PathVariable Long id, @Valid @RequestBody QaCategorySaveDTO dto) {
        return Result.success(qaCategoryAdminService.update(id, dto));
    }

    @DeleteMapping("/category/{id}")
    @PreAuthorize("hasAuthority('op:category:manage')")
    @Operation(summary = "删除分类", description = "分类管理操作")
    public Result<Void> delete(@PathVariable Long id) {
        qaCategoryAdminService.delete(id);
        return Result.success(null);
    }
}
