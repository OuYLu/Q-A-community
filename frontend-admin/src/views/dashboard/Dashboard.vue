<template>
  <el-card class="dashboard-card">
    <div class="head-row">
      <div>
        <h2>运营看板</h2>
        <p>聚合内容增长、治理趋势与运营热点。</p>
      </div>
      <div class="head-actions">
        <el-select v-model="days" style="width: 120px" @change="loadData">
          <el-option label="近7天" :value="7" />
          <el-option label="近14天" :value="14" />
          <el-option label="近30天" :value="30" />
        </el-select>
        <el-button type="primary" @click="loadData">刷新</el-button>
      </div>
    </div>

    <section class="section overview-section">
      <div class="section-head">
        <h3 class="section-title">概览 Overview</h3>
        <p class="section-sub">核心KPI与关键趋势</p>
      </div>

      <div class="kpi-wrap" v-loading="loading">
      <div class="kpi-main-grid">
        <div v-for="card in primaryKpis" :key="card.key" class="kpi-card is-main" :class="card.key">
          <div class="kpi-head">
            <div class="kpi-label strong">{{ card.label }}</div>
            <el-button type="primary" link @click="card.key === 'pendingAudit' ? goAuditHandle() : goReportHandle()">
              去处理
            </el-button>
          </div>
          <div class="kpi-value">{{ num(card.value) }}</div>
          <div class="kpi-delta" :class="deltaClass(card.delta)">
            <span class="arrow">{{ deltaArrow(card.delta) }}</span>
            <span>{{ card.deltaLabel }} {{ signed(card.delta) }}</span>
          </div>
          <div class="kpi-sub">{{ card.sub }}</div>
        </div>
      </div>

      <div class="kpi-grid">
        <div v-for="card in secondaryKpis" :key="card.key" class="kpi-card" :class="card.key">
          <div class="kpi-label">{{ card.label }}</div>
          <div class="kpi-value">{{ num(card.value) }}</div>
          <div class="kpi-delta" :class="deltaClass(card.delta)">
            <span class="arrow">{{ deltaArrow(card.delta) }}</span>
            <span>{{ card.deltaLabel }} {{ signed(card.delta) }}</span>
          </div>
          <div class="kpi-sub">{{ card.sub }}</div>
        </div>
      </div>
      </div>

      <div class="panel-grid" v-loading="loading">
      <div class="panel">
        <div class="panel-title-row">
          <div class="panel-title">近{{ days }}天内容趋势</div>
          <div class="panel-summary">{{ contentTrendSummary }}</div>
        </div>
        <div class="trend-switch">
          <el-radio-group v-model="contentMetricMode" size="small">
            <el-radio-button label="content">问题/回答/评论</el-radio-button>
            <el-radio-button label="user" :disabled="!hasUserSearchTrendData">新增用户/活跃用户/搜索量</el-radio-button>
          </el-radio-group>
          <span v-if="!hasUserSearchTrendData" class="switch-hint">暂无“用户/搜索”趋势数据</span>
        </div>
        <div class="legend">
          <span v-for="series in activeContentSeries" :key="series.key">
            <i class="dot" :style="{ background: series.color }"></i>{{ series.label }}
          </span>
        </div>
        <div class="chart-wrap">
          <svg v-if="activeContentDates.length > 0" :viewBox="`0 0 ${chartW} ${chartH}`" class="chart-svg" preserveAspectRatio="none">
            <line v-for="(y, idx) in gridY" :key="`c-grid-${idx}`" :x1="padL" :x2="chartW - padR" :y1="y" :y2="y" class="chart-grid light" />
            <path v-for="series in activeContentSeries" :key="`line-${series.key}`" :d="buildLinePath(series.values, contentMax)" :style="{ stroke: series.color }" class="line-common" />
            <g v-for="series in activeContentSeries" :key="`last-${series.key}`">
              <circle v-if="lineLastPoint(series.values, contentMax)" :cx="lineLastPoint(series.values, contentMax)!.x" :cy="lineLastPoint(series.values, contentMax)!.y" r="3.5" :fill="series.color" />
              <text
                v-if="lineLastPoint(series.values, contentMax)"
                :x="labelX(lineLastPoint(series.values, contentMax)!.x)"
                :y="labelY(lineLastPoint(series.values, contentMax)!.y, activeContentSeries.findIndex((it) => it.key === series.key))"
                class="last-label"
              >
                {{ lineLastPoint(series.values, contentMax)!.value }}
              </text>
            </g>
            <g v-if="contentPeakPoint">
              <circle :cx="contentPeakPoint.x" :cy="contentPeakPoint.y" r="4.5" class="peak-dot" />
            </g>
          </svg>
          <div v-else class="empty">暂无数据</div>
        </div>
        <div class="chart-labels" :style="{ gridTemplateColumns: `repeat(${Math.max(activeContentDates.length, 1)}, minmax(0, 1fr))` }">
          <div v-for="item in activeContentDates" :key="`content-${item}`" class="label">{{ item }}</div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-title-row">
          <div class="panel-title">近{{ days }}天治理趋势</div>
          <div class="panel-summary">{{ governanceTrendSummary }}</div>
        </div>
        <div class="legend">
          <span><i class="dot r"></i>举报单</span>
          <span><i class="dot u"></i>审核单</span>
        </div>
        <div class="chart-wrap">
          <svg v-if="governanceTrend.length > 0" :viewBox="`0 0 ${chartW} ${chartH}`" class="chart-svg" preserveAspectRatio="none">
            <line v-for="(y, idx) in gridY" :key="`g-grid-${idx}`" :x1="padL" :x2="chartW - padR" :y1="y" :y2="y" class="chart-grid light" />
            <path :d="governanceLineReport" class="line-r line-common" />
            <path :d="governanceLineAudit" class="line-u line-common" />
            <g v-if="lineLastPoint(governanceReportSeries, governanceMax)">
              <circle :cx="lineLastPoint(governanceReportSeries, governanceMax)!.x" :cy="lineLastPoint(governanceReportSeries, governanceMax)!.y" r="3.5" class="line-r-fill" />
              <text
                :x="labelX(lineLastPoint(governanceReportSeries, governanceMax)!.x)"
                :y="labelY(lineLastPoint(governanceReportSeries, governanceMax)!.y, 0)"
                class="last-label"
              >
                {{ lineLastPoint(governanceReportSeries, governanceMax)!.value }}
              </text>
            </g>
            <g v-if="lineLastPoint(governanceAuditSeries, governanceMax)">
              <circle :cx="lineLastPoint(governanceAuditSeries, governanceMax)!.x" :cy="lineLastPoint(governanceAuditSeries, governanceMax)!.y" r="3.5" class="line-u-fill" />
              <text
                :x="labelX(lineLastPoint(governanceAuditSeries, governanceMax)!.x)"
                :y="labelY(lineLastPoint(governanceAuditSeries, governanceMax)!.y, 1)"
                class="last-label"
              >
                {{ lineLastPoint(governanceAuditSeries, governanceMax)!.value }}
              </text>
            </g>
          </svg>
          <div v-else class="empty">暂无数据</div>
        </div>
        <div class="chart-labels" :style="{ gridTemplateColumns: `repeat(${Math.max(governanceTrend.length, 1)}, minmax(0, 1fr))` }">
          <div v-for="item in governanceTrend" :key="`govern-${item.date}`" class="label">{{ item.date }}</div>
        </div>
      </div>
    </div>
    </section>

    <div class="section-divider"></div>

    <section class="section insights-section">
      <div class="section-head">
        <h3 class="section-title">热点 Insights</h3>
        <p class="section-sub">热门标签、热门专题与新增标签趋势</p>
      </div>

      <div class="panel-grid" v-loading="loading">
      <div class="panel">
        <div class="panel-title">热门标签 TOP{{ hotLimit }}</div>
        <div class="hot-tag-list">
          <div v-for="(item, idx) in hotTagsRanked" :key="`tag-${item.id}`" class="hot-tag-item">
            <div class="tag-rank" :class="{ top: idx < 3 }">#{{ idx + 1 }}</div>
            <div class="tag-main">
              <div class="tag-line">
                <el-tooltip :content="item.name" placement="top">
                  <span class="tag-name">{{ item.name }}</span>
                </el-tooltip>
                <span class="tag-count">{{ num(item.useCount) }}</span>
              </div>
              <div class="tag-bar-bg">
                <div class="tag-bar" :style="{ width: metricBarWidth(item.useCount, hotTagMax) }"></div>
              </div>
            </div>
          </div>
          <div v-if="hotTagsRanked.length === 0" class="empty">暂无数据</div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-title-row">
          <div class="panel-title">热门专题 TOP{{ hotLimit }}</div>
          <el-segmented
            v-model="hotTopicSortBy"
            :options="[
              { label: '按关注数', value: 'follow' },
              { label: '按问题数', value: 'question' }
            ]"
            size="small"
          />
        </div>
        <el-table :data="sortedHotTopics" size="small" stripe style="width: 100%" class="dense-table">
          <el-table-column prop="title" label="专题" min-width="180" show-overflow-tooltip>
            <template #default="scope">
              <el-tooltip :content="scope.row.title" placement="top">
                <div class="cell-ellipsis key-col">{{ scope.row.title }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="关注数" width="90">
            <template #default="scope">
              <div class="metric-cell">
                <span>{{ num(scope.row.followCount) }}</span>
                <div class="mini-bar-bg"><div class="mini-bar follow" :style="{ width: metricBarWidth(scope.row.followCount, hotTopicMaxFollow) }"></div></div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="问题数" width="90">
            <template #default="scope">
              <div class="metric-cell">
                <span>{{ num(scope.row.questionCount) }}</span>
                <div class="mini-bar-bg"><div class="mini-bar question" :style="{ width: metricBarWidth(scope.row.questionCount, hotTopicMaxQuestion) }"></div></div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="scope">{{ topicStatusText(scope.row.status) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

      <div class="panel" v-loading="loading">
        <div class="panel-title">新增标签趋势（最近{{ days }}天）</div>
        <div class="new-tag-head">总数：<strong>{{ num(newUserTagCount) }}</strong></div>
        <div class="new-tag-trend">
          <div v-for="item in newTagTrendDisplay" :key="`trend-${item.date}`" class="trend-item">
            <div class="trend-date">{{ item.date }}</div>
            <div class="trend-bar-wrap">
              <div class="trend-bar" :style="{ width: `${item.percent}%` }"></div>
            </div>
            <div class="trend-val">{{ item.count }}</div>
          </div>
          <div v-if="newTagTrendDisplay.length === 0" class="empty">暂无数据</div>
        </div>
        <el-table :data="newUserTags" style="width: 100%; margin-top: 10px">
          <el-table-column prop="id" label="标签ID" width="100" />
          <el-table-column prop="name" label="标签名" min-width="220" />
          <el-table-column label="来源" width="120">
            <template #default="scope">{{ tagSourceText(scope.row.source) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">{{ scope.row.status === 1 ? '启用' : '禁用' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" width="190">
            <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <div class="section-divider"></div>

    <section class="section actions-section">
      <div class="section-head">
        <h3 class="section-title action-title">待办 Actions</h3>
        <p class="section-sub">优先处理审核与举报任务</p>
      </div>

      <div class="panel-grid" v-loading="loading">
      <div class="panel">
        <div class="panel-title">待审审核单 TOP{{ todoLimit }}</div>
        <el-table :data="pendingAudits" size="small" stripe style="width: 100%" class="action-table dense-table">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="业务类型" width="100">
            <template #default="scope">{{ bizTypeText(scope.row.bizType) }}</template>
          </el-table-column>
          <el-table-column label="来源" width="100">
            <template #default="scope">{{ triggerSourceText(scope.row.triggerSource) }}</template>
          </el-table-column>
          <el-table-column label="风险" width="100">
            <template #default="scope">
              <el-tag size="small" effect="plain" :type="riskText(scope.row).includes('高') ? 'danger' : riskText(scope.row).includes('中') ? 'warning' : 'info'">
                {{ riskText(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="命中" min-width="140" show-overflow-tooltip>
            <template #default="scope">
              <el-tooltip :content="hitText(scope.row)" placement="top">
                <div class="cell-ellipsis">{{ hitText(scope.row) }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="标题/摘要" min-width="220" show-overflow-tooltip>
            <template #default="scope">
              <el-tooltip :content="oneLine(scope.row.contentTitle || scope.row.title || scope.row.contentSnippet || scope.row.contentSummary)" placement="top">
                <div class="cell-ellipsis key-col">{{ oneLine(scope.row.contentTitle || scope.row.title || scope.row.contentSnippet || scope.row.contentSummary) }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="scope">
              <el-tag size="small" type="warning">{{ auditStatusText(scope.row.auditStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="scope">
              <el-button size="small" type="warning" @click="goAuditHandle(scope.row.id)">去处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel">
        <div class="panel-title">待处理举报 TOP{{ todoLimit }}</div>
        <el-table :data="pendingReports" size="small" stripe style="width: 100%" class="action-table dense-table">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="业务类型" width="100">
            <template #default="scope">{{ bizTypeText(scope.row.bizType) }}</template>
          </el-table-column>
          <el-table-column label="原因" width="100">
            <template #default="scope">
              <el-tag size="small" effect="dark" :type="reasonTagType(scope.row.reasonType)">{{ reasonTypeText(scope.row.reasonType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="内容摘要" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              <el-tooltip :content="oneLine(scope.row.contentTitle || scope.row.contentSnippet)" placement="top">
                <div class="cell-ellipsis key-col">{{ oneLine(scope.row.contentTitle || scope.row.contentSnippet) }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="scope">
              <el-tag size="small" type="danger">{{ reportStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="scope">
              <el-button size="small" type="danger" @click="goReportHandle(scope.row.id)">去处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    </section>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  getDashboardContentTrend,
  getDashboardGovernanceTrend,
  getDashboardHotTags,
  getDashboardHotTopics,
  getDashboardKpi,
  getDashboardNewTags,
  getDashboardUserActivityTrend,
  getDashboardTodoAudits,
  getDashboardTodoReports
} from "../../api/dashboard";
import type {
  DashboardContentTrendVO,
  DashboardGovernanceTrendVO,
  DashboardHotTagVO,
  DashboardHotTopicVO,
  DashboardKpiVO,
  DashboardKpiMetricVO,
  DashboardNewTagVO,
  DashboardUserActivityTrendVO
} from "../../types/dashboard";
import type { CmsAuditPageItemVO } from "../../types/audit";
import type { CmsReportPageItemVO } from "../../types/report";

const loading = ref(false);
const router = useRouter();
const days = ref(7);
const todoLimit = ref(10);
const hotLimit = ref(10);
const contentMetricMode = ref<"content" | "user">("content");

const kpi = ref<DashboardKpiVO>({});
const contentTrend = ref<DashboardContentTrendVO[]>([]);
const governanceTrend = ref<DashboardGovernanceTrendVO[]>([]);
const contentSummaryText = ref("");
const governanceSummaryText = ref("");
const contentPeakDate = ref("");
const contentPeakValue = ref<number | null>(null);
const userSummaryText = ref("");
const userPeakDate = ref("");
const userPeakValue = ref<number | null>(null);
const pendingAudits = ref<CmsAuditPageItemVO[]>([]);
const pendingReports = ref<CmsReportPageItemVO[]>([]);
const hotTags = ref<DashboardHotTagVO[]>([]);
const hotTopics = ref<DashboardHotTopicVO[]>([]);
const newUserTagCount = ref(0);
const newUserTags = ref<DashboardNewTagVO[]>([]);
const userSearchTrend = ref<DashboardUserActivityTrendVO[]>([]);
const newTagTrend = ref<Array<{ date: string; count: number; percent?: number }>>([]);

const chartW = 620;
const chartH = 220;
const padL = 24;
const padR = 14;
const padT = 14;
const padB = 34;
const labelWidth = 16;

const num = (value?: number) => String(value || 0);
const signed = (value: number) => (value > 0 ? `+${value}` : `${value}`);
const deltaArrow = (value: number) => (value > 0 ? "↑" : value < 0 ? "↓" : "→");
const deltaClass = (value: number) => (value > 0 ? "up" : value < 0 ? "down" : "flat");
const metricFromKpi = (keys: string[]): DashboardKpiMetricVO => {
  for (const key of keys) {
    const raw = kpi.value[key];
    if (raw == null) continue;
    if (typeof raw === "number") {
      return { value: raw, dayOverDay: 0, weekAvg: 0, weekTotal: 0 };
    }
    return {
      value: raw.value || 0,
      dayOverDay: raw.dayOverDay || 0,
      weekAvg: raw.weekAvg || 0,
      weekTotal: raw.weekTotal || 0
    };
  }
  return { value: 0, dayOverDay: 0, weekAvg: 0, weekTotal: 0 };
};

const bizTypeText = (value?: number) => {
  if (value === 1) return "问题";
  if (value === 2) return "回答";
  if (value === 3) return "评论";
  if (value === 4) return "知识库";
  return value == null ? "-" : String(value);
};

const reasonTypeText = (value?: number) => {
  if (value === 1) return "垃圾营销";
  if (value === 2) return "违法违规";
  if (value === 3) return "低质灌水";
  if (value === 4) return "其他";
  return value == null ? "-" : String(value);
};

const reasonTagType = (value?: number) => {
  if (value === 2) return "danger";
  if (value === 3) return "warning";
  if (value === 1) return "info";
  return "info";
};

const triggerSourceText = (value?: number) => {
  if (value === 1) return "用户提交";
  if (value === 2) return "举报触发";
  if (value === 3) return "人工转审";
  return value == null ? "-" : String(value);
};

const tagSourceText = (value?: number) => {
  if (value === 1) return "系统";
  if (value === 2) return "用户";
  return value == null ? "-" : String(value);
};

const topicStatusText = (value?: number) => {
  if (value === 1) return "草稿";
  if (value === 2) return "已发布";
  if (value === 3) return "已下线";
  if (value === 4) return "已删除";
  return value == null ? "-" : String(value);
};

const auditStatusText = (value?: number) => {
  if (value === 1) return "待审核";
  if (value === 2) return "已通过";
  if (value === 3) return "已驳回";
  return value == null ? "-" : String(value);
};

const reportStatusText = (value?: number) => {
  if (value === 1) return "待处理";
  if (value === 2) return "已处理";
  if (value === 3) return "不成立";
  return value == null ? "-" : String(value);
};

const formatDateTime = (value?: string) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const pad = (numVal: number) => String(numVal).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}:${pad(date.getSeconds())}`;
};

const buildLinePath = (values: number[], max: number) => {
  if (values.length === 0) return "";
  const usableW = chartW - padL - padR;
  const usableH = chartH - padT - padB;
  const stepX = values.length > 1 ? usableW / (values.length - 1) : 0;
  return values
    .map((val, idx) => {
      const x = padL + idx * stepX;
      const y = padT + usableH - (val / max) * usableH;
      return `${idx === 0 ? "M" : "L"} ${x} ${y}`;
    })
    .join(" ");
};

const pointAt = (values: number[], max: number, index: number) => {
  if (values.length === 0 || index < 0 || index >= values.length) return null;
  const usableW = chartW - padL - padR;
  const usableH = chartH - padT - padB;
  const stepX = values.length > 1 ? usableW / (values.length - 1) : 0;
  const value = values[index] || 0;
  return {
    x: padL + index * stepX,
    y: padT + usableH - (value / max) * usableH,
    value,
    index
  };
};

const lineLastPoint = (values: number[], max: number) => pointAt(values, max, values.length - 1);
const labelX = (x: number) => Math.min(chartW - padR - labelWidth, x + 6);
const labelY = (y: number, seriesIndex = 0) => Math.max(padT + 12, y - 8 + seriesIndex * 14);

const gridY = computed(() => {
  const lines = 4;
  const usableH = chartH - padT - padB;
  return Array.from({ length: lines + 1 }, (_v, idx) => padT + (usableH / lines) * idx);
});

const hasUserSearchTrendData = computed(() => userSearchTrend.value.length > 0);

const activeContentDates = computed(() => {
  if (contentMetricMode.value === "user") return userSearchTrend.value.map((it) => it.date);
  return contentTrend.value.map((it) => it.date);
});

const activeContentSeries = computed(() => {
  if (contentMetricMode.value === "user" && hasUserSearchTrendData.value) {
    return [
      { key: "newUser", label: "新增用户", color: "#409eff", values: userSearchTrend.value.map((it) => it.newUserCount || 0) },
      { key: "activeUser", label: "活跃用户", color: "#67c23a", values: userSearchTrend.value.map((it) => it.activeUserCount || 0) },
      { key: "searchCount", label: "搜索量", color: "#e6a23c", values: userSearchTrend.value.map((it) => it.searchCount || 0) }
    ];
  }
  return [
    { key: "question", label: "问题", color: "#409eff", values: contentTrend.value.map((it) => it.questionCount || 0) },
    { key: "answer", label: "回答", color: "#67c23a", values: contentTrend.value.map((it) => it.answerCount || 0) },
    { key: "comment", label: "评论", color: "#e6a23c", values: contentTrend.value.map((it) => it.commentCount || 0) }
  ];
});

const contentMax = computed(() => {
  const values = activeContentSeries.value.flatMap((it) => it.values);
  return Math.max(...values, 1);
});

const governanceMax = computed(() => {
  const values = governanceTrend.value.flatMap((it) => [it.reportCount || 0, it.auditCount || 0]);
  return Math.max(...values, 1);
});

const governanceLineReport = computed(() =>
  buildLinePath(governanceTrend.value.map((it) => it.reportCount || 0), governanceMax.value)
);
const governanceLineAudit = computed(() => buildLinePath(governanceTrend.value.map((it) => it.auditCount || 0), governanceMax.value));

const governanceReportSeries = computed(() => governanceTrend.value.map((it) => it.reportCount || 0));
const governanceAuditSeries = computed(() => governanceTrend.value.map((it) => it.auditCount || 0));

const contentTrendSummary = computed(() =>
  contentMetricMode.value === "user"
    ? userSummaryText.value || "暂无摘要"
    : contentSummaryText.value || "暂无摘要"
);

const governanceTrendSummary = computed(() => governanceSummaryText.value || "暂无摘要");

const contentPeakPoint = computed(() => {
  const values = activeContentSeries.value[0]?.values || [];
  if (values.length === 0) return null;
  const peakDate = contentMetricMode.value === "user" ? userPeakDate.value : contentPeakDate.value;
  const peakValue = contentMetricMode.value === "user" ? userPeakValue.value : contentPeakValue.value;
  if (!peakDate || peakValue == null) return null;
  const idx = activeContentDates.value.findIndex((it) => it === peakDate);
  if (idx < 0) return null;
  return pointAt(values, contentMax.value, idx);
});

const primaryKpis = computed(() => [
  {
    key: "pendingAudit",
    label: "待审核",
    value: metricFromKpi(["pendingAudit", "pendingAuditCount"]).value || 0,
    delta: metricFromKpi(["pendingAudit", "pendingAuditCount"]).dayOverDay || 0,
    deltaLabel: "较昨日",
    sub: `近${days.value}天审核单累计 ${metricFromKpi(["pendingAudit", "pendingAuditCount"]).weekTotal || 0}`
  },
  {
    key: "pendingReport",
    label: "待处理举报",
    value: metricFromKpi(["pendingReport", "pendingReportCount"]).value || 0,
    delta: metricFromKpi(["pendingReport", "pendingReportCount"]).dayOverDay || 0,
    deltaLabel: "较昨日",
    sub: `近${days.value}天举报单累计 ${metricFromKpi(["pendingReport", "pendingReportCount"]).weekTotal || 0}`
  }
]);

const secondaryKpis = computed(() => [
  {
    key: "todayQuestion",
    label: "今日新增问题",
    value: metricFromKpi(["todayQuestion", "todayQuestionCount"]).value || 0,
    delta: metricFromKpi(["todayQuestion", "todayQuestionCount"]).dayOverDay || 0,
    deltaLabel: "较昨日",
    sub: `${days.value}日均值 ${metricFromKpi(["todayQuestion", "todayQuestionCount"]).weekAvg || 0}`
  },
  {
    key: "todayAnswer",
    label: "今日新增回答",
    value: metricFromKpi(["todayAnswer", "todayAnswerCount"]).value || 0,
    delta: metricFromKpi(["todayAnswer", "todayAnswerCount"]).dayOverDay || 0,
    deltaLabel: "较昨日",
    sub: `${days.value}日均值 ${metricFromKpi(["todayAnswer", "todayAnswerCount"]).weekAvg || 0}`
  },
  {
    key: "todayComment",
    label: "今日新增评论",
    value: metricFromKpi(["todayComment", "todayCommentCount"]).value || 0,
    delta: metricFromKpi(["todayComment", "todayCommentCount"]).dayOverDay || 0,
    deltaLabel: "较昨日",
    sub: `${days.value}日均值 ${metricFromKpi(["todayComment", "todayCommentCount"]).weekAvg || 0}`
  },
  {
    key: "todayReport",
    label: "今日新增举报",
    value: metricFromKpi(["todayReport", "todayReportCount"]).value || 0,
    delta: metricFromKpi(["todayReport", "todayReportCount"]).dayOverDay || 0,
    deltaLabel: "较昨日",
    sub: `${days.value}日均值 ${metricFromKpi(["todayReport", "todayReportCount"]).weekAvg || 0}`
  }
]);

const loadData = async () => {
  loading.value = true;
  try {
    const [
      kpiRes,
      contentRes,
      governanceRes,
      userActivityRes,
      todoAuditRes,
      todoReportRes,
      hotTagRes,
      hotTopicRes,
      newTagRes
    ] = await Promise.all([
      getDashboardKpi(),
      getDashboardContentTrend({ days: days.value }),
      getDashboardGovernanceTrend({ days: days.value }),
      getDashboardUserActivityTrend({ days: days.value }),
      getDashboardTodoAudits({ limit: todoLimit.value }),
      getDashboardTodoReports({ limit: todoLimit.value }),
      getDashboardHotTags({ limit: hotLimit.value }),
      getDashboardHotTopics({ limit: hotLimit.value }),
      getDashboardNewTags({ days: days.value, limit: hotLimit.value })
    ]);

    kpi.value = kpiRes.data || {};
    contentTrend.value = contentRes.data?.points || [];
    contentSummaryText.value = contentRes.data?.summaryText || "";
    contentPeakDate.value = contentRes.data?.peakDate || "";
    contentPeakValue.value = contentRes.data?.peakValue ?? null;
    governanceTrend.value = governanceRes.data?.points || [];
    governanceSummaryText.value = governanceRes.data?.summaryText || "";
    userSearchTrend.value = userActivityRes.data?.points || [];
    userSummaryText.value = userActivityRes.data?.summaryText || "";
    userPeakDate.value = userActivityRes.data?.peakDate || "";
    userPeakValue.value = userActivityRes.data?.peakValue ?? null;
    pendingAudits.value = todoAuditRes.data || [];
    pendingReports.value = todoReportRes.data || [];
    hotTags.value = hotTagRes.data || [];
    hotTopics.value = hotTopicRes.data || [];
    newUserTagCount.value = newTagRes.data?.count || 0;
    newUserTags.value = newTagRes.data?.items || [];
    newTagTrend.value = newTagRes.data?.trend || [];
  } finally {
    loading.value = false;
  }
};

const goAuditHandle = (id?: number) => {
  router.push({ path: "/content/audit", query: id ? { id: String(id) } : undefined });
};

const goReportHandle = (id?: number) => {
  router.push({ path: "/content/report", query: id ? { id: String(id) } : undefined });
};

const oneLine = (value?: string) => (value || "-").replace(/\s+/g, " ").trim() || "-";

const riskText = (row: CmsAuditPageItemVO) => {
  if (typeof row.riskLevel === "string" && row.riskLevel) return row.riskLevel;
  return "-";
};

const hitText = (row: CmsAuditPageItemVO) => {
  const keywords = row.hitKeywords as unknown;
  if (Array.isArray(keywords)) return oneLine(keywords.join(", "));
  if (typeof keywords === "string") return oneLine(keywords);
  return "-";
};

const hotTopicSortBy = ref<"follow" | "question">("follow");
const hotTopicMaxFollow = computed(() => Math.max(...hotTopics.value.map((it) => it.followCount || 0), 1));
const hotTopicMaxQuestion = computed(() => Math.max(...hotTopics.value.map((it) => it.questionCount || 0), 1));
const sortedHotTopics = computed(() => {
  const list = [...hotTopics.value];
  if (hotTopicSortBy.value === "question") {
    list.sort((a, b) => (b.questionCount || 0) - (a.questionCount || 0));
  } else {
    list.sort((a, b) => (b.followCount || 0) - (a.followCount || 0));
  }
  return list;
});

const metricBarWidth = (value?: number, max?: number) => {
  if (!max) return "0%";
  return `${Math.min(100, ((value || 0) / max) * 100)}%`;
};

const hotTagsRanked = computed(() => hotTags.value.slice(0, hotLimit.value));
const hotTagMax = computed(() => Math.max(...hotTagsRanked.value.map((it) => it.useCount || 0), 1));
const newTagTrendDisplay = computed(() => {
  if (newTagTrend.value.length === 0) return [];
  return newTagTrend.value.map((it) => ({
    date: it.date,
    count: it.count || 0,
    percent: typeof it.percent === "number" ? it.percent : 0
  }));
});


onMounted(() => {
  loadData();
});
</script>

<style scoped>
.dashboard-card {
  --dash-gutter: 16px;
}

.head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.head-actions {
  display: flex;
  gap: 8px;
}

.section {
  margin-top: 16px;
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 10px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  color: #2a344b;
  font-weight: 700;
}

.action-title {
  color: #1f2a44;
}

.section-sub {
  margin: 0;
  color: #8a93a5;
  font-size: 12px;
}

.section-divider {
  margin-top: 16px;
  border-top: 1px solid #eef1f6;
}

.kpi-wrap {
  margin-top: 12px;
}

.kpi-main-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--dash-gutter);
}

.kpi-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--dash-gutter);
}

.kpi-card {
  position: relative;
  border: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.05);
  padding: 16px;
  overflow: hidden;
}

.kpi-card::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: #c0c4cc;
}

.kpi-card.pendingAudit::before {
  background: #e6a23c;
}

.kpi-card.pendingReport::before {
  background: #f56c6c;
}

.kpi-card.todayQuestion::before {
  background: #409eff;
}

.kpi-card.todayAnswer::before {
  background: #67c23a;
}

.kpi-card.todayComment::before {
  background: #e6a23c;
}

.kpi-card.todayReport::before {
  background: #909399;
}

.kpi-card.is-main {
  background: linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
}

.kpi-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.kpi-label {
  color: #677288;
  font-size: 12px;
  letter-spacing: 0.2px;
}

.kpi-label.strong {
  color: #24324a;
  font-size: 13px;
  font-weight: 600;
}

.kpi-value {
  margin-top: 8px;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: #1f2a44;
}

.kpi-delta {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.kpi-delta.up {
  color: #e67e22;
}

.kpi-delta.down {
  color: #409eff;
}

.kpi-delta.flat {
  color: #909399;
}

.arrow {
  font-size: 12px;
}

.kpi-sub {
  margin-top: 4px;
  color: #9097a5;
  font-size: 12px;
}

.panel-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--dash-gutter);
}

.panel {
  border: 1px solid #eef1f6;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04);
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 10px;
  color: #2a344b;
}

.panel-title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.panel-summary {
  color: #8e97a8;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.trend-switch {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.switch-hint {
  color: #a0a8b8;
  font-size: 12px;
}

.legend {
  display: flex;
  gap: 12px;
  color: var(--app-text-muted);
  font-size: 12px;
  margin-bottom: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 4px;
}

.dot.q,
.line-q {
  background: #409eff;
  stroke: #409eff;
}

.dot.a,
.line-a {
  background: #67c23a;
  stroke: #67c23a;
}

.dot.c,
.line-c {
  background: #e6a23c;
  stroke: #e6a23c;
}

.dot.r,
.line-r {
  background: #f56c6c;
  stroke: #f56c6c;
}

.dot.u,
.line-u {
  background: #909399;
  stroke: #909399;
}

.chart-wrap {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  min-height: 220px;
}

.chart-svg {
  width: 100%;
  height: 220px;
  display: block;
}

.chart-grid {
  stroke: var(--app-border);
  stroke-width: 1;
}

.chart-grid.light {
  stroke: #f1f4f9;
}

.line-common {
  fill: none;
  stroke-width: 2.2;
  stroke-linecap: round;
}

.line-q,
.line-a,
.line-c,
.line-r,
.line-u {
  fill: none;
  stroke-width: 2;
}

.line-r-fill {
  fill: #f56c6c;
}

.line-u-fill {
  fill: #909399;
}

.last-label {
  font-size: 11px;
  fill: #6b7380;
  font-weight: 600;
}

.peak-dot {
  fill: #409eff;
}

.chart-labels {
  margin-top: 6px;
  display: grid;
  gap: 4px;
}

.label {
  font-size: 12px;
  color: var(--app-text-muted);
  text-align: center;
}

.empty {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-text-muted);
}

.new-tag-head {
  margin-bottom: 8px;
  color: var(--app-text-muted);
}

.hot-tag-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hot-tag-item {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 8px;
  align-items: center;
}

.tag-rank {
  color: #8a93a5;
  font-size: 12px;
  font-weight: 700;
}

.tag-rank.top {
  color: #e67e22;
}

.tag-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tag-line {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: baseline;
}

.tag-name {
  color: #2d3a52;
  font-weight: 600;
  max-width: 260px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-count {
  color: #677288;
  font-size: 12px;
}

.tag-bar-bg {
  height: 8px;
  border-radius: 999px;
  background: #f0f4fb;
  overflow: hidden;
}

.tag-bar {
  height: 100%;
  background: linear-gradient(90deg, #9ac5ff 0%, #4d8dff 100%);
}

.metric-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.mini-bar-bg {
  height: 5px;
  border-radius: 999px;
  background: #f1f4f9;
  overflow: hidden;
}

.mini-bar {
  height: 100%;
}

.mini-bar.follow {
  background: #409eff;
}

.mini-bar.question {
  background: #67c23a;
}

.new-tag-trend {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 8px 10px;
}

.trend-item {
  display: grid;
  grid-template-columns: 90px 1fr 40px;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.trend-item:last-child {
  margin-bottom: 0;
}

.trend-date {
  color: #677288;
  font-size: 12px;
}

.trend-bar-wrap {
  height: 8px;
  background: #f3f5f9;
  border-radius: 999px;
  overflow: hidden;
}

.trend-bar {
  height: 100%;
  background: linear-gradient(90deg, #79bbff 0%, #409eff 100%);
}

.trend-val {
  text-align: right;
  font-size: 12px;
  color: #495266;
}

.actions-section .section-head {
  margin-bottom: 12px;
}

.actions-section .section-title {
  font-size: 22px;
}

.actions-section :deep(.action-table .el-table__cell) {
  padding-top: 4px;
  padding-bottom: 4px;
}

.actions-section :deep(.action-table .el-button--small) {
  padding: 5px 12px;
  font-weight: 600;
}

:deep(.dense-table .el-table__cell) {
  padding-top: 6px;
  padding-bottom: 6px;
}

:deep(.dense-table .el-table__row:hover > td.el-table__cell) {
  background: #f5f8ff !important;
}

:deep(.dense-table .el-table__row--striped > td.el-table__cell) {
  background: #fbfcff;
}

.cell-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.key-col {
  color: #2b3a55;
  font-weight: 600;
}

@media (max-width: 1280px) {
  .kpi-main-grid {
    grid-template-columns: 1fr;
  }

  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }

  .panel-title-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
}
</style>
