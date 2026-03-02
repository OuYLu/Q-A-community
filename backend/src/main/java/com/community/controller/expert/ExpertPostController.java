package com.community.controller.expert;

import com.community.common.Result;
import com.community.dto.AppExpertPostCreateDTO;
import com.community.dto.AppExpertPostPageQueryDTO;
import com.community.service.ExpertPostService;
import com.community.vo.AppExpertPostDetailVO;
import com.community.vo.AppExpertPostItemVO;
import com.community.vo.AppKbCategoryVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expert/posts")
@RequiredArgsConstructor
@Tag(name = "专家科普文章")
public class ExpertPostController {
    private final ExpertPostService expertPostService;

    @GetMapping("/categories")
    @Operation(summary = "科普分类列表（知识库分类）")
    public Result<List<AppKbCategoryVO>> categories() {
        return Result.success(expertPostService.categories());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('expert:post:create')")
    @Operation(summary = "发布科普文章（仅认证专家）")
    public Result<Long> create(@Valid @RequestBody AppExpertPostCreateDTO dto) {
        return Result.success(expertPostService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('expert:post:update')")
    @Operation(summary = "更新科普文章（仅作者）")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AppExpertPostCreateDTO dto) {
        expertPostService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('expert:post:delete')")
    @Operation(summary = "删除科普文章（仅作者）")
    public Result<Void> delete(@PathVariable Long id) {
        expertPostService.delete(id);
        return Result.success(null);
    }

    @GetMapping
    @Operation(summary = "科普文章分页")
    public Result<PageInfo<AppExpertPostItemVO>> page(@ModelAttribute AppExpertPostPageQueryDTO query) {
        return Result.success(expertPostService.page(query));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('expert:post:my')")
    @Operation(summary = "我的科普文章分页（仅认证专家）")
    public Result<PageInfo<AppExpertPostItemVO>> myPage(@ModelAttribute AppExpertPostPageQueryDTO query) {
        return Result.success(expertPostService.myPage(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "科普文章详情")
    public Result<AppExpertPostDetailVO> detail(@PathVariable Long id) {
        return Result.success(expertPostService.detail(id));
    }
}