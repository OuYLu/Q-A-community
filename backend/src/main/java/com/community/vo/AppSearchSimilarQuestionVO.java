package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用搜索相似问题建议")
public class AppSearchSimilarQuestionVO {
    private Long id;
    private String title;
    private String titleHighlight;
}
