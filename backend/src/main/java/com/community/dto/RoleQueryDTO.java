package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Role query")
public class RoleQueryDTO {
    @Schema(description = "Role name")
    private String name;

    @Schema(description = "Page number")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;
}
