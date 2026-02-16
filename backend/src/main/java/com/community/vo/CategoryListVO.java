package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Category list item")
public class CategoryListVO {
    @Schema(description = "Category id")
    private Long id;

    @Schema(description = "Category name")
    private String name;

    @Schema(description = "Parent category id")
    private Long parentId;

    @Schema(description = "Category icon")
    private String icon;

    @Schema(description = "Category description")
    private String description;

    @Schema(description = "Status")
    private Integer status;

    @Schema(description = "Sort")
    private Integer sort;

    @Schema(description = "Question count under this category")
    private Long questionCount;

    @Schema(description = "Created time")
    private LocalDateTime createdAt;

    @Schema(description = "Updated time")
    private LocalDateTime updatedAt;
}
