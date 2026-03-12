<template>
  <el-card>
    <h2>问答管理</h2>
    <p>管理员可对全量问题与回答进行检索、上下架和删除操作。</p>

    <el-tabs v-model="activeTab" class="qa-tabs">
      <el-tab-pane label="问题管理" name="question">
        <div class="toolbar">
          <el-form :inline="true" :model="questionQuery" class="toolbar-form">
            <el-form-item label="关键词">
              <el-input
                v-model="questionQuery.keyword"
                clearable
                placeholder="标题/内容/作者"
                @keyup.enter="loadQuestionData"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="questionQuery.status" clearable placeholder="全部状态" style="width: 130px">
                <el-option label="已发布" :value="1" />
                <el-option label="待审核" :value="2" />
                <el-option label="驳回" :value="3" />
                <el-option label="下架" :value="4" />
                <el-option label="用户删除" :value="6" />
              </el-select>
            </el-form-item>
            <el-form-item label="删除标记">
              <el-select v-model="questionQuery.deleteFlag" clearable placeholder="默认未删除" style="width: 130px">
                <el-option label="未删除" :value="0" />
                <el-option label="已删除" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item label="分类ID">
              <el-input-number v-model="questionQuery.categoryId" :min="1" :controls="false" placeholder="分类ID" />
            </el-form-item>
            <el-form-item label="专题ID">
              <el-input-number v-model="questionQuery.topicId" :min="1" :controls="false" placeholder="专题ID" />
            </el-form-item>
            <el-form-item label="作者ID">
              <el-input-number v-model="questionQuery.userId" :min="1" :controls="false" placeholder="作者ID" />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="questionDateRange"
                type="datetimerange"
                value-format="YYYY-MM-DDTHH:mm:ss"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
              />
            </el-form-item>
            <el-form-item label="排序字段">
              <el-select v-model="questionQuery.sortBy" clearable placeholder="默认" style="width: 140px">
                <el-option label="创建时间" value="createdAt" />
                <el-option label="更新时间" value="updatedAt" />
                <el-option label="回答数" value="answerCount" />
                <el-option label="浏览数" value="viewCount" />
                <el-option label="点赞数" value="likeCount" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-select v-model="questionQuery.sortOrder" clearable placeholder="默认" style="width: 120px">
                <el-option label="升序" value="asc" />
                <el-option label="降序" value="desc" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadQuestionData">查询</el-button>
              <el-button @click="resetQuestionQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table :data="questionRows" v-loading="questionLoading" style="width: 100%">
          <el-table-column prop="id" label="问题ID" width="90" />
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="summary" label="摘要" min-width="260" show-overflow-tooltip />
          <el-table-column label="分类" width="130" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.categoryName || (scope.row.categoryId ? `#${scope.row.categoryId}` : "-") }}
            </template>
          </el-table-column>
          <el-table-column label="专题" width="130" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.topicTitle || (scope.row.topicId ? `#${scope.row.topicId}` : "-") }}
            </template>
          </el-table-column>
          <el-table-column label="作者" width="140" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.authorName || (scope.row.authorId ? `#${scope.row.authorId}` : "-") }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="scope">
              <el-tag :type="questionStatusTagType(scope.row.status)">
                {{ questionStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="删除" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.deleteFlag === 1 ? 'danger' : 'success'">
                {{ scope.row.deleteFlag === 1 ? "已删" : "正常" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="回答" width="80">
            <template #default="scope">{{ scope.row.answerCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="浏览" width="80">
            <template #default="scope">{{ scope.row.viewCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="点赞" width="80">
            <template #default="scope">{{ scope.row.likeCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="176">
            <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="176">
            <template #default="scope">{{ formatDateTime(scope.row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button
                v-if="scope.row.deleteFlag !== 1 && scope.row.status !== 1"
                size="small"
                type="success"
                @click="changeQuestionStatus(scope.row, 1)"
              >
                发布
              </el-button>
              <el-button
                v-if="scope.row.deleteFlag !== 1 && scope.row.status === 1"
                size="small"
                type="warning"
                @click="changeQuestionStatus(scope.row, 4)"
              >
                下架
              </el-button>
              <el-button
                v-if="scope.row.deleteFlag !== 1"
                size="small"
                type="danger"
                plain
                @click="removeQuestion(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            :current-page="questionQuery.page"
            :page-size="questionQuery.pageSize"
            :page-sizes="pageSizes"
            :total="questionTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="handleQuestionSizeChange"
            @current-change="handleQuestionPage"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="回答管理" name="answer">
        <div class="toolbar">
          <el-form :inline="true" :model="answerQuery" class="toolbar-form">
            <el-form-item label="关键词">
              <el-input
                v-model="answerQuery.keyword"
                clearable
                placeholder="回答内容/问题标题/作者"
                @keyup.enter="loadAnswerData"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="answerQuery.status" clearable placeholder="全部状态" style="width: 130px">
                <el-option label="已发布" :value="1" />
                <el-option label="待审核" :value="2" />
                <el-option label="驳回" :value="3" />
                <el-option label="下架" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="删除标记">
              <el-select v-model="answerQuery.deleteFlag" clearable placeholder="默认未删除" style="width: 130px">
                <el-option label="未删除" :value="0" />
                <el-option label="已删除" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item label="问题ID">
              <el-input-number v-model="answerQuery.questionId" :min="1" :controls="false" placeholder="问题ID" />
            </el-form-item>
            <el-form-item label="作者ID">
              <el-input-number v-model="answerQuery.userId" :min="1" :controls="false" placeholder="作者ID" />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="answerDateRange"
                type="datetimerange"
                value-format="YYYY-MM-DDTHH:mm:ss"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
              />
            </el-form-item>
            <el-form-item label="排序字段">
              <el-select v-model="answerQuery.sortBy" clearable placeholder="默认" style="width: 140px">
                <el-option label="创建时间" value="createdAt" />
                <el-option label="更新时间" value="updatedAt" />
                <el-option label="点赞数" value="likeCount" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-select v-model="answerQuery.sortOrder" clearable placeholder="默认" style="width: 120px">
                <el-option label="升序" value="asc" />
                <el-option label="降序" value="desc" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAnswerData">查询</el-button>
              <el-button @click="resetAnswerQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table :data="answerRows" v-loading="answerLoading" style="width: 100%">
          <el-table-column prop="id" label="回答ID" width="90" />
          <el-table-column prop="questionTitle" label="所属问题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="contentPreview" label="回答摘要" min-width="300" show-overflow-tooltip />
          <el-table-column label="回答作者" width="140" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.authorName || (scope.row.authorId ? `#${scope.row.authorId}` : "-") }}
            </template>
          </el-table-column>
          <el-table-column label="提问者" width="140" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.questionAuthorName || (scope.row.questionAuthorId ? `#${scope.row.questionAuthorId}` : "-") }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="scope">
              <el-tag :type="answerStatusTagType(scope.row.status)">
                {{ answerStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="删除" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.deleteFlag === 1 ? 'danger' : 'success'">
                {{ scope.row.deleteFlag === 1 ? "已删" : "正常" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="点赞" width="80">
            <template #default="scope">{{ scope.row.likeCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="176">
            <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="176">
            <template #default="scope">{{ formatDateTime(scope.row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button
                v-if="scope.row.deleteFlag !== 1 && scope.row.status !== 1"
                size="small"
                type="success"
                @click="changeAnswerStatus(scope.row, 1)"
              >
                发布
              </el-button>
              <el-button
                v-if="scope.row.deleteFlag !== 1 && scope.row.status === 1"
                size="small"
                type="warning"
                @click="changeAnswerStatus(scope.row, 4)"
              >
                下架
              </el-button>
              <el-button
                v-if="scope.row.deleteFlag !== 1"
                size="small"
                type="danger"
                plain
                @click="removeAnswer(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            :current-page="answerQuery.page"
            :page-size="answerQuery.pageSize"
            :page-sizes="pageSizes"
            :total="answerTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="handleAnswerSizeChange"
            @current-change="handleAnswerPage"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  deleteQaManageAnswer,
  deleteQaManageQuestion,
  pageQaManageAnswers,
  pageQaManageQuestions,
  updateQaManageAnswerStatus,
  updateQaManageQuestionStatus
} from "../../api/qa";
import type {
  AdminQaAnswerPageItemVO,
  AdminQaQuestionPageItemVO,
  QaManageAnswerPageQueryDTO,
  QaManageQuestionPageQueryDTO
} from "../../types/qa";

const activeTab = ref<"question" | "answer">("question");
const pageSizes = [10, 20, 50, 100];

const createQuestionQuery = (): QaManageQuestionPageQueryDTO => ({
  page: 1,
  pageSize: 10,
  keyword: "",
  status: null,
  deleteFlag: 0,
  categoryId: null,
  topicId: null,
  userId: null,
  sortBy: "createdAt",
  sortOrder: "desc"
});

const createAnswerQuery = (): QaManageAnswerPageQueryDTO => ({
  page: 1,
  pageSize: 10,
  keyword: "",
  status: null,
  deleteFlag: 0,
  questionId: null,
  userId: null,
  sortBy: "createdAt",
  sortOrder: "desc"
});

const questionQuery = reactive<QaManageQuestionPageQueryDTO>(createQuestionQuery());
const questionDateRange = ref<string[]>([]);
const questionRows = ref<AdminQaQuestionPageItemVO[]>([]);
const questionTotal = ref(0);
const questionLoading = ref(false);

const answerQuery = reactive<QaManageAnswerPageQueryDTO>(createAnswerQuery());
const answerDateRange = ref<string[]>([]);
const answerRows = ref<AdminQaAnswerPageItemVO[]>([]);
const answerTotal = ref(0);
const answerLoading = ref(false);

const questionStatusText = (status?: number) => {
  if (status === 1) return "已发布";
  if (status === 2) return "待审核";
  if (status === 3) return "驳回";
  if (status === 4) return "下架";
  if (status === 5) return "仅自己可见";
  if (status === 6) return "用户删除";
  return status == null ? "-" : String(status);
};

const questionStatusTagType = (status?: number) => {
  if (status === 1) return "success";
  if (status === 2) return "warning";
  if (status === 3) return "danger";
  if (status === 4) return "info";
  if (status === 5) return "info";
  if (status === 6) return "danger";
  return "info";
};

const answerStatusText = (status?: number) => {
  if (status === 1) return "已发布";
  if (status === 2) return "待审核";
  if (status === 3) return "驳回";
  if (status === 4) return "下架";
  if (status === 0) return "删除";
  return status == null ? "-" : String(status);
};

const answerStatusTagType = (status?: number) => {
  if (status === 1) return "success";
  if (status === 2) return "warning";
  if (status === 3) return "danger";
  if (status === 4) return "info";
  if (status === 0) return "danger";
  return "info";
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

const buildQuestionQuery = () => {
  const [startTime, endTime] = questionDateRange.value || [];
  return {
    ...questionQuery,
    keyword: questionQuery.keyword?.trim() || undefined,
    status: questionQuery.status ?? undefined,
    deleteFlag: questionQuery.deleteFlag ?? undefined,
    categoryId: questionQuery.categoryId ?? undefined,
    topicId: questionQuery.topicId ?? undefined,
    userId: questionQuery.userId ?? undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
    sortBy: questionQuery.sortBy || undefined,
    sortOrder: questionQuery.sortOrder || undefined
  };
};

const buildAnswerQuery = () => {
  const [startTime, endTime] = answerDateRange.value || [];
  return {
    ...answerQuery,
    keyword: answerQuery.keyword?.trim() || undefined,
    status: answerQuery.status ?? undefined,
    deleteFlag: answerQuery.deleteFlag ?? undefined,
    questionId: answerQuery.questionId ?? undefined,
    userId: answerQuery.userId ?? undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
    sortBy: answerQuery.sortBy || undefined,
    sortOrder: answerQuery.sortOrder || undefined
  };
};

const loadQuestionData = async () => {
  questionLoading.value = true;
  try {
    const res = await pageQaManageQuestions(buildQuestionQuery());
    questionRows.value = res.data.list || [];
    questionTotal.value = res.data.total || 0;
  } finally {
    questionLoading.value = false;
  }
};

const loadAnswerData = async () => {
  answerLoading.value = true;
  try {
    const res = await pageQaManageAnswers(buildAnswerQuery());
    answerRows.value = res.data.list || [];
    answerTotal.value = res.data.total || 0;
  } finally {
    answerLoading.value = false;
  }
};

const resetQuestionQuery = () => {
  Object.assign(questionQuery, createQuestionQuery());
  questionDateRange.value = [];
  loadQuestionData();
};

const resetAnswerQuery = () => {
  Object.assign(answerQuery, createAnswerQuery());
  answerDateRange.value = [];
  loadAnswerData();
};

const handleQuestionPage = (page: number) => {
  questionQuery.page = page;
  loadQuestionData();
};

const handleQuestionSizeChange = (size: number) => {
  questionQuery.pageSize = size;
  questionQuery.page = 1;
  loadQuestionData();
};

const handleAnswerPage = (page: number) => {
  answerQuery.page = page;
  loadAnswerData();
};

const handleAnswerSizeChange = (size: number) => {
  answerQuery.pageSize = size;
  answerQuery.page = 1;
  loadAnswerData();
};

const changeQuestionStatus = async (row: AdminQaQuestionPageItemVO, status: 1 | 4) => {
  if (!row.id) return;
  const actionText = status === 1 ? "发布" : "下架";
  await ElMessageBox.confirm(`确认${actionText}该问题吗？`, "提示", { type: "warning" });
  await updateQaManageQuestionStatus(row.id, { status });
  ElMessage.success(`${actionText}成功`);
  await loadQuestionData();
};

const changeAnswerStatus = async (row: AdminQaAnswerPageItemVO, status: 1 | 4) => {
  if (!row.id) return;
  const actionText = status === 1 ? "发布" : "下架";
  await ElMessageBox.confirm(`确认${actionText}该回答吗？`, "提示", { type: "warning" });
  await updateQaManageAnswerStatus(row.id, { status });
  ElMessage.success(`${actionText}成功`);
  await loadAnswerData();
};

const removeQuestion = async (row: AdminQaQuestionPageItemVO) => {
  if (!row.id) return;
  await ElMessageBox.confirm("确认删除该问题吗？删除后将不可在前台展示。", "提示", { type: "warning" });
  await deleteQaManageQuestion(row.id);
  ElMessage.success("删除成功");
  await loadQuestionData();
};

const removeAnswer = async (row: AdminQaAnswerPageItemVO) => {
  if (!row.id) return;
  await ElMessageBox.confirm("确认删除该回答吗？删除后将不可在前台展示。", "提示", { type: "warning" });
  await deleteQaManageAnswer(row.id);
  ElMessage.success("删除成功");
  await loadAnswerData();
};

onMounted(async () => {
  await Promise.all([loadQuestionData(), loadAnswerData()]);
});
</script>

<style scoped>
.qa-tabs {
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
