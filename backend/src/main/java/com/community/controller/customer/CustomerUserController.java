package com.community.controller.customer;

import com.community.common.Result;
import com.community.dto.AppPageQueryDTO;
import com.community.service.CustomerUserService;
import com.community.vo.AppExpertPostItemVO;
import com.community.vo.AppMyAnswerItemVO;
import com.community.vo.AppUserHomeVO;
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
@RequestMapping("/api/customer/users")
@RequiredArgsConstructor
@Tag(name = "客户端用户主页")
public class CustomerUserController {
    private final CustomerUserService customerUserService;

    @GetMapping("/{id}/home")
    @Operation(summary = "用户主页")
    public Result<AppUserHomeVO> home(@PathVariable Long id) {
        return Result.success(customerUserService.home(id));
    }

    @PostMapping("/{id}/follow")
    @Operation(summary = "关注用户")
    public Result<Void> follow(@PathVariable Long id) {
        customerUserService.follow(id);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/follow")
    @Operation(summary = "取消关注用户")
    public Result<Void> unfollow(@PathVariable Long id) {
        customerUserService.unfollow(id);
        return Result.success(null);
    }

    @GetMapping("/{id}/answers")
    @Operation(summary = "用户有效回答分页")
    public Result<PageInfo<AppMyAnswerItemVO>> answers(@PathVariable Long id, @ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerUserService.answers(id, query));
    }

    @GetMapping("/{id}/expert-posts")
    @Operation(summary = "用户科普文章分页")
    public Result<PageInfo<AppExpertPostItemVO>> expertPosts(@PathVariable Long id, @ModelAttribute AppPageQueryDTO query) {
        return Result.success(customerUserService.expertPosts(id, query));
    }
}