package com.community.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserCreateRequest {
    @NotBlank(message = "username 不能为空")
    @Size(min = 3, message = "username 长度至少3位")
    private String username;

    @NotBlank(message = "password 不能为空")
    @Size(min = 6, message = "password 长度至少6位")
    private String password;

    @NotBlank(message = "phone 不能为空")
    private String phone;

    private String nickname;

    private List<String> roleCodes;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}
