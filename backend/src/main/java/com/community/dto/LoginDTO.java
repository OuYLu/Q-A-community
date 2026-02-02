package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginDTO {
    @NotBlank(message = "username is required")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "password is required")
    @Schema(description = "密码", example = "Admin123!")
    private String password;
}
