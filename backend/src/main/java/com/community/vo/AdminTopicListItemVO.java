package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Admin topic list item")
public class AdminTopicListItemVO {
    private Long id;
    private String title;
    private String subtitle;
    private String coverImg;
    private Integer status;
    private Integer followCount;
    private Integer questionCount;
    private Integer todayNewCount;
    private Integer categoryCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
