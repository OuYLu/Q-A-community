package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "管理员创建员工用户请求")
public class AdminCreateStaffDTO {
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "staff001")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "Staff123!" )
    private String password;

    @Schema(description = "昵称", example = "客服小李")
    private String nickname;

    @Schema(description = "手机号", example = "13900000000")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "staff@example.com")
    private String email;

    @Schema(description = "头像链接")
    private String avatar;
}
