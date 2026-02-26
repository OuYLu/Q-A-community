package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppMyFavoriteItemVO {
    private Long questionId;
    private String title;
    private Integer answerCount;
    private Integer likeCount;
    private Integer favoriteCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime favoriteAt;
}
