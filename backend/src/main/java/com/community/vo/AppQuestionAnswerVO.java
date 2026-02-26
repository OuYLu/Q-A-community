package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Question detail answer view")
public class AppQuestionAnswerVO {
    @Schema(description = "Answer id")
    private Long id;

    @Schema(description = "Question id")
    private Long questionId;

    @Schema(description = "Answer content")
    private String content;

    @Schema(description = "Answer author id")
    private Long authorId;

    @Schema(description = "Answer author name")
    private String authorName;

    @Schema(description = "Answer author avatar")
    private String authorAvatar;

    @JsonIgnore
    private String imageUrlsRaw;

    @Schema(description = "Answer image URLs")
    private List<String> imageUrls;

    @Schema(description = "Anonymous flag")
    private Integer isAnonymous;

    @Schema(description = "Like count")
    private Integer likeCount;

    @Schema(description = "Comment count")
    private Integer commentCount;

    @Schema(description = "Favorite count")
    private Integer favoriteCount;

    @Schema(description = "Current user liked")
    private Boolean liked;

    @Schema(description = "Current user favorited")
    private Boolean favorited;

    @Schema(description = "Is accepted answer")
    private Boolean bestAnswer;

    @Schema(description = "Current user can recommend as best")
    private Boolean canRecommend;

    @Schema(description = "Created time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Current user can edit")
    private Boolean canEdit;
}
