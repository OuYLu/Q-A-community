package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatBindPhoneDTO {
    @NotBlank(message = "绑定凭证不能为空")
    private String bindTicket;

    private String phoneCode;

    private String phone;

    private String nickname;

    private String avatar;
}
