package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用搜索查询")
public class AppSearchQueryDTO {
    @Schema(description = "搜索关键词")
    private String query;

    @Schema(description = "搜索类型: all/question/topic/tag/kb")
    private String type = "all";

    @Schema(description = "问题排序: comprehensive/latest/hot")
    private String sortBy = "comprehensive";

    @Schema(description = "分类ID筛选(可选)")
    private Long categoryId;

    @Schema(description = "专题ID筛选(可选)")
    private Long topicId;

    @Schema(description = "仅待解决(可选)")
    private Boolean onlyUnsolved;

    private Integer page = 1;
    private Integer pageSize = 10;
}