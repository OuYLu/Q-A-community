package com.community.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "应用搜索知识库条目")
public class AppSearchKbVO {
    private Long id;
    private String title;
    private String summary;
    private String titleHighlight;
    private String summaryHighlight;
    private Integer viewCount;
    private Integer likeCount;
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
