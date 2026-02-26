package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用话题分页查询")
public class AppTopicPageQueryDTO {
    @Schema(description = "话题标题关键词")
    private String keyword;

    @Schema(description = "排序字段：关注数/问题数/今日新增/创建时间")
    private String sortBy = "followCount";

    @Schema(description = "升序/降序")
    private String sortOrder = "desc";

    private Integer page = 1;
    private Integer pageSize = 10;
}


