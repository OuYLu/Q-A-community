package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用话题列表项")
public class AppTopicListItemVO {
    private Long id;
    private String title;
    private String subtitle;
    private String coverImg;
    private Integer followCount;
    private Integer questionCount;
    private Integer todayNewCount;
}

