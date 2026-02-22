package com.community.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardUserActivityTrendRespVO {
    private List<DashboardUserActivityTrendVO> points;
    private String peakDate;
    private Long peakValue;
    private String summaryText;
}
