package com.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "话题趋势点")
public class TopicTrendPointVO {
    private String date;
    private Long cnt;
}
