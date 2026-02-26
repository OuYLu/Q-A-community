<template>
  <el-card>
    <h2>举报处理</h2>
    <p>支持举报列表筛选、详情查看、处理动作执行与转审核队列。</p>

    <div class="toolbar">
      <el-form :inline="true" :model="query" class="toolbar-form">
        <el-form-item label="业务类型">
          <el-select v-model="query.bizType" clearable placeholder="全部类型" style="width: 140px">
            <el-option label="问题" :value="1" />
            <el-option label="回答" :value="2" />
            <el-option label="评论" :value="3" />
            <el-option label="知识库" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="举报状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="待处理" :value="1" />
            <el-option label="已处理" :value="2" />
            <el-option label="不成立" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因类型">
          <el-select v-model="query.reasonType" clearable placeholder="全部类型" style="width: 140px">
            <el-option label="垃圾营销" :value="1" />
            <el-option label="违法违规" :value="2" />
            <el-option label="低质灌水" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="标题/内容/作者" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="rows" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="举报ID" width="90" />
      <el-table-column label="业务类型" width="100">
        <template #default="scope">{{ bizTypeText(scope.row.bizType) }}</template>
      </el-table-column>
      <el-table-column label="举报原因" width="130">
        <template #default="scope">{{ reasonTypeText(scope.row.reasonType) }}</template>
      </el-table-column>
      <el-table-column label="内容摘要" min-width="260" show-overflow-tooltip>
        <template #default="scope">
          <div class="summary">
            <div class="summary-title">{{ scope.row.contentTitle || "-" }}</div>
            <div class="summary-content">{{ scope.row.contentSnippet || "-" }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="作者" width="150" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.authorName || (scope.row.authorId ? `#${scope.row.authorId}` : "-") }}</template>
      </el-table-column>
      <el-table-column label="举报人" width="100">
        <template #default="scope">{{ scope.row.reporterId || "-" }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="reportStatusTagType(scope.row.status)">{{ reportStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理信息" min-width="220" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ handleInfoText(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="176">
        <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="openDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        :current-page="query.page"
        :page-size="query.pageSize"
        :page-sizes="pageSizes"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePage"
      />
    </div>
  </el-card>

  <el-dialog v-model="detailVisible" title="举报详情" width="980px">
    <div v-loading="detailLoading" class="detail-wrap">
      <template v-if="detail">
        <h4>举报单信息</h4>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="业务类型">{{ bizTypeText(detail.report.bizType) }}</el-descriptions-item>
          <el-descriptions-item label="原因类型">{{ reasonTypeText(detail.report.reasonType) }}</el-descriptions-item>
          <el-descriptions-item label="举报人ID">{{ detail.report.reporterId || "-" }}</el-descriptions-item>
          <el-descriptions-item label="举报状态">{{ reportStatusText(detail.report.status) }}</el-descriptions-item>
          <el-descriptions-item label="处理动作">{{ handleActionText(detail.report.handleAction) }}</el-descriptions-item>
          <el-descriptions-item label="处理人ID">{{ detail.report.handlerId || "-" }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatDateTime(detail.report.handledAt) }}</el-descriptions-item>
          <el-descriptions-item label="举报说明" :span="3">{{ detail.report.reasonDetail || "-" }}</el-descriptions-item>
          <el-descriptions-item label="处理结果" :span="3">{{ detail.report.handleResult || "-" }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detail.report.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(detail.report.updatedAt) }}</el-descriptions-item>
        </el-descriptions>

        <h4>被举报内容</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">{{ detail.content.title || "-" }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ contentStatusText(detail.content.status) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detail.content.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="驳回原因" :span="2">{{ detail.content.rejectReason || "-" }}</el-descriptions-item>
        </el-descriptions>
        <div class="content-box">{{ detail.content.content || "-" }}</div>

        <h4>作者信息</h4>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="用户ID">{{ detail.author.id || "-" }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detail.author.username || "-" }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detail.author.nickname || "-" }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">{{ userStatusText(detail.author.status) }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </div>
    <template #footer>
      <template v-if="detail?.report?.status === 1">
        <el-button type="warning" @click="openHandleDialog(detail.report.id, 1)">下架</el-button>
        <el-button type="info" @click="openHandleDialog(detail.report.id, 2)">警告</el-button>
        <el-button type="danger" @click="openHandleDialog(detail.report.id, 3)">封禁/禁言</el-button>
        <el-button @click="openHandleDialog(detail.report.id, 4)">不处理</el-button>
        <el-button type="success" plain @click="toAudit(detail.report.id)">转审核</el-button>
      </template>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="handleDialogVisible" :title="`举报处理 - ${handleActionText(pendingAction)}`" width="560px">
    <div class="handle-brief" :class="`is-${pendingAction}`">
      <div class="handle-brief-title">本次将执行：{{ handleActionText(pendingAction) }}</div>
      <div class="handle-brief-desc">{{ handleActionDesc(pendingAction) }}</div>
    </div>
    <el-form label-width="86px">
      <el-form-item label="处理说明">
        <el-input
          v-model="handleResult"
          type="textarea"
          :rows="5"
          maxlength="300"
          show-word-limit
          :placeholder="handlePlaceholder(pendingAction)"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleDialogVisible = false">取消</el-button>
      <el-button :type="handleActionBtnType(pendingAction)" @click="submitHandle">
        确认执行
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getCmsReportDetail, handleCmsReport, pageCmsReport, transferReportToAudit } from "../../api/report";
import type { CmsReportDetailVO, CmsReportPageItemVO, CmsReportPageQueryDTO } from "../../types/report";

const pageSizes = [10, 20, 50, 100];
const query = reactive<CmsReportPageQueryDTO>({
  page: 1,
  pageSize: 10,
  status: 1,
  bizType: null,
  reasonType: null,
  keyword: ""
});
const dateRange = ref<string[]>([]);
const rows = ref<CmsReportPageItemVO[]>([]);
const total = ref(0);
const loading = ref(false);

const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<CmsReportDetailVO | null>(null);

const handleDialogVisible = ref(false);
const pendingId = ref<number | null>(null);
const pendingAction = ref<1 | 2 | 3 | 4>(1);
const handleResult = ref("");

const bizTypeText = (value?: number) => {
  if (value === 1) return "问题";
  if (value === 2) return "回答";
  if (value === 3) return "评论";
  if (value === 4) return "知识库";
  return value == null ? "-" : String(value);
};

const reportStatusText = (value?: number) => {
  if (value === 1) return "待处理";
  if (value === 2) return "已处理";
  if (value === 3) return "不成立";
  return value == null ? "-" : String(value);
};

const reportStatusTagType = (value?: number) => {
  if (value === 1) return "warning";
  if (value === 2) return "success";
  if (value === 3) return "info";
  return "info";
};

const reasonTypeText = (value?: number) => {
  if (value === 1) return "垃圾营销";
  if (value === 2) return "违法违规";
  if (value === 3) return "低质灌水";
  if (value === 4) return "其他";
  return value == null ? "-" : String(value);
};

const handleActionText = (value?: number) => {
  if (value === 1) return "下架内容";
  if (value === 2) return "警告用户";
  if (value === 3) return "封禁/禁言";
  if (value === 4) return "不处理";
  return value == null ? "-" : String(value);
};

const handleActionDesc = (value?: number) => {
  if (value === 1) return "被举报内容会立即下架，举报单将标记为已处理。";
  if (value === 2) return "向被举报用户发送站内警告通知，举报单将标记为已处理。";
  if (value === 3) return "将被举报用户账号状态置为禁用，举报单将标记为已处理。";
  if (value === 4) return "判定本次举报不成立，举报单状态改为不成立。";
  return "请确认处理动作。";
};

const handlePlaceholder = (value?: number) => {
  if (value === 1) return "可填写下架依据，例如：发布误导性医疗建议。";
  if (value === 2) return "建议填写警告说明，便于后续追踪。";
  if (value === 3) return "建议填写封禁原因，便于审计。";
  if (value === 4) return "建议填写不处理原因，便于复核。";
  return "请输入处理说明。";
};

const handleActionBtnType = (value?: number) => {
  if (value === 1) return "warning";
  if (value === 2) return "info";
  if (value === 3) return "danger";
  return "primary";
};

const contentStatusText = (value?: number) => {
  if (value === 1) return "已通过";
  if (value === 2) return "待审核";
  if (value === 3) return "已驳回";
  if (value === 4) return "已下架";
  return value == null ? "-" : String(value);
};

const userStatusText = (value?: number) => {
  if (value === 1) return "正常";
  if (value === 0) return "禁用";
  return value == null ? "-" : String(value);
};

const handleInfoText = (row: CmsReportPageItemVO) => {
  if (!row.handleAction) return "-";
  const action = handleActionText(row.handleAction);
  const at = formatDateTime(row.handledAt);
  const who = row.handlerId ? `#${row.handlerId}` : "-";
  const result = row.handleResult || "-";
  return `${action} | 处理人:${who} | 时间:${at} | ${result}`;
};

const formatDateTime = (value?: string) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const pad = (num: number) => String(num).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}:${pad(date.getSeconds())}`;
};

const buildQuery = () => {
  const [startTime, endTime] = dateRange.value || [];
  return {
    ...query,
    bizType: query.bizType ?? undefined,
    status: query.status ?? undefined,
    reasonType: query.reasonType ?? undefined,
    keyword: query.keyword?.trim() || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined
  };
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await pageCmsReport(buildQuery());
    rows.value = res.data.list || [];
    total.value = res.data.total || 0;
  } finally {
    loading.value = false;
  }
};

const handlePage = (page: number) => {
  query.page = page;
  loadData();
};

const handleSizeChange = (size: number) => {
  query.pageSize = size;
  query.page = 1;
  loadData();
};

const resetQuery = () => {
  query.bizType = null;
  query.status = 1;
  query.reasonType = null;
  query.keyword = "";
  query.page = 1;
  dateRange.value = [];
  loadData();
};

const fetchDetail = async (id: number) => {
  detailLoading.value = true;
  try {
    const res = await getCmsReportDetail(id);
    detail.value = res.data;
  } finally {
    detailLoading.value = false;
  }
};

const openDetail = async (row: CmsReportPageItemVO) => {
  detailVisible.value = true;
  await fetchDetail(row.id);
};

const openHandleDialog = (reportId: number, action: 1 | 2 | 3 | 4) => {
  pendingId.value = reportId;
  pendingAction.value = action;
  handleResult.value = "";
  handleDialogVisible.value = true;
};

const submitHandle = async () => {
  if (!pendingId.value) return;
  await handleCmsReport(pendingId.value, {
    handleAction: pendingAction.value,
    handleResult: handleResult.value.trim() || undefined
  });
  ElMessage.success("处理成功");
  handleDialogVisible.value = false;
  await loadData();
  if (detailVisible.value && detail.value?.report?.id) {
    await fetchDetail(detail.value.report.id);
  }
};

const toAudit = async (reportId: number) => {
  await ElMessageBox.confirm("确认转入审核队列吗？已存在待审举报触发单时不会重复创建。", "提示", { type: "warning" });
  await transferReportToAudit(reportId);
  ElMessage.success("已转入审核队列");
  await loadData();
  if (detailVisible.value && detail.value?.report?.id) {
    await fetchDetail(detail.value.report.id);
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.summary {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.summary-title {
  font-weight: 600;
  color: var(--app-text);
}

.summary-content {
  color: var(--app-text-muted);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-box {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 10px;
  white-space: pre-wrap;
  max-height: 260px;
  overflow: auto;
}

.handle-brief {
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 12px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-muted);
}

.handle-brief-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.handle-brief-desc {
  color: var(--app-text-muted);
  font-size: 13px;
  line-height: 1.5;
}

.handle-brief.is-1 {
  border-color: #e6a23c66;
  background: #fdf6ec;
}

.handle-brief.is-2 {
  border-color: #90939966;
  background: #f4f4f5;
}

.handle-brief.is-3 {
  border-color: #f56c6c66;
  background: #fef0f0;
}

.handle-brief.is-4 {
  border-color: #79bbff66;
  background: #ecf5ff;
}
</style>
