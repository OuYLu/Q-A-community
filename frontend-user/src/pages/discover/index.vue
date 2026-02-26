<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { discoverApi, type AppCategoryVO, type AppExpertCardVO, type AppTopicListItemVO } from "@/api/discover";
import { topicApi } from "@/api/topic";
import { useAuthStore } from "@/stores/auth";
import { useQaStore } from "@/stores/qa";
import { openQuestionDetail } from "@/utils/nav";

const qaStore = useQaStore();
const authStore = useAuthStore();
const categories = ref<AppCategoryVO[]>([]);
const topics = ref<AppTopicListItemVO[]>([]);
const experts = ref<AppExpertCardVO[]>([]);
const failedTopicCoverIds = ref<number[]>([]);
const loading = ref(false);
const needLogin = computed(() => !authStore.isLogin);

async function loadData() {
  loading.value = true;
  try {
    const [cateData, topicData, expertData] = await Promise.all([
      discoverApi.getCategories(),
      discoverApi.getHotTopics(6),
      discoverApi.getExperts(6)
    ]);
    categories.value = cateData;
    topics.value = topicData;
    failedTopicCoverIds.value = [];
    experts.value = expertData;
    if (topicData.length > 0) {
      const firstTopicQuestions = await topicApi.questions(topicData[0].id, {
        sortBy: "hot",
        page: 1,
        pageSize: 8
      });
      qaStore.upsertFromTopicQuestions(firstTopicQuestions.list || []);
    }
  } catch {
    uni.showToast({ title: "发现页面加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function openTopicQuestion() {
  const q = qaStore.questions[0];
  if (!q) return;
  openQuestionDetail(q.id);
}

function hasTopicCover(item: AppTopicListItemVO) {
  return !!item.coverImg && !failedTopicCoverIds.value.includes(item.id);
}

function onTopicCoverError(topicId: number) {
  if (failedTopicCoverIds.value.includes(topicId)) return;
  failedTopicCoverIds.value = [...failedTopicCoverIds.value, topicId];
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
      <view class="auth-sub">分类、专题和达人内容仅登录后可用</view>
      <button class="auth-btn" @click="goLogin">去登录</button>
    </view>

    <template v-else>
      <view v-if="loading" class="state">加载中...</view>
      <view v-else>
        <view class="block">
          <view class="title">分类浏览</view>
          <view class="cate-grid">
            <view v-for="item in categories" :key="item.id" class="app-card cate-item">
              <text class="name">{{ item.name }}</text>
              <text class="cnt">{{ item.questionCount }}问答</text>
            </view>
          </view>
        </view>

        <view class="block">
          <view class="title">热门专题</view>
          <view v-for="item in topics" :key="item.id" class="app-card topic-item" @click="openTopicQuestion">
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
            <view class="topic-title">{{ item.title }}</view>
            <view class="topic-sub">{{ item.subtitle }}</view>
            <view class="cnt">{{ item.questionCount }}问题 {{ item.followCount }}人关注 · 今日+{{ item.todayNewCount || 0 }}</view>
          </view>
        </view>

        <view class="block">
          <view class="title">达人</view>
          <view class="expert-row">
            <view v-for="item in experts" :key="item.userId" class="app-card expert-item">
              <view class="expert-name">{{ item.nickname }}</view>
              <view class="expert-title">{{ item.title || item.organization }}</view>
            </view>
          </view>
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

.block {
  margin-bottom: 26rpx;
}

.title {
  font-size: 34rpx;
  font-weight: 700;
  margin-bottom: 14rpx;
}

.cate-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.cate-item {
  padding: 18rpx;
  min-height: 120rpx;
}

.topic-item,
.expert-item {
  padding: 20rpx;
  margin-bottom: 12rpx;
}

.topic-cover {
  width: 100%;
  height: 220rpx;
  border-radius: 14rpx;
  background: #f2f6fa;
  margin-bottom: 14rpx;
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
.expert-name {
  font-size: 30rpx;
  font-weight: 700;
}

.topic-sub,
.expert-title,
.cnt {
  margin-top: 8rpx;
  color: #8ba0b3;
}

.expert-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
}

.state {
  text-align: center;
  color: #8ba0b3;
  margin-top: 80rpx;
}
</style>
