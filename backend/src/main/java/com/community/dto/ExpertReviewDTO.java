package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Expert apply review request")
public class ExpertReviewDTO {
    @NotNull(message = "applyId is required")
    @Schema(description = "Application id")
    private Long applyId;

    @NotNull(message = "status is required")
    @Schema(description = "Review result: 2-pass, 3-reject", example = "2")
    private Integer status;

    @Schema(description = "Reject reason (required when status=3)")
    private String rejectReason;
}
