package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.BizException;
import com.community.common.ResultCode;
import com.community.common.SecurityUser;
import com.community.dto.AdminCreateStaffDTO;
import com.community.dto.CustomerRegisterDTO;
import com.community.entity.Role;
import com.community.entity.User;
import com.community.entity.UserRole;
import com.community.mapper.RoleMapper;
import com.community.mapper.UserMapper;
import com.community.mapper.UserRoleMapper;
import com.community.service.UserService;
import com.community.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public User findByUsernameOrPhone(String usernameOrPhone) {
        return this.baseMapper.selectByUsernameOrPhone(usernameOrPhone);
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        return this.baseMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    @Transactional
    public UserVO registerCustomer(CustomerRegisterDTO dto) {
        User exist = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exist != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        if (StringUtils.hasText(dto.getPhone())) {
            User phoneExist = this.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
            if (phoneExist != null) {
                throw new BizException(ResultCode.BAD_REQUEST, "该手机号已被使用");
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setStatus(1);
        user.setFollowingCount(0);
        user.setFollowerCount(0);
        user.setLikeReceivedCount(0);
        user.setExpertStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        this.save(user);

        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "customer"));
        if (role == null) {
            throw new BizException(ResultCode.SERVER_ERROR, "角色 customer 未配置");
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRole.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), List.of("customer"));
    }

    @Override
    @Transactional
    public UserVO createStaff(AdminCreateStaffDTO dto) {
        User exist = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exist != null) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        if (StringUtils.hasText(dto.getPhone())) {
            User phoneExist = this.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
            if (phoneExist != null) {
                throw new BizException(ResultCode.BAD_REQUEST, "该手机号已被使用");
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setStatus(1);
        user.setFollowingCount(0);
        user.setFollowerCount(0);
        user.setLikeReceivedCount(0);
        user.setExpertStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        this.save(user);

        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "staff"));
        if (role == null) {
            throw new BizException(ResultCode.SERVER_ERROR, "角色 staff 未配置");
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRole.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), List.of("staff"));
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
        return new UserVO(
            securityUser.getId(),
            securityUser.getUsername(),
            securityUser.getNickname(),
            roles,
            securityUser.getRoleCodes(),
            securityUser.getPermCodes()
        );
    }

    @Override
    public List<String> getPermCodes(Long id) {
        return this.baseMapper.selectPermCodesByUserId(id);
    }
}
