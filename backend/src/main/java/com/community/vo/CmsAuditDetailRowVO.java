package com.community.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CmsAuditDetailRowVO {
    private Long id;
    private Integer bizType;
    private Long bizId;
    private Integer triggerSource;
    private Integer auditType;
    private Integer auditStatus;
    private String action;
    private String modelLabel;
    private BigDecimal modelScore;
    private JsonNode hitDetail;
    private String rejectReason;
    private Long submitUserId;
    private Long auditorId;
    private LocalDateTime auditedAt;
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
