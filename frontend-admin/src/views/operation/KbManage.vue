<template>
  <el-card>
    <h2>知识库管理</h2>
    <p>管理员可以管理知识库分类与条目。</p>

    <el-tabs v-model="activeTab" class="kb-tabs">
      <el-tab-pane label="分类管理" name="category">
        <div class="toolbar">
          <div class="toolbar-left"></div>
          <el-button type="primary" @click="openCategoryCreate">新增分类</el-button>
        </div>

        <el-table
          :data="categoryTree"
          v-loading="categoryLoading"
          style="width: 100%"
          row-key="id"
          default-expand-all
          :tree-props="{ children: 'children' }"
        >
          <el-table-column prop="name" label="分类名称" min-width="220" />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip />
          <el-table-column label="操作" width="260">
            <template #default="scope">
              <el-button size="small" @click="openCategoryEdit(scope.row)">编辑</el-button>
              <el-button
                v-if="scope.row.status === 1"
                size="small"
                type="warning"
                @click="toggleCategoryStatus(scope.row, 0)"
              >
                禁用
              </el-button>
              <el-button
                v-else
                size="small"
                type="success"
                @click="toggleCategoryStatus(scope.row, 1)"
              >
                启用
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="条目管理" name="entry">
        <div class="toolbar">
          <el-form :inline="true" :model="entryQuery" class="toolbar-form">
            <el-form-item label="关键词">
              <el-input v-model="entryQuery.keyword" placeholder="标题/摘要关键词" clearable @keyup.enter="loadEntryData" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="entryQuery.status" clearable placeholder="全部状态" style="width: 130px">
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="entryQuery.categoryId" clearable placeholder="全部分类" style="width: 180px" filterable>
                <el-option v-for="item in flatCategories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签">
              <el-select v-model="entryQuery.tagId" clearable placeholder="全部标签" style="width: 160px" filterable>
                <el-option v-for="item in tagOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="entryDateRange"
                type="datetimerange"
                value-format="YYYY-MM-DDTHH:mm:ss"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
              />
            </el-form-item>
            <el-form-item label="排序字段">
              <el-select v-model="entryQuery.sortBy" clearable placeholder="默认" style="width: 140px">
                <el-option label="创建时间" value="createdAt" />
                <el-option label="更新时间" value="updatedAt" />
                <el-option label="浏览数" value="viewCount" />
                <el-option label="点赞数" value="likeCount" />
                <el-option label="收藏数" value="favoriteCount" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-select v-model="entryQuery.sortOrder" clearable placeholder="默认" style="width: 120px">
                <el-option label="升序" value="asc" />
                <el-option label="降序" value="desc" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadEntryData">查询</el-button>
              <el-button @click="resetEntryQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-button type="primary" @click="openEntryCreate">新增条目</el-button>
        </div>

        <el-table :data="entryRows" v-loading="entryLoading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="160" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '上架' : scope.row.status === 4 ? '下架' : `状态${scope.row.status}` }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="浏览" width="80">
            <template #default="scope">{{ scope.row.viewCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="点赞" width="80">
            <template #default="scope">{{ scope.row.likeCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="收藏" width="80">
            <template #default="scope">{{ scope.row.favoriteCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="190">
            <template #default="scope">{{ formatDateTime(scope.row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="scope">
              <el-button size="small" type="primary" plain @click="openEntryDetail(scope.row)">详情</el-button>
              <el-button size="small" @click="openEntryEdit(scope.row)">编辑</el-button>
              <el-button
                v-if="scope.row.status !== 1"
                size="small"
                type="success"
                @click="changeEntryStatus(scope.row, 1)"
              >
                上架
              </el-button>
              <el-button
                v-else
                size="small"
                type="warning"
                @click="changeEntryStatus(scope.row, 4)"
              >
                下架
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            :current-page="entryQuery.page"
            :page-size="entryQuery.pageSize"
            :page-sizes="pageSizes"
            :total="entryTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="handleEntrySizeChange"
            @current-change="handleEntryPage"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-dialog v-model="categoryEditVisible" :title="categoryEditMode === 'create' ? '新增分类' : '编辑分类'" width="560px">
    <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="90px">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="categoryForm.name" maxlength="30" show-word-limit />
      </el-form-item>
      <el-form-item label="父分类">
        <el-tree-select
          v-model="categoryForm.parentId"
          clearable
          check-strictly
          node-key="id"
          :data="categoryTree"
          :props="categoryTreeProps"
          placeholder="不选则为一级分类"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="categoryForm.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="categoryForm.sort" :min="0" :max="9999" controls-position="right" />
      </el-form-item>
      <el-form-item label="图标">
        <div class="icon-upload-block">
          <div class="icon-upload-main">
            <el-upload :show-file-list="false" :before-upload="handleCategoryIconUpload" accept="image/*">
              <el-image v-if="categoryIconPreview" :src="categoryIconPreview" fit="cover" class="icon-preview" />
              <div v-else class="icon-placeholder">上传图标</div>
            </el-upload>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="categoryForm.description" type="textarea" :rows="3" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="categoryEditVisible = false">取消</el-button>
      <el-button type="primary" @click="submitCategoryEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="entryEditVisible" :title="entryEditMode === 'create' ? '新增条目' : '编辑条目'" width="760px">
    <el-form ref="entryFormRef" :model="entryForm" :rules="entryRules" label-width="90px">
      <el-form-item label="所属分类" prop="categoryId">
        <el-select v-model="entryForm.categoryId" style="width: 100%" filterable>
          <el-option v-for="item in flatCategories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="entryForm.title" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="entryForm.summary" maxlength="300" show-word-limit />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="entryForm.content" type="textarea" :rows="8" />
      </el-form-item>
      <el-form-item label="来源">
        <el-input v-model="entryForm.source" maxlength="120" show-word-limit />
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="entryForm.tagIds" multiple filterable clearable style="width: 100%">
          <el-option v-for="item in tagOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="entryEditVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEntryEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="entryDetailVisible" title="条目详情" width="900px">
    <div v-if="entryDetail" class="detail-wrap">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="ID">{{ entryDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ resolveCategoryName(entryDetail.categoryId) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ entryDetail.status === 1 ? '上架' : entryDetail.status === 4 ? '下架' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="3">{{ entryDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="摘要" :span="3">{{ entryDetail.summary || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源" :span="3">{{ entryDetail.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="浏览数">{{ entryDetail.viewCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="点赞数">{{ entryDetail.likeCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="收藏数">{{ entryDetail.favoriteCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(entryDetail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(entryDetail.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <h4>标签</h4>
      <div class="tag-wrap">
        <el-tag v-for="tag in entryDetail.tags || []" :key="tag.id" type="info">{{ tag.name }}</el-tag>
        <span v-if="!entryDetail.tags || entryDetail.tags.length === 0">-</span>
      </div>

      <h4>正文</h4>
      <div class="content-box">{{ entryDetail.content }}</div>
    </div>
    <template #footer>
      <el-button @click="entryDetailVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type UploadProps } from "element-plus";
import {
  createKbCategory,
  createKbEntry,
  getKbCategoryTree,
  getKbEntryDetail,
  pageKbEntries,
  updateKbCategory,
  updateKbCategoryStatus,
  updateKbEntry,
  updateKbEntryStatus
} from "../../api/kb";
import { listTags } from "../../api/tag";
import { uploadAvatar } from "../../api/upload";
import type {
  KbCategorySaveDTO,
  KbCategoryTreeVO,
  KbEntryDetailVO,
  KbEntryPageItemVO,
  KbEntryPageQueryDTO,
  KbEntrySaveDTO
} from "../../types/kb";
import type { QaTag } from "../../types/tag";

type CategoryForm = {
  name: string;
  parentId?: number;
  description: string;
  icon: string;
  sort: number;
  status: 0 | 1;
};

type EntryForm = {
  categoryId?: number;
  title: string;
  summary: string;
  content: string;
  source: string;
  tagIds: number[];
};

const activeTab = ref("category");
const pageSizes = [10, 20, 50, 100];

const categoryTree = ref<KbCategoryTreeVO[]>([]);
const categoryLoading = ref(false);
const categoryEditVisible = ref(false);
const categoryEditMode = ref<"create" | "edit">("create");
const currentCategoryId = ref<number | null>(null);
const categoryFormRef = ref<FormInstance>();
const categoryIconLocalPreview = ref("");
const categoryForm = reactive<CategoryForm>({
  name: "",
  parentId: undefined,
  description: "",
  icon: "",
  sort: 0,
  status: 1
});

const categoryRules = {
  name: [{ required: true, message: "请输入分类名称", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
  sort: [{ required: true, message: "请输入排序", trigger: "blur" }]
};

const categoryTreeProps = {
  label: "name",
  value: "id",
  children: "children"
} as const;

const entryQuery = reactive<KbEntryPageQueryDTO>({ page: 1, pageSize: 10, status: null, categoryId: null, tagId: null });
const entryDateRange = ref<string[]>([]);
const entryRows = ref<KbEntryPageItemVO[]>([]);
const entryTotal = ref(0);
const entryLoading = ref(false);

const entryEditVisible = ref(false);
const entryEditMode = ref<"create" | "edit">("create");
const currentEntryId = ref<number | null>(null);
const entryFormRef = ref<FormInstance>();
const entryForm = reactive<EntryForm>({
  categoryId: undefined,
  title: "",
  summary: "",
  content: "",
  source: "",
  tagIds: []
});

const entryRules = {
  categoryId: [{ required: true, message: "请选择分类", trigger: "change" }],
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入内容", trigger: "blur" }]
};

const entryDetailVisible = ref(false);
const entryDetail = ref<KbEntryDetailVO | null>(null);

const tagOptions = ref<QaTag[]>([]);

const mediaBaseUrl = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/+$/, "");
const resolveMediaUrl = (raw?: string): string => {
  if (!raw) return "";
  if (/^(https?:)?\/\//i.test(raw) || raw.startsWith("data:") || raw.startsWith("blob:")) {
    return raw;
  }
  if (!mediaBaseUrl) return raw;
  if (raw.startsWith("/")) {
    if (mediaBaseUrl.startsWith("http")) {
      if (mediaBaseUrl.endsWith("/api") && raw.startsWith("/api/")) {
        return `${mediaBaseUrl.replace(/\/api$/, "")}${raw}`;
      }
      return `${mediaBaseUrl}${raw}`;
    }
    return `${mediaBaseUrl}${raw}`;
  }
  return `${mediaBaseUrl}/${raw}`;
};

const categoryIconPreview = computed(() => categoryIconLocalPreview.value || resolveMediaUrl(categoryForm.icon));

const flatCategories = computed(() => {
  const result: KbCategoryTreeVO[] = [];
  const walk = (nodes: KbCategoryTreeVO[]) => {
    nodes.forEach((node) => {
      result.push(node);
      if (node.children && node.children.length > 0) {
        walk(node.children);
      }
    });
  };
  walk(categoryTree.value);
  return result;
});

const formatDateTime = (value?: string) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const pad = (num: number) => String(num).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}:${pad(date.getSeconds())}`;
};

const loadCategoryTree = async () => {
  categoryLoading.value = true;
  try {
    const res = await getKbCategoryTree();
    categoryTree.value = res.data;
  } finally {
    categoryLoading.value = false;
  }
};

const loadTagOptions = async () => {
  const res = await listTags({ pageNum: 1, pageSize: 999 });
  tagOptions.value = res.data.list;
};

const buildEntryQuery = () => {
  const [startTime, endTime] = entryDateRange.value || [];
  return {
    ...entryQuery,
    keyword: entryQuery.keyword?.trim() || undefined,
    status: entryQuery.status ?? undefined,
    categoryId: entryQuery.categoryId ?? undefined,
    tagId: entryQuery.tagId ?? undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
    sortBy: entryQuery.sortBy || undefined,
    sortOrder: entryQuery.sortOrder || undefined
  };
};

const loadEntryData = async () => {
  entryLoading.value = true;
  try {
    const res = await pageKbEntries(buildEntryQuery());
    entryRows.value = res.data.list;
    entryTotal.value = res.data.total;
  } finally {
    entryLoading.value = false;
  }
};

const handleEntryPage = (page: number) => {
  entryQuery.page = page;
  loadEntryData();
};

const handleEntrySizeChange = (size: number) => {
  entryQuery.pageSize = size;
  entryQuery.page = 1;
  loadEntryData();
};

const resetEntryQuery = () => {
  entryQuery.keyword = "";
  entryQuery.status = null;
  entryQuery.categoryId = null;
  entryQuery.tagId = null;
  entryQuery.sortBy = "";
  entryQuery.sortOrder = "";
  entryQuery.page = 1;
  entryDateRange.value = [];
  loadEntryData();
};

const resetCategoryForm = () => {
  categoryForm.name = "";
  categoryForm.parentId = undefined;
  categoryForm.description = "";
  categoryForm.icon = "";
  categoryForm.sort = 0;
  categoryForm.status = 1;
  categoryIconLocalPreview.value = "";
};

const openCategoryCreate = () => {
  categoryEditMode.value = "create";
  currentCategoryId.value = null;
  resetCategoryForm();
  categoryFormRef.value?.clearValidate();
  categoryEditVisible.value = true;
};

const openCategoryEdit = (row: KbCategoryTreeVO) => {
  categoryEditMode.value = "edit";
  currentCategoryId.value = row.id;
  categoryForm.name = row.name;
  categoryForm.parentId = row.parentId ?? undefined;
  categoryForm.description = row.description || "";
  categoryForm.icon = row.icon || "";
  categoryForm.sort = row.sort || 0;
  categoryForm.status = row.status === 0 ? 0 : 1;
  categoryIconLocalPreview.value = "";
  categoryFormRef.value?.clearValidate();
  categoryEditVisible.value = true;
};

const submitCategoryEdit = async () => {
  if (categoryFormRef.value) {
    const valid = await categoryFormRef.value.validate().catch(() => false);
    if (!valid) return;
  }

  const payload: KbCategorySaveDTO = {
    parentId: categoryForm.parentId ?? null,
    name: categoryForm.name.trim(),
    description: categoryForm.description.trim() || undefined,
    icon: categoryForm.icon.trim() || undefined,
    sort: categoryForm.sort,
    status: categoryForm.status
  };

  if (categoryEditMode.value === "create") {
    await createKbCategory(payload);
    ElMessage.success("创建成功");
  } else if (currentCategoryId.value) {
    await updateKbCategory(currentCategoryId.value, payload);
    ElMessage.success("保存成功");
  }
  categoryEditVisible.value = false;
  await loadCategoryTree();
};

const toggleCategoryStatus = async (row: KbCategoryTreeVO, status: 0 | 1) => {
  await updateKbCategoryStatus(row.id, { status });
  ElMessage.success(status === 1 ? "已启用" : "已禁用");
  await loadCategoryTree();
};

const handleCategoryIconUpload: UploadProps["beforeUpload"] = async (rawFile) => {
  if (categoryIconLocalPreview.value.startsWith("blob:")) {
    URL.revokeObjectURL(categoryIconLocalPreview.value);
  }
  categoryIconLocalPreview.value = URL.createObjectURL(rawFile);
  const res = await uploadAvatar(rawFile);
  categoryForm.icon = res.data;
  return false;
};

const resetEntryForm = () => {
  entryForm.categoryId = undefined;
  entryForm.title = "";
  entryForm.summary = "";
  entryForm.content = "";
  entryForm.source = "";
  entryForm.tagIds = [];
};

const openEntryCreate = () => {
  entryEditMode.value = "create";
  currentEntryId.value = null;
  resetEntryForm();
  entryFormRef.value?.clearValidate();
  entryEditVisible.value = true;
};

const openEntryEdit = async (row: KbEntryPageItemVO) => {
  entryEditMode.value = "edit";
  currentEntryId.value = row.id;
  const res = await getKbEntryDetail(row.id);
  entryForm.categoryId = res.data.categoryId;
  entryForm.title = res.data.title;
  entryForm.summary = res.data.summary || "";
  entryForm.content = res.data.content;
  entryForm.source = res.data.source || "";
  entryForm.tagIds = (res.data.tags || []).map((tag) => tag.id);
  entryFormRef.value?.clearValidate();
  entryEditVisible.value = true;
};

const submitEntryEdit = async () => {
  if (entryFormRef.value) {
    const valid = await entryFormRef.value.validate().catch(() => false);
    if (!valid) return;
  }
  const payload: KbEntrySaveDTO = {
    categoryId: Number(entryForm.categoryId),
    title: entryForm.title.trim(),
    summary: entryForm.summary.trim() || undefined,
    content: entryForm.content,
    source: entryForm.source.trim() || undefined,
    tagIds: entryForm.tagIds
  };

  if (entryEditMode.value === "create") {
    await createKbEntry(payload);
    ElMessage.success("创建成功");
  } else if (currentEntryId.value) {
    await updateKbEntry(currentEntryId.value, payload);
    ElMessage.success("保存成功");
  }
  entryEditVisible.value = false;
  await loadEntryData();
};

const changeEntryStatus = async (row: KbEntryPageItemVO, status: 1 | 4) => {
  await updateKbEntryStatus(row.id, { status });
  ElMessage.success(status === 1 ? "已上架" : "已下架");
  await loadEntryData();
};

const openEntryDetail = async (row: KbEntryPageItemVO) => {
  const res = await getKbEntryDetail(row.id);
  entryDetail.value = res.data;
  entryDetailVisible.value = true;
};

const resolveCategoryName = (categoryId?: number) => {
  if (!categoryId) return "-";
  const found = flatCategories.value.find((item) => item.id === categoryId);
  return found?.name || `#${categoryId}`;
};

onMounted(async () => {
  await Promise.all([loadCategoryTree(), loadEntryData(), loadTagOptions()]);
});
</script>

<style scoped>
.kb-tabs {
  margin-top: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  min-width: 1px;
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

.icon-upload-block {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-upload-main {
  width: 52px;
  height: 52px;
}

.icon-preview {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  border: 1px solid var(--app-border);
}

.icon-placeholder {
  width: 52px;
  height: 52px;
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

.tag-wrap {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
