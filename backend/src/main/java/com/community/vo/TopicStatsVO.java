package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Topic stats")
public class TopicStatsVO {
    private Integer followCount;
    private Integer questionCount;
    private Integer todayNewCount;
}
