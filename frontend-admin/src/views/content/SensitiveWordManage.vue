<template>
  <el-card>
    <h2>敏感词规则</h2>
    <p>支持敏感词分级管理：`1` 为疑似复审，`2` 为强拦截。</p>

    <div class="toolbar">
      <el-form :inline="true" :model="query" class="toolbar-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="敏感词/分类" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="query.level" clearable placeholder="全部级别" style="width: 140px">
            <el-option label="疑似复审" :value="1" />
            <el-option label="强拦截" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.enabled" clearable placeholder="全部状态" style="width: 130px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar-actions">
        <el-button type="success" plain :disabled="selectedIds.length === 0" @click="handleBatchEnable">批量启用</el-button>
        <el-button type="warning" plain :disabled="selectedIds.length === 0" @click="handleBatchDisable">批量停用</el-button>
        <el-button type="primary" @click="openCreate">新增敏感词</el-button>
      </div>
    </div>

    <el-table :data="rows" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="word" label="敏感词" min-width="170" />
      <el-table-column label="级别" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.level === 1 ? 'warning' : 'danger'">
            {{ levelText(scope.row.level) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="hitActionDesc" label="命中提示" min-width="180" show-overflow-tooltip />
      <el-table-column prop="reasonTemplate" label="拦截提示" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.enabled === 1 ? 'success' : 'info'">
            {{ scope.row.enabled === 1 ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button
            v-if="scope.row.enabled === 1"
            size="small"
            type="warning"
            @click="toggleStatus(scope.row, 0)"
          >
            停用
          </el-button>
          <el-button
            v-else
            size="small"
            type="success"
            @click="toggleStatus(scope.row, 1)"
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

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增敏感词' : '编辑敏感词'" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="敏感词" prop="word">
        <el-input v-model="form.word" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="敏感级别" prop="level">
        <el-radio-group v-model="form.level">
          <el-radio :label="1">疑似复审</el-radio>
          <el-radio :label="2">强拦截</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="分类">
        <el-input v-model="form.category" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="命中提示">
        <el-input v-model="form.hitActionDesc" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="拦截提示">
        <el-input v-model="form.reasonTemplate" maxlength="255" show-word-limit />
      </el-form-item>
      <el-form-item label="状态" prop="enabled">
        <el-radio-group v-model="form.enabled">
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
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import {
  batchDisableSensitiveWords,
  batchEnableSensitiveWords,
  createSensitiveWord,
  getSensitiveWordDetail,
  listSensitiveWords,
  updateSensitiveWord
} from "../../api/sensitive";
import type { CmsSensitiveWord, CmsSensitiveWordQueryDTO, CmsSensitiveWordSaveDTO } from "../../types/sensitive";

const pageSizes = [10, 20, 50, 100];
const query = reactive<CmsSensitiveWordQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  level: null,
  enabled: null
});
const rows = ref<CmsSensitiveWord[]>([]);
const total = ref(0);
const loading = ref(false);
const selectedIds = ref<number[]>([]);

const editVisible = ref(false);
const editMode = ref<"create" | "edit">("create");
const currentId = ref<number | null>(null);
const formRef = ref<FormInstance>();
const form = reactive<CmsSensitiveWordSaveDTO>({
  word: "",
  level: 1,
  category: "",
  hitActionDesc: "",
  reasonTemplate: "",
  enabled: 1
});

const rules = {
  word: [{ required: true, message: "请输入敏感词", trigger: "blur" }],
  level: [{ required: true, message: "请选择敏感级别", trigger: "change" }],
  enabled: [{ required: true, message: "请选择状态", trigger: "change" }]
};

const levelText = (level?: number) => {
  if (level === 1) return "疑似复审";
  if (level === 2) return "强拦截";
  return "-";
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

const loadData = async () => {
  loading.value = true;
  try {
    const res = await listSensitiveWords({
      ...query,
      keyword: query.keyword?.trim() || undefined,
      category: query.category?.trim() || undefined,
      level: query.level ?? undefined,
      enabled: query.enabled ?? undefined
    });
    rows.value = res.data.list || [];
    total.value = res.data.total || 0;
    selectedIds.value = [];
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  query.keyword = "";
  query.category = "";
  query.level = null;
  query.enabled = null;
  query.pageNum = 1;
  loadData();
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

const handleSelectionChange = (selected: CmsSensitiveWord[]) => {
  selectedIds.value = selected.map((item) => item.id);
};

const resetForm = () => {
  form.word = "";
  form.level = 1;
  form.category = "";
  form.hitActionDesc = "";
  form.reasonTemplate = "";
  form.enabled = 1;
};

const openCreate = () => {
  editMode.value = "create";
  currentId.value = null;
  resetForm();
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const openEdit = async (row: CmsSensitiveWord) => {
  editMode.value = "edit";
  currentId.value = row.id;
  const res = await getSensitiveWordDetail(row.id);
  form.word = res.data.word || "";
  form.level = res.data.level === 2 ? 2 : 1;
  form.category = res.data.category || "";
  form.hitActionDesc = res.data.hitActionDesc || "";
  form.reasonTemplate = res.data.reasonTemplate || "";
  form.enabled = res.data.enabled === 0 ? 0 : 1;
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const submitEdit = async () => {
  await formRef.value?.validate();
  const payload: CmsSensitiveWordSaveDTO = {
    word: form.word.trim(),
    level: form.level === 2 ? 2 : 1,
    category: form.category?.trim() || undefined,
    hitActionDesc: form.hitActionDesc?.trim() || undefined,
    reasonTemplate: form.reasonTemplate?.trim() || undefined,
    enabled: form.enabled === 0 ? 0 : 1
  };
  if (editMode.value === "create") {
    await createSensitiveWord(payload);
    ElMessage.success("新增成功");
  } else {
    if (!currentId.value) return;
    await updateSensitiveWord(currentId.value, payload);
    ElMessage.success("更新成功");
  }
  editVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: CmsSensitiveWord, enabled: 0 | 1) => {
  await ElMessageBox.confirm(
    `确认${enabled === 1 ? "启用" : "停用"}敏感词「${row.word}」吗？`,
    "提示",
    { type: "warning" }
  );
  if (enabled === 1) {
    await batchEnableSensitiveWords([row.id]);
    ElMessage.success("已启用");
  } else {
    await batchDisableSensitiveWords([row.id]);
    ElMessage.success("已停用");
  }
  await loadData();
};

const handleBatchEnable = async () => {
  if (selectedIds.value.length === 0) return;
  await ElMessageBox.confirm(`确认启用选中的 ${selectedIds.value.length} 条敏感词吗？`, "提示", { type: "warning" });
  await batchEnableSensitiveWords(selectedIds.value);
  ElMessage.success("批量启用成功");
  await loadData();
};

const handleBatchDisable = async () => {
  if (selectedIds.value.length === 0) return;
  await ElMessageBox.confirm(`确认停用选中的 ${selectedIds.value.length} 条敏感词吗？`, "提示", { type: "warning" });
  await batchDisableSensitiveWords(selectedIds.value);
  ElMessage.success("批量停用成功");
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
  flex: 1;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>

