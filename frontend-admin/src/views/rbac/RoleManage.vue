<template>
  <el-card>
    <h2>角色管理</h2>
    <p>管理员可以管理角色，并配置角色权限。</p>

    <div class="toolbar">
      <el-form :inline="true" :model="query" class="toolbar-form">
        <el-form-item label="角色名称">
          <el-input v-model="query.name" placeholder="角色名称" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="openCreate">新增角色</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" style="width: 100%">
      <el-table-column prop="code" label="编码" min-width="180" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="description" label="描述" min-width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="190">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340">
        <template #default="scope">
          <el-button size="small" @click="openDetail(scope.row)">详情</el-button>
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="primary" plain @click="openPermConfig(scope.row)">权限配置</el-button>
          <el-button size="small" type="danger" @click="removeRole(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePage"
      />
    </div>
  </el-card>

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增角色' : '编辑角色'" width="460px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="编码" prop="code">
        <el-input v-model="form.code" :disabled="editMode === 'edit'" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" title="角色详情" width="760px">
    <el-descriptions v-if="currentDetail" :column="2" border>
      <el-descriptions-item label="编码">{{ currentDetail.code }}</el-descriptions-item>
      <el-descriptions-item label="名称">{{ currentDetail.name }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ currentDetail.description || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(currentDetail.createdAt) }}</el-descriptions-item>
    </el-descriptions>

    <h4 style="margin: 14px 0 8px">已拥有权限</h4>
    <el-table :data="detailPerms" style="width: 100%" max-height="320">
      <el-table-column prop="code" label="权限编码" min-width="220" />
      <el-table-column prop="name" label="权限名称" min-width="140" />
      <el-table-column prop="type" label="类型" width="90" />
    </el-table>

    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="permVisible" title="配置角色权限" width="620px">
    <el-form label-width="90px">
      <el-form-item label="角色">
        <span>{{ currentPermRole?.name }} ({{ currentPermRole?.code }})</span>
      </el-form-item>
      <el-form-item label="权限选择">
        <el-select v-model="selectedPermIds" multiple filterable clearable style="width: 100%" placeholder="请选择权限">
          <el-option v-for="perm in allPerms" :key="perm.id" :label="`${perm.name} (${perm.code})`" :value="perm.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="permVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPermConfig">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import {
  createRole,
  deleteRole,
  getRoleDetail,
  listPermissions,
  listRolePermissions,
  listRoles,
  updateRole,
  updateRolePermissions
} from "../../api/user";
import type {
  Permission,
  Role,
  RoleCreateDTO,
  RoleQueryDTO,
  RoleUpdateDTO
} from "../../types/adminUser";

const query = reactive<RoleQueryDTO>({ pageNum: 1, pageSize: 10 });
const rows = ref<Role[]>([]);
const total = ref(0);
const loading = ref(false);

const editVisible = ref(false);
const detailVisible = ref(false);
const permVisible = ref(false);
const editMode = ref<"create" | "edit">("create");
const currentId = ref<number | null>(null);
const currentDetail = ref<Role | null>(null);
const currentPermRole = ref<Role | null>(null);
const detailPerms = ref<Permission[]>([]);
const allPerms = ref<Permission[]>([]);
const selectedPermIds = ref<number[]>([]);

const formRef = ref<FormInstance>();
const form = reactive<RoleCreateDTO>({ code: "", name: "", description: "" });

const rules = {
  code: [{ required: true, message: "请输入角色编码", trigger: "blur" }],
  name: [{ required: true, message: "请输入角色名称", trigger: "blur" }]
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
    const res = await listRoles(query);
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
  form.description = "";
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const openEdit = async (row: Role) => {
  editMode.value = "edit";
  currentId.value = row.id;
  const res = await getRoleDetail(row.id);
  form.code = res.data.code;
  form.name = res.data.name;
  form.description = res.data.description || "";
  formRef.value?.clearValidate();
  editVisible.value = true;
};

const submitEdit = async () => {
  if (formRef.value) {
    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) return;
  }

  if (editMode.value === "create") {
    await createRole(form);
    ElMessage.success("创建成功");
  } else if (currentId.value) {
    const payload: RoleUpdateDTO = { name: form.name };
    await updateRole(currentId.value, payload);
    ElMessage.success("保存成功");
  }

  editVisible.value = false;
  await loadData();
};

const removeRole = async (row: Role) => {
  await ElMessageBox.confirm("确认删除该角色吗？", "提示", { type: "warning" });
  await deleteRole(row.id);
  ElMessage.success("删除成功");
  await loadData();
};

const openDetail = async (row: Role) => {
  const [roleRes, permsRes] = await Promise.all([
    getRoleDetail(row.id),
    listRolePermissions(row.id, 1, 999)
  ]);
  currentDetail.value = roleRes.data;
  detailPerms.value = permsRes.data.list;
  detailVisible.value = true;
};

const loadAllPermissions = async () => {
  const res = await listPermissions({ pageNum: 1, pageSize: 999 });
  allPerms.value = res.data.list;
};

const openPermConfig = async (row: Role) => {
  currentPermRole.value = row;
  const [allRes, ownRes] = await Promise.all([
    listPermissions({ pageNum: 1, pageSize: 999 }),
    listRolePermissions(row.id, 1, 999)
  ]);
  allPerms.value = allRes.data.list;
  selectedPermIds.value = ownRes.data.list.map((item) => item.id);
  permVisible.value = true;
};

const submitPermConfig = async () => {
  if (!currentPermRole.value) return;
  await updateRolePermissions({ roleId: currentPermRole.value.id, permissionIds: selectedPermIds.value });
  ElMessage.success("权限配置已更新");
  permVisible.value = false;
};

onMounted(async () => {
  await Promise.all([loadData(), loadAllPermissions()]);
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
