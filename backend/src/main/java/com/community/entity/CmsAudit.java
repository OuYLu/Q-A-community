package com.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName(value = "cms_audit", autoResultMap = true)
public class CmsAudit {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer bizType;

    private Long bizId;

    private Integer triggerSource;

    private Integer auditType;

    private Integer auditStatus;

    private String action;

    private String modelLabel;

    private BigDecimal modelScore;

    @TableField(value = "hit_detail", typeHandler = JacksonTypeHandler.class)
    private JsonNode hitDetail;

    private String rejectReason;

    private Long submitUserId;

    private Long auditorId;

    private LocalDateTime auditedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
