package com.community.service;

import com.community.dto.LoginDTO;
import com.community.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO loginDTO);
}
