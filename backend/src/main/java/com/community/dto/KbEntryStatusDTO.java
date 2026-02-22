package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "KB entry status update")
public class KbEntryStatusDTO {
    @NotNull(message = "status is required")
    @Min(value = 1, message = "status must be 1 or 4")
    @Max(value = 4, message = "status must be 1 or 4")
    private Integer status;
}
