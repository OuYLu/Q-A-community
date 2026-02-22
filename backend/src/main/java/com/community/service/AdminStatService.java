package com.community.service;

import com.community.vo.CmsAuditPageItemVO;
import com.community.vo.CmsReportPageItemVO;
import com.community.vo.DashboardContentTrendRespVO;
import com.community.vo.DashboardGovernanceTrendRespVO;
import com.community.vo.DashboardHotTagVO;
import com.community.vo.DashboardHotTopicVO;
import com.community.vo.DashboardKpiVO;
import com.community.vo.DashboardNewTagTrendPointVO;
import com.community.vo.DashboardNewTagVO;
import com.community.vo.DashboardOverviewVO;
import com.community.vo.DashboardUserActivityTrendRespVO;

import java.util.List;

public interface AdminStatService {
    DashboardKpiVO kpi();

    DashboardContentTrendRespVO contentTrend(Integer days);

    DashboardGovernanceTrendRespVO governanceTrend(Integer days);

    DashboardUserActivityTrendRespVO userActivityTrend(Integer days);

    List<CmsAuditPageItemVO> pendingAudits(Integer limit);

    List<CmsReportPageItemVO> pendingReports(Integer limit);

    List<DashboardHotTagVO> hotTags(Integer limit);

    List<DashboardHotTopicVO> hotTopics(Integer limit);

    long newUserTagCount(Integer days);

    List<DashboardNewTagVO> newUserTags(Integer days, Integer limit);

    List<DashboardNewTagTrendPointVO> newUserTagTrend(Integer days);

    DashboardOverviewVO overview(Integer days, Integer todoLimit, Integer hotLimit);
}
