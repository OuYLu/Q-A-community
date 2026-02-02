package com.community.service;

import com.community.entity.User;
import com.community.vo.UserVO;

public interface UserService {
    User findByUsername(String username);

    UserVO getCurrentUser();
}
