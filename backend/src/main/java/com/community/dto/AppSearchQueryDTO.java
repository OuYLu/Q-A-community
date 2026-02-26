package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用搜索查询")
public class AppSearchQueryDTO {
    @Schema(description = "搜索关键词")
    private String query;

    @Schema(description = "搜索类型：全部/问题/话题/标签")
    private String type = "all";

    private Integer page = 1;
    private Integer pageSize = 10;
}



