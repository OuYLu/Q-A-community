package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "CMS report handle")
public class CmsReportHandleDTO {
    @NotNull(message = "handleAction is required")
    @Min(value = 1, message = "handleAction must be between 1 and 4")
    @Max(value = 4, message = "handleAction must be between 1 and 4")
    private Integer handleAction;

    private String handleResult;
}
