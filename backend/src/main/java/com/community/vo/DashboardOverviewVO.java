package com.community.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardOverviewVO {
    private DashboardKpiVO kpi;
    private DashboardContentTrendRespVO contentTrend;
    private DashboardGovernanceTrendRespVO governanceTrend;
    private DashboardUserActivityTrendRespVO userActivityTrend;
    private List<CmsAuditPageItemVO> pendingAudits;
    private List<CmsReportPageItemVO> pendingReports;
    private List<DashboardHotTagVO> hotTags;
    private List<DashboardHotTopicVO> hotTopics;
    private Long newUserTagCount;
    private List<DashboardNewTagVO> newUserTags;
    private List<DashboardNewTagTrendPointVO> newUserTagTrend;
}
