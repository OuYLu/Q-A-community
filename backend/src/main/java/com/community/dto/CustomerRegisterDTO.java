package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "客户注册请求")
public class CustomerRegisterDTO {
    @NotBlank(message = "username is required")
    @Schema(description = "用户名", example = "customer001")
    private String username;

    @NotBlank(message = "password is required")
    @Schema(description = "密码", example = "Cust123!")
    private String password;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Email(message = "email format is invalid")
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;
}
