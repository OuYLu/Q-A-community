package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Expert status update")
public class ExpertStatusUpdateDTO {
    @NotNull(message = "expertStatus is required")
    @Schema(description = "Expert status: 3-certified, 0-disabled", example = "0")
    private Integer expertStatus;
}
