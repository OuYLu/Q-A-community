package com.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.domain.user.User;

public interface UserService extends IService<User> {
    Long register(String username, String rawPassword);
}
