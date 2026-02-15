package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpertManageVO {
    private Long userId;
    private String realName;
    private String organization;
    private String title;
    private String expertise;
    private Integer expertStatus;
    private LocalDateTime verifiedAt;
}
