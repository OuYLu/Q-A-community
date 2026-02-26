package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Question detail view")
public class AppQuestionDetailVO {
    @Schema(description = "Question id")
    private Long id;

    @Schema(description = "Question title")
    private String title;

    @Schema(description = "Question content")
    private String content;

    @JsonIgnore
    private String imageUrlsRaw;

    @Schema(description = "Question image URLs")
    private List<String> imageUrls;

    @Schema(description = "Category id")
    private Long categoryId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Topic id")
    private Long topicId;

    @Schema(description = "Topic title")
    private String topicTitle;

    @Schema(description = "Author id")
    private Long authorId;

    @Schema(description = "Author name")
    private String authorName;

    @Schema(description = "Author avatar")
    private String authorAvatar;

    @Schema(description = "View count")
    private Integer viewCount;

    @Schema(description = "Answer count")
    private Integer answerCount;

    @Schema(description = "Like count")
    private Integer likeCount;

    @Schema(description = "Favorite count")
    private Integer favoriteCount;

    @Schema(description = "Current user liked")
    private Boolean liked;

    @Schema(description = "Current user favorited")
    private Boolean favorited;

    @Schema(description = "Created time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last active time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;

    @Schema(description = "Tags")
    private List<String> tags;

    @Schema(description = "Answers")
    private List<AppQuestionAnswerVO> answers;
}