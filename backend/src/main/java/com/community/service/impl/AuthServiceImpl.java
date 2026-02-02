package com.community.service.impl;

import com.community.dto.LoginDTO;
import com.community.common.SecurityUser;
import com.community.service.AuthService;
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

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // Step 1: authenticate username/password via AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        // Step 2: put Authentication into SecurityContext (not required for stateless JWT but useful here)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 3: generate JWT for the client to use in subsequent requests
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        long expiresAt = Instant.now().plusSeconds(jwtUtil.getExpireMinutes() * 60).toEpochMilli();

        return new LoginVO(token, expiresAt, user.getId());
    }
}
