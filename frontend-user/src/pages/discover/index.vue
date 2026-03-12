<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import {
  discoverApi,
  type AppCategoryVO,
  type AppExpertCardVO,
  type AppQuestionHotItemVO,
  type AppTopicListItemVO
} from "@/api/discover";
import { expertApi, type AppExpertPostCategoryVO } from "@/api/expert";
import { useAuthStore } from "@/stores/auth";
import { openQuestionDetail, openUserHomePage } from "@/utils/nav";

type DiscoverTab = "category" | "rank" | "expert";
type CategoryBizType = "qa" | "kb";
type CategoryCard = AppCategoryVO | AppExpertPostCategoryVO;

const authStore = useAuthStore();
const needLogin = computed(() => !authStore.isLogin);
const loading = ref(false);
const activeTab = ref<DiscoverTab>("category");
const categoryBizType = ref<CategoryBizType>("qa");

const qaCategories = ref<AppCategoryVO[]>([]);
const kbCategories = ref<AppExpertPostCategoryVO[]>([]);
const topics = ref<AppTopicListItemVO[]>([]);
const hotQuestions = ref<AppQuestionHotItemVO[]>([]);
const experts = ref<AppExpertCardVO[]>([]);
const failedCategoryIconIds = ref<number[]>([]);
const failedTopicCoverIds = ref<number[]>([]);
const failedAvatarIds = ref<number[]>([]);

const tabs: Array<{ key: DiscoverTab; label: string; icon: string; activeIcon: string }> = [
  { key: "category", label: "分类", icon: "/static/tabbar/topic.png", activeIcon: "/static/tabbar/topic-active.png" },
  { key: "rank", label: "热榜", icon: "/static/tabbar/hot.png", activeIcon: "/static/tabbar/hot-active.png" },
  { key: "expert", label: "专家", icon: "/static/tabbar/expert.png", activeIcon: "/static/tabbar/expert-active.png" }
];
const categoryCards = computed<CategoryCard[]>(() => {
  if (categoryBizType.value === "qa") {
    return qaCategories.value || [];
  }
  const rows = kbCategories.value || [];
  const hasParent = rows.some((item) => Number(item.parentId || 0) > 0);
  return hasParent ? rows.filter((item) => Number(item.parentId || 0) <= 0) : rows;
});

async function loadData() {
  loading.value = true;
  try {
    const [categoryData, kbCategoryData, topicData, rankData, expertData] = await Promise.all([
      discoverApi.getCategories(),
      expertApi.categories(),
      discoverApi.getHotTopics(12),
      discoverApi.getHotQuestions(20),
      discoverApi.getExperts(20)
    ]);
    qaCategories.value = categoryData || [];
    kbCategories.value = kbCategoryData || [];
    topics.value = topicData || [];
    hotQuestions.value = rankData || [];
    experts.value = expertData || [];
    failedCategoryIconIds.value = [];
    failedTopicCoverIds.value = [];
    failedAvatarIds.value = [];
  } catch {
    uni.showToast({ title: "发现页加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function setTab(tab: DiscoverTab) {
  activeTab.value = tab;
}

function setCategoryBizType(type: CategoryBizType) {
  categoryBizType.value = type;
}

function hasCategoryIcon(item: CategoryCard) {
  if (categoryBizType.value !== "qa") return false;
  const row = item as AppCategoryVO;
  return !!row.icon && !failedCategoryIconIds.value.includes(Number(row.id));
}

function categoryIcon(item: CategoryCard) {
  if (categoryBizType.value !== "qa") return "";
  return (item as AppCategoryVO).icon || "";
}

function onCategoryIconError(categoryId: number | string) {
  const id = Number(categoryId);
  if (!id) return;
  if (failedCategoryIconIds.value.includes(id)) return;
  failedCategoryIconIds.value = [...failedCategoryIconIds.value, id];
}

function openCategory(item: CategoryCard) {
  const type = categoryBizType.value;
  uni.navigateTo({
    url: `/pages/discover/category-detail?categoryId=${item.id}&categoryName=${encodeURIComponent(item.name || "")}&categoryType=${type}`
  });
}

function hasTopicCover(item: AppTopicListItemVO) {
  return !!item.coverImg && !failedTopicCoverIds.value.includes(item.id);
}

function onTopicCoverError(topicId: number) {
  if (failedTopicCoverIds.value.includes(topicId)) return;
  failedTopicCoverIds.value = [...failedTopicCoverIds.value, topicId];
}

function avatarAvailable(userId: number, avatar?: string) {
  return !!avatar && !failedAvatarIds.value.includes(userId);
}

function onAvatarError(userId: number) {
  if (failedAvatarIds.value.includes(userId)) return;
  failedAvatarIds.value = [...failedAvatarIds.value, userId];
}

function openTopic(item: AppTopicListItemVO) {
  uni.navigateTo({
    url: `/pages/discover/topic-detail?topicId=${item.id}&topicTitle=${encodeURIComponent(item.title || "")}`
  });
}

function openHotQuestion(item: AppQuestionHotItemVO) {
  openQuestionDetail(item.id);
}

function openExpert(item: AppExpertCardVO) {
  if (!item?.userId) return;
  openUserHomePage(Number(item.userId));
}

function goLogin() {
  uni.navigateTo({
    url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/discover/index")}`
  });
}

onShow(async () => {
  if (needLogin.value) return;
  await loadData();
});
</script>

<template>
  <view class="page">
    <view v-if="needLogin" class="auth-card app-card">
      <view class="title">登录后可查看发现内容</view>
      <view class="auth-sub">专题、热榜和专家内容仅登录后可用</view>
      <button class="auth-btn" @click="goLogin">去登录</button>
    </view>

    <template v-else>
      <view class="tabs">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="setTab(tab.key)"
        >
          <image class="tab-icon" :src="activeTab === tab.key ? tab.activeIcon : tab.icon" mode="aspectFit" />
          <text class="tab-label">{{ tab.label }}</text>
        </view>
      </view>

      <view v-if="loading" class="state">加载中...</view>
      <view v-else>
        <view v-if="activeTab === 'category'">
          <view class="section-head section-head--compact">
            <view class="section-title">分类浏览</view>
            <view class="category-switch">
              <view
                class="category-switch-item"
                :class="{ active: categoryBizType === 'qa' }"
                @click="setCategoryBizType('qa')"
              >
                问答分类
              </view>
              <view
                class="category-switch-item"
                :class="{ active: categoryBizType === 'kb' }"
                @click="setCategoryBizType('kb')"
              >
                知识库分类
              </view>
            </view>
          </view>
          <view v-if="!categoryCards.length" class="state">暂无分类</view>
          <view v-else class="category-grid">
            <view
              v-for="item in categoryCards"
              :key="`${categoryBizType}-${item.id}`"
              class="app-card category-item"
              @click="openCategory(item)"
            >
              <image
                v-if="hasCategoryIcon(item)"
                class="category-icon"
                :src="categoryIcon(item)"
                mode="aspectFill"
                @error="onCategoryIconError(item.id)"
              />
              <view v-else class="category-icon category-icon--fallback">
                <text>{{ (item.name || "分").slice(0, 1) }}</text>
              </view>
              <view class="category-name">{{ item.name }}</view>
            </view>
          </view>

          <view class="section-title section-gap">热门专题</view>
          <view v-if="!topics.length" class="state">暂无专题</view>
          <view v-else>
            <view v-for="item in topics" :key="item.id" class="app-card topic-item" @click="openTopic(item)">
              <image
                v-if="hasTopicCover(item)"
                class="topic-cover"
                :src="item.coverImg"
                mode="aspectFill"
                @error="onTopicCoverError(item.id)"
              />
              <view v-else class="topic-cover topic-cover--fallback">
                <text class="fallback-text">专题封面</text>
              </view>
              <view class="topic-main">
                <view class="topic-title">{{ item.title }}</view>
                <view class="topic-sub">{{ item.subtitle || "点击查看该专题热门问题" }}</view>
                <view class="topic-meta">
                  <view class="meta-item">
                    <text class="meta-label">问题</text>
                    <text>{{ item.questionCount }}</text>
                  </view>
                  <view class="meta-item">
                    <text class="meta-label">关注</text>
                    <text>{{ item.followCount }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view v-else-if="activeTab === 'rank'">
          <view v-if="!hotQuestions.length" class="state">暂无热榜</view>
          <view v-else>
            <view
              v-for="(item, idx) in hotQuestions"
              :key="item.id"
              class="app-card rank-item"
              @click="openHotQuestion(item)"
            >
              <view class="rank-no" :class="{ top3: idx < 3 }">{{ idx + 1 }}</view>
              <view class="rank-main">
                <view class="rank-title">{{ item.title }}</view>
                <view class="rank-author">{{ item.authorName }}</view>
                <view class="rank-meta">
                  <view class="meta-item">
                    <text class="meta-label">回答</text>
                    <text>{{ item.answerCount }}</text>
                  </view>
                  <view class="meta-item">
                    <text class="meta-label">浏览</text>
                    <text>{{ item.viewCount }}</text>
                  </view>
                  <view class="meta-item">
                    <text class="meta-label">点赞</text>
                    <text>{{ item.likeCount }}</text>
                  </view>
                </view>
              </view>
              <view class="hot-tag">热</view>
            </view>
          </view>
        </view>

        <view v-else>
          <view v-if="!experts.length" class="state">暂无专家</view>
          <view v-else>
            <view v-for="item in experts" :key="item.userId" class="app-card expert-item" @click="openExpert(item)">
              <image
                v-if="avatarAvailable(item.userId, item.avatar)"
                class="expert-avatar"
                :src="item.avatar"
                mode="aspectFill"
                @error="onAvatarError(item.userId)"
              />
              <view v-else class="expert-avatar expert-avatar--fallback">
                <text>{{ (item.nickname || "专").slice(0, 1) }}</text>
              </view>

              <view class="expert-main">
                <view class="expert-name-row">
                  <text class="expert-name">{{ item.nickname }}</text>
                  <view class="expert-badge">专家</view>
                </view>
                <view class="expert-title">{{ item.title || item.organization || "认证专家" }}</view>
                <view class="expert-skill">{{ item.expertise || "擅长健康问答与科普" }}</view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 20rpx 24rpx 24rpx;
  min-height: 100vh;
  box-sizing: border-box;
}

.auth-card {
  padding: 30rpx 24rpx;
  margin-top: 0;
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

.tabs {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 18rpx;
  border-bottom: 1rpx solid #d8e4ef;
}

.tab-item {
  height: 86rpx;
  padding: 6rpx 12rpx 8rpx;
  color: #6b8297;
  display: flex;
  flex-direction: row;
  gap: 8rpx;
  align-items: center;
  justify-content: center;
  position: relative;
}

.tab-icon {
  width: 28rpx;
  height: 28rpx;
}

.tab-label {
  font-size: 26rpx;
  line-height: 1.2;
}

.tab-item.active {
  color: #1f3f55;
  font-weight: 700;
}

.tab-item.active::after {
  content: "";
  position: absolute;
  left: 12rpx;
  right: 12rpx;
  bottom: -1rpx;
  height: 5rpx;
  border-radius: 5rpx;
  background: #5aadd6;
}

.topic-item,
.rank-item,
.expert-item {
  margin-bottom: 14rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f3f55;
  margin: 8rpx 0 12rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-head--compact {
  margin-bottom: 6rpx;
}

.category-switch {
  display: flex;
  gap: 8rpx;
}

.category-switch-item {
  padding: 6rpx 14rpx;
  border-radius: 16rpx;
  border: 2rpx solid #d2deea;
  color: #7f94a8;
  font-size: 24rpx;
  background: #fff;
}

.category-switch-item.active {
  border-color: #4ba7d9;
  color: #2d89be;
  background: #eaf6fd;
}

.section-gap {
  margin-top: 18rpx;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8rpx;
}

.category-item {
  padding: 6rpx 8rpx;
  min-height: 66rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 8rpx;
  border-radius: 14rpx;
  background: #f7f5eb;
  border: 2rpx solid #cfe4f1;
}

.category-icon {
  width: 36rpx;
  height: 36rpx;
  border-radius: 8rpx;
  background: #e9f1f8;
  flex-shrink: 0;
}

.category-icon--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5f7386;
  font-size: 24rpx;
  font-weight: 700;
}

.category-name {
  font-size: 23rpx;
  font-weight: 700;
  color: #24465e;
  text-align: left;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topic-item {
  padding: 16rpx;
  display: flex;
  gap: 16rpx;
  align-items: flex-start;
}

.topic-cover {
  width: 220rpx;
  height: 160rpx;
  border-radius: 14rpx;
  background: #f2f6fa;
  flex-shrink: 0;
}

.topic-cover--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #d9ecf8 0%, #edf6fd 100%);
  border: 1rpx solid #d8e8f6;
}

.fallback-text {
  color: #6f8ca3;
  font-size: 24rpx;
  letter-spacing: 2rpx;
}

.topic-title,
.rank-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f3f55;
}

.topic-main {
  min-width: 0;
  flex: 1;
}

.topic-sub,
.rank-author,
.expert-title,
.expert-skill {
  color: #8096aa;
  margin-top: 8rpx;
}

.topic-sub {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-meta,
.rank-meta {
  margin-top: 12rpx;
  display: flex;
  gap: 18rpx;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  color: #6f879b;
  font-size: 24rpx;
}

.meta-label {
  color: #8ba0b3;
}

.rank-item {
  padding: 18rpx;
  display: flex;
  gap: 14rpx;
  align-items: flex-start;
}

.rank-no {
  width: 42rpx;
  height: 42rpx;
  border-radius: 12rpx;
  background: #ebf2f8;
  color: #6f8294;
  font-size: 24rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.rank-no.top3 {
  background: #ffe8b0;
  color: #9a6b00;
}

.rank-main {
  flex: 1;
}

.hot-tag {
  min-width: 44rpx;
  height: 36rpx;
  padding: 0 8rpx;
  border-radius: 10rpx;
  background: #ffe8b0;
  color: #8c5a00;
  font-size: 22rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.expert-item {
  padding: 18rpx;
  display: flex;
  gap: 14rpx;
  align-items: flex-start;
}

.expert-avatar {
  width: 78rpx;
  height: 78rpx;
  border-radius: 50%;
  background: #e8f0f7;
  flex-shrink: 0;
}

.expert-avatar--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5f7386;
  font-size: 30rpx;
  font-weight: 700;
}

.expert-main {
  flex: 1;
}

.expert-name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.expert-badge {
  padding: 2rpx 10rpx;
  border-radius: 10rpx;
  background: #dff0fb;
  color: #3f7ea2;
  font-size: 20rpx;
}

.expert-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f3f55;
}

.state {
  text-align: center;
  color: #8ba0b3;
  margin-top: 80rpx;
}
</style>
