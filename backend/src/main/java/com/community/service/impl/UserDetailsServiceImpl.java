package com.community.service.impl;

import com.community.common.SecurityUser;
import com.community.entity.User;
import com.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String usernameOrPhone) throws UsernameNotFoundException {
        User user = userService.findByUsernameOrPhone(usernameOrPhone);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + usernameOrPhone);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new DisabledException("账号已被禁用");
        }
        List<String> roleCodes = userService.getRoleCodes(user.getId());
        List<String> permCodes = userService.getPermCodes(user.getId());
        return SecurityUser.from(user, roleCodes,permCodes);
    }
}
