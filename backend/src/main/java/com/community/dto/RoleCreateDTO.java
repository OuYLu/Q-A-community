package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Role create request")
public class RoleCreateDTO {
    @NotBlank(message = "code is required")
    @Schema(description = "Role code", example = "expert")
    private String code;

    @NotBlank(message = "name is required")
    @Schema(description = "Role name", example = "Expert")
    private String name;

    @Schema(description = "Role description")
    private String description;
}
