<template>
  <el-card>
    <h2>审核治理</h2>
    <p>支持审核队列查询、详情查看、单条审核、批量审核与重新打开审核。</p>

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
        <el-form-item label="审核状态">
          <el-select v-model="query.auditStatus" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="待审核" :value="1" />
            <el-option label="审核通过" :value="2" />
            <el-option label="审核驳回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发来源">
          <el-select v-model="query.triggerSource" clearable placeholder="全部来源" style="width: 140px">
            <el-option label="用户提交" :value="1" />
            <el-option label="模型触发" :value="2" />
            <el-option label="人工转审" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="标题/内容/提交人" @keyup.enter="loadData" />
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
        <el-form-item label="排序字段">
          <el-select v-model="query.sortBy" clearable placeholder="默认" style="width: 140px">
            <el-option label="创建时间" value="createdAt" />
            <el-option label="模型分数" value="modelScore" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序方式">
          <el-select v-model="query.sortOrder" clearable placeholder="默认" style="width: 120px">
            <el-option label="升序" value="asc" />
            <el-option label="降序" value="desc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar-right">
        <el-button :disabled="selectedIds.length === 0" type="success" @click="batchPass">批量通过</el-button>
        <el-button :disabled="selectedIds.length === 0" type="danger" @click="openRejectDialog('batch')">批量驳回</el-button>
      </div>
    </div>

    <el-table :data="rows" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="审核ID" width="90" />
      <el-table-column label="业务类型" width="110">
        <template #default="scope">
          <el-tag type="info">{{ bizTypeText(scope.row.bizType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="内容摘要" min-width="240" show-overflow-tooltip>
        <template #default="scope">
          <div class="summary">
            <div class="summary-title">{{ scope.row.contentTitle || scope.row.title || "-" }}</div>
            <div class="summary-content">
              {{ scope.row.contentSnippet || scope.row.contentSummary || scope.row.content || "-" }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="提交人" width="150" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.submitUserName || scope.row.submitterName || scope.row.submitUserId || scope.row.submitterId || "-" }}
        </template>
      </el-table-column>
      <el-table-column label="风险" width="120">
        <template #default="scope">
          <span>{{ riskText(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="模型分" width="90">
        <template #default="scope">{{ scoreText(scope.row.modelScore) }}</template>
      </el-table-column>
      <el-table-column label="审核状态" width="110">
        <template #default="scope">
          <el-tag :type="auditStatusTagType(scope.row.auditStatus)">
            {{ auditStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="触发来源" width="110">
        <template #default="scope">{{ triggerSourceText(scope.row.triggerSource) }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="176">
        <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="reviewedAt" label="处理时间" width="176">
        <template #default="scope">{{ formatDateTime(scope.row.auditedAt || scope.row.reviewedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="openDetail(scope.row)">详情</el-button>
          <el-button
            v-if="scope.row.auditStatus === 2 || scope.row.auditStatus === 3"
            size="small"
            type="warning"
            @click="reopen(scope.row)"
          >
            重新打开
          </el-button>
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

  <el-dialog v-model="detailVisible" title="审核详情" width="980px">
    <div v-loading="detailLoading" class="detail-wrap">
      <template v-if="detail">
        <el-descriptions :column="3" border>
          <el-descriptions-item v-for="item in auditPairs" :key="item.key" :label="item.key">
            {{ item.value }}
          </el-descriptions-item>
        </el-descriptions>

        <h4>内容信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">{{ detail.content.title || "-" }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ contentStatusText(detail.content.status) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detail.content.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="驳回原因" :span="2">{{ detail.content.rejectReason || "-" }}</el-descriptions-item>
        </el-descriptions>
        <div class="content-box">{{ detail.content.content || "-" }}</div>

        <h4>提交人信息</h4>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="用户ID">{{ detail.author.id || "-" }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detail.author.username || "-" }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detail.author.nickname || "-" }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.author.phone || "-" }}</el-descriptions-item>
          <el-descriptions-item label="邮箱" :span="2">{{ detail.author.email || "-" }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </div>
    <template #footer>
      <el-button
        v-if="detailAuditStatus === 1"
        type="success"
        @click="reviewFromDetail('pass')"
      >
        通过
      </el-button>
      <el-button
        v-if="detailAuditStatus === 1"
        type="danger"
        @click="rejectFromDetail"
      >
        驳回
      </el-button>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="rejectDialogVisible"
    :title="rejectMode === 'batch' ? '批量驳回审核' : '驳回审核'"
    width="520px"
  >
    <el-form label-width="86px">
      <el-form-item label="驳回原因">
        <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          placeholder="请输入驳回原因"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="rejectDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitReject">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  batchReviewCmsAudit,
  getCmsAuditDetail,
  pageCmsAuditQueue,
  reopenCmsAudit,
  reviewCmsAudit
} from "../../api/audit";
import type {
  CmsAuditAction,
  CmsAuditDetailVO,
  CmsAuditPageItemVO,
  CmsAuditPageQueryDTO
} from "../../types/audit";

const pageSizes = [10, 20, 50, 100];
const query = reactive<CmsAuditPageQueryDTO>({
  page: 1,
  pageSize: 10,
  bizType: null,
  auditStatus: 1,
  triggerSource: null,
  keyword: "",
  sortBy: "createdAt",
  sortOrder: "desc"
});
const dateRange = ref<string[]>([]);
const rows = ref<CmsAuditPageItemVO[]>([]);
const loading = ref(false);
const total = ref(0);
const selectedIds = ref<number[]>([]);

const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<CmsAuditDetailVO | null>(null);
const detailAuditId = computed(() => {
  const raw = detail.value?.audit?.id;
  if (raw == null) return null;
  const id = Number(raw);
  return Number.isNaN(id) ? null : id;
});
const detailAuditStatus = computed(() => {
  const raw = detail.value?.audit?.auditStatus ?? detail.value?.audit?.status;
  if (raw == null) return null;
  const status = Number(raw);
  return Number.isNaN(status) ? null : status;
});

const rejectDialogVisible = ref(false);
const rejectMode = ref<"single" | "batch">("single");
const rejectTargetId = ref<number | null>(null);
const rejectReason = ref("");

const auditKeyLabelMap: Record<string, string> = {
  id: "审核ID",
  bizType: "业务类型",
  bizId: "业务ID",
  triggerSource: "触发来源",
  auditType: "审核类型",
  auditStatus: "审核状态",
  action: "处理动作",
  modelLabel: "模型标签",
  modelScore: "模型分数",
  hitDetail: "命中详情",
  rejectReason: "驳回原因",
  submitUserId: "提交人ID",
  submitUserName: "提交人",
  auditorId: "审核人ID",
  auditorName: "审核人",
  auditedAt: "审核时间",
  createdAt: "创建时间",
  updatedAt: "更新时间"
};

const auditPairs = computed(() => {
  if (!detail.value?.audit) return [];
  return Object.entries(detail.value.audit).map(([key, value]) => ({
    key: auditKeyLabelMap[key] || `字段(${key})`,
    value: formatAuditFieldValue(key, value)
  }))
    .filter((item) => item.key !== "审核ID" && item.key !== "业务ID");
});

const bizTypeText = (value?: number) => {
  if (value === 1) return "问题";
  if (value === 2) return "回答";
  if (value === 3) return "评论";
  if (value === 4) return "知识库";
  return value == null ? "-" : String(value);
};

const auditStatusText = (status?: number) => {
  if (status === 1) return "待审核";
  if (status === 2) return "审核通过";
  if (status === 3) return "审核驳回";
  return "-";
};

const auditTypeText = (value?: number) => {
  if (value === 1) return "模型审核";
  if (value === 2) return "人工审核";
  return value == null ? "-" : String(value);
};

const auditStatusTagType = (status?: number) => {
  if (status === 1) return "warning";
  if (status === 2) return "success";
  if (status === 3) return "danger";
  return "info";
};

const triggerSourceText = (value?: number) => {
  if (value === 1) return "用户提交";
  if (value === 2) return "模型触发";
  if (value === 3) return "人工转审";
  return value == null ? "-" : String(value);
};

const contentStatusText = (status?: number) => {
  if (status === 1) return "已通过";
  if (status === 2) return "待审核";
  if (status === 3) return "已驳回";
  return status == null ? "-" : String(status);
};

const scoreText = (score?: number) => (typeof score === "number" ? String(score) : "-");

const riskText = (row: CmsAuditPageItemVO) => {
  if (row.riskLabel) return row.riskLabel;
  if (row.riskLevel) return row.riskLevel;
  if (row.modelLabel) return row.modelLabel;
  return "-";
};

const formatAny = (value: unknown): string => {
  if (value == null) return "-";
  if (Array.isArray(value)) return value.map((item) => formatAny(item)).join(", ");
  if (typeof value === "object") return JSON.stringify(value);
  return String(value);
};

const formatAuditFieldValue = (key: string, value: unknown): string => {
  if (value == null || value === "") return "-";
  if (key === "bizType") return bizTypeText(Number(value));
  if (key === "triggerSource") return triggerSourceText(Number(value));
  if (key === "auditType") return auditTypeText(Number(value));
  if (key === "auditStatus") return auditStatusText(Number(value));
  if (key === "action") {
    if (value === "pass") return "通过";
    if (value === "reject") return "驳回";
  }
  if (key === "createdAt" || key === "updatedAt" || key === "auditedAt") return formatDateTime(String(value));
  return formatAny(value);
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
    auditStatus: query.auditStatus ?? undefined,
    triggerSource: query.triggerSource ?? undefined,
    keyword: query.keyword?.trim() || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
    sortBy: query.sortBy || undefined,
    sortOrder: query.sortOrder || undefined
  };
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await pageCmsAuditQueue(buildQuery());
    const pageData = res.data;
    rows.value = pageData?.list || [];
    total.value = pageData?.total || 0;
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  query.bizType = null;
  query.auditStatus = 1;
  query.triggerSource = null;
  query.keyword = "";
  query.sortBy = "createdAt";
  query.sortOrder = "desc";
  query.page = 1;
  dateRange.value = [];
  loadData();
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

const handleSelectionChange = (selected: CmsAuditPageItemVO[]) => {
  selectedIds.value = selected.map((item) => item.id);
};

const openDetail = async (row: CmsAuditPageItemVO) => {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getCmsAuditDetail(row.id);
    const detailData = res.data;
    detail.value = detailData || null;
  } finally {
    detailLoading.value = false;
  }
};

const reviewFromDetail = async (action: CmsAuditAction) => {
  if (!detailAuditId.value) return;
  if (detailAuditStatus.value !== 1) {
    ElMessage.warning("仅待审核单可处理");
    return;
  }
  await reviewCmsAudit(detailAuditId.value, { action });
  ElMessage.success(action === "pass" ? "已通过" : "已驳回");
  detailVisible.value = false;
  await loadData();
};

const rejectFromDetail = () => {
  if (!detailAuditId.value) return;
  detailVisible.value = false;
  openRejectDialog("single", detailAuditId.value);
};

const batchPass = async () => {
  if (selectedIds.value.length === 0) return;
  await ElMessageBox.confirm(`确认批量通过选中的 ${selectedIds.value.length} 条审核单吗？`, "提示", { type: "warning" });
  await batchReviewCmsAudit({ ids: selectedIds.value, action: "pass" });
  ElMessage.success("批量通过成功");
  await loadData();
};

const openRejectDialog = (mode: "single" | "batch", id?: number) => {
  rejectMode.value = mode;
  rejectTargetId.value = id ?? null;
  rejectReason.value = "";
  rejectDialogVisible.value = true;
};

const submitReject = async () => {
  const reason = rejectReason.value.trim();
  if (!reason) {
    ElMessage.warning("请输入驳回原因");
    return;
  }
  if (rejectMode.value === "single") {
    if (!rejectTargetId.value) return;
    await reviewCmsAudit(rejectTargetId.value, { action: "reject", rejectReason: reason });
    ElMessage.success("驳回成功");
  } else {
    if (selectedIds.value.length === 0) return;
    await batchReviewCmsAudit({ ids: selectedIds.value, action: "reject", rejectReason: reason });
    ElMessage.success("批量驳回成功");
  }
  rejectDialogVisible.value = false;
  await loadData();
};

const reopen = async (row: CmsAuditPageItemVO) => {
  await ElMessageBox.confirm("确认将该审核单重新打开为待审核吗？", "提示", { type: "warning" });
  await reopenCmsAudit(row.id);
  ElMessage.success("已重新打开");
  await loadData();
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

.toolbar-right {
  display: flex;
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
</style>
