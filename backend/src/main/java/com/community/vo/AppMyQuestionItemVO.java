package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppMyQuestionItemVO {
    private Long id;
    private String title;
    private String categoryName;

    @JsonIgnore
    private String imageUrlsRaw;

    private List<String> imageUrls;
    private Integer status;
    private Integer answerCount;
    private Integer likeCount;
    private Integer viewCount;
    private Long acceptedAnswerId;
    private List<String> tags;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
