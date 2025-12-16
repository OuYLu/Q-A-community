package com.community.controller;

import com.community.common.exception.BizException;
import com.community.common.result.ErrorCode;
import com.community.common.result.Result;
import com.community.domain.user.User;
import com.community.dto.user.SeedUserRequest;
import com.community.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserDebugController {

    @Autowired
    private UserService userService;

    @PostMapping("/_seed")
    public Result<Long> seed(@Validated @RequestBody SeedUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword("test");
        user.setStatus(1);
        userService.save(user);
        return Result.success(user.getId());
    }

    @GetMapping("/_by-username")
    public Result<User> findByUsername(@RequestParam("username") @NotBlank(message = "username 不能为空") String username) {
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        user.setPassword(null);
        return Result.success(user);
    }
}
