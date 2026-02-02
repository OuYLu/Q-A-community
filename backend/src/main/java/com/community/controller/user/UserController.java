package com.community.controller.user;

import com.community.common.Result;
import com.community.service.UserService;
import com.community.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "用户端-用户")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "需要携带JWT")
    public Result<UserVO> me() {
        return Result.success(userService.getCurrentUser());
    }
}
