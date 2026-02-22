package com.community.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardGovernanceTrendRespVO {
    private List<DashboardGovernanceTrendVO> points;
    private String peakDate;
    private Long peakValue;
    private String summaryText;
}
