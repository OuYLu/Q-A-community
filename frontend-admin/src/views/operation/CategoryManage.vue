<template>
  <el-card>
    <h2>分类管理</h2>
    <p>默认展示一级分类，点击“详情”逐级查看下级分类。</p>

    <div class="level-nav">
      <div class="path">当前层级：{{ currentPathLabel }}</div>
      <div class="actions" v-if="parentStack.length > 0">
        <el-button link type="primary" @click="goBack">返回上级</el-button>
        <el-button link type="primary" @click="goRoot">返回一级</el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-form :inline="true" :model="query" class="toolbar-form">
        <el-form-item label="分类名称">
          <el-input v-model="query.name" placeholder="分类名称" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 130px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" min-width="180" />
      <el-table-column label="父分类" min-width="150">
        <template #default="scope">
          {{ resolveParentName(scope.row.parentId) }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column label="问题数" width="100">
        <template #default="scope">
          {{ scope.row.questionCount ?? 0 }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="190">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="290">
        <template #default="scope">
          <el-button v-if="hasChildren(scope.row.id)" size="small" type="primary" plain @click="openChildren(scope.row)">详情</el-button>
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="removeCategory(scope.row)">删除</el-button>
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

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增分类' : '编辑分类'" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="form.name" maxlength="30" show-word-limit />
      </el-form-item>
      <el-form-item label="父分类">
        <el-tree-select
          v-model="form.parentId"
          clearable
          filterable
          check-strictly
          lazy
          node-key="id"
          :data="parentTreeRoots"
          :props="treeProps"
          :load="loadParentTreeNode"
          placeholder="不选则为一级分类"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
      </el-form-item>
      <el-form-item label="图标">
        <div class="icon-upload-block">
          <div class="icon-upload-main">
            <el-upload
              class="icon-uploader"
              :show-file-list="false"
              :before-upload="handleIconUpload"
              accept="image/*"
            >
              <el-image v-if="iconPreviewSrc" :src="iconPreviewSrc" fit="cover" class="icon-preview" />
              <div v-else class="icon-placeholder">上传图标</div>
            </el-upload>
            <button
              v-if="iconPreviewSrc"
              type="button"
              class="icon-remove"
              aria-label="移除图标"
              @click="clearIcon"
            >
              ×
            </button>
          </div>
          <span class="icon-tip">支持图片文件，点击上传</span>
        </div>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type UploadProps } from "element-plus";
import {
  createCategory,
  deleteCategory,
  getCategoryDetail,
  listCategoryTree,
  listCategories,
  updateCategory
} from "../../api/category";
import { uploadAvatar } from "../../api/upload";
import type { CategoryTreeVO, QaCategory, QaCategoryQueryDTO, QaCategorySaveDTO } from "../../types/category";

type CategoryForm = {
  name: string;
  parentId?: number;
  icon: string;
  description: string;
  status: 0 | 1;
  sort: number;
};

type CategoryPathNode = {
  id: number;
  name: string;
};

type CategoryTreeNode = CategoryTreeVO & {
  label: string;
  leaf: boolean;
  disabled?: boolean;
};

const query = reactive<QaCategoryQueryDTO>({ pageNum: 1, pageSize: 10, status: null });
const pageSizes = [10, 20, 50, 100];
const rows = ref<QaCategory[]>([]);
const total = ref(0);
const loading = ref(false);

const currentParentId = ref<number | null>(null);
const parentStack = ref<CategoryPathNode[]>([]);

const allCategories = ref<QaCategory[]>([]);
const parentTreeRoots = ref<CategoryTreeNode[]>([]);
const parentIdSet = computed(() => {
  const set = new Set<number>();
  allCategories.value.forEach((item) => {
    if (typeof item.parentId === "number") {
      set.add(item.parentId);
    }
  });
  return set;
});
const treeProps = {
  label: "label",
  value: "id",
  children: "children",
  isLeaf: "leaf",
  disabled: "disabled"
} as const;
const forbiddenParentIds = computed(() => {
  const set = new Set<number>();
  if (!currentId.value) {
    return set;
  }
  set.add(currentId.value);
  let changed = true;
  while (changed) {
    changed = false;
    allCategories.value.forEach((item) => {
      if (typeof item.parentId === "number" && set.has(item.parentId) && !set.has(item.id)) {
        set.add(item.id);
        changed = true;
      }
    });
  }
  return set;
});

const currentPathLabel = computed(() => {
  if (parentStack.value.length === 0) {
    return "一级分类";
  }
  return `一级分类 / ${parentStack.value.map((item) => item.name).join(" / ")}`;
});

const editVisible = ref(false);
const editMode = ref<"create" | "edit">("create");
const currentId = ref<number | null>(null);
const iconLocalPreview = ref("");

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

const formRef = ref<FormInstance>();
const form = reactive<CategoryForm>({
  name: "",
  parentId: undefined,
  icon: "",
  description: "",
  status: 1,
  sort: 0
});
const iconPreviewSrc = computed(() => iconLocalPreview.value || resolveMediaUrl(form.icon || ""));

const rules = {
  name: [{ required: true, message: "请输入分类名称", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
  sort: [{ required: true, message: "请输入排序值", trigger: "blur" }]
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

const buildBaseQuery = () => {
  return {
    name: query.name?.trim() || undefined,
    status: query.status ?? undefined
  };
};

const loadData = async () => {
  loading.value = true;
  try {
    const baseQuery = buildBaseQuery();
    if (currentParentId.value === null) {
      const res = await listCategories({ ...baseQuery, pageNum: 1, pageSize: 9999 });
      const roots = res.data.list.filter((item) => !item.parentId);
      total.value = roots.length;
      const start = ((query.pageNum || 1) - 1) * (query.pageSize || 10);
      const end = start + (query.pageSize || 10);
      rows.value = roots.slice(start, end);
      return;
    }

    const res = await listCategories({
      ...baseQuery,
      parentId: currentParentId.value,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    });
    rows.value = res.data.list;
    total.value = res.data.total;
  } finally {
    loading.value = false;
  }
};

const loadAllCategories = async () => {
  const res = await listCategories({ pageNum: 1, pageSize: 9999 });
  allCategories.value = res.data.list;
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
  query.status = null;
  query.pageNum = 1;
  loadData();
};

const goRoot = async () => {
  parentStack.value = [];
  currentParentId.value = null;
  query.pageNum = 1;
  await loadData();
};

const goBack = async () => {
  if (parentStack.value.length === 0) {
    return;
  }
  parentStack.value.pop();
  currentParentId.value = parentStack.value.length ? parentStack.value[parentStack.value.length - 1].id : null;
  query.pageNum = 1;
  await loadData();
};

const openChildren = async (row: QaCategory) => {
  if (!hasChildren(row.id)) {
    return;
  }
  parentStack.value.push({ id: row.id, name: row.name });
  currentParentId.value = row.id;
  query.pageNum = 1;
  await loadData();
};

const hasChildren = (id: number) => {
  return parentIdSet.value.has(id);
};

const toTreeNode = (item: CategoryTreeVO): CategoryTreeNode => {
  return {
    ...item,
    label: item.label || item.name,
    leaf: item.leaf ?? !item.hasChildren,
    disabled: forbiddenParentIds.value.has(item.id)
  };
};

const loadParentTreeRoots = async () => {
  const res = await listCategoryTree();
  parentTreeRoots.value = res.data.map(toTreeNode);
};

const loadParentTreeNode = async (
  node: { level: number; data?: CategoryTreeNode },
  resolve: (data: CategoryTreeNode[]) => void
) => {
  if (node.level === 0) {
    resolve(parentTreeRoots.value);
    return;
  }
  const parentId = node.data?.id;
  if (!parentId) {
    resolve([]);
    return;
  }
  const res = await listCategoryTree(parentId);
  resolve(res.data.map(toTreeNode));
};

const resetForm = () => {
  form.name = "";
  form.parentId = currentParentId.value ?? undefined;
  form.icon = "";
  iconLocalPreview.value = "";
  form.description = "";
  form.status = 1;
  form.sort = 0;
};

const openCreate = async () => {
  editMode.value = "create";
  currentId.value = null;
  await loadParentTreeRoots();
  resetForm();
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const openEdit = async (row: QaCategory) => {
  editMode.value = "edit";
  currentId.value = row.id;
  await loadParentTreeRoots();
  const res = await getCategoryDetail(row.id);
  form.name = res.data.name;
  form.parentId = res.data.parentId ?? undefined;
  form.icon = res.data.icon || "";
  iconLocalPreview.value = "";
  form.description = res.data.description || "";
  form.status = res.data.status === 0 ? 0 : 1;
  form.sort = Number.isFinite(res.data.sort) ? res.data.sort : 0;
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const resolveParentName = (parentId?: number | null) => {
  if (!parentId) return "-";
  const found = allCategories.value.find((item) => item.id === parentId);
  return found?.name || `#${parentId}`;
};

const toSavePayload = (): QaCategorySaveDTO => {
  return {
    name: form.name.trim(),
    parentId: form.parentId ?? null,
    icon: form.icon.trim() || undefined,
    description: form.description.trim() || undefined,
    status: form.status,
    sort: form.sort
  };
};

const clearIcon = () => {
  if (iconLocalPreview.value.startsWith("blob:")) {
    URL.revokeObjectURL(iconLocalPreview.value);
  }
  iconLocalPreview.value = "";
  form.icon = "";
};

const handleIconUpload: UploadProps["beforeUpload"] = async (rawFile) => {
  if (iconLocalPreview.value.startsWith("blob:")) {
    URL.revokeObjectURL(iconLocalPreview.value);
  }
  iconLocalPreview.value = URL.createObjectURL(rawFile);
  const res = await uploadAvatar(rawFile);
  form.icon = res.data;
  return false;
};

const submitEdit = async () => {
  if (formRef.value) {
    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) return;
  }

  const payload = toSavePayload();
  if (editMode.value === "create") {
    await createCategory(payload);
    ElMessage.success("创建成功");
  } else if (currentId.value) {
    await updateCategory(currentId.value, payload);
    ElMessage.success("保存成功");
  }

  editVisible.value = false;
  await Promise.all([loadData(), loadAllCategories()]);
};

const removeCategory = async (row: QaCategory) => {
  await ElMessageBox.confirm("确认删除该分类吗？", "提示", { type: "warning" });
  await deleteCategory(row.id);
  ElMessage.success("删除成功");
  await Promise.all([loadData(), loadAllCategories()]);
};

onMounted(async () => {
  await Promise.all([loadData(), loadAllCategories(), loadParentTreeRoots()]);
});
</script>

<style scoped>
.level-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.path {
  font-size: 14px;
  color: #606266;
}

.actions {
  display: flex;
  gap: 12px;
}

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

.icon-upload-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.icon-upload-main {
  position: relative;
  width: 52px;
  height: 52px;
}

.icon-uploader {
  line-height: 1;
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

.icon-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 16px;
  height: 16px;
  border: none;
  border-radius: 50%;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  line-height: 16px;
  text-align: center;
  padding: 0;
  cursor: pointer;
}

.icon-tip {
  font-size: 12px;
  color: var(--app-text-muted);
}
</style>
