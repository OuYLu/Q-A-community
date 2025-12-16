package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BizException;
import com.community.common.result.ErrorCode;
import com.community.domain.role.Role;
import com.community.domain.user.User;
import com.community.domain.user.UserRole;
import com.community.mapper.UserMapper;
import com.community.mapper.UserRoleMapper;
import com.community.mapper.RoleMapper;
import com.community.service.PasswordService;
import com.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(String username, String rawPassword) {
        boolean exists = lambdaQuery().eq(User::getUsername, username).one() != null;
        if (exists) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordService.hash(rawPassword));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        save(user);

        Role defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "customer"));
        if (defaultRole == null) {
            defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "user"));
        }
        if (defaultRole == null) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "默认角色不存在");
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRole.getId());
        userRoleMapper.insert(userRole);

        return user.getId();
    }
}
