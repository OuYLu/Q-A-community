package com.community.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetUserPasswordRequest {
    @NotBlank(message = "newPassword 不能为空")
    @Size(min = 6, message = "newPassword 长度至少6位")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
