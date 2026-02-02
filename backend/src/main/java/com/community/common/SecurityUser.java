package com.community.common;

import com.community.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class SecurityUser implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    private SecurityUser(Long id, String username, String password, String nickname, boolean enabled, List<GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static SecurityUser from(User user) {
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(resolveRole(user)));
        return new SecurityUser(user.getId(), user.getUsername(), user.getPassword(), user.getNickname(), enabled, authorities);
    }

    private static String resolveRole(User user) {
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
