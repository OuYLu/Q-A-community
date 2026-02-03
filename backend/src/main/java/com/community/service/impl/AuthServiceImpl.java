package com.community.service.impl;

import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.LoginDTO;
import com.community.entity.User;
import com.community.service.AuthService;
import com.community.service.UserService;
import com.community.util.JwtUtil;
import com.community.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // Pre-check status to provide clearer message for disabled accounts
        User user = userService.findByUsernameOrPhone(loginDTO.getUsername());
        if (user == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ResultCode.UNAUTHORIZED, "你的账号已被冻结，如有疑问联系管理员777@gmail.com");
        }

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(securityUser.getId(), securityUser.getUsername(), securityUser.getRoleCodes());
        long expiresAt = Instant.now().plusSeconds(jwtUtil.getExpireMinutes() * 60).toEpochMilli();

        return new LoginVO(token, expiresAt, securityUser.getId());
    }
}
