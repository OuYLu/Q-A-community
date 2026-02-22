package com.community.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardContentTrendRespVO {
    private List<DashboardContentTrendVO> points;
    private String peakDate;
    private Long peakValue;
    private String summaryText;
}
