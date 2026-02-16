<template>
  <el-card>
    <h2>用户管理</h2>
    <p>管理员可以管理 staff / customer / expert 用户。</p>

    <el-tabs v-model="activeTab" class="user-tabs">
      <el-tab-pane label="Staff 管理" name="staff">
        <div class="toolbar">
          <el-form :inline="true" :model="staffQuery" class="toolbar-form">
            <el-form-item label="用户名">
              <el-input v-model="staffQuery.username" placeholder="用户名" clearable />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="staffQuery.nickname" placeholder="昵称" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="staffQuery.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="resetStaff">重置</el-button>
            </el-form-item>
          </el-form>
          <el-button type="primary" @click="openCreateStaff">新增 Staff</el-button>
        </div>

        <el-table :data="staffList" v-loading="staffLoading" style="width: 100%">
          <el-table-column label="头像" width="90">
            <template #default="scope">
              <el-avatar :src="resolveAvatar(scope.row)">{{ scope.row.username?.slice(0, 1) }}</el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
              <el-tag v-else type="danger">冻结</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="190">
            <template #default="scope">
              {{ formatDateTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="scope">
              <el-button size="small" @click="openEditByUser(scope.row.id)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeUser(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            :current-page="staffQuery.pageNum"
            :page-size="staffQuery.pageSize"
            :page-sizes="pageSizes"
            :total="staffTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="(s: number) => handleStaffSizeChange(s)"
            @current-change="(p: number) => handleStaffPage(p)"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="Customer 管理" name="customer">
        <div class="toolbar">
          <el-form :inline="true" :model="customerQuery" class="toolbar-form">
            <el-form-item label="用户名">
              <el-input v-model="customerQuery.username" placeholder="用户名" clearable />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="customerQuery.nickname" placeholder="昵称" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="customerQuery.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="resetCustomer">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table :data="customerList" v-loading="customerLoading" style="width: 100%">
          <el-table-column label="头像" width="90">
            <template #default="scope">
              <el-avatar :src="resolveAvatar(scope.row)">{{ scope.row.username?.slice(0, 1) }}</el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
              <el-tag v-else type="danger">冻结</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="190">
            <template #default="scope">
              {{ formatDateTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button size="small" @click="toggleFreeze(scope.row)">
                {{ scope.row.status === 1 ? "冻结" : "解冻" }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            :current-page="customerQuery.pageNum"
            :page-size="customerQuery.pageSize"
            :page-sizes="pageSizes"
            :total="customerTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="(s: number) => handleCustomerSizeChange(s)"
            @current-change="(p: number) => handleCustomerPage(p)"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="Expert 管理" name="expert">
        <el-card class="sub-card">
          <div class="toolbar">
            <el-form :inline="true" :model="expertQuery" class="toolbar-form">
              <el-form-item label="姓名">
                <el-input v-model="expertQuery.realName" placeholder="姓名" clearable />
              </el-form-item>
              <el-form-item label="机构">
                <el-input v-model="expertQuery.organization" placeholder="机构" clearable />
              </el-form-item>
              <el-form-item label="专长">
                <el-input v-model="expertQuery.expertise" placeholder="专长" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="expertQuery.expertStatus" placeholder="全部" clearable style="width: 120px">
                  <el-option label="已认证" :value="3" />
                  <el-option label="已冻结" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button @click="resetExpert">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="expertList" v-loading="expertLoading" style="width: 100%">
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="organization" label="机构" />
            <el-table-column prop="title" label="职称" />
            <el-table-column prop="expertise" label="专长" />
            <el-table-column prop="expertStatus" label="状态" width="120">
              <template #default="scope">
                <el-tag v-if="scope.row.expertStatus === 3" type="success">已认证</el-tag>
                <el-tag v-else type="danger">已冻结</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button size="small" @click="openExpertDetail(scope.row)">详情</el-button>
                <el-button size="small" @click="toggleExpertStatus(scope.row)">
                  {{ scope.row.expertStatus === 3 ? "冻结" : "解冻" }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              :current-page="expertQuery.pageNum"
              :page-size="expertQuery.pageSize"
              :page-sizes="pageSizes"
              :total="expertTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="(s: number) => handleExpertSizeChange(s)"
              @current-change="(p: number) => handleExpertPage(p)"
            />
          </div>
        </el-card>

        <el-card class="sub-card" style="margin-top: 16px">
          <h3>专家资质审核</h3>
          <div class="toolbar">
            <el-form :inline="true" :model="applyQuery" class="toolbar-form">
              <el-form-item label="姓名">
                <el-input v-model="applyQuery.realName" placeholder="姓名" clearable />
              </el-form-item>
              <el-form-item label="机构">
                <el-input v-model="applyQuery.organization" placeholder="机构" clearable />
              </el-form-item>
              <el-form-item label="专长">
                <el-input v-model="applyQuery.expertise" placeholder="专长" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="applyQuery.status" placeholder="全部" clearable style="width: 120px">
                  <el-option label="待审核" :value="1" />
                  <el-option label="通过" :value="2" />
                  <el-option label="拒绝" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button @click="resetApplies">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="applyList" v-loading="applyLoading" style="width: 100%">
            <el-table-column prop="realName" label="姓名" />
            <el-table-column prop="organization" label="机构" />
            <el-table-column prop="title" label="职称" />
            <el-table-column prop="expertise" label="专长" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 1" type="warning">待审核</el-tag>
                <el-tag v-else-if="scope.row.status === 2" type="success">已认证</el-tag>
                <el-tag v-else type="danger">拒绝</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280">
              <template #default="scope">
                <el-button size="small" @click="openApplyDetail(scope.row)">详情</el-button>
                <el-button size="small" type="success" @click="openReview(scope.row, 2)">通过</el-button>
                <el-button size="small" type="danger" @click="openReview(scope.row, 3)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              :current-page="applyQuery.pageNum"
              :page-size="applyQuery.pageSize"
              :page-sizes="pageSizes"
              :total="applyTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="(s: number) => handleApplySizeChange(s)"
              @current-change="(p: number) => handleApplyPage(p)"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-dialog v-model="editVisible" title="编辑用户" width="460px">
    <div class="create-avatar-section">
      <el-upload class="avatar-uploader" :show-file-list="false" :before-upload="handleEditAvatarUpload">
        <el-avatar :size="76" :src="editAvatarSrc" class="clickable-avatar">
          <span>上传</span>
        </el-avatar>
      </el-upload>
      <div class="avatar-tip">点击头像上传</div>
    </div>

    <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="editForm.nickname" />
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="editForm.username" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="editForm.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="editForm.email" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="createVisible" title="新增 Staff" width="460px">
    <div class="create-avatar-section">
      <el-upload class="avatar-uploader" :show-file-list="false" :before-upload="handleCreateAvatarUpload">
        <el-avatar :size="76" :src="createAvatarSrc" class="clickable-avatar">
          <span>上传</span>
        </el-avatar>
      </el-upload>
      <div class="avatar-tip">点击头像上传</div>
    </div>

    <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="createForm.nickname" />
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="createForm.username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="createForm.password" type="password" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="createForm.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="createForm.email" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" @click="submitCreate">创建</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="reviewVisible" title="专家审核" width="420px">
    <el-form :model="reviewForm" label-width="80px">
      <el-form-item label="结果">
        <el-radio-group v-model="reviewForm.status">
          <el-radio :label="2">通过</el-radio>
          <el-radio :label="3">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="reviewForm.status === 3" label="原因">
        <el-input v-model="reviewForm.rejectReason" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="reviewVisible = false">取消</el-button>
      <el-button type="primary" @click="submitReview">提交</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" :title="detailTitle" width="760px">
    <div v-if="currentExpertDetail" class="detail-wrap">
      <div v-if="currentApplyUserAvatar" class="detail-avatar-row">
        <el-avatar :size="56" :src="currentApplyUserAvatar" />
        <span>申请人头像</span>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ currentExpertDetail.realName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="机构">{{ currentExpertDetail.organization || "-" }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ currentExpertDetail.title || "-" }}</el-descriptions-item>
        <el-descriptions-item label="专长">{{ currentExpertDetail.expertise || "-" }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ expertApplyStatusText(currentExpertDetail.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentExpertDetail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(currentExpertDetail.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <div class="proof-block">
        <h4>认证资料</h4>
        <div v-if="proofGroups.length === 0" class="empty-proof">无附件</div>
        <div v-else class="proof-group-list">
          <div v-for="group in proofGroups" :key="group.category" class="proof-group">
            <h5>{{ proofCategoryLabel(group.category) }}</h5>
            <div class="proof-grid">
              <div
                v-for="item in group.items"
                :key="`${group.category}-${item.url}-${item.name || ''}`"
                class="proof-item"
              >
                <img
                  v-if="isImage(item.url, item.mime)"
                  :src="resolveMediaUrl(item.url)"
                  class="proof-image"
                  :alt="item.name || fileName(item.url)"
                />
                <a v-else :href="resolveMediaUrl(item.url)" target="_blank" rel="noopener noreferrer">
                  {{ item.name || fileName(item.url) }}
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
      <el-button
        v-if="detailScene === 'apply' && currentApplyRow"
        type="success"
        @click="openReviewFromDetail(2)"
      >
        通过
      </el-button>
      <el-button
        v-if="detailScene === 'apply' && currentApplyRow"
        type="danger"
        @click="openReviewFromDetail(3)"
      >
        拒绝
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type UploadProps } from "element-plus";
import {
  createStaff,
  deleteUser,
  getExpertApplyDetail,
  getExpertUserDetail,
  getUserDetail,
  listExpertApplies,
  listExpertUsers,
  listUsers,
  reviewExpertApply,
  updateExpertStatus,
  updateUser,
  updateUserStatus
} from "../../api/user";
import { uploadAvatar } from "../../api/upload";
import type {
  AdminCreateStaffDTO,
  ExpertApply,
  ExpertApplyDetailVO,
  ExpertApplyQueryDTO,
  ExpertManageVO,
  ExpertReviewDTO,
  ProofFileItem,
  ExpertUserQueryDTO,
  UserManageVO,
  UserQueryDTO,
  UserUpdateDTO
} from "../../types/adminUser";

const activeTab = ref("staff");
const pageSizes = [10, 20, 50, 100];

const staffQuery = reactive<UserQueryDTO>({ pageNum: 1, pageSize: 10, roleCode: "staff" });
const customerQuery = reactive<UserQueryDTO>({ pageNum: 1, pageSize: 10, roleCode: "customer" });
const expertQuery = reactive<ExpertUserQueryDTO>({ pageNum: 1, pageSize: 10 });

const staffList = ref<UserManageVO[]>([]);
const customerList = ref<UserManageVO[]>([]);
const expertList = ref<ExpertManageVO[]>([]);
const staffTotal = ref(0);
const customerTotal = ref(0);
const expertTotal = ref(0);
const staffLoading = ref(false);
const customerLoading = ref(false);
const expertLoading = ref(false);

const applyQuery = reactive<ExpertApplyQueryDTO>({ pageNum: 1, pageSize: 10, status: 1 });
const applyList = ref<ExpertApply[]>([]);
const applyTotal = ref(0);
const applyLoading = ref(false);

const editVisible = ref(false);
const createVisible = ref(false);
const reviewVisible = ref(false);
const detailVisible = ref(false);
const createAvatarPreview = ref("");
const editAvatarPreview = ref("");

const detailScene = ref<"apply" | "expert">("expert");
const detailTitle = ref("专家认证资料详情");
const currentExpertDetail = ref<ExpertApplyDetailVO | null>(null);
const currentApplyRow = ref<ExpertApply | null>(null);
const currentApplyUserAvatar = ref("");
const proofGroups = ref<Array<{ category: string; items: ProofFileItem[] }>>([]);

const editId = ref<number | null>(null);
const editForm = reactive<UserUpdateDTO>({});

const createForm = reactive<AdminCreateStaffDTO>({ username: "", password: "" });

const reviewForm = reactive<ExpertReviewDTO>({ applyId: 0, status: 2, rejectReason: "" });
const createFormRef = ref<FormInstance>();
const editFormRef = ref<FormInstance>();

const createRules = {
  nickname: [{ required: true, message: "请输入昵称", trigger: "blur" }],
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }]
};

const editRules = {
  nickname: [{ required: true, message: "请输入昵称", trigger: "blur" }],
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  phone: [{ required: true, message: "请输入手机号", trigger: "blur" }]
};

const fileName = (url: string) => {
  const idx = url.lastIndexOf("/");
  return idx >= 0 ? url.slice(idx + 1) : url;
};

const isImage = (url: string, mime?: string): boolean => {
  if (mime?.startsWith("image/")) return true;
  return /\.(png|jpe?g|gif|webp|bmp)$/i.test(url.split("?")[0]);
};

const normalizeProofItem = (raw: unknown): ProofFileItem | null => {
  if (typeof raw === "string") {
    return raw ? { url: raw } : null;
  }
  if (raw && typeof raw === "object") {
    const item = raw as { url?: unknown; mime?: unknown; name?: unknown; size?: unknown };
    if (typeof item.url !== "string" || !item.url) return null;
    return {
      url: item.url,
      mime: typeof item.mime === "string" ? item.mime : undefined,
      name: typeof item.name === "string" ? item.name : undefined,
      size: typeof item.size === "number" ? item.size : undefined
    };
  }
  return null;
};

const parseProofGroups = (raw: unknown): Array<{ category: string; items: ProofFileItem[] }> => {
  if (!raw) return [];
  if (typeof raw === "string") {
    try {
      const parsed = JSON.parse(raw) as unknown;
      return parseProofGroups(parsed);
    } catch {
      const item = normalizeProofItem(raw);
      return item ? [{ category: "OTHER", items: [item] }] : [];
    }
  }
  if (Array.isArray(raw)) {
    const items = raw.map((entry) => normalizeProofItem(entry)).filter((entry): entry is ProofFileItem => !!entry);
    return items.length ? [{ category: "OTHER", items }] : [];
  }
  if (raw && typeof raw === "object") {
    const record = raw as Record<string, unknown>;
    const groups = Object.entries(record)
      .map(([category, value]) => {
        const list = Array.isArray(value) ? value : [value];
        const items = list.map((entry) => normalizeProofItem(entry)).filter((entry): entry is ProofFileItem => !!entry);
        return { category, items };
      })
      .filter((group) => group.items.length > 0);
    if (groups.length > 0) return groups;
  }
  return [];
};

const proofCategoryLabel = (code: string) => {
  const dict: Record<string, string> = {
    EDUCATION: "学历证明",
    EMPLOYMENT: "在职证明",
    LICENSE: "执业许可",
    TITLE: "职称证明",
    OTHER: "其他材料"
  };
  return dict[code] || code;
};

const expertApplyStatusText = (status?: number) => {
  if (status === 1) return "待审核";
  if (status === 2) return "已认证";
  if (status === 3) return "拒绝";
  return "-";
};

const avatarBaseUrl = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/+$/, "");
const resolveMediaUrl = (raw?: string): string => {
  if (!raw) return "";
  if (/^(https?:)?\/\//i.test(raw) || raw.startsWith("data:") || raw.startsWith("blob:")) {
    return raw;
  }
  if (!avatarBaseUrl) return raw;
  if (raw.startsWith("/")) {
    if (avatarBaseUrl.startsWith("http")) {
      if (avatarBaseUrl.endsWith("/api") && raw.startsWith("/api/")) {
        return `${avatarBaseUrl.replace(/\/api$/, "")}${raw}`;
      }
      return `${avatarBaseUrl}${raw}`;
    }
    if (raw === avatarBaseUrl || raw.startsWith(`${avatarBaseUrl}/`)) {
      return raw;
    }
    return `${avatarBaseUrl}${raw}`;
  }
  return `${avatarBaseUrl}/${raw}`;
};

const resolveAvatar = (row: UserManageVO) => resolveMediaUrl(row.avatar);
const createAvatarSrc = computed(() => resolveMediaUrl(createAvatarPreview.value || createForm.avatar || undefined));
const editAvatarSrc = computed(() => resolveMediaUrl(editAvatarPreview.value || editForm.avatar || undefined));

const formatDateTime = (value?: string) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const pad = (num: number) => String(num).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
    date.getMinutes()
  )}:${pad(date.getSeconds())}`;
};

const debounce = <T extends (...args: never[]) => void>(fn: T, delay = 300) => {
  let timer: ReturnType<typeof setTimeout> | null = null;
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
};

const loadStaff = async () => {
  staffLoading.value = true;
  try {
    const res = await listUsers(staffQuery);
    staffList.value = res.data.list;
    staffTotal.value = res.data.total;
  } finally {
    staffLoading.value = false;
  }
};

const loadCustomer = async () => {
  customerLoading.value = true;
  try {
    const res = await listUsers(customerQuery);
    customerList.value = res.data.list;
    customerTotal.value = res.data.total;
  } finally {
    customerLoading.value = false;
  }
};

const loadExpert = async () => {
  expertLoading.value = true;
  try {
    const res = await listExpertUsers(expertQuery);
    expertList.value = res.data.list;
    expertTotal.value = res.data.total;
  } finally {
    expertLoading.value = false;
  }
};

const loadApplies = async () => {
  applyLoading.value = true;
  try {
    const res = await listExpertApplies(applyQuery);
    applyList.value = res.data.list;
    applyTotal.value = res.data.total;
  } finally {
    applyLoading.value = false;
  }
};

const resetStaff = () => {
  staffQuery.username = "";
  staffQuery.nickname = "";
  staffQuery.status = null;
  staffQuery.pageNum = 1;
  loadStaff();
};

const resetCustomer = () => {
  customerQuery.username = "";
  customerQuery.nickname = "";
  customerQuery.status = null;
  customerQuery.pageNum = 1;
  loadCustomer();
};

const resetExpert = () => {
  expertQuery.realName = "";
  expertQuery.organization = "";
  expertQuery.expertise = "";
  expertQuery.expertStatus = null;
  expertQuery.pageNum = 1;
  loadExpert();
};

const resetApplies = () => {
  applyQuery.realName = "";
  applyQuery.organization = "";
  applyQuery.expertise = "";
  applyQuery.status = null;
  applyQuery.pageNum = 1;
  loadApplies();
};

const handleStaffPage = (page: number) => {
  staffQuery.pageNum = page;
  loadStaff();
};

const handleStaffSizeChange = (size: number) => {
  staffQuery.pageSize = size;
  staffQuery.pageNum = 1;
  loadStaff();
};

const handleCustomerPage = (page: number) => {
  customerQuery.pageNum = page;
  loadCustomer();
};

const handleCustomerSizeChange = (size: number) => {
  customerQuery.pageSize = size;
  customerQuery.pageNum = 1;
  loadCustomer();
};

const handleExpertPage = (page: number) => {
  expertQuery.pageNum = page;
  loadExpert();
};

const handleExpertSizeChange = (size: number) => {
  expertQuery.pageSize = size;
  expertQuery.pageNum = 1;
  loadExpert();
};

const handleApplyPage = (page: number) => {
  applyQuery.pageNum = page;
  loadApplies();
};

const handleApplySizeChange = (size: number) => {
  applyQuery.pageSize = size;
  applyQuery.pageNum = 1;
  loadApplies();
};

const openEditByUser = async (userId: number) => {
  editId.value = userId;
  editForm.username = "";
  editForm.nickname = "";
  editForm.phone = "";
  editForm.email = "";
  editForm.avatar = undefined;
  const res = await getUserDetail(userId);
  editForm.username = res.data.username;
  editForm.nickname = res.data.nickname;
  editForm.phone = res.data.phone;
  editForm.email = res.data.email;
  editForm.avatar = res.data.avatar;
  editAvatarPreview.value = "";
  editFormRef.value?.clearValidate();
  editVisible.value = true;
};

const debouncedLoadStaff = debounce(() => {
  loadStaff();
}, 250);

const debouncedLoadCustomer = debounce(() => {
  loadCustomer();
}, 250);

const debouncedLoadExpert = debounce(() => {
  loadExpert();
}, 250);

const debouncedLoadApplies = debounce(() => {
  loadApplies();
}, 250);

const handleCreateAvatarUpload: UploadProps["beforeUpload"] = async (rawFile) => {
  if (createAvatarPreview.value) {
    URL.revokeObjectURL(createAvatarPreview.value);
  }
  createAvatarPreview.value = URL.createObjectURL(rawFile);
  const res = await uploadAvatar(rawFile);
  createForm.avatar = res.data;
  return false;
};

const handleEditAvatarUpload: UploadProps["beforeUpload"] = async (rawFile) => {
  if (editAvatarPreview.value) {
    URL.revokeObjectURL(editAvatarPreview.value);
  }
  editAvatarPreview.value = URL.createObjectURL(rawFile);
  const res = await uploadAvatar(rawFile);
  editForm.avatar = res.data;
  return false;
};

const submitEdit = async () => {
  if (!editId.value) return;
  if (editFormRef.value) {
    const valid = await editFormRef.value.validate().catch(() => false);
    if (!valid) return;
  }
  const payload: UserUpdateDTO = {
    username: editForm.username,
    nickname: editForm.nickname,
    phone: editForm.phone,
    email: editForm.email,
    avatar: editForm.avatar
  };
  await updateUser(editId.value, payload);
  ElMessage.success("保存成功");
  editVisible.value = false;
  await Promise.all([loadStaff(), loadCustomer(), loadExpert()]);
};

const toggleStatus = async (row: UserManageVO, enabled: boolean) => {
  const nextStatus = enabled ? 1 : 0;
  const prev = row.status;
  row.status = nextStatus;
  try {
    await updateUserStatus(row.id, { status: nextStatus });
  } catch {
    row.status = prev;
  }
};

const toggleFreeze = async (row: UserManageVO) => {
  const nextStatus = row.status === 1 ? 0 : 1;
  await updateUserStatus(row.id, { status: nextStatus });
  row.status = nextStatus;
};

const toggleExpertStatus = async (row: ExpertManageVO) => {
  const nextStatus = row.expertStatus === 3 ? 0 : 3;
  await updateExpertStatus(row.userId, { expertStatus: nextStatus });
  row.expertStatus = nextStatus;
};

const removeUser = async (row: UserManageVO) => {
  await ElMessageBox.confirm("确认删除该用户吗？", "提示", { type: "warning" });
  await deleteUser(row.id);
  ElMessage.success("删除成功");
  await loadStaff();
};

const openCreateStaff = () => {
  createForm.username = "";
  createForm.password = "";
  createForm.nickname = "";
  createForm.phone = "";
  createForm.email = "";
  createForm.avatar = "";
  if (createAvatarPreview.value) {
    URL.revokeObjectURL(createAvatarPreview.value);
  }
  createAvatarPreview.value = "";
  createFormRef.value?.clearValidate();
  createVisible.value = true;
};

const submitCreate = async () => {
  if (createFormRef.value) {
    const valid = await createFormRef.value.validate().catch(() => false);
    if (!valid) return;
  }
  await createStaff(createForm);
  createVisible.value = false;
  ElMessage.success("创建成功");
  await loadStaff();
};

const openReview = (row: ExpertApply, status: number) => {
  reviewForm.applyId = row.id;
  reviewForm.status = status;
  reviewForm.rejectReason = "";
  reviewVisible.value = true;
};

const loadDetailAvatar = async (userId: number) => {
  currentApplyUserAvatar.value = "";
  try {
    const userRes = await getUserDetail(userId);
    currentApplyUserAvatar.value = resolveMediaUrl(userRes.data.avatar || "");
  } catch {
    currentApplyUserAvatar.value = "";
  }
};

const openApplyDetail = async (row: ExpertApply) => {
  detailScene.value = "apply";
  detailTitle.value = "专家申请详情";
  currentApplyRow.value = row;
  const res = await getExpertApplyDetail(row.userId);
  currentExpertDetail.value = res.data;
  proofGroups.value = parseProofGroups(res.data.proofUrls);
  await loadDetailAvatar(row.userId);
  detailVisible.value = true;
};

const openExpertDetail = async (row: ExpertManageVO) => {
  detailScene.value = "expert";
  detailTitle.value = "专家详情";
  currentApplyRow.value = null;
  const res = await getExpertUserDetail(row.userId);
  currentExpertDetail.value = res.data;
  proofGroups.value = parseProofGroups(res.data.proofUrls);
  await loadDetailAvatar(row.userId);
  detailVisible.value = true;
};

const openReviewFromDetail = (status: number) => {
  if (!currentApplyRow.value) return;
  detailVisible.value = false;
  openReview(currentApplyRow.value, status);
};

const submitReview = async () => {
  if (reviewForm.status === 3 && !reviewForm.rejectReason) {
    ElMessage.warning("请输入拒绝原因");
    return;
  }
  await reviewExpertApply(reviewForm);
  reviewVisible.value = false;
  ElMessage.success("审核完成");
  await Promise.all([loadApplies(), loadExpert()]);
};

watch(
  () => [staffQuery.username, staffQuery.nickname, staffQuery.status],
  () => {
    staffQuery.pageNum = 1;
    debouncedLoadStaff();
  }
);

watch(
  () => [customerQuery.username, customerQuery.nickname, customerQuery.status],
  () => {
    customerQuery.pageNum = 1;
    debouncedLoadCustomer();
  }
);

watch(
  () => [expertQuery.realName, expertQuery.organization, expertQuery.expertise, expertQuery.expertStatus],
  () => {
    expertQuery.pageNum = 1;
    debouncedLoadExpert();
  }
);

watch(
  () => [applyQuery.realName, applyQuery.organization, applyQuery.expertise, applyQuery.status],
  () => {
    applyQuery.pageNum = 1;
    debouncedLoadApplies();
  }
);

onMounted(() => {
  loadStaff();
  loadCustomer();
  loadExpert();
  loadApplies();
});
</script>

<style scoped>
.user-tabs {
  margin-top: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.sub-card {
  box-shadow: none;
  border: 1px solid var(--app-border);
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.create-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.avatar-uploader {
  line-height: 1;
}

.clickable-avatar {
  cursor: pointer;
  border: 2px dashed var(--app-border);
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.clickable-avatar:hover {
  transform: translateY(-1px);
  border-color: var(--app-primary);
}

.avatar-tip {
  font-size: 12px;
  color: var(--app-text-muted);
}

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.proof-block h4 {
  margin: 0 0 8px;
}

.proof-group-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.proof-group h5 {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--app-text-muted);
}

.proof-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.proof-item {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 8px;
  min-height: 84px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.proof-image {
  max-width: 100%;
  max-height: 110px;
  object-fit: cover;
  border-radius: 6px;
}

.empty-proof {
  color: var(--app-text-muted);
}

.detail-avatar-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--app-text-muted);
}
</style>
