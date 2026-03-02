package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "应用话题详情")
public class AppTopicDetailVO {
    private Long id;
    private String title;
    private String subtitle;
    private String coverImg;
    private String intro;
    private Integer followCount;
    private Integer questionCount;
    private Integer todayNewCount;
    private List<String> tags;
    private Boolean followed;
}
