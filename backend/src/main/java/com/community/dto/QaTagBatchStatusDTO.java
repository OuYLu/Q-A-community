package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "QA tag batch status update")
public class QaTagBatchStatusDTO {
    @NotNull(message = "ids is required")
    @NotEmpty(message = "ids cannot be empty")
    @Schema(description = "Tag ids")
    private List<Long> ids;
}
