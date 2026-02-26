<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { discoverApi, type AppCategoryVO, type AppExpertCardVO, type AppQuestionHotItemVO, type AppTopicListItemVO } from "@/api/discover";
import { useQaStore } from "@/stores/qa";
import { useAuthStore } from "@/stores/auth";
import { openAskPage, openQuestionDetail } from "@/utils/nav";

const qaStore = useQaStore();
const authStore = useAuthStore();
const loading = ref(false);
const errorText = ref("");

const categories = ref<AppCategoryVO[]>([]);
const hotTopics = ref<AppTopicListItemVO[]>([]);
const hotQuestions = ref<AppQuestionHotItemVO[]>([]);
const experts = ref<AppExpertCardVO[]>([]);

const questionCount = computed(() => hotQuestions.value.length);

async function fetchData() {
  loading.value = true;
  errorText.value = "";
  try {
    const home = await discoverApi.getHome();
    categories.value = home.categories || [];
    hotTopics.value = home.hotTopics || [];
    hotQuestions.value = home.hotQuestions || [];
    experts.value = home.experts || [];
    qaStore.upsertFromHotRank(hotQuestions.value);
  } catch {
    errorText.value = "首页加载失败，请检查后端服务地址";
  } finally {
    loading.value = false;
  }
}

function goSearch() {
  uni.switchTab({
    url: "/pages/search/index"
  });
}

function openCategory(item: AppCategoryVO) {
  if (authStore.isLogin) {
    uni.switchTab({ url: "/pages/discover/index" });
    return;
  }
  uni.showModal({
    title: "提示",
    content: `登录后才能查看“${item.name}”分类内容`,
    confirmText: "去登录",
    cancelText: "取消",
    success: (res) => {
      if (!res.confirm) return;
      const redirect = encodeURIComponent("/pages/discover/index");
      uni.navigateTo({ url: `/pages/auth/login?redirect=${redirect}` });
    }
  });
}

onShow(fetchData);
</script>

<template>
  <view class="page">
    <view class="search-bar" @click="goSearch">搜索健康问题，支持语义理解...</view>

    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="errorText" class="state error">{{ errorText }}</view>
    <template v-else>
      <view class="section">
        <view class="section-title">分类浏览</view>
        <view class="chips">
          <view v-for="item in categories" :key="item.id" class="app-chip category-chip" @click="openCategory(item)">
            {{ item.name }}
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-head">
          <text class="section-title">推荐问答</text>
          <text class="section-sub">{{ questionCount }}个问题</text>
        </view>

        <view v-if="!hotQuestions.length" class="state">暂无数据</view>
        <view v-else>
          <view
            v-for="item in hotQuestions"
            :key="item.id"
            class="app-card question-card"
            @click="openQuestionDetail(item.id)"
          >
            <view class="author-row">
              <view class="author-name">{{ item.authorName }}</view>
              <view class="meta-minor">{{ item.createdAt }}</view>
            </view>
            <view class="question-title">{{ item.title }}</view>
            <view class="meta">
              <text>{{ item.answerCount }}回答</text>
              <text>{{ item.viewCount }}浏览</text>
              <text>{{ item.likeCount }}点赞</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-title">热门专题</view>
        <view class="topic-list">
          <view v-for="item in hotTopics" :key="item.id" class="app-card topic-card">
            <view class="topic-title">{{ item.title }}</view>
            <view class="topic-sub">{{ item.subtitle }}</view>
            <view class="meta">
              <text>{{ item.questionCount }}问题</text>
              <text>{{ item.followCount }}关注</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-title">达人</view>
        <view class="expert-grid">
          <view v-for="item in experts" :key="item.userId" class="app-card expert-card">
            <view class="expert-name">{{ item.nickname }}</view>
            <view class="expert-title">{{ item.title || item.organization }}</view>
            <view class="expert-skill">{{ item.expertise }}</view>
          </view>
        </view>
      </view>
    </template>

    <view class="floating-ask" @click="openAskPage">
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

.section {
  margin-top: 28rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 40rpx;
  font-weight: 700;
}

.section-sub {
  color: #8ba0b3;
}

.chips {
  margin-top: 14rpx;
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.category-chip {
  background: #f3ecd1;
}

.question-card {
  padding: 24rpx;
  margin-bottom: 20rpx;
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
  font-size: 36rpx;
  font-weight: 700;
  margin: 12rpx 0;
  line-height: 1.35;
}

.meta {
  display: flex;
  gap: 22rpx;
  color: #8ba0b3;
}

.topic-list {
  margin-top: 12rpx;
}

.topic-card,
.expert-card {
  padding: 20rpx;
  margin-bottom: 12rpx;
}

.topic-title,
.expert-name {
  font-size: 30rpx;
  font-weight: 700;
}

.topic-sub,
.expert-title,
.expert-skill {
  margin-top: 8rpx;
  color: #8ba0b3;
}

.expert-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
}

.state {
  color: #8ba0b3;
  text-align: center;
  padding: 36rpx 0;
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
