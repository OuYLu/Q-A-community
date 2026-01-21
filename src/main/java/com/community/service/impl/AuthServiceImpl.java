package com.community.service.impl;

import com.community.common.context.CurrentUserContext;
import com.community.common.exception.BizException;
import com.community.common.result.ErrorCode;
import com.community.domain.user.User;
import com.community.dto.auth.MeResponse;
import com.community.dto.auth.TokenResponse;
import com.community.dto.user.RegisterRequest;
import com.community.service.AuthService;
import com.community.service.PasswordService;
import com.community.service.UserService;
import com.community.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Long register(RegisterRequest request) {
        return userService.register(request.getUsername(), request.getPassword());
    }

    @Override
    public TokenResponse login(String username, String password) {
        User user = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (user == null || !passwordService.matches(password, user.getPassword())) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "账号已被冻结");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        long expiresAt = System.currentTimeMillis() + jwtUtil.getExpireMillis();
        return new TokenResponse(token, expiresAt);
    }

    @Override
    public MeResponse currentUser() {
        CurrentUserContext.UserInfo userInfo = CurrentUserContext.get();
        if (userInfo == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期");
        }
        return new MeResponse(userInfo.getUserId(), userInfo.getUsername());
    }
}
