<script setup lang="ts">
import { computed, ref } from "vue";
import { onReachBottom, onShow } from "@dcloudio/uni-app";
import { discoverApi, type AppCategoryVO, type AppDiscoverQuestionItemVO } from "@/api/discover";
import { expertApi, type AppExpertPostCategoryVO, type AppExpertPostItemVO } from "@/api/expert";
import { useAuthStore } from "@/stores/auth";
import { openAskPage, openExpertPostCreatePage, openExpertPostDetailPage, openQuestionDetail } from "@/utils/nav";
import { meApi } from "@/api/me";
import { BASE_URL } from "@/utils/constants";

type ZoneKey = "science" | "qa";
type SortKey = "hot" | "latest";

const authStore = useAuthStore();
const isExpertUser = ref(false);

const activeZone = ref<ZoneKey>("science");
const loading = ref(false);
const errorText = ref("");

const scienceCategories = ref<AppExpertPostCategoryVO[]>([]);
const scienceSelectedCategoryId = ref<number | undefined>(undefined);
const scienceSortBy = ref<SortKey>("hot");
const sciencePage = ref(1);
const sciencePageSize = 10;
const scienceTotal = ref(0);
const scienceLoading = ref(false);
const scienceHasMore = ref(true);
const sciencePosts = ref<AppExpertPostItemVO[]>([]);

const qaCategories = ref<AppCategoryVO[]>([]);
const qaSelectedCategoryId = ref<number | undefined>(undefined);
const qaSortBy = ref<SortKey>("hot");
const qaPage = ref(1);
const qaPageSize = 10;
const qaTotal = ref(0);
const qaLoading = ref(false);
const qaHasMore = ref(true);
const qaQuestions = ref<AppDiscoverQuestionItemVO[]>([]);

const sortTabs: Array<{ key: SortKey; label: string }> = [
  { key: "hot", label: "推荐" },
  { key: "latest", label: "最新" }
];

const zoneTitle = computed(() => (activeZone.value === "science" ? "推荐文章" : "推荐问答"));
const officialAvatar = `${BASE_URL}/api/common/avatar/staff.png`;

async function loadHomeBase() {
  loading.value = true;
  errorText.value = "";
  try {
    const [kbCategories, home] = await Promise.all([expertApi.categories(), discoverApi.getHome()]);
    scienceCategories.value = kbCategories || [];
    qaCategories.value = home.categories || [];
  } catch {
    errorText.value = "首页加载失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}

async function refreshExpertStatus() {
  if (!authStore.isLogin) {
    isExpertUser.value = false;
    return;
  }
  try {
    const info = await meApi.overview();
    isExpertUser.value = info?.expertStatus === 3;
  } catch {
    isExpertUser.value = false;
  }
}

async function loadSciencePosts(reset = false) {
  if (scienceLoading.value || (!reset && !scienceHasMore.value)) return;
  if (reset) {
    sciencePage.value = 1;
    scienceTotal.value = 0;
    scienceHasMore.value = true;
  }
  scienceLoading.value = true;
  try {
    const data = await expertApi.page({
      page: sciencePage.value,
      pageSize: sciencePageSize,
      sortBy: scienceSortBy.value,
      categoryId: scienceSelectedCategoryId.value
    });
    const rows = data.list || [];
    sciencePosts.value = reset ? rows : [...sciencePosts.value, ...rows];
    scienceTotal.value = Number(data.total || 0);
    scienceHasMore.value = sciencePosts.value.length < scienceTotal.value;
    if (rows.length > 0) sciencePage.value += 1;
  } catch {
    uni.showToast({ title: "科普文章加载失败", icon: "none" });
  } finally {
    scienceLoading.value = false;
  }
}

async function loadQaQuestions(reset = false) {
  if (qaLoading.value || (!reset && !qaHasMore.value)) return;
  if (reset) {
    qaPage.value = 1;
    qaTotal.value = 0;
    qaHasMore.value = true;
  }
  qaLoading.value = true;
  try {
    const data = await discoverApi.getQuestionPage({
      page: qaPage.value,
      pageSize: qaPageSize,
      sortBy: qaSortBy.value,
      categoryId: qaSelectedCategoryId.value
    });
    const rows = data.list || [];
    qaQuestions.value = reset ? rows : [...qaQuestions.value, ...rows];
    qaTotal.value = Number(data.total || 0);
    qaHasMore.value = qaQuestions.value.length < qaTotal.value;
    if (rows.length > 0) qaPage.value += 1;
  } catch {
    uni.showToast({ title: "问答加载失败", icon: "none" });
  } finally {
    qaLoading.value = false;
  }
}

function goSearch() {
  uni.switchTab({ url: "/pages/search/index" });
}

function switchZone(zone: ZoneKey) {
  if (activeZone.value === zone) return;
  activeZone.value = zone;
  if (zone === "science" && sciencePosts.value.length === 0) {
    loadSciencePosts(true);
  }
  if (zone === "qa" && qaQuestions.value.length === 0) {
    loadQaQuestions(true);
  }
}

function switchSort(key: SortKey) {
  if (activeZone.value === "science") {
    if (scienceSortBy.value === key) return;
    scienceSortBy.value = key;
    loadSciencePosts(true);
    return;
  }
  if (qaSortBy.value === key) return;
  qaSortBy.value = key;
  loadQaQuestions(true);
}

function selectScienceCategory(item?: AppExpertPostCategoryVO) {
  const next = item?.id;
  if (scienceSelectedCategoryId.value === next) return;
  scienceSelectedCategoryId.value = next;
  loadSciencePosts(true);
}

function selectQaCategory(item?: AppCategoryVO) {
  const next = item?.id;
  if (qaSelectedCategoryId.value === next) return;
  qaSelectedCategoryId.value = next;
  loadQaQuestions(true);
}

function resolveAvatar(avatar?: string | null) {
  if (!avatar) return "";
  const raw = String(avatar).trim();
  if (!raw) return "";
  if (raw.startsWith("http://") || raw.startsWith("https://")) return raw;
  return `${BASE_URL}${raw}`;
}

function isOfficialPost(item: AppExpertPostItemVO) {
  return item?.source !== "expert_post";
}

function postAuthorName(item: AppExpertPostItemVO) {
  if (isOfficialPost(item)) return "问问官方";
  return item.authorName || "认证专家";
}

function postAuthorAvatar(item: AppExpertPostItemVO) {
  if (isOfficialPost(item)) return officialAvatar;
  return resolveAvatar(item.authorAvatar || "");
}

function openScienceDetail(item: AppExpertPostItemVO) {
  if (!item?.id) return;
  openExpertPostDetailPage(item.id);
}

function handleFloatingAction() {
  if (isExpertUser.value) {
    uni.showActionSheet({
      itemList: ["发科普帖子", "发起提问"],
      success: (res) => {
        if (res.tapIndex === 0) {
          openExpertPostCreatePage();
          return;
        }
        if (res.tapIndex === 1) {
          openAskPage();
        }
      }
    });
    return;
  }
  openAskPage();
}

onShow(async () => {
  await Promise.all([loadHomeBase(), refreshExpertStatus()]);
  if (activeZone.value === "science") {
    await loadSciencePosts(true);
    return;
  }
  await loadQaQuestions(true);
});

onReachBottom(() => {
  if (activeZone.value === "science") {
    loadSciencePosts(false);
    return;
  }
  loadQaQuestions(false);
});
</script>

<template>
  <view class="page">
    <view class="search-bar" @click="goSearch">搜索健康问题、科普关键词...</view>

    <view class="zone-switch">
      <view class="zone-item" :class="{ active: activeZone === 'science' }" @click="switchZone('science')">健康小科普</view>
      <view class="split-line" />
      <view class="zone-item" :class="{ active: activeZone === 'qa' }" @click="switchZone('qa')">问答专区</view>
    </view>

    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="errorText" class="state error">{{ errorText }}</view>

    <template v-else>
      <view class="section">
        <view class="section-title">分类浏览</view>
        <view class="chips" v-if="activeZone === 'science'">
          <view class="app-chip category-chip" :class="{ active: scienceSelectedCategoryId == null }" @click="selectScienceCategory(undefined)">全部</view>
          <view
            v-for="item in scienceCategories"
            :key="item.id"
            class="app-chip category-chip"
            :class="{ active: scienceSelectedCategoryId === item.id }"
            @click="selectScienceCategory(item)"
          >
            {{ item.name }}
          </view>
        </view>
        <view class="chips" v-else>
          <view class="app-chip category-chip" :class="{ active: qaSelectedCategoryId == null }" @click="selectQaCategory(undefined)">全部</view>
          <view
            v-for="item in qaCategories"
            :key="item.id"
            class="app-chip category-chip"
            :class="{ active: qaSelectedCategoryId === item.id }"
            @click="selectQaCategory(item)"
          >
            {{ item.name }}
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-head">
          <text class="section-title">{{ zoneTitle }}</text>
          <view class="sort-tabs">
            <view
              v-for="tab in sortTabs"
              :key="tab.key"
              class="sort-tab"
              :class="{ active: (activeZone === 'science' ? scienceSortBy : qaSortBy) === tab.key }"
              @click="switchSort(tab.key)"
            >
              {{ tab.label }}
            </view>
          </view>
        </view>

        <template v-if="activeZone === 'science'">
          <view v-if="!sciencePosts.length && scienceLoading" class="state">加载中...</view>
          <view v-else-if="!sciencePosts.length" class="state">暂无文章</view>
          <view v-else>
            <view
              v-for="item in sciencePosts"
              :key="item.id"
              class="app-card post-card"
              @click="openScienceDetail(item)"
            >
              <view class="post-main">
                <view class="post-author-row">
                  <image
                    v-if="postAuthorAvatar(item)"
                    class="post-author-avatar"
                    :src="postAuthorAvatar(item)"
                    mode="aspectFill"
                  />
                  <view v-else class="post-author-avatar post-author-avatar--fallback">问</view>
                  <view class="post-author-name">{{ postAuthorName(item) }}</view>
                  <view v-if="isOfficialPost(item)" class="post-official">官方</view>
                </view>
                <view class="post-title">{{ item.title }}</view>
                <view class="post-summary">{{ item.summary || '暂无摘要' }}</view>
                <view class="meta">
                  <text>{{ item.likeCount || 0 }}点赞</text>
                  <text>{{ item.viewCount || 0 }}浏览</text>
                  <text>{{ item.createdAt || '' }}</text>
                </view>
              </view>
              <image v-if="item.coverImage" class="post-cover" :src="item.coverImage" mode="aspectFill" />
            </view>
            <view class="state load-more">{{ scienceHasMore ? '上拉加载更多' : '没有更多了' }}</view>
          </view>
        </template>

        <template v-else>
          <view v-if="!qaQuestions.length && qaLoading" class="state">加载中...</view>
          <view v-else-if="!qaQuestions.length" class="state">暂无问答</view>
          <view v-else>
            <view v-for="item in qaQuestions" :key="item.id" class="app-card question-card" @click="openQuestionDetail(item.id)">
              <view class="author-row">
                <view class="author-name">{{ item.authorName || '匿名用户' }}</view>
                <view class="meta-minor">{{ item.createdAt || '' }}</view>
              </view>
              <view class="question-title">{{ item.title }}</view>
              <view class="meta">
                <text>{{ item.answerCount || 0 }}回答</text>
                <text>{{ item.viewCount || 0 }}浏览</text>
                <text>{{ item.likeCount || 0 }}点赞</text>
              </view>
            </view>
            <view class="state load-more">{{ qaHasMore ? '上拉加载更多' : '没有更多了' }}</view>
          </view>
        </template>
      </view>
    </template>

    <view class="floating-ask" @click="handleFloatingAction">
      <text class="plus">+</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 24rpx;
}

.search-bar {
  background: #fff;
  border: 2rpx solid #9bd1ef;
  color: #8ba0b3;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
}

.zone-switch {
  margin-top: 20rpx;
  height: 72rpx;
  border: 2rpx solid #d6e4ef;
  border-radius: 16rpx;
  background: #f8fcff;
  display: flex;
  align-items: center;
}

.zone-item {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  color: #7f94a8;
}

.zone-item.active {
  color: #2d89be;
  font-weight: 700;
}

.split-line {
  width: 2rpx;
  height: 32rpx;
  background: #d6e4ef;
}

.section {
  margin-top: 24rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #23384c;
}

.sort-tabs {
  display: flex;
  gap: 10rpx;
}

.sort-tab {
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  border: 2rpx solid #d2deea;
  color: #7f94a8;
  font-size: 24rpx;
}

.sort-tab.active {
  border-color: #4ba7d9;
  color: #2d89be;
  background: #eaf6fd;
}

.chips {
  margin-top: 12rpx;
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.category-chip {
  background: #f3ecd1;
}

.category-chip.active {
  border-color: #4ba7d9;
  background: #eaf6fd;
  color: #2d89be;
}

.post-card,
.question-card {
  padding: 20rpx;
  margin-bottom: 14rpx;
}

.post-card {
  display: flex;
  gap: 14rpx;
}

.post-main {
  flex: 1;
  min-width: 0;
}

.post-author-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 6rpx;
}

.post-author-avatar {
  width: 46rpx;
  height: 46rpx;
  border-radius: 50%;
  background: #eef3f8;
}

.post-author-avatar--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
  color: #6f7d8c;
}

.post-author-name {
  font-size: 26rpx;
  color: #2a3340;
  font-weight: 600;
}

.post-official {
  padding: 2rpx 10rpx;
  border-radius: 999rpx;
  border: 2rpx solid #3fa2d8;
  color: #2d89be;
  font-size: 22rpx;
}

.post-title,
.question-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #22364d;
  line-height: 1.35;
}

.post-summary {
  margin-top: 8rpx;
  font-size: 25rpx;
  color: #7e93a6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-cover {
  width: 172rpx;
  height: 132rpx;
  border-radius: 12rpx;
  background: #edf2f6;
}

.author-row {
  display: flex;
  justify-content: space-between;
  color: #8ba0b3;
}

.author-name {
  color: #1e3850;
  font-weight: 700;
}

.meta-minor {
  font-size: 24rpx;
}

.question-title {
  margin: 10rpx 0;
}

.meta {
  margin-top: 10rpx;
  display: flex;
  gap: 20rpx;
  color: #8ba0b3;
  font-size: 24rpx;
}

.state {
  color: #8ba0b3;
  text-align: center;
  padding: 24rpx 0;
}

.state.load-more {
  padding-top: 8rpx;
}

.error {
  color: #d66060;
}

.floating-ask {
  position: fixed;
  right: 28rpx;
  bottom: 50rpx;
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  background: #3fa2d8;
  box-shadow: 0 10rpx 26rpx rgba(63, 162, 216, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 30;
}

.plus {
  color: #fff;
  font-size: 64rpx;
  line-height: 1;
  transform: translateY(-4rpx);
}
</style>