package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "QA category save")
public class QaCategorySaveDTO {
    @NotBlank(message = "name is required")
    @Schema(description = "Category name")
    private String name;

    @Schema(description = "Parent category id")
    private Long parentId;

    @Schema(description = "Category icon url")
    private String icon;

    @Schema(description = "Category description")
    private String description;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    @Schema(description = "Status, 1 enabled, 0 disabled")
    private Integer status;

    @Min(value = 0, message = "sort must be >= 0")
    @Schema(description = "Sort order")
    private Integer sort;
}
