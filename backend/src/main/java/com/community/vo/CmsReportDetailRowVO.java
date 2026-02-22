package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CmsReportDetailRowVO {
    private Long id;
    private Integer bizType;
    private Long bizId;
    private Integer reasonType;
    private Long reporterId;
    private String reasonCode;
    private String reasonDetail;
    private Integer status;
    private Long handlerId;
    private Integer handleAction;
    private String handleResult;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String contentTitle;
    private String contentText;
    private Integer contentStatus;
    private String contentRejectReason;
    private LocalDateTime contentCreatedAt;

    private Long authorId;
    private String authorUsername;
    private String authorNickname;
    private Integer authorStatus;
}
