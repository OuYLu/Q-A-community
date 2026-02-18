package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Admin topic detail")
public class AdminTopicDetailVO {
    private Long id;
    private String title;
    private String subtitle;
    private String coverImg;
    private String intro;
    private Integer status;
    private Integer followCount;
    private Integer questionCount;
    private Integer todayNewCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TopicCategoryVO> categories;
    private List<TopicRecentQuestionVO> recentQuestions;
}
