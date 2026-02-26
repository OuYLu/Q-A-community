package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.AppQuestionPageQueryDTO;
import com.community.service.CustomerDiscoverService;
import com.community.service.QaCategoryAdminService;
import com.community.vo.AppCategoryVO;
import com.community.vo.AppExpertCardVO;
import com.community.vo.AppGuestHomeVO;
import com.community.vo.AppQuestionHotItemVO;
import com.community.vo.AppQuestionListItemVO;
import com.community.vo.AppTopicListItemVO;
import com.community.vo.CategoryTreeVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/discover")
@RequiredArgsConstructor
@Tag(name = "客户端发现页")
public class CustomerDiscoverController {
    private final CustomerDiscoverService customerDiscoverService;
    private final QaCategoryAdminService qaCategoryAdminService;

    @GetMapping("/home")
    @Operation(summary = "游客首页预览")
    public Result<AppGuestHomeVO> home(@RequestParam(required = false) Integer topicLimit,
                                       @RequestParam(required = false) Integer questionLimit,
                                       @RequestParam(required = false) Integer expertLimit) {
        return Result.success(customerDiscoverService.guestHome(topicLimit, questionLimit, expertLimit));
    }

    @GetMapping("/questions")
    @Operation(summary = "发现页问题分页（支持按分类切换）")
    public Result<PageInfo<AppQuestionListItemVO>> questions(@ModelAttribute AppQuestionPageQueryDTO query) {
        return Result.success(customerDiscoverService.questionPage(query));
    }

    @GetMapping("/categories")
    @Operation(summary = "发现页分类")
    public Result<List<AppCategoryVO>> categories() {
        return Result.success(customerDiscoverService.listCategories());
    }

    @GetMapping("/categories/tree")
    @Operation(summary = "分类树懒加载列表")
    public Result<List<CategoryTreeVO>> categoryTree(@RequestParam(required = false) Long parentId) {
        return Result.success(qaCategoryAdminService.listTreeLazy(parentId));
    }

    @GetMapping("/topics/hot")
    @Operation(summary = "热门话题")
    public Result<List<AppTopicListItemVO>> hotTopics(@RequestParam(required = false) Integer limit) {
        return Result.success(customerDiscoverService.hotTopics(limit));
    }

    @GetMapping("/rank/hot")
    @Operation(summary = "热门问题")
    public Result<List<AppQuestionHotItemVO>> hotQuestions(@RequestParam(required = false) Integer limit) {
        return Result.success(customerDiscoverService.hotQuestions(limit));
    }

    @GetMapping("/experts")
    @Operation(summary = "专家卡片")
    public Result<List<AppExpertCardVO>> experts(@RequestParam(required = false) Integer limit) {
        return Result.success(customerDiscoverService.expertCards(limit));
    }
}
