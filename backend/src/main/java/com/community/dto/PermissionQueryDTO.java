package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Permission query")
public class PermissionQueryDTO {
    @Schema(description = "Permission name")
    private String name;

    @Schema(description = "Page number")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;
}
