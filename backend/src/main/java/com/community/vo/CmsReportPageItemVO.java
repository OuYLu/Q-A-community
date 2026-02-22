package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CmsReportPageItemVO {
    private Long id;
    private Integer bizType;
    private Long bizId;
    private Integer reasonType;
    private String reasonCode;
    private String reasonDetail;
    private Long reporterId;
    private LocalDateTime createdAt;
    private Integer status;

    private String contentTitle;
    private String contentSnippet;
    private Integer contentStatus;
    private Long authorId;
    private String authorName;

    private Long handlerId;
    private LocalDateTime handledAt;
    private Integer handleAction;
    private String handleResult;
}
