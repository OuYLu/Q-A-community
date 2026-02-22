package com.community.vo;

import lombok.Data;

@Data
public class CmsAuditAuthorVO {
    private Long id;
    private String username;
    private String nickname;
    private Integer status;
}
