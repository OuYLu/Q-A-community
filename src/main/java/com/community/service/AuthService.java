package com.community.service;

import com.community.dto.auth.MeResponse;
import com.community.dto.auth.TokenResponse;
import com.community.dto.user.RegisterRequest;

public interface AuthService {
    Long register(RegisterRequest request);

    TokenResponse login(String username, String password);

    MeResponse currentUser();
}
