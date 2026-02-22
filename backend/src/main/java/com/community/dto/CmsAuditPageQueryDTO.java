package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "CMS audit page query")
public class CmsAuditPageQueryDTO {
    private Integer bizType;
    private Integer auditStatus = 1;
    private Integer triggerSource;
    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    private String sortBy;
    private String sortOrder;
    private Integer page = 1;
    private Integer pageSize = 10;
}
