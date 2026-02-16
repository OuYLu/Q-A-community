package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Tag detail extra info")
public class TagDetailExtraVO {
    @Schema(description = "Tag id")
    private Long tagId;

    @Schema(description = "Question manage path")
    private String questionManagePath;

    @Schema(description = "Question manage query key")
    private String questionManageQueryKey;

    @Schema(description = "Question manage query value")
    private Long questionManageQueryValue;

    @Schema(description = "Question manage full url")
    private String questionManageUrl;

    @Schema(description = "Recent questions using this tag")
    private List<TagRecentQuestionVO> recentQuestions;
}
