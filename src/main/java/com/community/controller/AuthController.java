package com.community.controller;

import com.community.common.result.Result;
import com.community.dto.user.LoginRequest;
import com.community.dto.user.RegisterRequest;
import com.community.common.context.CurrentUserContext;
import com.community.domain.user.User;
import com.community.service.UserService;
import com.community.service.PasswordService;
import com.community.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request.getUsername(), request.getPassword());
        return Result.success(userId);
    }

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.lambdaQuery().eq(User::getUsername, request.getUsername()).one();
        if (user == null || !passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new com.community.common.exception.BizException(com.community.common.result.ErrorCode.BIZ_ERROR.getCode(), "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        long expiresAt = System.currentTimeMillis() + jwtUtil.getExpireMillis();
        return Result.success(new TokenResponse(token, expiresAt));
    }

    @GetMapping("/me")
    public Result<MeResponse> me() {
        CurrentUserContext.UserInfo userInfo = CurrentUserContext.get();
        if (userInfo == null) {
            throw new com.community.common.exception.BizException(com.community.common.result.ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期");
        }
        return Result.success(new MeResponse(userInfo.getUserId(), userInfo.getUsername()));
    }

    public static class TokenResponse {
        private String token;
        private long expiresAt;

        public TokenResponse(String token, long expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(long expiresAt) {
            this.expiresAt = expiresAt;
        }
    }

    public static class MeResponse {
        private Long userId;
        private String username;

        public MeResponse(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
