package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "内容审核处理")
public class CmsAuditReviewDTO {
    @NotBlank(message = "操作不能为空")
    @Schema(description = "通过/驳回")
    private String action;

    private String rejectReason;
}
