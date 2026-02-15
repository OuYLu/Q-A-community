package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "User status update request")
public class UserStatusDTO {
    @NotNull(message = "status is required")
    @Schema(description = "Status: 1-enabled, 0-disabled", example = "1")
    private Integer status;
}
