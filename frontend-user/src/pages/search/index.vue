<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import {
  searchApi,
  type AppSearchHistoryVO,
  type AppSearchHotVO,
  type AppSearchAnswerVO,
  type AppSearchKbVO,
  type AppSearchQuestionVO,
  type AppSearchSimilarQuestionVO
} from "@/api/search";
import { useAuthStore } from "@/stores/auth";
import { openAnswerDetailPage, openExpertPostDetailPage, openQuestionDetail } from "@/utils/nav";
import { BASE_URL } from "@/utils/constants";

type SortKey = "comprehensive" | "latest" | "hot";

const authStore = useAuthStore();
const needLogin = computed(() => !authStore.isLogin);
const query = ref("");
const loading = ref(false);
const searching = ref(false);
const hotList = ref<AppSearchHotVO[]>([]);
const historyList = ref<AppSearchHistoryVO[]>([]);
const questionList = ref<AppSearchQuestionVO[]>([]);
const answerList = ref<AppSearchAnswerVO[]>([]);
const kbList = ref<AppSearchKbVO[]>([]);
const similarQuestionList = ref<AppSearchSimilarQuestionVO[]>([]);
const KB_INIT_LIMIT = 5;
const QUESTION_INIT_LIMIT = 5;
const ANSWER_INIT_LIMIT = 5;
const LOAD_MORE_STEP = 10;
const kbLimit = ref(KB_INIT_LIMIT);
const questionLimit = ref(QUESTION_INIT_LIMIT);
const answerLimit = ref(ANSWER_INIT_LIMIT);
const kbHasMore = ref(false);
const questionHasMore = ref(false);
const answerHasMore = ref(false);
const kbLoading = ref(false);
const questionLoading = ref(false);
const answerLoading = ref(false);
const inResult = ref(false);
const sortBy = ref<SortKey>("comprehensive");
const officialAvatar = `${BASE_URL}/api/common/avatar/staff.png`;

const LOCAL_HISTORY_KEY_PREFIX = "search_history_cache_";

const sortTabs: Array<{ key: SortKey; label: string }> = [
  { key: "comprehensive", label: "综合" },
  { key: "latest", label: "最新" },
  { key: "hot", label: "最热" }
];

const hasResult = computed(
  () => kbList.value.length > 0 || questionList.value.length > 0 || answerList.value.length > 0
);

function localHistoryKey() {
  return `${LOCAL_HISTORY_KEY_PREFIX}${authStore.user?.userId || "guest"}`;
}

function getLocalHistory(): AppSearchHistoryVO[] {
  try {
    const raw = uni.getStorageSync(localHistoryKey());
    if (!raw) return [];
    const list = JSON.parse(raw) as AppSearchHistoryVO[];
    if (!Array.isArray(list)) return [];
    return list
      .filter((item) => item && typeof item.queryText === "string" && item.queryText.trim())
      .slice(0, 20);
  } catch {
    return [];
  }
}

function saveLocalHistory(list: AppSearchHistoryVO[]) {
  try {
    uni.setStorageSync(localHistoryKey(), JSON.stringify((list || []).slice(0, 20)));
  } catch {
    // ignore
  }
}

function appendLocalHistory(queryText: string) {
  const text = (queryText || "").trim();
  if (!text) return;
  const now = new Date().toISOString();
  const current = getLocalHistory().filter((item) => item.queryText !== text);
  current.unshift({ queryText: text, lastTime: now });
  saveLocalHistory(current);
}

function mergeHistory(remote: AppSearchHistoryVO[], local: AppSearchHistoryVO[]) {
  const map = new Map<string, AppSearchHistoryVO>();
  [...remote, ...local].forEach((item) => {
    if (!item?.queryText) return;
    const key = item.queryText.trim();
    if (!key) return;
    const existing = map.get(key);
    if (!existing) {
      map.set(key, { queryText: key, lastTime: item.lastTime || "" });
      return;
    }
    const oldTime = new Date(existing.lastTime || 0).getTime();
    const newTime = new Date(item.lastTime || 0).getTime();
    if (newTime > oldTime) {
      map.set(key, { queryText: key, lastTime: item.lastTime || "" });
    }
  });
  return Array.from(map.values())
    .sort((a, b) => new Date(b.lastTime || 0).getTime() - new Date(a.lastTime || 0).getTime())
    .slice(0, 10);
}

async function loadPanels() {
  loading.value = true;
  try {
    const [hot, history] = await Promise.all([searchApi.hot(10), searchApi.history(10)]);
    hotList.value = hot || [];
    historyList.value = mergeHistory(history || [], getLocalHistory());
  } catch {
    historyList.value = getLocalHistory();
    uni.showToast({ title: "搜索面板加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function fetchKbRows() {
  const text = query.value.trim();
  if (!text) {
    kbList.value = [];
    kbHasMore.value = false;
    return 0;
  }
  kbLoading.value = true;
  try {
    const data = await searchApi.search({
      query: text,
      type: "kb",
      sortBy: sortBy.value,
      page: 1,
      pageSize: kbLimit.value + 1
    });
    const rows = data.kbEntries || [];
    kbList.value = rows.slice(0, kbLimit.value);
    kbHasMore.value = rows.length > kbLimit.value;
    return kbList.value.length;
  } finally {
    kbLoading.value = false;
  }
}

async function fetchQuestionRows() {
  const text = query.value.trim();
  if (!text) {
    questionList.value = [];
    questionHasMore.value = false;
    similarQuestionList.value = [];
    return 0;
  }
  questionLoading.value = true;
  try {
    const data = await searchApi.search({
      query: text,
      type: "question",
      sortBy: sortBy.value,
      page: 1,
      pageSize: questionLimit.value + 1
    });
    const rows = data.questions || [];
    questionList.value = rows.slice(0, questionLimit.value);
    questionHasMore.value = rows.length > questionLimit.value;
    similarQuestionList.value = data.similarQuestions || [];
    return questionList.value.length;
  } finally {
    questionLoading.value = false;
  }
}

async function fetchAnswerRows() {
  const text = query.value.trim();
  if (!text) {
    answerList.value = [];
    answerHasMore.value = false;
    return 0;
  }
  answerLoading.value = true;
  try {
    const data = await searchApi.search({
      query: text,
      type: "answer",
      sortBy: sortBy.value,
      page: 1,
      pageSize: answerLimit.value + 1
    });
    const rows = data.answers || [];
    answerList.value = rows.slice(0, answerLimit.value);
    answerHasMore.value = rows.length > answerLimit.value;
    return answerList.value.length;
  } finally {
    answerLoading.value = false;
  }
}

async function search(reset = true) {
  const text = query.value.trim();
  query.value = text;
  if (!text || searching.value) return;

  if (reset) {
    kbLimit.value = KB_INIT_LIMIT;
    questionLimit.value = QUESTION_INIT_LIMIT;
    answerLimit.value = ANSWER_INIT_LIMIT;
    questionList.value = [];
    answerList.value = [];
    kbList.value = [];
    similarQuestionList.value = [];
    kbHasMore.value = false;
    questionHasMore.value = false;
    answerHasMore.value = false;
    inResult.value = true;
  }

  searching.value = true;
  try {
    const [questionHit, answerHit, kbHit] = await Promise.all([
      fetchQuestionRows(),
      fetchAnswerRows(),
      fetchKbRows()
    ]);

    if (reset) {
      appendLocalHistory(text);
      historyList.value = mergeHistory(historyList.value, getLocalHistory());
      try {
        await searchApi.logSearch({
          queryText: text,
          searchType: 1,
          hitCount: questionHit + answerHit + kbHit
        });
        await loadPanels();
      } catch {
        // ignore search log failure
      }
    }
  } catch {
    uni.showToast({ title: "搜索失败", icon: "none" });
  } finally {
    searching.value = false;
  }
}

async function clearHistory() {
  try {
    await searchApi.clearHistory();
  } catch {
    // ignore backend clear failure, still clear local cache for UX
  }
  historyList.value = [];
  saveLocalHistory([]);
}

function chooseWord(word: string) {
  query.value = word;
  search(true);
}

function switchSort(key: SortKey) {
  if (sortBy.value === key) return;
  sortBy.value = key;
  if (inResult.value && query.value.trim()) {
    search(true);
  }
}

function resetPanel() {
  inResult.value = false;
  questionList.value = [];
  answerList.value = [];
  kbList.value = [];
  similarQuestionList.value = [];
  kbLimit.value = KB_INIT_LIMIT;
  questionLimit.value = QUESTION_INIT_LIMIT;
  answerLimit.value = ANSWER_INIT_LIMIT;
  kbHasMore.value = false;
  questionHasMore.value = false;
  answerHasMore.value = false;
}

function isOfficialKb(item: AppSearchKbVO) {
  return item?.source !== "expert_post";
}

function openKb(item: AppSearchKbVO) {
  if (!item?.id) return;
  openExpertPostDetailPage(item.id);
}

function openAnswerQuestion(questionId?: number) {
  if (!questionId) return;
  openQuestionDetail(questionId);
}

async function loadMoreKb() {
  if (!kbHasMore.value || kbLoading.value) return;
  kbLimit.value += LOAD_MORE_STEP;
  try {
    await fetchKbRows();
  } catch {
    uni.showToast({ title: "加载更多失败", icon: "none" });
  }
}

async function loadMoreQuestions() {
  if (!questionHasMore.value || questionLoading.value) return;
  questionLimit.value += LOAD_MORE_STEP;
  try {
    await fetchQuestionRows();
  } catch {
    uni.showToast({ title: "加载更多失败", icon: "none" });
  }
}

async function loadMoreAnswers() {
  if (!answerHasMore.value || answerLoading.value) return;
  answerLimit.value += LOAD_MORE_STEP;
  try {
    await fetchAnswerRows();
  } catch {
    uni.showToast({ title: "加载更多失败", icon: "none" });
  }
}

function goLogin() {
  uni.navigateTo({
    url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/search/index")}`
  });
}

onShow(async () => {
  if (needLogin.value) return;
  if (inResult.value && query.value.trim()) {
    await search(true);
  } else {
    await loadPanels();
  }
});
</script>

<template>
  <view class="page">
    <view v-if="needLogin" class="auth-card app-card">
      <view class="auth-title">登录后可查看搜索内容</view>
      <view class="auth-sub">历史记录、热门搜索和搜索结果仅登录后可用</view>
      <button class="auth-btn" @click="goLogin">去登录</button>
    </view>

    <template v-else>
      <view class="search-line">
        <input
          v-model="query"
          class="search-input"
          type="text"
          placeholder="输入症状、疾病或科普关键词..."
          confirm-type="search"
          @confirm="search(true)"
        />
        <button class="search-btn" @click="search(true)">搜索</button>
      </view>

      <view v-if="inResult" class="panel">
        <view class="sort-tabs">
          <view
            v-for="tab in sortTabs"
            :key="tab.key"
            class="sort-tab"
            :class="{ active: sortBy === tab.key }"
            @click="switchSort(tab.key)"
          >
            {{ tab.label }}
          </view>
          <view class="back-link" @click="resetPanel">返回面板</view>
        </view>

        <view v-if="searching && !hasResult" class="state">搜索中...</view>
        <view v-else-if="!hasResult" class="state">没有匹配内容</view>
        <view v-else class="result-body">
          <view v-if="kbList.length" class="result-section section-kb">
            <view class="section-title">科普文章</view>
            <view
              v-for="item in kbList"
              :key="item.id"
              class="app-card result-item"
              @click="openKb(item)"
            >
              <view v-if="isOfficialKb(item)" class="kb-author-row">
                <image class="kb-author-avatar" :src="officialAvatar" mode="aspectFill" />
                <view class="kb-author-name">问问官方</view>
                <view class="kb-official">官方</view>
              </view>
              <view class="result-title" v-html="item.titleHighlight || item.title"></view>
              <view class="result-desc" v-html="item.summaryHighlight || item.summary || 'No summary'"></view>
              <view class="meta">
                <text>{{ item.viewCount || 0 }}浏览</text>
                <text>{{ item.likeCount || 0 }}点赞</text>
              </view>
            </view>
            <view v-if="kbHasMore" class="module-more" @click="loadMoreKb">
              {{ kbLoading ? "加载中..." : "展示更多" }}
            </view>
          </view>

          <view v-if="questionList.length" class="result-section section-question">
            <view class="section-title">问题结果</view>
            <view
              v-for="item in questionList"
              :key="item.id"
              class="app-card result-item"
              @click="openQuestionDetail(item.id)"
            >
              <view class="result-title" v-html="item.titleHighlight || item.title"></view>
              <view class="result-desc" v-html="item.summaryHighlight || item.summary || 'No summary'"></view>
              <view class="meta">
                <text>{{ item.answerCount || 0 }}回答</text>
                <text>{{ item.viewCount || 0 }}浏览</text>
                <text>{{ item.likeCount || 0 }}点赞</text>
              </view>
            </view>
            <view v-if="questionHasMore" class="module-more" @click="loadMoreQuestions">
              {{ questionLoading ? "加载中..." : "展示更多" }}
            </view>
          </view>


          <view v-if="similarQuestionList.length" class="result-section section-similar">
            <view class="section-title">相似问题推荐</view>
            <view class="chips">
              <view
                v-for="item in similarQuestionList"
                :key="item.id"
                class="app-chip similar-chip"
                @click="openQuestionDetail(item.id)"
                v-html="item.titleHighlight || item.title"
              ></view>
            </view>
          </view>
          <view v-if="answerList.length" class="result-section section-answer">
            <view class="section-title">回答结果</view>
            <view
              v-for="item in answerList"
              :key="item.answerId"
              class="app-card result-item"
              @click="openAnswerDetailPage(item.answerId)"
            >
              <view class="result-title" @click.stop="openAnswerQuestion(item.questionId)" v-html="item.questionTitleHighlight || item.questionTitle"></view>
              <view class="result-desc" v-html="item.contentPreviewHighlight || item.contentPreview || 'No answer content'"></view>
              <view class="meta">
                <text>{{ item.likeCount || 0 }}点赞</text>
              </view>
            </view>
            <view v-if="answerHasMore" class="module-more" @click="loadMoreAnswers">
              {{ answerLoading ? "加载中..." : "展示更多" }}
            </view>
          </view>
        </view>
      </view>

      <view v-else class="panel">
        <view class="head">
          <text class="title">搜索历史</text>
          <text class="action" @click="clearHistory">清空</text>
        </view>
        <view class="chips">
          <text v-for="item in historyList" :key="item.queryText" class="app-chip" @click="chooseWord(item.queryText)">
            {{ item.queryText }}
          </text>
        </view>

        <view class="head second">
          <text class="title">热门搜索</text>
        </view>
        <view class="chips">
          <text v-for="item in hotList" :key="item.queryText" class="app-chip hot" @click="chooseWord(item.queryText)">
            {{ item.queryText }}
          </text>
        </view>
      </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 24rpx;
  min-height: 100vh;
  box-sizing: border-box;
}

.auth-card {
  padding: 30rpx 24rpx;
  margin-top: 0;
}

.auth-title {
  font-size: 34rpx;
  font-weight: 700;
}

.auth-sub {
  margin-top: 10rpx;
  color: #8ba0b3;
}

.auth-btn {
  margin-top: 18rpx;
  border: none;
  border-radius: 16rpx;
  background: #4ba7d9;
  color: #fff;
}

.page > .auth-card {
  position: relative;
  top: 40vh;
  transform: translateY(-50%);
}

.search-line {
  display: flex;
  gap: 14rpx;
}

.search-input {
  flex: 1;
  height: 76rpx;
  background: #fff;
  border: 2rpx solid #9bd1ef;
  border-radius: 20rpx;
  padding: 0 24rpx;
}

.search-btn {
  width: 140rpx;
  margin: 0;
  border: none;
  background: #4ba7d9;
  color: #fff;
  border-radius: 20rpx;
  font-size: 28rpx;
}

.panel {
  margin-top: 24rpx;
}

.sort-tabs {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.sort-tab {
  padding: 8rpx 16rpx;
  border-radius: 18rpx;
  border: 2rpx solid #d2deea;
  color: #7f94a8;
}

.sort-tab.active {
  border-color: #4ba7d9;
  color: #2d89be;
  background: #eaf6fd;
}

.back-link {
  margin-left: auto;
  color: #8ba0b3;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.second {
  margin-top: 24rpx;
}

.title {
  font-size: 34rpx;
  font-weight: 700;
}

.action {
  color: #8ba0b3;
}

.chips {
  margin-top: 14rpx;
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.hot {
  background: #f3ecd1;
}

.similar-chip {
  display: inline-block;
}

.result-body {
  display: flex;
  flex-direction: column;
}

.result-section {
  margin-bottom: 20rpx;
}

.section-kb {
  order: 1;
}

.section-question {
  order: 2;
}

.section-similar {
  order: 3;
}

.section-answer {
  order: 4;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.kb-author-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 8rpx;
}

.kb-author-avatar {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #eef3f8;
}

.kb-author-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #2a3340;
}

.kb-official {
  padding: 2rpx 10rpx;
  border-radius: 999rpx;
  border: 2rpx solid #3fa2d8;
  color: #2d89be;
  font-size: 22rpx;
}

.result-item {
  padding: 22rpx;
  margin-top: 14rpx;
}

.result-title {
  font-size: 30rpx;
  font-weight: 700;
}

.result-desc {
  color: #5f7488;
  margin-top: 10rpx;
}

:deep(.search-hit) {
  color: #d9480f;
  font-style: normal;
  font-weight: 700;
}

.meta {
  margin-top: 12rpx;
  display: flex;
  gap: 18rpx;
  color: #8ba0b3;
}

.module-more {
  margin-top: 10rpx;
  color: #4ba7d9;
  text-align: center;
  padding: 8rpx 0;
}

.state {
  color: #8ba0b3;
  text-align: center;
  padding: 32rpx 0;
}

.state.inline {
  padding: 10rpx 0 18rpx;
}

.state.load-more {
  padding-top: 18rpx;
}
</style>
