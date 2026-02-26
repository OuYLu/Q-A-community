<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { ensurePageAuth } from "@/utils/auth-guard";
import { openAnswerPage, openAnswerDetailPage } from "@/utils/nav";
import { questionApi, type AppQuestionAnswerVO, type AppQuestionDetailVO } from "@/api/question";

const questionId = ref(0);
const loading = ref(false);
const loadFailed = ref(false);
const actionLoading = ref(false);
const question = ref<AppQuestionDetailVO | null>(null);
const entered = ref(false);
const defaultAvatarText = "用户";

const answers = computed(() => question.value?.answers || []);
const bannerImages = computed(() => question.value?.imageUrls || []);

function previewImages(index: number, urls: string[]) {
  if (!urls.length) return;
  uni.previewImage({ current: urls[index], urls });
}

function firstImage(item: AppQuestionAnswerVO) {
  return item.imageUrls && item.imageUrls.length ? item.imageUrls[0] : "";
}

async function loadDetail() {
  if (!questionId.value) return;
  loading.value = true;
  loadFailed.value = false;
  try {
    question.value = await questionApi.detail(questionId.value);
  } catch (err: any) {
    loadFailed.value = true;
    uni.showToast({ title: err?.message || "加载问题失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function toggleLike() {
  if (!question.value) return;
  try {
    const updated = await questionApi.toggleLike(question.value.id);
    question.value = { ...question.value, likeCount: updated.likeCount, liked: updated.liked };
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  }
}

async function toggleFavorite() {
  if (!question.value) return;
  try {
    const updated = await questionApi.toggleFavorite(question.value.id);
    question.value = { ...question.value, favoriteCount: updated.favoriteCount, favorited: updated.favorited };
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  }
}

async function toggleAnswerLike(answerId: number) {
  if (actionLoading.value) return;
  actionLoading.value = true;
  try {
    await questionApi.toggleAnswerLike(answerId);
    await loadDetail();
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  } finally {
    actionLoading.value = false;
  }
}

async function toggleAnswerFavorite(answerId: number) {
  if (actionLoading.value) return;
  actionLoading.value = true;
  try {
    await questionApi.toggleAnswerFavorite(answerId);
    await loadDetail();
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  } finally {
    actionLoading.value = false;
  }
}

async function toggleBest(answerId: number, isBest?: boolean) {
  if (!question.value || actionLoading.value) return;
  actionLoading.value = true;
  try {
    await questionApi.recommendBest(question.value.id, answerId);
    uni.showToast({ title: isBest ? "已取消最佳" : "已设为最佳", icon: "success" });
    await loadDetail();
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  } finally {
    actionLoading.value = false;
  }
}

function openAnswerDetail(answerId: number) {
  openAnswerDetailPage(answerId);
}

onLoad((options) => {
  questionId.value = Number(options?.id || 0);
  loadDetail();
});

onShow(() => {
  ensurePageAuth();
  if (!entered.value) {
    entered.value = true;
    return;
  }
  loadDetail();
});
</script>

<template>
  <view class="page">
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!question" class="state">{{ loadFailed ? "问题不存在或已删除" : "未找到问题" }}</view>
    <view v-else>
      <view class="question-card">
        <view class="author-row">
          <image v-if="question.authorAvatar" class="avatar" :src="question.authorAvatar" mode="aspectFill" />
          <view v-else class="avatar-fallback">{{ defaultAvatarText }}</view>
          <view class="author-info">
            <view class="author-name">{{ question.authorName || "匿名用户" }}</view>
            <view class="author-time">{{ question.createdAt || "" }}</view>
          </view>
        </view>

        <swiper v-if="bannerImages.length" class="hero-swiper" circular indicator-dots autoplay>
          <swiper-item v-for="(url, idx) in bannerImages" :key="url + idx">
            <image class="hero-image" :src="url" mode="aspectFill" @tap="previewImages(idx, bannerImages)" />
          </swiper-item>
        </swiper>

        <view class="title">{{ question.title }}</view>
        <view class="content">{{ question.content || "暂无详细描述" }}</view>

        <view v-if="question.tags?.length" class="tags">
          <text v-for="tag in question.tags || []" :key="tag" class="tag-chip">#{{ tag }}</text>
        </view>

        <view class="meta-row">
          <view class="meta">
            <text>{{ question.answerCount || 0 }} 回答</text>
            <text>{{ question.viewCount || 0 }} 浏览</text>
          </view>
          <view class="actions">
            <view class="action-btn like-btn" :class="{ active: question.liked }" @click="toggleLike">
              <text class="action-icon">❤</text>
              <text>{{ question.likeCount || 0 }}</text>
            </view>
            <view class="action-btn" :class="{ active: question.favorited }" @click="toggleFavorite">
              <text class="action-icon">⭐</text>
              <text>{{ question.favoriteCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>

      <button class="answer-btn" @click="openAnswerPage(question.id, question.title)">写回答</button>

      <view class="section-title">全部回答 ({{ answers.length }})</view>
      <view v-if="!answers.length" class="empty-card">还没有回答，来抢沙发吧</view>

      <view v-for="item in answers" :key="item.id" class="answer-card" @tap="openAnswerDetail(item.id)">
        <view v-if="item.canRecommend" class="best-top" :class="{ active: item.bestAnswer }" @click.stop="toggleBest(item.id, item.bestAnswer)">
          <text class="best-icon">👑</text>
          <text>{{ item.bestAnswer ? "取消" : "最佳" }}</text>
        </view>

        <view class="answer-author">
          <image v-if="item.authorAvatar" class="avatar mini" :src="item.authorAvatar" mode="aspectFill" />
          <view v-else class="avatar-fallback mini">{{ defaultAvatarText }}</view>
          <view class="author-info">
            <view class="author-name small">
              {{ item.authorName || "匿名用户" }}
              <text v-if="item.bestAnswer" class="best-tag">最佳</text>
            </view>
            <view class="author-time">{{ item.createdAt || "" }}</view>
          </view>
        </view>

        <image v-if="firstImage(item)" class="single-image" :src="firstImage(item)" mode="aspectFill" />

        <view class="answer-content one-line">{{ item.content }}</view>

        <view class="answer-actions" @click.stop>
          <view class="mini-action like-btn" :class="{ active: item.liked }" @tap="toggleAnswerLike(item.id)">
            <text class="mini-icon">❤</text>
            <text>{{ item.likeCount || 0 }}</text>
          </view>
          <view class="mini-action" @tap="openAnswerDetail(item.id)">
            <text class="mini-icon">💬</text>
            <text>{{ item.commentCount || 0 }}</text>
          </view>
          <view class="mini-action" :class="{ active: item.favorited }" @tap="toggleAnswerFavorite(item.id)">
            <text class="mini-icon">⭐</text>
            <text>{{ item.favoriteCount || 0 }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #ebe7dd;
  padding: 16rpx 14rpx 24rpx;
}

.question-card,
.answer-card,
.empty-card {
  background: #f8f8f6;
  border: 2rpx solid #d5d9df;
  border-radius: 20rpx;
}

.question-card {
  padding: 20rpx;
}

.author-row,
.answer-author {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.avatar,
.avatar-fallback {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
}

.avatar {
  background: #ece4d7;
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ece4d7;
  color: #7f8895;
  font-size: 20rpx;
}

.avatar.mini,
.avatar-fallback.mini {
  width: 52rpx;
  height: 52rpx;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.author-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #2a3340;
}

.author-name.small {
  font-size: 26rpx;
}

.best-tag {
  margin-left: 8rpx;
  padding: 0 10rpx;
  border-radius: 10rpx;
  background: #f7d37e;
  color: #6f5213;
  font-size: 20rpx;
}

.author-time {
  font-size: 22rpx;
  color: #8b95a3;
}

.hero-swiper {
  margin-top: 16rpx;
  width: 100%;
  height: 360rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.title {
  margin-top: 16rpx;
  font-size: 34rpx;
  line-height: 1.4;
  font-weight: 700;
  color: #2a3340;
}

.content {
  margin-top: 10rpx;
  color: #58667a;
  line-height: 1.7;
}

.tags {
  margin-top: 14rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.tag-chip {
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  background: #efe8d6;
  color: #86765a;
  font-size: 22rpx;
}

.meta-row {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta {
  display: flex;
  gap: 18rpx;
  font-size: 24rpx;
  color: #7e8a9a;
}

.actions {
  display: flex;
  gap: 10rpx;
}

.action-btn {
  min-width: 88rpx;
  height: 52rpx;
  border-radius: 26rpx;
  border: 2rpx solid #d3dce9;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  color: #7e8a9a;
  font-size: 22rpx;
}

.action-btn.active {
  border-color: #57a9d8;
  color: #2d89be;
  background: #eef7fc;
}

.action-btn.like-btn.active {
  border-color: #ec9aab;
  color: #d95b78;
  background: #fff0f4;
}

.answer-btn {
  margin-top: 14rpx;
  border: none;
  border-radius: 16rpx;
  background: #58add7;
  color: #fff;
  font-weight: 700;
}

.section-title {
  margin: 22rpx 6rpx 12rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #2a3340;
}

.answer-card {
  position: relative;
  padding: 18rpx;
  margin-bottom: 12rpx;
}

.best-top {
  position: absolute;
  right: 16rpx;
  top: 14rpx;
  height: 44rpx;
  padding: 0 12rpx;
  border-radius: 22rpx;
  border: 2rpx solid #e4c66f;
  background: #f6e7ba;
  color: #6e5618;
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
}

.best-top.active {
  border-color: #c9cfd9;
  background: #edf1f6;
  color: #5f6d80;
}

.single-image {
  margin-top: 12rpx;
  width: 100%;
  height: 200rpx;
  border-radius: 14rpx;
}

.answer-content {
  margin-top: 10rpx;
  color: #4a586b;
  line-height: 1.6;
}

.one-line {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.answer-actions {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.mini-action {
  min-width: 72rpx;
  height: 44rpx;
  padding: 0 12rpx;
  border-radius: 22rpx;
  border: 2rpx solid #d3dce9;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  color: #738398;
  font-size: 22rpx;
}

.mini-action.active {
  border-color: #57a9d8;
  color: #2d89be;
  background: #eef7fc;
}

.mini-action.like-btn.active {
  border-color: #ec9aab;
  color: #d95b78;
  background: #fff0f4;
}

.empty-card {
  padding: 22rpx;
  text-align: center;
  color: #91a0b1;
}

.state {
  margin-top: 120rpx;
  text-align: center;
  color: #8ba0b3;
}
</style>
