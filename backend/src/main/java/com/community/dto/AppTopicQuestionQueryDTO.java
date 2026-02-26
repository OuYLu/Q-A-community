package com.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用话题问题查询")
public class AppTopicQuestionQueryDTO {
    @Schema(description = "排序方式：热门/最新/未解决")
    private String sortBy = "hot";

    private Integer page = 1;
    private Integer pageSize = 10;
}


