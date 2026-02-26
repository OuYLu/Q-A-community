package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "知识库条目详情")
public class KbEntryDetailVO {
    private Long id;
    private Long categoryId;
    private String title;
    private String summary;
    private String content;
    private String source;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<KbTagSimpleVO> tags;
}
