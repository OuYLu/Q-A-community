<template>
  <el-card>
    <h2>权限点管理</h2>
    <p>管理员可以管理权限点（增删查改）。</p>

    <div class="toolbar">
      <el-form :inline="true" :model="query" class="toolbar-form">
        <el-form-item label="权限名称">
          <el-input v-model="query.name" placeholder="权限名称" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="openCreate">新增权限点</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" style="width: 100%">
      <el-table-column prop="code" label="权限编码" min-width="220" />
      <el-table-column prop="name" label="名称" min-width="130" />
      <el-table-column prop="type" label="类型" width="90" />
      <el-table-column prop="pathOrApi" label="路径/API" min-width="180" />
      <el-table-column prop="method" label="方法" width="90" />
      <el-table-column prop="createdAt" label="创建时间" width="190">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="removePermission(scope.row)">删除</el-button>
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

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增权限点' : '编辑权限点'" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="权限编码" prop="code">
        <el-input v-model="form.code" :disabled="editMode === 'edit'" />
      </el-form-item>
      <el-form-item label="权限名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="form.type" style="width: 100%">
          <el-option label="api" value="api" />
          <el-option label="menu" value="menu" />
          <el-option label="button" value="button" />
        </el-select>
      </el-form-item>
      <el-form-item label="路径/API">
        <el-input v-model="form.pathOrApi" />
      </el-form-item>
      <el-form-item label="请求方法">
        <el-input v-model="form.method" placeholder="GET/POST/PUT/DELETE" />
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
  createPermission,
  deletePermission,
  getPermissionDetail,
  listPermissions,
  updatePermission
} from "../../api/user";
import type {
  Permission,
  PermissionQueryDTO,
  PermissionSaveDTO
} from "../../types/adminUser";

const query = reactive<PermissionQueryDTO>({ pageNum: 1, pageSize: 10 });
const pageSizes = [10, 20, 50, 100];
const rows = ref<Permission[]>([]);
const total = ref(0);
const loading = ref(false);

const editVisible = ref(false);
const editMode = ref<"create" | "edit">("create");
const currentId = ref<number | null>(null);

const formRef = ref<FormInstance>();
const form = reactive<PermissionSaveDTO>({
  code: "",
  name: "",
  type: "api",
  pathOrApi: "",
  method: ""
});

const rules = {
  code: [{ required: true, message: "请输入权限编码", trigger: "blur" }],
  name: [{ required: true, message: "请输入权限名称", trigger: "blur" }],
  type: [{ required: true, message: "请选择权限类型", trigger: "change" }]
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
    const res = await listPermissions(query);
    rows.value = res.data.list;
    total.value = res.data.total;
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
  query.pageNum = 1;
  loadData();
};

const openCreate = () => {
  editMode.value = "create";
  currentId.value = null;
  form.code = "";
  form.name = "";
  form.type = "api";
  form.pathOrApi = "";
  form.method = "";
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const openEdit = async (row: Permission) => {
  editMode.value = "edit";
  currentId.value = row.id;
  const res = await getPermissionDetail(row.id);
  form.code = res.data.code;
  form.name = res.data.name;
  form.type = res.data.type;
  form.pathOrApi = res.data.pathOrApi || "";
  form.method = res.data.method || "";
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const submitEdit = async () => {
  if (formRef.value) {
    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) return;
  }

  if (editMode.value === "create") {
    await createPermission(form);
    ElMessage.success("创建成功");
  } else if (currentId.value) {
    await updatePermission(currentId.value, form);
    ElMessage.success("保存成功");
  }

  editVisible.value = false;
  await loadData();
};

const removePermission = async (row: Permission) => {
  await ElMessageBox.confirm("确认删除该权限点吗？", "提示", { type: "warning" });
  await deletePermission(row.id);
  ElMessage.success("删除成功");
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

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
