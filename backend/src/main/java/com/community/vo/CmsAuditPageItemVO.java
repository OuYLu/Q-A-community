package com.community.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CmsAuditPageItemVO {
    private Long id;
    private Integer bizType;
    private Long bizId;
    private Integer triggerSource;
    private Integer auditType;
    private Integer auditStatus;
    private LocalDateTime createdAt;

    private String contentTitle;
    private String contentSnippet;
    private Integer contentStatus;

    private Long submitUserId;
    private String submitUserName;

    private String modelLabel;
    private BigDecimal modelScore;
    private JsonNode hitDetail;
    private String riskLevel;
    private List<String> hitKeywords;

    private Long auditorId;
    private LocalDateTime auditedAt;
    private String rejectReason;
}
