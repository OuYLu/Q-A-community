package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {
    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "过期时间戳(毫秒)")
    private long expiresAt;

    @Schema(description = "用户编号")
    private Long userId;
}
