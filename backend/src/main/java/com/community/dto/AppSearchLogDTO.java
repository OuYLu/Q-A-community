package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "应用搜索日志")
public class AppSearchLogDTO {
    @NotBlank
    @Schema(description = "搜索文本")
    private String queryText;

    @Schema(description = "搜索类型：1-综合 2-问题 3-知识库 4-话题")
    private Integer searchType = 1;

    @Schema(description = "命中次数")
    private Integer hitCount = 0;
}



