package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Role permission update request")
public class RolePermUpdateDTO {
    @NotNull(message = "roleId is required")
    @Schema(description = "Role id")
    private Long roleId;

    @Schema(description = "Permission ids")
    private List<Long> permissionIds;
}
