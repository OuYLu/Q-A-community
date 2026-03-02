<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onReachBottom, onShow } from "@dcloudio/uni-app";
import { ensurePageAuth } from "@/utils/auth-guard";
import { userApi, type AppUserAnswerItemVO, type AppUserHomeVO } from "@/api/user";
import { openAnswerDetailPage, openExpertPostDetailPage } from "@/utils/nav";
import type { AppExpertPostItemVO } from "@/api/expert";

const userId = ref(0);
const loading = ref(false);
const actionLoading = ref(false);
const failedAvatar = ref(false);
const userHome = ref<AppUserHomeVO | null>(null);
const entered = ref(false);

const activeTab = ref<"answers" | "posts">("answers");

const answerLoading = ref(false);
const answerPage = ref(1);
const answerPageSize = 10;
const answerTotal = ref(0);
const answerFinished = ref(false);
const answers = ref<AppUserAnswerItemVO[]>([]);

const postLoading = ref(false);
const postPage = ref(1);
const postPageSize = 10;
const postTotal = ref(0);
const postFinished = ref(false);
const posts = ref<AppExpertPostItemVO[]>([]);

const isExpert = computed(() => userHome.value?.expertStatus === 3);
const statColumns = computed(() => (isExpert.value ? 5 : 4));

async function loadHome() {
  if (!userId.value) return;
  loading.value = true;
  try {
    userHome.value = await userApi.home(userId.value);
    failedAvatar.value = false;
  } catch (err: any) {
    uni.showToast({ title: err?.message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function loadAnswers(reset = false) {
  if (!userId.value) return;
  if (answerLoading.value || (!reset && answerFinished.value)) return;
  answerLoading.value = true;
  try {
    const page = reset ? 1 : answerPage.value;
    const resp = await userApi.answers(userId.value, { page, pageSize: answerPageSize });
    const list = resp?.list || [];
    if (reset) answers.value = list;
    else answers.value = answers.value.concat(list);
    answerTotal.value = Number(resp?.total || 0);
    answerPage.value = page + 1;
    answerFinished.value = answers.value.length >= answerTotal.value || list.length < answerPageSize;
  } catch (err: any) {
    if (reset) answers.value = [];
    uni.showToast({ title: err?.message || "回答加载失败", icon: "none" });
  } finally {
    answerLoading.value = false;
  }
}

async function loadPosts(reset = false) {
  if (!userId.value) return;
  if (postLoading.value || (!reset && postFinished.value)) return;
  postLoading.value = true;
  try {
    const page = reset ? 1 : postPage.value;
    const resp = await userApi.expertPosts(userId.value, { page, pageSize: postPageSize });
    const list = resp?.list || [];
    if (reset) posts.value = list;
    else posts.value = posts.value.concat(list);
    postTotal.value = Number(resp?.total || 0);
    postPage.value = page + 1;
    postFinished.value = posts.value.length >= postTotal.value || list.length < postPageSize;
  } catch (err: any) {
    if (reset) posts.value = [];
    uni.showToast({ title: err?.message || "科普文章加载失败", icon: "none" });
  } finally {
    postLoading.value = false;
  }
}

async function toggleFollow() {
  if (!userHome.value || userHome.value.self || actionLoading.value) return;
  actionLoading.value = true;
  try {
    if (userHome.value.followed) {
      await userApi.unfollow(userHome.value.userId);
      userHome.value = {
        ...userHome.value,
        followed: false,
        followerCount: Math.max(0, Number(userHome.value.followerCount || 0) - 1)
      };
      uni.showToast({ title: "已取消关注", icon: "none" });
    } else {
      await userApi.follow(userHome.value.userId);
      userHome.value = {
        ...userHome.value,
        followed: true,
        followerCount: Number(userHome.value.followerCount || 0) + 1
      };
      uni.showToast({ title: "关注成功", icon: "success" });
    }
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  } finally {
    actionLoading.value = false;
  }
}

function openAnswer(item: AppUserAnswerItemVO) {
  if (!item?.answerId) return;
  openAnswerDetailPage(Number(item.answerId));
}

function openPost(item: AppExpertPostItemVO) {
  if (!item?.id) return;
  openExpertPostDetailPage(Number(item.id));
}

function switchTab(tab: "answers" | "posts") {
  if (activeTab.value === tab) return;
  activeTab.value = tab;
  if (tab === "answers") {
    answerPage.value = 1;
    answerTotal.value = 0;
    answerFinished.value = false;
    loadAnswers(true);
  } else {
    postPage.value = 1;
    postTotal.value = 0;
    postFinished.value = false;
    loadPosts(true);
  }
}

async function initPage() {
  await loadHome();
  answerPage.value = 1;
  answerTotal.value = 0;
  answerFinished.value = false;
  await loadAnswers(true);
  if (isExpert.value) {
    postPage.value = 1;
    postTotal.value = 0;
    postFinished.value = false;
    await loadPosts(true);
  } else {
    posts.value = [];
  }
}

onLoad(async (options) => {
  if (!ensurePageAuth()) return;
  userId.value = Number(options?.userId || 0);
  if (!userId.value) {
    uni.showToast({ title: "用户参数错误", icon: "none" });
    setTimeout(() => uni.navigateBack(), 150);
    return;
  }
  await initPage();
});

onShow(async () => {
  if (!entered.value) {
    entered.value = true;
    return;
  }
  if (!ensurePageAuth()) return;
  await initPage();
});

onReachBottom(() => {
  if (activeTab.value === "posts") {
    loadPosts(false);
  } else {
    loadAnswers(false);
  }
});
</script>

<template>
  <view class="page">
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!userHome" class="state">暂无用户信息</view>
    <view v-else>
      <view class="profile-card app-card">
        <view class="head-row">
          <image
            v-if="userHome.avatar && !failedAvatar"
            class="avatar-img"
            :src="userHome.avatar"
            mode="aspectFill"
            @error="failedAvatar = true"
          />
          <view v-else class="avatar">{{ (userHome.nickname || "用").slice(0, 1) }}</view>
          <view class="user-meta">
            <view class="name-row">
              <view class="name">{{ userHome.nickname || "用户" }}</view>
              <view v-if="userHome.expertStatus === 3" class="expert-pill">已认证专家</view>
            </view>
            <view class="desc">{{ userHome.slogan || "这个人很懒，还没有填写个性签名" }}</view>
          </view>
        </view>

        <view class="stat-row" :style="{ gridTemplateColumns: `repeat(${statColumns}, 1fr)` }">
          <view class="stat-item">
            <view class="stat-value">{{ userHome.questionCount || 0 }}</view>
            <view class="stat-label">提问</view>
          </view>
          <view class="stat-item">
            <view class="stat-value">{{ userHome.answerCount || 0 }}</view>
            <view class="stat-label">回答</view>
          </view>
          <view v-if="userHome.expertStatus === 3" class="stat-item">
            <view class="stat-value">{{ userHome.expertPostCount || 0 }}</view>
            <view class="stat-label">科普</view>
          </view>
          <view class="stat-item">
            <view class="stat-value">{{ userHome.followingCount || 0 }}</view>
            <view class="stat-label">关注</view>
          </view>
          <view class="stat-item">
            <view class="stat-value">{{ userHome.followerCount || 0 }}</view>
            <view class="stat-label">粉丝</view>
          </view>
        </view>

        <button
          v-if="!userHome.self"
          class="follow-btn"
          :class="{ active: userHome.followed }"
          :disabled="actionLoading"
          @click="toggleFollow"
        >
          {{ userHome.followed ? "取消关注" : "关注" }}
        </button>
      </view>

      <view v-if="userHome.expertStatus === 3" class="tab-row">
        <view class="tab-pill" :class="{ active: activeTab === 'posts' }" @click="switchTab('posts')">科普文章</view>
        <view class="tab-pill" :class="{ active: activeTab === 'answers' }" @click="switchTab('answers')">回答</view>
      </view>

      <view class="section-title">{{ activeTab === 'posts' ? "科普文章" : "有效回答" }}</view>

      <template v-if="activeTab === 'posts'">
        <view v-if="!posts.length && postLoading" class="state">加载中...</view>
        <view v-else-if="!posts.length" class="state">暂无科普文章</view>
        <view v-else>
          <view v-for="item in posts" :key="item.id" class="post-card app-card" @tap="openPost(item)">
            <view class="post-title">{{ item.title }}</view>
            <view class="post-summary">{{ item.summary || "暂无摘要" }}</view>
            <view class="post-meta">
              <text>{{ item.likeCount || 0 }} 点赞</text>
              <text>{{ item.viewCount || 0 }} 浏览</text>
              <text>{{ item.createdAt || "" }}</text>
            </view>
          </view>

          <view v-if="postLoading" class="state">加载中...</view>
          <view v-else-if="postFinished" class="state">没有更多了</view>
        </view>
      </template>

      <template v-else>
        <view v-if="!answers.length && answerLoading" class="state">加载中...</view>
        <view v-else-if="!answers.length" class="state">暂无有效回答</view>
        <view v-else>
          <view v-for="item in answers" :key="item.answerId" class="answer-card app-card" @tap="openAnswer(item)">
            <view class="question-title">{{ item.questionTitle }}</view>
            <view class="answer-preview">{{ item.contentPreview || "暂无内容" }}</view>
            <view class="answer-meta">
              <text>{{ item.likeCount || 0 }} 点赞</text>
              <text>{{ item.createdAt || "" }}</text>
            </view>
          </view>

          <view v-if="answerLoading" class="state">加载中...</view>
          <view v-else-if="answerFinished" class="state">没有更多了</view>
        </view>
      </template>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 24rpx;
  min-height: 100vh;
  box-sizing: border-box;
}

.profile-card {
  padding: 24rpx;
}

.head-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 2rpx solid #9bd1ef;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2f9ad2;
  font-size: 52rpx;
  font-weight: 700;
  background: #f3eed7;
}

.avatar-img {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 2rpx solid #9bd1ef;
  background: #fff;
}

.user-meta {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 44rpx;
  font-weight: 700;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}

.expert-pill {
  max-width: 260rpx;
  height: 34rpx;
  line-height: 34rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background: #eaf4fb;
  color: #2f6f99;
  font-size: 20rpx;
}

.desc {
  margin-top: 6rpx;
  color: #7f95a8;
  line-height: 1.3;
}

.follow-btn {
  margin-top: 18rpx;
  width: 100%;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0;
  font-size: 26rpx;
  border: 2rpx solid #4ba7d9;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
}

.follow-btn.active {
  border-color: #cfdbe4;
  background: #eef3f7;
  color: #6f879b;
}

.stat-row {
  margin-top: 22rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f0ead7;
  display: grid;
  gap: 10rpx;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 46rpx;
  font-weight: 700;
}

.stat-label {
  color: #8ba0b3;
  margin-top: 4rpx;
}

.tab-row {
  margin-top: 18rpx;
  display: flex;
  gap: 12rpx;
}

.tab-pill {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  border: 2rpx solid #d6e3f0;
  color: #5d7084;
  font-weight: 700;
  background: #f6f9fc;
}

.tab-pill.active {
  border-color: #5aa9d9;
  color: #2d89be;
  background: #eaf6ff;
}

.section-title {
  margin: 18rpx 0 10rpx;
  font-size: 36rpx;
  font-weight: 700;
  color: #8ea1b2;
}

.answer-card,
.post-card {
  margin-bottom: 12rpx;
  padding: 18rpx;
}

.question-title,
.post-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #22364d;
  line-height: 1.35;
}

.answer-preview,
.post-summary {
  margin-top: 8rpx;
  color: #6e8599;
  line-height: 1.5;
}

.answer-meta,
.post-meta {
  margin-top: 10rpx;
  display: flex;
  justify-content: space-between;
  color: #8ea1b2;
  font-size: 24rpx;
}

.state {
  text-align: center;
  color: #9ab0bf;
  padding: 24rpx 0;
}
</style>