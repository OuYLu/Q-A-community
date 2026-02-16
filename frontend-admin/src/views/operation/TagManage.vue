<template>
  <el-card>
    <h2>标签管理</h2>
    <p>管理员可以管理标签（增删查改）。</p>

    <div class="toolbar">
      <el-form :inline="true" :model="query" class="toolbar-form">
        <el-form-item label="标签名称">
          <el-input v-model="query.name" placeholder="标签名称" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="query.source" clearable placeholder="全部来源" style="width: 140px">
            <el-option label="系统" :value="1" />
            <el-option label="用户" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 130px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用次数">
          <el-select v-model="useCountRange" clearable placeholder="全部范围" style="width: 150px">
            <el-option label="0" value="0" />
            <el-option label="1-10" value="1-10" />
            <el-option label="10-100" value="10-100" />
            <el-option label="100+" value="100+" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar-actions">
        <el-button type="success" plain :disabled="selectedIds.length === 0" @click="handleBatchEnable">批量启用</el-button>
        <el-button type="warning" plain :disabled="selectedIds.length === 0" @click="handleBatchDisable">批量禁用</el-button>
        <el-button type="primary" @click="openCreate">新增标签</el-button>
      </div>
    </div>

    <el-table :data="rows" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="标签名称" min-width="180" />
      <el-table-column label="来源" width="100">
        <template #default="scope">
          {{ sourceText(scope.row.source) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="使用次数" width="100">
        <template #default="scope">
          {{ scope.row.useCount ?? 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="190">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="290">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="openDetail(scope.row)">详情</el-button>
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button
            v-if="scope.row.status === 1"
            size="small"
            type="warning"
            @click="toggleTagStatus(scope.row, 0)"
          >
            禁用
          </el-button>
          <el-button
            v-else
            size="small"
            type="success"
            @click="toggleTagStatus(scope.row, 1)"
          >
            启用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        :page-sizes="pageSizes"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePage"
      />
    </div>
  </el-card>

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增标签' : '编辑标签'" width="460px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="form.name" maxlength="30" show-word-limit />
      </el-form-item>
      <el-form-item label="来源" prop="source">
        <el-radio-group v-model="form.source">
          <el-radio :label="1">系统</el-radio>
          <el-radio :label="2">用户</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" title="标签详情" width="860px">
    <div v-loading="detailLoading" class="detail-wrap">
      <el-descriptions v-if="detailTag" :column="3" border>
        <el-descriptions-item label="标签ID">{{ detailTag.id }}</el-descriptions-item>
        <el-descriptions-item label="标签名称">{{ detailTag.name }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ sourceText(detailTag.source) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailTag.status === 1 ? "启用" : "停用" }}</el-descriptions-item>
        <el-descriptions-item label="使用次数">{{ detailTag.useCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detailTag.createdAt) }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-actions">
        <el-button type="primary" @click="goQuestionManage">去问题管理</el-button>
      </div>

      <h4>最近使用该标签的问题（10条）</h4>
      <el-table :data="detailRecentQuestions" style="width: 100%">
        <el-table-column prop="id" label="问题ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="320" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            {{ questionStatusText(scope.row.status) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="190">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <h4>近7天趋势</h4>
      <div class="trend-chart">
        <div v-if="trendChartData.length === 0" class="trend-empty">暂无趋势数据</div>
        <svg v-else :viewBox="`0 0 ${chartWidth} ${chartHeight}`" class="trend-svg" preserveAspectRatio="none">
          <line
            v-for="(lineY, idx) in gridLines"
            :key="`grid-${idx}`"
            :x1="paddingLeft"
            :x2="chartWidth - paddingRight"
            :y1="lineY"
            :y2="lineY"
            class="trend-grid"
          />
          <path :d="trendPath" class="trend-line" />
          <circle
            v-for="point in trendChartData"
            :key="`pt-${point.date}`"
            :cx="point.x"
            :cy="point.y"
            r="3"
            class="trend-dot"
          />
        </svg>
        <div
          v-if="trendChartData.length > 0"
          class="trend-labels"
          :style="{ gridTemplateColumns: `repeat(${trendChartData.length}, minmax(0, 1fr))` }"
        >
          <div v-for="point in trendChartData" :key="`label-${point.date}`" class="trend-label">
            <div class="trend-date">{{ point.date }}</div>
            <div class="trend-count">{{ point.count }}</div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import {
  batchDisableTags,
  batchEnableTags,
  createTag,
  getTagDetail,
  getTagDetailExtra,
  getTagUsageTrend,
  listTags,
  updateTag
} from "../../api/tag";
import type { QaTag, QaTagQueryDTO, QaTagSaveDTO, TagDetailExtra, TagRecentQuestion, TagUsageTrendPoint } from "../../types/tag";

type TagForm = {
  name: string;
  source: 1 | 2;
  status: 0 | 1;
};

const query = reactive<QaTagQueryDTO>({ pageNum: 1, pageSize: 10, source: null, status: null });
const pageSizes = [10, 20, 50, 100];
const useCountRange = ref<string | null>(null);
const rows = ref<QaTag[]>([]);
const selectedIds = ref<number[]>([]);
const total = ref(0);
const loading = ref(false);

const editVisible = ref(false);
const detailVisible = ref(false);
const detailLoading = ref(false);
const editMode = ref<"create" | "edit">("create");
const currentId = ref<number | null>(null);
const detailTag = ref<QaTag | null>(null);
const detailExtra = ref<TagDetailExtra | null>(null);
const detailRecentQuestions = ref<TagRecentQuestion[]>([]);
const detailUsageTrend = ref<TagUsageTrendPoint[]>([]);
const chartWidth = 760;
const chartHeight = 220;
const paddingTop = 20;
const paddingRight = 20;
const paddingBottom = 40;
const paddingLeft = 26;

const trendChartData = computed(() => {
  const data = detailUsageTrend.value;
  if (data.length === 0) return [];
  const maxCount = Math.max(...data.map((item) => item.count ?? 0), 1);
  const usableWidth = chartWidth - paddingLeft - paddingRight;
  const usableHeight = chartHeight - paddingTop - paddingBottom;
  const stepX = data.length > 1 ? usableWidth / (data.length - 1) : 0;
  return data.map((item, index) => {
    const count = item.count ?? 0;
    const x = paddingLeft + stepX * index;
    const y = paddingTop + usableHeight - (count / maxCount) * usableHeight;
    return { date: item.date || "-", count, x, y };
  });
});

const trendPath = computed(() => {
  if (trendChartData.value.length === 0) return "";
  return trendChartData.value
    .map((point, index) => `${index === 0 ? "M" : "L"} ${point.x} ${point.y}`)
    .join(" ");
});

const gridLines = computed(() => {
  const rows = 4;
  const usableHeight = chartHeight - paddingTop - paddingBottom;
  return Array.from({ length: rows + 1 }, (_item, index) => paddingTop + (usableHeight / rows) * index);
});

const formRef = ref<FormInstance>();
const form = reactive<TagForm>({
  name: "",
  source: 1,
  status: 1
});

const rules = {
  name: [{ required: true, message: "请输入标签名称", trigger: "blur" }],
  source: [{ required: true, message: "请选择来源", trigger: "change" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }]
};

const normalizeUsageTrend = (rows: TagUsageTrendPoint[]) => {
  return rows.map((item) => ({
    date: item.date || item.statDate || "-",
    count: item.count ?? item.refCount ?? 0
  }));
};

const sourceText = (source?: number) => {
  if (source === 1) return "系统";
  if (source === 2) return "用户";
  return "-";
};

const questionStatusText = (status?: number) => {
  if (status === 0) return "草稿";
  if (status === 1) return "待审核";
  if (status === 2) return "已发布";
  if (status === 3) return "已下线";
  return status == null ? "-" : String(status);
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

const resolveUseCountQuery = () => {
  if (useCountRange.value === "0") {
    return { useCountMin: 0, useCountMax: 0 };
  }
  if (useCountRange.value === "1-10") {
    return { useCountMin: 1, useCountMax: 10 };
  }
  if (useCountRange.value === "10-100") {
    return { useCountMin: 10, useCountMax: 100 };
  }
  if (useCountRange.value === "100+") {
    return { useCountMin: 100, useCountMax: null };
  }
  return { useCountMin: null, useCountMax: null };
};

const loadData = async () => {
  loading.value = true;
  try {
    const useCountQuery = resolveUseCountQuery();
    const res = await listTags({
      ...query,
      name: query.name?.trim() || undefined,
      source: query.source ?? undefined,
      status: query.status ?? undefined,
      useCountMin: useCountQuery.useCountMin ?? undefined,
      useCountMax: useCountQuery.useCountMax ?? undefined
    });
    rows.value = res.data.list;
    total.value = res.data.total;
    selectedIds.value = [];
  } finally {
    loading.value = false;
  }
};

const handlePage = (page: number) => {
  query.pageNum = page;
  loadData();
};

const handleSizeChange = (size: number) => {
  query.pageSize = size;
  query.pageNum = 1;
  loadData();
};

const resetQuery = () => {
  query.name = "";
  query.source = null;
  query.status = null;
  query.useCountMin = null;
  query.useCountMax = null;
  useCountRange.value = null;
  query.pageNum = 1;
  loadData();
};

const resetForm = () => {
  form.name = "";
  form.source = 1;
  form.status = 1;
};

const openCreate = () => {
  editMode.value = "create";
  currentId.value = null;
  resetForm();
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const openEdit = async (row: QaTag) => {
  editMode.value = "edit";
  currentId.value = row.id;
  const res = await getTagDetail(row.id);
  form.name = res.data.name;
  form.source = res.data.source === 2 ? 2 : 1;
  form.status = res.data.status === 0 ? 0 : 1;
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const openDetail = async (row: QaTag) => {
  detailLoading.value = true;
  detailVisible.value = true;
  try {
    const [tagRes, extraRes, trendRes] = await Promise.all([
      getTagDetail(row.id),
      getTagDetailExtra(row.id),
      getTagUsageTrend(row.id, 7)
    ]);
    detailTag.value = tagRes.data;
    detailExtra.value = extraRes.data;
    detailRecentQuestions.value = extraRes.data.recentQuestions || [];
    detailUsageTrend.value = normalizeUsageTrend(trendRes.data || []);
  } finally {
    detailLoading.value = false;
  }
};

const buildQuestionManageUrl = () => {
  if (!detailTag.value || !detailExtra.value) return "";
  const extra = detailExtra.value;
  const base = extra.questionManageUrl || extra.questionManagePath || "";
  if (!base) return "";
  const isAbs = /^https?:\/\//i.test(base);
  try {
    const url = new URL(base, window.location.origin);
    if (extra.questionManageQueryKey) {
      url.searchParams.set(extra.questionManageQueryKey, extra.questionManageQueryValue || String(detailTag.value.id));
    } else {
      url.searchParams.set("tagId", String(detailTag.value.id));
    }
    return isAbs ? url.toString() : `${url.pathname}${url.search}${url.hash}`;
  } catch {
    return base;
  }
};

const goQuestionManage = () => {
  const url = buildQuestionManageUrl();
  if (!url) {
    ElMessage.warning("未返回问题管理跳转地址");
    return;
  }
  window.open(url, "_blank");
};

const submitEdit = async () => {
  if (formRef.value) {
    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) return;
  }

  const payload: QaTagSaveDTO = {
    name: form.name.trim(),
    source: form.source,
    status: form.status
  };

  if (editMode.value === "create") {
    await createTag(payload);
    ElMessage.success("创建成功");
  } else if (currentId.value) {
    await updateTag(currentId.value, payload);
    ElMessage.success("保存成功");
  }

  editVisible.value = false;
  await loadData();
};

const toggleTagStatus = async (row: QaTag, nextStatus: 0 | 1) => {
  const actionText = nextStatus === 1 ? "启用" : "禁用";
  await ElMessageBox.confirm(`确认${actionText}该标签吗？`, "提示", { type: "warning" });
  await updateTag(row.id, { name: row.name, source: row.source === 2 ? 2 : 1, status: nextStatus });
  ElMessage.success(`${actionText}成功`);
  await loadData();
};

const handleSelectionChange = (selected: QaTag[]) => {
  selectedIds.value = selected.map((item) => item.id);
};

const handleBatchEnable = async () => {
  if (selectedIds.value.length === 0) return;
  await ElMessageBox.confirm(`确认批量启用选中的 ${selectedIds.value.length} 个标签吗？`, "提示", { type: "warning" });
  await batchEnableTags(selectedIds.value);
  ElMessage.success("批量启用成功");
  await loadData();
};

const handleBatchDisable = async () => {
  if (selectedIds.value.length === 0) return;
  await ElMessageBox.confirm(`确认批量禁用选中的 ${selectedIds.value.length} 个标签吗？`, "提示", { type: "warning" });
  await batchDisableTags(selectedIds.value);
  ElMessage.success("批量禁用成功");
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

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
}

.trend-chart {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 10px 10px 8px;
}

.trend-empty {
  color: var(--app-text-muted);
  text-align: center;
  padding: 28px 0;
}

.trend-svg {
  width: 100%;
  height: 220px;
  display: block;
}

.trend-grid {
  stroke: var(--app-border);
  stroke-width: 1;
}

.trend-line {
  fill: none;
  stroke: #409eff;
  stroke-width: 2;
}

.trend-dot {
  fill: #409eff;
}

.trend-labels {
  display: grid;
  gap: 6px;
  margin-top: 4px;
}

.trend-label {
  text-align: center;
  font-size: 12px;
  color: var(--app-text-muted);
}

.trend-date {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.trend-count {
  color: #303133;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
