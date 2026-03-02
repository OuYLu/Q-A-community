package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "专家科普文章分页查询")
public class AppExpertPostPageQueryDTO {
    @Schema(description = "页码", example = "1")
    private Integer page;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;

    @Schema(description = "关键词（标题/摘要）")
    private String keyword;

    @Schema(description = "排序：latest/hot", example = "latest")
    private String sortBy;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "状态筛选（我的列表可用）", example = "1")
    private Integer status;
}