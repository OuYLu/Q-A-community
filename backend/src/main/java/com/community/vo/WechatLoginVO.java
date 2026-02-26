package com.community.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WechatLoginVO {
    private String token;
    private long expiresAt;
    private Long userId;
    private String username;
    private boolean newUser;
    private boolean needPhoneBind;
    private String bindTicket;
}
