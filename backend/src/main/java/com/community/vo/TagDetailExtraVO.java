package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "标签扩展信息")
public class TagDetailExtraVO {
    @Schema(description = "标签编号")
    private Long tagId;

    @Schema(description = "问题管理路径")
    private String questionManagePath;

    @Schema(description = "问题管理查询键")
    private String questionManageQueryKey;

    @Schema(description = "问题管理查询值")
    private Long questionManageQueryValue;

    @Schema(description = "问题管理完整链接")
    private String questionManageUrl;

    @Schema(description = "使用该标签的最近问题")
    private List<TagRecentQuestionVO> recentQuestions;
}
