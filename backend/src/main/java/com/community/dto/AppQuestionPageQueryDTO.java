package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "客户端问题分页查询")
public class AppQuestionPageQueryDTO {
    @Schema(description = "页码", example = "1")
    private Integer page;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;

    @Schema(description = "关键字（标题/内容）")
    private String keyword;

    @Schema(description = "分类编号")
    private Long categoryId;

    @Schema(description = "话题编号")
    private Long topicId;

    @Schema(description = "排序：hot/latest")
    private String sortBy;

    @Schema(description = "仅看未解决：true/false")
    private Boolean onlyUnsolved;
}
