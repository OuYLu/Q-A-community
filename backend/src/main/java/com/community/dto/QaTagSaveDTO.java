package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "QA tag save")
public class QaTagSaveDTO {
    @NotBlank(message = "name is required")
    @Schema(description = "Tag name")
    private String name;

    @NotNull(message = "source is required")
    @Min(value = 1, message = "source must be 1 or 2")
    @Max(value = 2, message = "source must be 1 or 2")
    @Schema(description = "Source: 1 system, 2 user")
    private Integer source;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    @Schema(description = "Status: 1 enabled, 0 disabled")
    private Integer status;
}
