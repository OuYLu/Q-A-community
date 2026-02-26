package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.AppTopicPageQueryDTO;
import com.community.dto.AppTopicQuestionQueryDTO;
import com.community.service.CustomerTopicService;
import com.community.vo.AppTopicDetailVO;
import com.community.vo.AppTopicListItemVO;
import com.community.vo.AppTopicQuestionItemVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/topics")
@RequiredArgsConstructor
@Tag(name = "客户端话题")
public class CustomerTopicController {
    private final CustomerTopicService customerTopicService;

    @GetMapping
    @Operation(summary = "话题列表")
    public Result<PageInfo<AppTopicListItemVO>> page(@ModelAttribute AppTopicPageQueryDTO query) {
        return Result.success(customerTopicService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "话题详情")
    public Result<AppTopicDetailVO> detail(@PathVariable Long id) {
        return Result.success(customerTopicService.detail(id));
    }

    @GetMapping("/{id}/questions")
    @Operation(summary = "话题下问题列表")
    public Result<PageInfo<AppTopicQuestionItemVO>> questions(@PathVariable Long id,
                                                              @ModelAttribute AppTopicQuestionQueryDTO query) {
        return Result.success(customerTopicService.topicQuestions(id, query));
    }

    @PostMapping("/{id}/follow")
    @Operation(summary = "关注话题")
    public Result<Void> follow(@PathVariable Long id) {
        customerTopicService.follow(id);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/follow")
    @Operation(summary = "取消关注话题")
    public Result<Void> unfollow(@PathVariable Long id) {
        customerTopicService.unfollow(id);
        return Result.success(null);
    }
}
