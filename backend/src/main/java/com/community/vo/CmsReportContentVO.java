package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CmsReportContentVO {
    private Integer bizType;
    private Long bizId;
    private String title;
    private String content;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createdAt;
}
