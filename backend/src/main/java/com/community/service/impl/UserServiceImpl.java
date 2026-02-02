package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.SecurityUser;
import com.community.entity.User;
import com.community.mapper.UserMapper;
import com.community.service.UserService;
import com.community.vo.UserVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public User findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public UserVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return null;
        }
        List<String> roles = securityUser.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        return new UserVO(securityUser.getId(), securityUser.getUsername(), securityUser.getNickname(), roles);
    }
}
