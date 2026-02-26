<template>
  <el-card>
    <h2>专题管理</h2>
    <p>管理员可以管理专题（增删查改、状态维护、详情查看）。</p>

    <div class="toolbar">
      <el-form :inline="true" :model="query" class="toolbar-form">
        <el-form-item label="专题标题">
          <el-input v-model="query.title" placeholder="专题标题" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 130px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="待审核" :value="2" />
            <el-option label="驳回" :value="3" />
            <el-option label="下架" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序字段">
          <el-select v-model="query.sortBy" clearable placeholder="默认" style="width: 160px">
            <el-option label="关注数" value="followCount" />
            <el-option label="问题数" value="questionCount" />
            <el-option label="今日新增" value="todayNewCount" />
            <el-option label="创建时间" value="createdAt" />
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
      <el-button type="primary" @click="openCreate">新增专题</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="封面" width="90">
        <template #default="scope">
          <el-image
            v-if="scope.row.coverImg"
            :src="resolveMediaUrl(scope.row.coverImg)"
            fit="cover"
            class="cover-thumb"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="subtitle" label="副标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="topicStatusType(scope.row.status)">
            {{ topicStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关注数" width="90">
        <template #default="scope">{{ scope.row.followCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="问题数" width="90">
        <template #default="scope">{{ scope.row.questionCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="今日新增" width="90">
        <template #default="scope">{{ scope.row.todayNewCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="分类数" width="90">
        <template #default="scope">{{ scope.row.categoryCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="190">
        <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="340">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="openDetail(scope.row)">详情</el-button>
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button
            v-if="scope.row.status !== 1"
            size="small"
            type="success"
            @click="changeStatus(scope.row, 1)"
          >
            发布
          </el-button>
          <el-button
            v-else
            size="small"
            type="warning"
            @click="changeStatus(scope.row, 4)"
          >
            下线
          </el-button>
          <el-button size="small" type="danger" @click="removeTopic(scope.row)">删除</el-button>
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

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增专题' : '编辑专题'" width="680px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="专题标题" prop="title">
        <el-input v-model="form.title" maxlength="60" show-word-limit />
      </el-form-item>
      <el-form-item label="副标题">
        <el-input v-model="form.subtitle" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="封面图">
        <div class="cover-upload-wrap">
          <el-upload :show-file-list="false" :before-upload="handleCoverUpload" accept="image/*">
            <el-image v-if="coverPreview" :src="coverPreview" fit="cover" class="cover-preview" />
            <div v-else class="cover-placeholder">上传封面</div>
          </el-upload>
        </div>
      </el-form-item>
      <el-form-item label="专题状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio
            v-for="item in statusEditOptions"
            :key="item.value"
            :label="item.value"
          >
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="关联分类">
        <el-select v-model="form.categoryIds" multiple clearable filterable style="width: 100%" placeholder="选择分类">
          <el-option
            v-for="item in categoryOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="专题简介">
        <el-input v-model="form.intro" type="textarea" :rows="4" maxlength="1000" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" title="专题详情" width="920px">
    <div v-loading="detailLoading" class="detail-wrap">
      <el-descriptions v-if="detail" :column="3" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ topicStatusText(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="关注数">{{ detailStats.followCount ?? detail.followCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="问题数">{{ detailStats.questionCount ?? detail.questionCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="今日新增">{{ detailStats.todayNewCount ?? detail.todayNewCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="副标题" :span="3">{{ detail.subtitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="3">{{ detail.intro || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detail?.coverImg" class="detail-cover">
        <el-image :src="resolveMediaUrl(detail.coverImg)" fit="cover" class="detail-cover-img" />
      </div>

      <h4>关联分类</h4>
      <div class="category-tags">
        <el-tag v-for="item in detailCategories" :key="item.id" type="info">{{ item.name }}</el-tag>
        <span v-if="detailCategories.length === 0">-</span>
      </div>

      <h4>最近问题</h4>
      <el-table :data="detailRecentQuestions" style="width: 100%">
        <el-table-column prop="id" label="问题ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="scope">{{ questionStatusText(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="140" />
        <el-table-column prop="createdAt" label="创建时间" width="190">
          <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
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
import { ElMessage, ElMessageBox, type FormInstance, type UploadProps } from "element-plus";
import { listCategories } from "../../api/category";
import {
  createTopic,
  deleteTopic,
  getTopicDetail,
  getTopicStats,
  getTopicTrend,
  pageTopics,
  updateTopic,
  updateTopicStatus
} from "../../api/topic";
import { uploadAvatar } from "../../api/upload";
import type {
  AdminTopicDetailVO,
  AdminTopicListItemVO,
  QaTopicPageQueryDTO,
  QaTopicSaveDTO,
  TopicCategoryVO,
  TopicRecentQuestionVO,
  TopicStatsVO,
  TopicTrendPointVO
} from "../../types/topic";
import type { QaCategory } from "../../types/category";

type TopicForm = {
  title: string;
  subtitle: string;
  coverImg: string;
  intro: string;
  status: 0 | 1 | 2 | 3 | 4;
  categoryIds: number[];
};

const pageSizes = [10, 20, 50, 100];
const query = reactive<QaTopicPageQueryDTO>({ page: 1, pageSize: 10, status: null, createdBy: null });
const rows = ref<AdminTopicListItemVO[]>([]);
const total = ref(0);
const loading = ref(false);

const categoryOptions = ref<QaCategory[]>([]);

const editVisible = ref(false);
const editMode = ref<"create" | "edit">("create");
const currentId = ref<number | null>(null);
const formRef = ref<FormInstance>();
const form = reactive<TopicForm>({
  title: "",
  subtitle: "",
  coverImg: "",
  intro: "",
  status: 0,
  categoryIds: []
});
const coverLocalPreview = ref("");

const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<AdminTopicDetailVO | null>(null);
const detailStats = ref<TopicStatsVO>({});
const detailCategories = ref<TopicCategoryVO[]>([]);
const detailRecentQuestions = ref<TopicRecentQuestionVO[]>([]);
const detailTrend = ref<TopicTrendPointVO[]>([]);

const chartWidth = 760;
const chartHeight = 220;
const paddingTop = 20;
const paddingRight = 20;
const paddingBottom = 40;
const paddingLeft = 26;

const trendChartData = computed(() => {
  const data = detailTrend.value;
  if (data.length === 0) return [];
  const maxCount = Math.max(...data.map((item) => item.cnt || 0), 1);
  const usableWidth = chartWidth - paddingLeft - paddingRight;
  const usableHeight = chartHeight - paddingTop - paddingBottom;
  const stepX = data.length > 1 ? usableWidth / (data.length - 1) : 0;
  return data.map((item, index) => {
    const count = item.cnt || 0;
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
  const rowsCount = 4;
  const usableHeight = chartHeight - paddingTop - paddingBottom;
  return Array.from({ length: rowsCount + 1 }, (_item, index) => paddingTop + (usableHeight / rowsCount) * index);
});

const rules = {
  title: [{ required: true, message: "请输入专题标题", trigger: "blur" }],
  status: [{ required: true, message: "请选择专题状态", trigger: "change" }]
};
const statusEditOptions = computed(() => {
  if (editMode.value === "create") {
    return [
      { label: "草稿", value: 0 as const },
      { label: "已发布", value: 1 as const }
    ];
  }
  return [
    { label: "已发布", value: 1 as const },
    { label: "下架", value: 4 as const }
  ];
});

const mediaBaseUrl = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/+$/, "");
const resolveMediaUrl = (raw?: string): string => {
  if (!raw) return "";
  const cleanRaw = raw.trim();
  if (/^(https?:)?\/\//i.test(cleanRaw) || cleanRaw.startsWith("data:") || cleanRaw.startsWith("blob:")) {
    return cleanRaw;
  }
  if (!mediaBaseUrl) return cleanRaw;
  const hostBase = mediaBaseUrl.replace(/\/api$/, "");
  const normalizedRaw = cleanRaw.startsWith("/") ? cleanRaw : `/${cleanRaw}`;
  const isStaticAssetPath =
    normalizedRaw.startsWith("/uploads/") ||
    normalizedRaw.startsWith("/upload/") ||
    normalizedRaw.startsWith("/static/") ||
    normalizedRaw.startsWith("/files/");
  if (cleanRaw.startsWith("/")) {
    if (mediaBaseUrl.startsWith("http")) {
      if (mediaBaseUrl.endsWith("/api")) {
        if (cleanRaw.startsWith("/api/")) {
          return encodeURI(`${hostBase}${cleanRaw}`);
        }
        if (isStaticAssetPath) {
          return encodeURI(`${hostBase}${cleanRaw}`);
        }
      }
      return encodeURI(`${mediaBaseUrl}${cleanRaw}`);
    }
    if (cleanRaw === mediaBaseUrl || cleanRaw.startsWith(`${mediaBaseUrl}/`)) {
      return encodeURI(cleanRaw);
    }
    return encodeURI(`${mediaBaseUrl}${cleanRaw}`);
  }
  if (mediaBaseUrl.startsWith("http") && mediaBaseUrl.endsWith("/api") && isStaticAssetPath) {
    return encodeURI(`${hostBase}${normalizedRaw}`);
  }
  return encodeURI(`${mediaBaseUrl}/${cleanRaw}`);
};
const coverPreview = computed(() => coverLocalPreview.value || resolveMediaUrl(form.coverImg));

const topicStatusText = (status?: number) => {
  if (status === 0) return "草稿";
  if (status === 1) return "已发布";
  if (status === 2) return "待审核";
  if (status === 3) return "驳回";
  if (status === 4) return "下架";
  return "-";
};

const topicStatusType = (status?: number) => {
  if (status === 1) return "success";
  if (status === 2) return "warning";
  if (status === 3) return "danger";
  if (status === 4) return "info";
  return "info";
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

const loadCategories = async () => {
  const res = await listCategories({ pageNum: 1, pageSize: 9999 });
  categoryOptions.value = res.data.list;
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await pageTopics({
      ...query,
      title: query.title?.trim() || undefined,
      status: query.status ?? undefined,
      createdBy: query.createdBy ?? undefined,
      sortBy: query.sortBy || undefined,
      sortOrder: query.sortOrder || undefined
    });
    rows.value = res.data.list;
    total.value = res.data.total;
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
  query.title = "";
  query.status = null;
  query.sortBy = "";
  query.sortOrder = "";
  query.page = 1;
  loadData();
};

const resetForm = () => {
  form.title = "";
  form.subtitle = "";
  form.coverImg = "";
  form.intro = "";
  form.status = 0;
  form.categoryIds = [];
  coverLocalPreview.value = "";
};

const openCreate = () => {
  editMode.value = "create";
  currentId.value = null;
  resetForm();
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const openEdit = async (row: AdminTopicListItemVO) => {
  editMode.value = "edit";
  currentId.value = row.id;
  const res = await getTopicDetail(row.id);
  form.title = res.data.title;
  form.subtitle = res.data.subtitle || "";
  form.coverImg = res.data.coverImg || "";
  form.intro = res.data.intro || "";
  form.status = (res.data.status === 4 ? 4 : 1) as 0 | 1 | 2 | 3 | 4;
  form.categoryIds = (res.data.categories || []).map((item) => item.id);
  coverLocalPreview.value = "";
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const toSavePayload = (): QaTopicSaveDTO => ({
  title: form.title.trim(),
  subtitle: form.subtitle.trim() || undefined,
  coverImg: form.coverImg.trim() || undefined,
  intro: form.intro.trim() || undefined,
  status: form.status,
  categoryIds: form.categoryIds
});

const submitEdit = async () => {
  if (formRef.value) {
    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) return;
  }

  const payload = toSavePayload();
  if (editMode.value === "create") {
    await createTopic(payload);
    ElMessage.success("创建成功");
  } else if (currentId.value) {
    await updateTopic(currentId.value, payload);
    ElMessage.success("保存成功");
  }
  editVisible.value = false;
  await loadData();
};

const changeStatus = async (row: AdminTopicListItemVO, status: number) => {
  await updateTopicStatus(row.id, { status });
  ElMessage.success("状态更新成功");
  await loadData();
};

const removeTopic = async (row: AdminTopicListItemVO) => {
  await ElMessageBox.confirm("确认删除该专题吗？", "提示", { type: "warning" });
  await deleteTopic(row.id);
  ElMessage.success("删除成功");
  await loadData();
};

const openDetail = async (row: AdminTopicListItemVO) => {
  detailLoading.value = true;
  detailVisible.value = true;
  try {
    const [detailRes, statsRes, trendRes] = await Promise.all([
      getTopicDetail(row.id),
      getTopicStats(row.id),
      getTopicTrend(row.id, 7)
    ]);
    detail.value = detailRes.data;
    detailStats.value = statsRes.data;
    detailCategories.value = detailRes.data.categories || [];
    detailRecentQuestions.value = detailRes.data.recentQuestions || [];
    detailTrend.value = trendRes.data || [];
  } finally {
    detailLoading.value = false;
  }
};

const handleCoverUpload: UploadProps["beforeUpload"] = async (rawFile) => {
  if (coverLocalPreview.value.startsWith("blob:")) {
    URL.revokeObjectURL(coverLocalPreview.value);
  }
  coverLocalPreview.value = URL.createObjectURL(rawFile);
  const res = await uploadAvatar(rawFile);
  form.coverImg = res.data;
  return false;
};

onMounted(async () => {
  await Promise.all([loadData(), loadCategories()]);
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

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.cover-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  border: 1px solid var(--app-border);
}

.cover-upload-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cover-preview {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  border: 1px solid var(--app-border);
}

.cover-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  border: 1px dashed var(--app-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--app-text-muted);
  cursor: pointer;
}

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-cover {
  display: flex;
}

.detail-cover-img {
  width: 160px;
  height: 90px;
  border-radius: 8px;
  border: 1px solid var(--app-border);
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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
</style>
