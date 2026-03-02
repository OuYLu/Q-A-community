package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppReportFeedbackDetailVO {
    private Long reportId;
    private Integer bizType;
    private Long bizId;

    private String contentTitle;
    private String contentText;

    private String reasonCode;
    private String reasonDetail;

    private Integer status;
    private Integer handleAction;
    private String handleResult;

    private Long reporterId;
    private String reporterName;

    private Long authorId;
    private String authorName;

    private Long handlerId;
    private String handlerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handledAt;
}
