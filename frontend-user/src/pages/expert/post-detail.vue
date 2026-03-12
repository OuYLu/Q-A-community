<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { expertApi, type AppExpertPostDetailVO, type AppKbCommentVO, type AppKbInteractVO } from "@/api/expert";
import { openUserHomePage } from "@/utils/nav";
import { BASE_URL } from "@/utils/constants";

const postId = ref(0);
const loading = ref(false);
const detail = ref<AppExpertPostDetailVO | null>(null);
const interaction = ref<AppKbInteractVO | null>(null);
const comments = ref<AppKbCommentVO[]>([]);
const commentInput = ref("");
const posting = ref(false);
const replyParentId = ref<number | null>(null);
const replyToName = ref("");
const officialAvatar = `${BASE_URL}/api/common/avatar/staff.png`;
const defaultAvatarText = "用户";

function resolveMediaUrl(raw?: string | null) {
  const url = String(raw || "").trim();
  if (!url) return "";
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `${BASE_URL}${url}`;
  return `${BASE_URL}/${url}`;
}

const authorAvatarUrl = computed(() => resolveMediaUrl(detail.value?.authorAvatar));
const isOfficial = computed(() => detail.value?.source && detail.value.source !== "expert_post");
const showAvatar = computed(() => (isOfficial.value ? officialAvatar : authorAvatarUrl.value));
const authorName = computed(() => (isOfficial.value ? "问问官方" : detail.value?.authorName || "匿名用户"));
const liked = computed(() => Boolean(interaction.value?.liked));
const favorited = computed(() => Boolean(interaction.value?.favorited));
const likeCount = computed(() => Number(interaction.value?.likeCount ?? detail.value?.likeCount ?? 0));
const favoriteCount = computed(() => Number(interaction.value?.favoriteCount ?? detail.value?.favoriteCount ?? 0));
const commentCount = computed(() => Number(interaction.value?.commentCount ?? comments.value.length));

const contentBlocks = computed(() => {
  if (!detail.value?.contentBlocks?.length) return [];
  return detail.value.contentBlocks.map((x) => ({
    ...x,
    url: x.url ? resolveMediaUrl(x.url) : x.url
  }));
});

const imageUrls = computed(() => {
  const list = detail.value?.imageUrls || [];
  return list.map((x) => resolveMediaUrl(x)).filter(Boolean);
});

const commentMap = computed(() => {
  const map = new Map<number, AppKbCommentVO>();
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

function applyInteraction(data?: AppKbInteractVO | null) {
  if (!data) return;
  interaction.value = {
    kbEntryId: Number(data.kbEntryId || postId.value),
    likeCount: Number(data.likeCount ?? 0),
    favoriteCount: Number(data.favoriteCount ?? 0),
    commentCount: Number(data.commentCount ?? comments.value.length),
    liked: Boolean(data.liked),
    favorited: Boolean(data.favorited)
  };
}

async function loadComments() {
  comments.value = await expertApi.kbComments(postId.value);
}

async function loadDetail() {
  if (!postId.value) return;
  loading.value = true;
  try {
    const post = await expertApi.detail(postId.value);
    detail.value = post;
    const [interactRes, commentRes] = await Promise.all([
      expertApi.kbInteraction(postId.value).catch(() => null),
      expertApi.kbComments(postId.value).catch(() => [])
    ]);
    comments.value = commentRes || [];
    if (interactRes) {
      applyInteraction(interactRes);
    } else {
      interaction.value = {
        kbEntryId: postId.value,
        likeCount: Number(post.likeCount || 0),
        favoriteCount: Number(post.favoriteCount || 0),
        commentCount: comments.value.length,
        liked: false,
        favorited: false
      };
    }
    if (detail.value?.title) {
      uni.setNavigationBarTitle({ title: detail.value.title });
    }
  } catch (err: any) {
    uni.showToast({ title: err?.message || "加载科普详情失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function previewImage(current: string) {
  if (!current) return;
  const urls = imageUrls.value.length ? imageUrls.value : [current];
  uni.previewImage({ current, urls });
}

async function toggleLike() {
  if (!postId.value) return;
  try {
    const data = await expertApi.toggleKbLike(postId.value);
    applyInteraction(data);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "点赞失败", icon: "none" });
  }
}

async function toggleFavorite() {
  if (!postId.value) return;
  try {
    const data = await expertApi.toggleKbFavorite(postId.value);
    applyInteraction(data);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "收藏失败", icon: "none" });
  }
}

function openPostAuthor() {
  if (!detail.value?.authorUserId) return;
  openUserHomePage(Number(detail.value.authorUserId));
}

function openCommentAuthor(comment: AppKbCommentVO) {
  if (!comment?.authorId) return;
  openUserHomePage(Number(comment.authorId));
}

function startReply(comment: AppKbCommentVO) {
  replyParentId.value = comment.id;
  replyToName.value = comment.authorName || "用户";
}

function cancelReply() {
  replyParentId.value = null;
  replyToName.value = "";
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
    await expertApi.createKbComment(postId.value, content, replyParentId.value || undefined);
    commentInput.value = "";
    cancelReply();
    await loadComments();
    try {
      const latest = await expertApi.kbInteraction(postId.value);
      applyInteraction(latest);
    } catch {
      if (interaction.value) interaction.value.commentCount = comments.value.length;
    }
  } catch (err: any) {
    uni.showToast({ title: err?.message || "评论失败", icon: "none" });
  } finally {
    posting.value = false;
  }
}

onLoad((options) => {
  postId.value = Number(options?.id || 0);
  if (!postId.value) {
    uni.showToast({ title: "科普ID无效", icon: "none" });
    return;
  }
  loadDetail();
});
</script>

<template>
  <view class="page">
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!detail" class="state">暂无内容</view>
    <view v-else>
      <view class="app-card article-card">
        <view class="author-row" @click="openPostAuthor">
          <image v-if="showAvatar" class="avatar" :src="showAvatar" mode="aspectFill" />
          <view v-else class="avatar-fallback">{{ (authorName || "问").slice(0, 1) }}</view>
          <view class="author-info">
            <view class="author-name-row">
              <view class="author-name">{{ authorName }}</view>
              <view v-if="isOfficial" class="official-badge">官方</view>
            </view>
            <view class="author-sub">
              {{ isOfficial ? "官方科普" : detail.authorTitle || detail.authorExpertise || "健康科普作者" }}
            </view>
          </view>
        </view>

        <view class="title">{{ detail.title }}</view>
        <view v-if="detail.summary" class="summary">{{ detail.summary }}</view>
        <view class="meta">
          <text>{{ detail.createdAt || "" }}</text>
          <text>{{ detail.viewCount || 0 }} 浏览</text>
        </view>

        <view class="article-content">
          <template v-if="contentBlocks.length">
            <view v-for="(block, idx) in contentBlocks" :key="`b-${idx}`" class="block">
              <view v-if="block.type === 'text'" class="text-block">{{ block.text || "" }}</view>
              <image
                v-else-if="block.type === 'image' && block.url"
                class="img-block"
                :src="block.url"
                mode="widthFix"
                @tap="previewImage(block.url)"
              />
            </view>
          </template>
          <view v-else class="text-block">{{ detail.content || "暂无正文" }}</view>
        </view>
        <view class="article-actions-inline actions">
          <view class="action-btn like-btn" :class="{ active: liked }" @click="toggleLike">
            <text class="action-icon">👍</text>
            <text>{{ likeCount }}</text>
          </view>
          <view class="action-btn" :class="{ active: favorited }" @click="toggleFavorite">
            <text class="action-icon">♥</text>
            <text>{{ favoriteCount }}</text>
          </view>
          <view class="action-btn">
            <text class="action-icon">💬</text>
            <text>{{ commentCount }}</text>
          </view>
        </view>
      </view>

      <view class="section-title">评论 ({{ commentCount }})</view>
      <view v-if="!comments.length" class="empty">还没有评论</view>

      <view v-for="root in rootComments" :key="root.id" class="comment-card">
        <view class="comment-main">
          <view class="comment-author-row" @click.stop="openCommentAuthor(root)">
            <image v-if="root.authorAvatar" class="avatar mini" :src="root.authorAvatar" mode="aspectFill" />
            <view v-else class="avatar-fallback mini">{{ defaultAvatarText }}</view>
            <view class="author-info">
              <view class="author-name small">{{ root.authorName || "匿名用户" }}</view>
              <view class="author-time">{{ root.createdAt || "" }}</view>
            </view>
            <view class="reply-btn" @click.stop="startReply(root)">↩</view>
          </view>
          <view class="comment-content">{{ root.content }}</view>
        </view>

        <view v-if="childrenOf(root.id).length" class="child-list">
          <view v-for="child in childrenOf(root.id)" :key="child.id" class="child-item">
            <view class="comment-author-row" @click.stop="openCommentAuthor(child)">
              <image v-if="child.authorAvatar" class="avatar mini" :src="child.authorAvatar" mode="aspectFill" />
              <view v-else class="avatar-fallback mini">{{ defaultAvatarText }}</view>
              <view class="author-info">
                <view class="author-name small">{{ child.authorName || "匿名用户" }}</view>
                <view class="author-time">{{ child.createdAt || "" }}</view>
              </view>
              <view class="reply-btn" @click.stop="startReply(child)">↩</view>
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

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 18rpx 16rpx 24rpx;
  box-sizing: border-box;
}

.article-card {
  padding: 18rpx;
  margin-bottom: 12rpx;
}

.article-content {
  margin-top: 14rpx;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.avatar,
.avatar-fallback {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #6d7f90;
  background: #e9eef2;
}

.author-info {
  min-width: 0;
}

.author-name-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.author-name {
  font-size: 28rpx;
  line-height: 1.25;
  font-weight: 700;
  color: #20384e;
}

.official-badge {
  padding: 2rpx 10rpx;
  border-radius: 999rpx;
  border: 2rpx solid #3fa2d8;
  color: #2d89be;
  font-size: 22rpx;
}

.author-sub {
  margin-top: 4rpx;
  font-size: 23rpx;
  color: #7f95a7;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  color: #20384e;
  line-height: 1.4;
}

.summary {
  margin-top: 10rpx;
  font-size: 27rpx;
  color: #6c8297;
  line-height: 1.6;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  font-size: 23rpx;
  color: #8ea1b2;
}

.actions {
  display: flex;
  gap: 10rpx;
  justify-content: flex-end;
}

.article-actions-inline {
  margin-top: 14rpx;
}

.action-btn {
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

.action-icon {
  font-size: 24rpx;
  line-height: 1;
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

.block + .block {
  margin-top: 14rpx;
}

.text-block {
  font-size: 30rpx;
  color: #304b62;
  line-height: 1.8;
  white-space: pre-wrap;
}

.img-block {
  width: 100%;
  border-radius: 12rpx;
  background: #eef3f8;
}

.section-title {
  margin: 18rpx 4rpx 10rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #2f3d4f;
}

.empty,
.comment-card {
  background: #f8f8f6;
  border: 2rpx solid #d5d9df;
  border-radius: 20rpx;
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

.comment-author-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.avatar.mini,
.avatar-fallback.mini {
  width: 46rpx;
  height: 46rpx;
}

.author-name.small {
  font-size: 24rpx;
}

.author-time {
  font-size: 22rpx;
  color: #8795a7;
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

.comment-content {
  margin-top: 8rpx;
  color: #4b5e74;
  line-height: 1.6;
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
  text-align: center;
  color: #8ea1b2;
  margin-top: 140rpx;
}
</style>
