import type { CmsAuditPageItemVO } from "./audit";
import type { CmsReportPageItemVO } from "./report";

export type DashboardKpiMetricVO = {
  value?: number;
  dayOverDay?: number;
  weekAvg?: number;
  weekTotal?: number;
};

export type DashboardKpiVO = Record<string, DashboardKpiMetricVO | number | undefined>;

export type DashboardContentTrendVO = {
  date: string;
  questionCount?: number;
  answerCount?: number;
  commentCount?: number;
};

export type DashboardGovernanceTrendVO = {
  date: string;
  reportCount?: number;
  auditCount?: number;
};

export type DashboardTrendBundleVO<TPoint> = {
  points: TPoint[];
  peakDate?: string;
  peakValue?: number;
  summaryText?: string;
};

export type DashboardUserActivityTrendVO = {
  date: string;
  newUserCount?: number;
  activeUserCount?: number;
  searchCount?: number;
};

export type DashboardHotTagVO = {
  id: number;
  name: string;
  source?: number;
  status?: number;
  useCount?: number;
  createdAt?: string;
};

export type DashboardHotTopicVO = {
  id: number;
  title: string;
  status?: number;
  followCount?: number;
  questionCount?: number;
  todayNewCount?: number;
  createdAt?: string;
};

export type DashboardNewTagVO = {
  id: number;
  name: string;
  source?: number;
  status?: number;
  createdAt?: string;
};

export type DashboardOverviewVO = {
  kpi: DashboardKpiVO;
  contentTrend: DashboardTrendBundleVO<DashboardContentTrendVO>;
  governanceTrend: DashboardTrendBundleVO<DashboardGovernanceTrendVO>;
  userActivityTrend?: DashboardTrendBundleVO<DashboardUserActivityTrendVO>;
  pendingAudits: CmsAuditPageItemVO[];
  pendingReports: CmsReportPageItemVO[];
  hotTags: DashboardHotTagVO[];
  hotTopics: DashboardHotTopicVO[];
  newUserTagCount: number;
  newUserTags: DashboardNewTagVO[];
  newUserTagTrend?: Array<{ date: string; count: number; percent?: number }>;
};

export type DashboardNewTagsResponse = {
  count: number;
  items: DashboardNewTagVO[];
  trend?: Array<{ date: string; count: number; percent?: number }>;
};
