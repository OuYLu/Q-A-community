package com.community.dto.user;

import jakarta.validation.constraints.NotBlank;

public class SeedUserRequest {

    @NotBlank(message = "username 不能为空")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
