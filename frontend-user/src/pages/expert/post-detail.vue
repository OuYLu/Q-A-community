<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { expertApi, type AppExpertPostDetailVO } from "@/api/expert";
import { BASE_URL } from "@/utils/constants";

const postId = ref(0);
const loading = ref(false);
const detail = ref<AppExpertPostDetailVO | null>(null);
const officialAvatar = `${BASE_URL}/api/common/avatar/staff.png`;

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

async function loadDetail() {
  if (!postId.value) return;
  loading.value = true;
  try {
    detail.value = await expertApi.detail(postId.value);
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
      <view class="app-card header">
        <view class="author-row">
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
      </view>

      <view v-if="contentBlocks.length" class="app-card body">
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
      </view>
      <view v-else class="app-card body">
        <view class="text-block">{{ detail.content || "暂无正文" }}</view>
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

.header,
.body {
  padding: 18rpx;
  margin-bottom: 12rpx;
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
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  font-size: 23rpx;
  color: #8ea1b2;
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

.state {
  text-align: center;
  color: #8ea1b2;
  margin-top: 140rpx;
}
</style>