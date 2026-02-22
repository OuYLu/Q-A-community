package com.community.vo;

import lombok.Data;

@Data
public class DashboardKpiMetricVO {
    private Long value;
    private Long dayOverDay;
    private Double weekAvg;
    private Long weekTotal;
}
