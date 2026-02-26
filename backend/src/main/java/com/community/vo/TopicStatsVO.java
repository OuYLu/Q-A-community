package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "话题统计")
public class TopicStatsVO {
    private Integer followCount;
    private Integer questionCount;
    private Integer todayNewCount;
}
