package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppExpertPostDetailVO {
    private Long id;
    private Long categoryId;
    private String categoryName;

    private Long authorUserId;
    private String authorName;
    private String authorAvatar;
    private String authorExpertise;
    private String authorTitle;
    private String source;

    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private List<String> imageUrls;
    private List<String> tagNames;
    private List<AppExpertContentBlockVO> contentBlocks;
    private Integer status;

    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private String contentRefRaw;
}