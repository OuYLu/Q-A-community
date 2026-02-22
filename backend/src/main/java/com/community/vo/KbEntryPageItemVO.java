package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "KB entry page item")
public class KbEntryPageItemVO {
    private Long id;
    private String title;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
}
