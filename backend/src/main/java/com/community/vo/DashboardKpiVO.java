package com.community.vo;

import lombok.Data;

@Data
public class DashboardKpiVO {
    private DashboardKpiMetricVO todayQuestion;
    private DashboardKpiMetricVO todayAnswer;
    private DashboardKpiMetricVO todayComment;
    private DashboardKpiMetricVO todayReport;
    private DashboardKpiMetricVO pendingAudit;
    private DashboardKpiMetricVO pendingReport;
}
