package com.community.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchKbDoc {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String source;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer status;
    private LocalDateTime createdAt;
}
