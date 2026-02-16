package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Category tree node for lazy loading")
public class CategoryTreeVO {
    @Schema(description = "Category id")
    private Long id;

    @Schema(description = "Parent id")
    private Long parentId;

    @Schema(description = "Category name")
    private String name;

    @Schema(description = "Display label")
    private String label;

    @Schema(description = "Category status")
    private Integer status;

    @Schema(description = "Sort order")
    private Integer sort;

    @Schema(description = "Whether current node has children")
    private Boolean hasChildren;

    @Schema(description = "Whether current node is leaf")
    private Boolean leaf;
}
