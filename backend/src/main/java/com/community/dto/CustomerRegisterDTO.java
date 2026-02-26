package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "客户注册请求")
public class CustomerRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "customer001")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "Cust123!")
    private String password;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "头像链接")
    private String avatar;
}
