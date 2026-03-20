package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppMyFavoriteItemVO {
    /**
     * 1-question, 2-kb entry, 3-answer
     */
    private Integer bizType;
    private Long bizId;
    private Long questionId;
    private Long answerId;
    private String title;
    private String questionTitle;
    private String contentPreview;
    private Integer answerCount;
    private Integer likeCount;
    private Integer favoriteCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime favoriteAt;
}
