package com.community.controller.user;

import com.community.common.Result;
import com.community.dto.LoginDTO;
import com.community.dto.WechatBindPhoneDTO;
import com.community.dto.WechatLoginDTO;
import com.community.service.AuthService;
import com.community.vo.LoginVO;
import com.community.vo.WechatLoginVO;
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
@Tag(name = "用户认证")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "账号登录", description = "用户名密码登录，返回 JWT")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/wechat/login")
    @Operation(summary = "微信登录")
    public Result<WechatLoginVO> wechatLogin(@Valid @RequestBody WechatLoginDTO wechatLoginDTO) {
        return Result.success(authService.wechatLogin(wechatLoginDTO));
    }

    @PostMapping("/wechat/bind-phone")
    @Operation(summary = "微信绑定手机号并登录")
    public Result<WechatLoginVO> wechatBindPhone(@Valid @RequestBody WechatBindPhoneDTO bindPhoneDTO) {
        return Result.success(authService.wechatBindPhone(bindPhoneDTO));
    }
}
