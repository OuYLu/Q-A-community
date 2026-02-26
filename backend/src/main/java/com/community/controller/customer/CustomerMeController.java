package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.AppMePasswordChangeDTO;
import com.community.dto.AppMePasswordSetFirstDTO;
import com.community.dto.AppMeProfileUpdateDTO;
import com.community.dto.AppPageQueryDTO;
import com.community.service.CustomerMeService;
import com.community.vo.AppDocVO;
import com.community.vo.AppFollowUserItemVO;
import com.community.vo.AppMeOverviewVO;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppMyFavoriteItemVO;
import com.community.vo.AppMyHistoryItemVO;
import com.community.vo.AppMyQuestionItemVO;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/me")
@RequiredArgsConstructor
@Tag(name = "客户端个人中心")
public class CustomerMeController {
    private final CustomerMeService customerMeService;

    @GetMapping("/overview")
    @Operation(summary = "我的页面概览")
    public Result<AppMeOverviewVO> overview() {
        return Result.success(customerMeService.overview());
    }

    @PutMapping("/profile")
    @Operation(summary = "更新我的资料")
    public Result<Void> updateProfile(@Valid @RequestBody AppMeProfileUpdateDTO dto) {
        customerMeService.updateProfile(dto);
        return Result.success(null);
    }

    @PostMapping("/password/set-first")
    @Operation(summary = "社交账号设置首个密码")
    public Result<Void> setFirstPassword(@Valid @RequestBody AppMePasswordSetFirstDTO dto) {
        customerMeService.setFirstPassword(dto);
        return Result.success(null);
    }

    @PostMapping("/password/change")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody AppMePasswordChangeDTO dto) {
        customerMeService.changePassword(dto);
        return Result.success(null);
    }

    @GetMapping("/favorites")
    @Operation(summary = "我的收藏")
    public Result<PageInfo<AppMyFavoriteItemVO>> favorites(@ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerMeService.favorites(query));
    }

    @GetMapping("/history")
    @Operation(summary = "我的浏览历史")
    public Result<PageInfo<AppMyHistoryItemVO>> history(@ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerMeService.history(query));
    }

    @GetMapping("/questions")
    @Operation(summary = "我的提问")
    public Result<PageInfo<AppMyQuestionItemVO>> questions(@ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerMeService.myQuestions(query));
    }

    @GetMapping("/answers")
    @Operation(summary = "我的回答")
    public Result<PageInfo<AppMyAnswerItemVO>> answers(@ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerMeService.myAnswers(query));
    }

    @GetMapping("/following")
    @Operation(summary = "我的关注用户")
    public Result<PageInfo<AppFollowUserItemVO>> following(@ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerMeService.following(query));
    }

    @GetMapping("/followers")
    @Operation(summary = "我的粉丝")
    public Result<PageInfo<AppFollowUserItemVO>> followers(@ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerMeService.followers(query));
    }

    @GetMapping("/docs/{type}")
    @Operation(summary = "静态文档：设置/帮助/用户协议/隐私政策")
    public Result<AppDocVO> doc(@PathVariable String type) {
        return Result.success(customerMeService.doc(type));
    }
}

