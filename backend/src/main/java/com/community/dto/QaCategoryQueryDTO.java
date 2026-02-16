package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "QA category query")
public class QaCategoryQueryDTO {
    @Schema(description = "Category name")
    private String name;

    @Schema(description = "Parent category id")
    private Long parentId;

    @Schema(description = "Status")
    private Integer status;

    @Schema(description = "Page number")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;
}
