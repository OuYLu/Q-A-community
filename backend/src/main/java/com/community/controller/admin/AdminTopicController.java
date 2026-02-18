package com.community.controller.admin;

import com.community.common.Result;
import com.community.dto.QaTopicCategoryUpdateDTO;
import com.community.dto.QaTopicPageQueryDTO;
import com.community.dto.QaTopicQuestionPageQueryDTO;
import com.community.dto.QaTopicSaveDTO;
import com.community.dto.QaTopicStatusUpdateDTO;
import com.community.service.QaTopicAdminService;
import com.community.vo.AdminTopicDetailVO;
import com.community.vo.AdminTopicListItemVO;
import com.community.vo.TopicCategoryVO;
import com.community.vo.TopicQuestionPageItemVO;
import com.community.vo.TopicRecentQuestionVO;
import com.community.vo.TopicStatsVO;
import com.community.vo.TopicTrendPointVO;
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
import java.util.Map;

@RestController
@RequestMapping("/api/admin/qa/topic")
@RequiredArgsConstructor
@Tag(name = "Admin Topic")
public class AdminTopicController {
    private final QaTopicAdminService qaTopicAdminService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Topic page")
    public Result<PageInfo<AdminTopicListItemVO>> page(@ModelAttribute QaTopicPageQueryDTO query) {
        return Result.success(qaTopicAdminService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Topic detail")
    public Result<AdminTopicDetailVO> detail(@PathVariable Long id) {
        return Result.success(qaTopicAdminService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Create topic")
    public Result<Map<String, Long>> create(@Valid @RequestBody QaTopicSaveDTO dto) {
        return Result.success(Map.of("id", qaTopicAdminService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Update topic")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody QaTopicSaveDTO dto) {
        qaTopicAdminService.update(id, dto);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Update topic status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody QaTopicStatusUpdateDTO dto) {
        qaTopicAdminService.updateStatus(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Delete topic (soft by status)")
    public Result<Void> delete(@PathVariable Long id) {
        qaTopicAdminService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/{id}/categories")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Topic categories")
    public Result<List<TopicCategoryVO>> categories(@PathVariable Long id) {
        return Result.success(qaTopicAdminService.listCategories(id));
    }

    @PutMapping("/{id}/categories")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Update topic categories")
    public Result<Void> updateCategories(@PathVariable Long id, @RequestBody QaTopicCategoryUpdateDTO dto) {
        qaTopicAdminService.updateCategories(id, dto);
        return Result.success(null);
    }

    @GetMapping("/{id}/recent-questions")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Recent questions in topic")
    public Result<List<TopicRecentQuestionVO>> recentQuestions(@PathVariable Long id,
                                                               @RequestParam(required = false) Integer limit) {
        return Result.success(qaTopicAdminService.recentQuestions(id, limit));
    }

    @GetMapping("/{id}/question/page")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Topic question page")
    public Result<PageInfo<TopicQuestionPageItemVO>> questionPage(@PathVariable Long id,
                                                                  @ModelAttribute QaTopicQuestionPageQueryDTO query) {
        return Result.success(qaTopicAdminService.questionPage(id, query));
    }

    @GetMapping("/{id}/trend")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Topic trend")
    public Result<List<TopicTrendPointVO>> trend(@PathVariable Long id,
                                                 @RequestParam(required = false) Integer days) {
        return Result.success(qaTopicAdminService.trend(id, days));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAuthority('op:topic:manage')")
    @Operation(summary = "Topic stats")
    public Result<TopicStatsVO> stats(@PathVariable Long id) {
        return Result.success(qaTopicAdminService.stats(id));
    }
}
