package com.community.service.impl;

import com.community.mapper.CmsAuditMapper;
import com.community.mapper.CmsReportMapper;
import com.community.mapper.DashboardStatMapper;
import com.community.service.AdminStatService;
import com.community.vo.CmsAuditPageItemVO;
import com.community.vo.CmsReportPageItemVO;
import com.community.vo.DashboardContentTrendRespVO;
import com.community.vo.DashboardContentTrendVO;
import com.community.vo.DashboardGovernanceTrendRespVO;
import com.community.vo.DashboardGovernanceTrendVO;
import com.community.vo.DashboardHotTagVO;
import com.community.vo.DashboardHotTopicVO;
import com.community.vo.DashboardKpiMetricVO;
import com.community.vo.DashboardKpiVO;
import com.community.vo.DashboardNewTagTrendPointVO;
import com.community.vo.DashboardNewTagVO;
import com.community.vo.DashboardOverviewVO;
import com.community.vo.DashboardUserActivityTrendRespVO;
import com.community.vo.DashboardUserActivityTrendVO;
import com.github.pagehelper.PageHelper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminStatServiceImpl implements AdminStatService {
    private final DashboardStatMapper dashboardStatMapper;
    private final CmsAuditMapper cmsAuditMapper;
    private final CmsReportMapper cmsReportMapper;

    @Override
    public DashboardKpiVO kpi() {
        List<DashboardContentTrendVO> content7d = dashboardStatMapper.selectContentTrend(6);
        List<DashboardGovernanceTrendVO> governance7d = dashboardStatMapper.selectGovernanceTrend(6);

        long todayQuestion = dashboardStatMapper.countTodayQuestion();
        long todayAnswer = dashboardStatMapper.countTodayAnswer();
        long todayComment = dashboardStatMapper.countTodayComment();
        long todayReport = dashboardStatMapper.countTodayReport();
        long pendingAudit = dashboardStatMapper.countPendingAudit();
        long pendingReport = dashboardStatMapper.countPendingReport();

        DashboardContentTrendVO todayContent = lastOrEmptyContent(content7d);
        DashboardContentTrendVO yesterdayContent = secondLastOrEmptyContent(content7d);
        DashboardGovernanceTrendVO todayGovernance = lastOrEmptyGovernance(governance7d);
        DashboardGovernanceTrendVO yesterdayGovernance = secondLastOrEmptyGovernance(governance7d);

        long weekQuestionTotal = sumContent(content7d, "question");
        long weekAnswerTotal = sumContent(content7d, "answer");
        long weekCommentTotal = sumContent(content7d, "comment");
        long weekReportTotal = sumGovernance(governance7d, "report");
        long weekAuditTotal = sumGovernance(governance7d, "audit");

        DashboardKpiVO vo = new DashboardKpiVO();
        vo.setTodayQuestion(metric(todayQuestion, todayContent.getQuestionCount() - yesterdayContent.getQuestionCount(), weekQuestionTotal));
        vo.setTodayAnswer(metric(todayAnswer, todayContent.getAnswerCount() - yesterdayContent.getAnswerCount(), weekAnswerTotal));
        vo.setTodayComment(metric(todayComment, todayContent.getCommentCount() - yesterdayContent.getCommentCount(), weekCommentTotal));
        vo.setTodayReport(metric(todayReport, todayGovernance.getReportCount() - yesterdayGovernance.getReportCount(), weekReportTotal));
        vo.setPendingAudit(metric(pendingAudit, todayGovernance.getAuditCount() - yesterdayGovernance.getAuditCount(), weekAuditTotal));
        vo.setPendingReport(metric(pendingReport, todayGovernance.getReportCount() - yesterdayGovernance.getReportCount(), weekReportTotal));
        return vo;
    }

    @Override
    public DashboardContentTrendRespVO contentTrend(Integer days) {
        List<DashboardContentTrendVO> points = dashboardStatMapper.selectContentTrend(resolveDaysMinusOne(days));
        long peakValue = -1;
        String peakDate = null;
        for (DashboardContentTrendVO p : points) {
            long total = nz(p.getQuestionCount()) + nz(p.getAnswerCount()) + nz(p.getCommentCount());
            if (total > peakValue) {
                peakValue = total;
                peakDate = p.getDate();
            }
        }
        DashboardContentTrendRespVO resp = new DashboardContentTrendRespVO();
        resp.setPoints(points);
        resp.setPeakDate(peakDate);
        resp.setPeakValue(Math.max(peakValue, 0));
        resp.setSummaryText(peakDate == null ? "暂无内容趋势数据" : "内容总量在 " + peakDate + " 达峰，峰值 " + peakValue);
        return resp;
    }

    @Override
    public DashboardGovernanceTrendRespVO governanceTrend(Integer days) {
        List<DashboardGovernanceTrendVO> points = dashboardStatMapper.selectGovernanceTrend(resolveDaysMinusOne(days));
        long peakValue = -1;
        String peakDate = null;
        for (DashboardGovernanceTrendVO p : points) {
            long total = nz(p.getReportCount()) + nz(p.getAuditCount());
            if (total > peakValue) {
                peakValue = total;
                peakDate = p.getDate();
            }
        }
        DashboardGovernanceTrendRespVO resp = new DashboardGovernanceTrendRespVO();
        resp.setPoints(points);
        resp.setPeakDate(peakDate);
        resp.setPeakValue(Math.max(peakValue, 0));
        resp.setSummaryText(peakDate == null ? "暂无治理趋势数据" : "治理事件在 " + peakDate + " 达峰，峰值 " + peakValue);
        return resp;
    }

    @Override
    public DashboardUserActivityTrendRespVO userActivityTrend(Integer days) {
        List<DashboardUserActivityTrendVO> points = dashboardStatMapper.selectUserActivityTrend(resolveDaysMinusOne(days));
        long peakValue = -1;
        String peakDate = null;
        for (DashboardUserActivityTrendVO p : points) {
            long total = nz(p.getNewUserCount()) + nz(p.getActiveUserCount()) + nz(p.getSearchCount());
            if (total > peakValue) {
                peakValue = total;
                peakDate = p.getDate();
            }
        }
        DashboardUserActivityTrendRespVO resp = new DashboardUserActivityTrendRespVO();
        resp.setPoints(points);
        resp.setPeakDate(peakDate);
        resp.setPeakValue(Math.max(peakValue, 0));
        resp.setSummaryText(peakDate == null ? "暂无用户活跃趋势数据" : "用户活跃总量在 " + peakDate + " 达峰，峰值 " + peakValue);
        return resp;
    }

    @Override
    public List<CmsAuditPageItemVO> pendingAudits(Integer limit) {
        int resolvedLimit = resolveLimit(limit, 10, 50);
        PageHelper.startPage(1, resolvedLimit);
        List<CmsAuditPageItemVO> items = cmsAuditMapper.selectAdminAuditPage(null, 1, null, null, null, null, "createdAt", "desc");
        for (CmsAuditPageItemVO item : items) {
            item.setRiskLevel(computeRiskLevel(item));
            item.setHitKeywords(extractHitKeywords(item.getHitDetail()));
        }
        return items;
    }

    @Override
    public List<CmsReportPageItemVO> pendingReports(Integer limit) {
        int resolvedLimit = resolveLimit(limit, 10, 50);
        PageHelper.startPage(1, resolvedLimit);
        return cmsReportMapper.selectAdminReportPage(null, 1, null, null, null, null);
    }

    @Override
    public List<DashboardHotTagVO> hotTags(Integer limit) {
        return dashboardStatMapper.selectHotTags(resolveLimit(limit, 10, 50));
    }

    @Override
    public List<DashboardHotTopicVO> hotTopics(Integer limit) {
        return dashboardStatMapper.selectHotTopics(resolveLimit(limit, 10, 50));
    }

    @Override
    public long newUserTagCount(Integer days) {
        return dashboardStatMapper.countNewUserTags(resolveDaysMinusOne(days));
    }

    @Override
    public List<DashboardNewTagVO> newUserTags(Integer days, Integer limit) {
        return dashboardStatMapper.selectNewUserTags(resolveDaysMinusOne(days), resolveLimit(limit, 10, 50));
    }

    @Override
    public List<DashboardNewTagTrendPointVO> newUserTagTrend(Integer days) {
        return dashboardStatMapper.selectNewUserTagTrend(resolveDaysMinusOne(days));
    }

    @Override
    public DashboardOverviewVO overview(Integer days, Integer todoLimit, Integer hotLimit) {
        int resolvedTodoLimit = resolveLimit(todoLimit, 10, 50);
        int resolvedHotLimit = resolveLimit(hotLimit, 10, 50);

        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setKpi(kpi());
        vo.setContentTrend(contentTrend(days));
        vo.setGovernanceTrend(governanceTrend(days));
        vo.setUserActivityTrend(userActivityTrend(days));
        vo.setPendingAudits(pendingAudits(resolvedTodoLimit));
        vo.setPendingReports(pendingReports(resolvedTodoLimit));
        vo.setHotTags(hotTags(resolvedHotLimit));
        vo.setHotTopics(hotTopics(resolvedHotLimit));
        vo.setNewUserTagCount(newUserTagCount(days));
        vo.setNewUserTags(newUserTags(days, resolvedHotLimit));
        vo.setNewUserTagTrend(newUserTagTrend(days));
        return vo;
    }

    private int resolveDaysMinusOne(Integer days) {
        int resolvedDays = (days == null || days <= 0) ? 7 : days;
        if (resolvedDays > 30) {
            resolvedDays = 30;
        }
        return resolvedDays - 1;
    }

    private int resolveLimit(Integer value, int defaultValue, int max) {
        int resolved = (value == null || value <= 0) ? defaultValue : value;
        return Math.min(resolved, max);
    }

    private DashboardKpiMetricVO metric(long value, long dayOverDay, long weekTotal) {
        DashboardKpiMetricVO metric = new DashboardKpiMetricVO();
        metric.setValue(value);
        metric.setDayOverDay(dayOverDay);
        metric.setWeekTotal(weekTotal);
        metric.setWeekAvg(Math.round((weekTotal / 7.0) * 10) / 10.0);
        return metric;
    }

    private long sumContent(List<DashboardContentTrendVO> points, String field) {
        long total = 0;
        for (DashboardContentTrendVO p : points) {
            switch (field) {
                case "question" -> total += nz(p.getQuestionCount());
                case "answer" -> total += nz(p.getAnswerCount());
                case "comment" -> total += nz(p.getCommentCount());
            }
        }
        return total;
    }

    private long sumGovernance(List<DashboardGovernanceTrendVO> points, String field) {
        long total = 0;
        for (DashboardGovernanceTrendVO p : points) {
            switch (field) {
                case "report" -> total += nz(p.getReportCount());
                case "audit" -> total += nz(p.getAuditCount());
            }
        }
        return total;
    }

    private long nz(Long value) {
        return value == null ? 0L : value;
    }

    private DashboardContentTrendVO lastOrEmptyContent(List<DashboardContentTrendVO> list) {
        if (list == null || list.isEmpty()) {
            DashboardContentTrendVO v = new DashboardContentTrendVO();
            v.setQuestionCount(0L);
            v.setAnswerCount(0L);
            v.setCommentCount(0L);
            return v;
        }
        return list.get(list.size() - 1);
    }

    private DashboardContentTrendVO secondLastOrEmptyContent(List<DashboardContentTrendVO> list) {
        if (list == null || list.size() < 2) {
            DashboardContentTrendVO v = new DashboardContentTrendVO();
            v.setQuestionCount(0L);
            v.setAnswerCount(0L);
            v.setCommentCount(0L);
            return v;
        }
        return list.get(list.size() - 2);
    }

    private DashboardGovernanceTrendVO lastOrEmptyGovernance(List<DashboardGovernanceTrendVO> list) {
        if (list == null || list.isEmpty()) {
            DashboardGovernanceTrendVO v = new DashboardGovernanceTrendVO();
            v.setReportCount(0L);
            v.setAuditCount(0L);
            return v;
        }
        return list.get(list.size() - 1);
    }

    private DashboardGovernanceTrendVO secondLastOrEmptyGovernance(List<DashboardGovernanceTrendVO> list) {
        if (list == null || list.size() < 2) {
            DashboardGovernanceTrendVO v = new DashboardGovernanceTrendVO();
            v.setReportCount(0L);
            v.setAuditCount(0L);
            return v;
        }
        return list.get(list.size() - 2);
    }

    private String computeRiskLevel(CmsAuditPageItemVO item) {
        if (item == null) {
            return "low";
        }
        if (item.getModelScore() != null) {
            double s = item.getModelScore().doubleValue();
            if (s >= 0.85) {
                return "high";
            }
            if (s >= 0.60) {
                return "medium";
            }
            return "low";
        }
        if (StringUtils.hasText(item.getModelLabel())) {
            String label = item.getModelLabel().toLowerCase(Locale.ROOT);
            if (label.contains("abuse") || label.contains("illegal")) {
                return "high";
            }
            if (label.contains("medical") || label.contains("spam")) {
                return "medium";
            }
        }
        return "low";
    }

    private List<String> extractHitKeywords(JsonNode hitDetail) {
        if (hitDetail == null || hitDetail.isNull()) {
            return Collections.emptyList();
        }
        List<String> keywords = new ArrayList<>();
        if (hitDetail.isArray()) {
            hitDetail.forEach(node -> addKeywordNode(node, keywords));
        } else {
            addKeywordNode(hitDetail, keywords);
        }
        return keywords.stream().filter(StringUtils::hasText).distinct().limit(10).toList();
    }

    private void addKeywordNode(JsonNode node, List<String> out) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isTextual()) {
            out.add(node.asText());
            return;
        }
        if (node.has("word")) {
            out.add(node.path("word").asText());
        }
        if (node.has("keyword")) {
            out.add(node.path("keyword").asText());
        }
        if (node.has("hitWord")) {
            out.add(node.path("hitWord").asText());
        }
        if (node.has("keywords") && node.path("keywords").isArray()) {
            node.path("keywords").forEach(n -> out.add(n.asText()));
        }
        if (node.has("hitWords") && node.path("hitWords").isArray()) {
            node.path("hitWords").forEach(n -> out.add(n.asText()));
        }
    }
}
