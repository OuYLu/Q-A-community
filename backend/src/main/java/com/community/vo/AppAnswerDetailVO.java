package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Answer detail view")
public class AppAnswerDetailVO {
    @Schema(description = "Answer data")
    private AppQuestionAnswerVO answer;

    @Schema(description = "Question id")
    private Long questionId;

    @Schema(description = "Question title")
    private String questionTitle;

    @Schema(description = "Comment list")
    private List<AppAnswerCommentVO> comments;
}
