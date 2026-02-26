package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginDTO {
    @NotBlank(message = "微信登录 code 不能为空")
    private String code;

    private String nickname;

    private String avatar;
}
