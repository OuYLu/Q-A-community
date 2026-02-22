package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "CMS audit review")
public class CmsAuditReviewDTO {
    @NotBlank(message = "action is required")
    @Schema(description = "pass/reject")
    private String action;

    private String rejectReason;
}
