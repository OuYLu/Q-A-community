package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "游客首页预览数据")
public class AppGuestHomeVO {
    @Schema(description = "分类列表")
    private List<AppCategoryVO> categories;

    @Schema(description = "热门话题")
    private List<AppTopicListItemVO> hotTopics;

    @Schema(description = "热门问题")
    private List<AppQuestionHotItemVO> hotQuestions;

    @Schema(description = "专家卡片")
    private List<AppExpertCardVO> experts;
}
