package com.community.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "username 不能为空")
    @Size(min = 3, message = "username 长度至少 3 位")
    private String username;

    @NotBlank(message = "password 不能为空")
    @Size(min = 6, message = "password 长度至少 6 位")
    private String password;

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
}
