package com.community.controller.user;

import com.community.common.Result;
import com.community.dto.LoginDTO;
import com.community.service.AuthService;
import com.community.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "用户端-认证")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "登录", description = "用户名密码登录，返回JWT")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }
}
