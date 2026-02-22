package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "CMS audit batch review")
public class CmsAuditBatchReviewDTO {
    @NotNull(message = "ids is required")
    @NotEmpty(message = "ids cannot be empty")
    private List<Long> ids;

    @NotBlank(message = "action is required")
    @Schema(description = "pass/reject")
    private String action;

    private String rejectReason;
}
