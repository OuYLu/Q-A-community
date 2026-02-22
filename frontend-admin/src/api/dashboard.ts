import http from "./http";
import type { Result } from "../types/api";
import type {
  DashboardContentTrendVO,
  DashboardGovernanceTrendVO,
  DashboardHotTagVO,
  DashboardHotTopicVO,
  DashboardKpiVO,
  DashboardNewTagsResponse,
  DashboardOverviewVO,
  DashboardTrendBundleVO,
  DashboardUserActivityTrendVO
} from "../types/dashboard";
import type { CmsAuditPageItemVO } from "../types/audit";
import type { CmsReportPageItemVO } from "../types/report";

export const getDashboardOverview = (params?: { days?: number; todoLimit?: number; hotLimit?: number }) => {
  return http.get<Result<DashboardOverviewVO>>("/admin/stat/dashboard/overview", { params });
};

export const getDashboardKpi = () => {
  return http.get<Result<DashboardKpiVO>>("/admin/stat/dashboard/kpi");
};

export const getDashboardContentTrend = (params?: { days?: number }) => {
  return http.get<Result<DashboardTrendBundleVO<DashboardContentTrendVO>>>("/admin/stat/dashboard/trend/content", { params });
};

export const getDashboardGovernanceTrend = (params?: { days?: number }) => {
  return http.get<Result<DashboardTrendBundleVO<DashboardGovernanceTrendVO>>>("/admin/stat/dashboard/trend/governance", { params });
};

export const getDashboardUserActivityTrend = (params?: { days?: number }) => {
  return http.get<Result<DashboardTrendBundleVO<DashboardUserActivityTrendVO>>>("/admin/stat/dashboard/trend/user-activity", { params });
};

export const getDashboardTodoAudits = (params?: { limit?: number }) => {
  return http.get<Result<CmsAuditPageItemVO[]>>("/admin/stat/dashboard/todo/audits", { params });
};

export const getDashboardTodoReports = (params?: { limit?: number }) => {
  return http.get<Result<CmsReportPageItemVO[]>>("/admin/stat/dashboard/todo/reports", { params });
};

export const getDashboardHotTags = (params?: { limit?: number }) => {
  return http.get<Result<DashboardHotTagVO[]>>("/admin/stat/dashboard/hot/tags", { params });
};

export const getDashboardHotTopics = (params?: { limit?: number }) => {
  return http.get<Result<DashboardHotTopicVO[]>>("/admin/stat/dashboard/hot/topics", { params });
};

export const getDashboardNewTags = (params?: { days?: number; limit?: number }) => {
  return http.get<Result<DashboardNewTagsResponse>>("/admin/stat/dashboard/new-tags", { params });
};
