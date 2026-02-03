package com.community.common;

import com.community.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class SecurityUser implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final boolean enabled;

    private final List<String> roleCodes;
    private final List<String> permCodes;
    private final List<GrantedAuthority> authorities;

    private SecurityUser(Long id, String username, String password, String nickname,
                         boolean enabled, List<String> roleCodes, List<String> permCodes) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.enabled = enabled;

        this.roleCodes = roleCodes == null ? List.of() : roleCodes;
        this.permCodes = permCodes == null ? List.of() : permCodes;

        List<GrantedAuthority> roleAuths = this.roleCodes.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toList());

        List<GrantedAuthority> permAuths = this.permCodes.stream()
                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority(code))
                .collect(Collectors.toList());

        // 合并 + 去重
        this.authorities = Stream.concat(roleAuths.stream(), permAuths.stream())
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(GrantedAuthority::getAuthority, a -> a, (a, b) -> a),
                        m -> List.copyOf(m.values())
                ));
    }

    public static SecurityUser from(User user, List<String> roleCodes, List<String> permCodes) {
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;
        return new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                enabled,
                roleCodes,
                permCodes
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
