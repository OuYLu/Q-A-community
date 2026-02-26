package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "客户端问题列表项")
public class AppQuestionListItemVO {
    @Schema(description = "问题编号")
    private Long id;

    @Schema(description = "问题标题")
    private String title;

    @Schema(description = "问题摘要")
    private String summary;

    @JsonIgnore
    private String imageUrlsRaw;

    @Schema(description = "问题图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "话题ID")
    private Long topicId;

    @Schema(description = "话题标题")
    private String topicTitle;

    @Schema(description = "作者ID")
    private Long authorId;

    @Schema(description = "作者昵称")
    private String authorName;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @Schema(description = "浏览数")
    private Integer viewCount;

    @Schema(description = "回答数")
    private Integer answerCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "最后活跃时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;
}
