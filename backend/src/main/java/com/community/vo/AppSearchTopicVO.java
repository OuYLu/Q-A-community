package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "应用搜索话题项")
public class AppSearchTopicVO {
    private Long id;
    private String title;
    private String subtitle;
    private String coverImg;
    private Integer followCount;
    private Integer questionCount;
}


