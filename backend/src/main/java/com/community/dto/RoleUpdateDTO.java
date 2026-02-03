package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Role update request")
public class RoleUpdateDTO {
    @NotBlank(message = "name is required")
    @Schema(description = "Role name", example = "Expert")
    private String name;
}
