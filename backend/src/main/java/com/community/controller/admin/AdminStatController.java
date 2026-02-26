package com.community.controller.admin;

import com.community.common.Result;
import com.community.service.AdminStatService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stat")
@RequiredArgsConstructor
@Tag(name = "后台仪表盘统计")
public class AdminStatController {
    private final AdminStatService adminStatService;

    @GetMapping("/dashboard/overview")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板总览")
    public Result<DashboardOverviewVO> overview(@RequestParam(required = false) Integer days,
                                                @RequestParam(required = false) Integer todoLimit,
                                                @RequestParam(required = false) Integer hotLimit) {
        return Result.success(adminStatService.overview(days, todoLimit, hotLimit));
    }

    @GetMapping("/dashboard/kpi")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板关键指标")
    public Result<DashboardKpiVO> kpi() {
        return Result.success(adminStatService.kpi());
    }

    @GetMapping("/dashboard/trend/content")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板内容趋势")
    public Result<DashboardContentTrendRespVO> contentTrend(@RequestParam(required = false) Integer days) {
        return Result.success(adminStatService.contentTrend(days));
    }

    @GetMapping("/dashboard/trend/governance")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板治理趋势")
    public Result<DashboardGovernanceTrendRespVO> governanceTrend(@RequestParam(required = false) Integer days) {
        return Result.success(adminStatService.governanceTrend(days));
    }

    @GetMapping("/dashboard/trend/user-activity")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板用户活跃趋势")
    public Result<DashboardUserActivityTrendRespVO> userActivityTrend(@RequestParam(required = false) Integer days) {
        return Result.success(adminStatService.userActivityTrend(days));
    }

    @GetMapping("/dashboard/todo/audits")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板待审核数")
    public Result<List<CmsAuditPageItemVO>> pendingAudits(@RequestParam(required = false) Integer limit) {
        return Result.success(adminStatService.pendingAudits(limit));
    }

    @GetMapping("/dashboard/todo/reports")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板待举报处理数")
    public Result<List<CmsReportPageItemVO>> pendingReports(@RequestParam(required = false) Integer limit) {
        return Result.success(adminStatService.pendingReports(limit));
    }

    @GetMapping("/dashboard/hot/tags")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板热门标签")
    public Result<List<DashboardHotTagVO>> hotTags(@RequestParam(required = false) Integer limit) {
        return Result.success(adminStatService.hotTags(limit));
    }

    @GetMapping("/dashboard/hot/topics")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板热门话题")
    public Result<List<DashboardHotTopicVO>> hotTopics(@RequestParam(required = false) Integer limit) {
        return Result.success(adminStatService.hotTopics(limit));
    }

    @GetMapping("/dashboard/new-tags")
    @PreAuthorize("hasAnyAuthority('menu:dashboard','dashboard:view','op:dashboard:view')")
    @Operation(summary = "看板新用户标签")
    public Result<Map<String, Object>> newTags(@RequestParam(required = false) Integer days,
                                               @RequestParam(required = false) Integer limit) {
        return Result.success(Map.of(
            "count", adminStatService.newUserTagCount(days),
            "items", adminStatService.newUserTags(days, limit),
            "trend", adminStatService.newUserTagTrend(days)
        ));
    }
}
