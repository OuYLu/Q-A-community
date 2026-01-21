package com.community.config;

import com.community.common.context.CurrentUserContext;
import com.community.common.exception.BizException;
import com.community.common.result.ErrorCode;
import com.community.domain.user.User;
import com.community.mapper.UserMapper;
import com.community.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期");
        }
        String token = authorization.substring("Bearer ".length());
        try {
            Claims claims = jwtUtil.parse(token);
            Long userId = claims.get("uid", Long.class);
            String username = claims.get("username", String.class);
            if (userId == null || username == null) {
                throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期");
            }
            User user = userMapper.selectById(userId);
            if (user == null || (user.getStatus() != null && user.getStatus() != 1)) {
                throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "账号已被冻结");
            }
            CurrentUserContext.set(new CurrentUserContext.UserInfo(userId, username));
            return true;
        } catch (ExpiredJwtException e) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期");
        } catch (JwtException e) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserContext.clear();
    }
}
