<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { ensurePageAuth } from "@/utils/auth-guard";
import { questionApi, type AppAnswerCommentVO, type AppAnswerDetailVO } from "@/api/question";

const answerId = ref(0);
const loading = ref(false);
const loadFailed = ref(false);
const loadErrorText = ref("");
const detail = ref<AppAnswerDetailVO | null>(null);
const commentInput = ref("");
const posting = ref(false);
const replyParentId = ref<number | null>(null);
const replyToName = ref("");
const defaultAvatarText = "用户";

const comments = computed(() => detail.value?.comments || []);
const commentMap = computed(() => {
  const map = new Map<number, AppAnswerCommentVO>();
  comments.value.forEach((item) => map.set(item.id, item));
  return map;
});

const rootComments = computed(() => {
  return comments.value.filter((item) => !item.parentId || !commentMap.value.has(item.parentId));
});

function childrenOf(parentId: number) {
  return comments.value.filter((item) => item.parentId === parentId);
}

const commentPlaceholder = computed(() => {
  return replyParentId.value ? `回复 ${replyToName.value}...` : "写评论...";
});

function previewImages(index: number, urls: string[]) {
  if (!urls.length) return;
  uni.previewImage({ current: urls[index], urls });
}

function startReply(comment: AppAnswerCommentVO) {
  replyParentId.value = comment.id;
  replyToName.value = comment.authorName || "用户";
}

function cancelReply() {
  replyParentId.value = null;
  replyToName.value = "";
}

async function loadDetail() {
  if (!answerId.value) return;
  loading.value = true;
  loadFailed.value = false;
  loadErrorText.value = "";
  try {
    detail.value = await questionApi.answerDetail(answerId.value);
  } catch (err: any) {
    loadFailed.value = true;
    loadErrorText.value = err?.message || "加载回答失败";
    uni.showToast({ title: loadErrorText.value, icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function toggleLike() {
  if (!detail.value) return;
  try {
    detail.value = await questionApi.toggleAnswerLike(answerId.value);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  }
}

async function toggleFavorite() {
  if (!detail.value) return;
  try {
    detail.value = await questionApi.toggleAnswerFavorite(answerId.value);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  }
}

async function submitComment() {
  const content = commentInput.value.trim();
  if (!content) {
    uni.showToast({ title: "请输入评论内容", icon: "none" });
    return;
  }
  if (posting.value) return;
  posting.value = true;
  try {
    await questionApi.createAnswerComment(answerId.value, content, replyParentId.value || undefined);
    commentInput.value = "";
    cancelReply();
    await loadDetail();
  } catch (err: any) {
    uni.showToast({ title: err?.message || "评论失败", icon: "none" });
  } finally {
    posting.value = false;
  }
}

onLoad((options) => {
  answerId.value = Number(options?.id || options?.answerId || 0);
  if (!answerId.value) {
    loadFailed.value = true;
    loadErrorText.value = "回答ID无效";
    return;
  }
  loadDetail();
});

onShow(() => {
  if (!ensurePageAuth()) return;
  if (answerId.value) {
    loadDetail();
  }
});
</script>

<template>
  <view class="page">
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!detail" class="state">{{ loadFailed ? (loadErrorText || "回答不存在或已删除") : "未找到回答" }}</view>
    <view v-else>
      <view class="question-title">问题：{{ detail.questionTitle }}</view>

      <view class="answer-card">
        <view class="author-row">
          <image v-if="detail.answer.authorAvatar" class="avatar" :src="detail.answer.authorAvatar" mode="aspectFill" />
          <view v-else class="avatar-fallback">{{ defaultAvatarText }}</view>
          <view class="author-info">
            <view class="author-name">
              {{ detail.answer.authorName || "匿名用户" }}
              <text v-if="detail.answer.bestAnswer" class="best-tag">最佳回答</text>
            </view>
            <view class="author-time">{{ detail.answer.createdAt || "" }}</view>
          </view>
        </view>

        <view class="content">{{ detail.answer.content }}</view>

        <view v-if="detail.answer.imageUrls?.length" class="images-wrap">
          <image
            v-for="(url, idx) in detail.answer.imageUrls"
            :key="url + idx"
            class="img"
            :src="url"
            mode="aspectFill"
            @tap="previewImages(idx, detail.answer.imageUrls || [])"
          />
        </view>

        <view class="answer-actions">
          <view class="mini-action like-btn" :class="{ active: detail.answer.liked }" @click="toggleLike">
            <text class="mini-icon">❤</text>
            <text>{{ detail.answer.likeCount || 0 }}</text>
          </view>
          <view class="mini-action" :class="{ active: detail.answer.favorited }" @click="toggleFavorite">
            <text class="mini-icon">⭐</text>
            <text>{{ detail.answer.favoriteCount || 0 }}</text>
          </view>
          <view class="mini-action">
            <text class="mini-icon">💬</text>
            <text>{{ detail.answer.commentCount || 0 }}</text>
          </view>
        </view>
      </view>

      <view class="section-title">评论 ({{ comments.length }})</view>
      <view v-if="!comments.length" class="empty">还没有评论</view>

      <view v-for="root in rootComments" :key="root.id" class="comment-card">
        <view class="comment-main">
          <view class="author-row">
            <image v-if="root.authorAvatar" class="avatar mini" :src="root.authorAvatar" mode="aspectFill" />
            <view v-else class="avatar-fallback mini">{{ defaultAvatarText }}</view>
            <view class="author-info">
              <view class="author-name small">{{ root.authorName || "匿名用户" }}</view>
              <view class="author-time">{{ root.createdAt || "" }}</view>
            </view>
            <view class="reply-btn" @click="startReply(root)">↩</view>
          </view>
          <view class="comment-content">{{ root.content }}</view>
        </view>

        <view v-if="childrenOf(root.id).length" class="child-list">
          <view v-for="child in childrenOf(root.id)" :key="child.id" class="child-item">
            <view class="author-row">
              <image v-if="child.authorAvatar" class="avatar mini" :src="child.authorAvatar" mode="aspectFill" />
              <view v-else class="avatar-fallback mini">{{ defaultAvatarText }}</view>
              <view class="author-info">
                <view class="author-name small">{{ child.authorName || "匿名用户" }}</view>
                <view class="author-time">{{ child.createdAt || "" }}</view>
              </view>
              <view class="reply-btn" @click="startReply(child)">↩</view>
            </view>
            <view class="comment-content">{{ child.content }}</view>
          </view>
        </view>
      </view>

      <view class="comment-input-wrap">
        <view v-if="replyParentId" class="replying">
          <text>回复 {{ replyToName }}</text>
          <text class="cancel-reply" @click="cancelReply">取消</text>
        </view>
        <input v-model="commentInput" class="comment-input" :placeholder="commentPlaceholder" />
        <button class="post-btn" @click="submitComment">{{ posting ? "发送中" : "发送" }}</button>
      </view>
    </view>
  </view>
</template>

<style lang="scss">
page {
  background: #ebe7dd;
}

.page {
  min-height: 100vh;
  background: #ebe7dd;
  padding: 16rpx 14rpx 24rpx;
}

.question-title {
  margin: 8rpx 4rpx 12rpx;
  font-size: 30rpx;
  color: #2f3d4f;
  font-weight: 700;
}

.answer-card,
.comment-card,
.empty {
  background: #f8f8f6;
  border: 2rpx solid #d5d9df;
  border-radius: 20rpx;
}

.answer-card {
  padding: 18rpx;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.avatar,
.avatar-fallback {
  width: 58rpx;
  height: 58rpx;
  border-radius: 50%;
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
  width: 46rpx;
  height: 46rpx;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.author-name {
  font-size: 28rpx;
  color: #2f3d4f;
  font-weight: 700;
}

.author-name.small {
  font-size: 24rpx;
}

.author-time {
  font-size: 22rpx;
  color: #8795a7;
}

.best-tag {
  margin-left: 10rpx;
  padding: 0 10rpx;
  border-radius: 10rpx;
  background: #f7d37e;
  color: #6f5213;
  font-size: 20rpx;
}

.content {
  margin-top: 12rpx;
  line-height: 1.7;
  color: #4f5f72;
}

.images-wrap {
  margin-top: 10rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.img {
  width: 210rpx;
  height: 210rpx;
  border-radius: 12rpx;
}

.answer-actions {
  margin-top: 12rpx;
  display: flex;
  gap: 10rpx;
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

.section-title {
  margin: 18rpx 4rpx 10rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #2f3d4f;
}

.empty {
  padding: 20rpx;
  text-align: center;
  color: #8fa0b5;
}

.comment-card {
  padding: 14rpx;
  margin-bottom: 10rpx;
}

.comment-content {
  margin-top: 8rpx;
  color: #4b5e74;
  line-height: 1.6;
}

.reply-btn {
  margin-left: auto;
  width: 44rpx;
  height: 44rpx;
  border-radius: 22rpx;
  border: 2rpx solid #d3dce9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6f8199;
}

.child-list {
  margin-top: 10rpx;
  padding-left: 20rpx;
  border-left: 4rpx solid #dbe2ec;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.child-item {
  padding: 10rpx;
  border-radius: 12rpx;
  background: #f2f4f8;
}

.comment-input-wrap {
  margin-top: 16rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-wrap: wrap;
}

.replying {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #6f8199;
  font-size: 22rpx;
}

.cancel-reply {
  color: #2d89be;
}

.comment-input {
  flex: 1;
  min-width: 0;
  height: 70rpx;
  border: 2rpx solid #d3dce8;
  border-radius: 14rpx;
  padding: 0 16rpx;
  background: #fff;
}

.post-btn {
  width: 104rpx;
  height: 62rpx;
  line-height: 62rpx;
  font-size: 24rpx;
  border: none;
  border-radius: 14rpx;
  background: #4ea5d7;
  color: #fff;
}

.state {
  margin-top: 120rpx;
  text-align: center;
  color: #8ba0b3;
}
</style>
