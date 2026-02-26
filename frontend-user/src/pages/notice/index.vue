<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import {
  notificationApi,
  type AppNotificationItemVO,
  type AppNotificationUnreadCountVO
} from "@/api/notification";
import { useAuthStore } from "@/stores/auth";

const items = ref<AppNotificationItemVO[]>([]);
const unread = ref<AppNotificationUnreadCountVO | null>(null);
const loading = ref(false);
const authStore = useAuthStore();
const needLogin = computed(() => !authStore.isLogin);

async function loadData() {
  loading.value = true;
  try {
    const [listData, unreadData] = await Promise.all([
      notificationApi.list({ page: 1, pageSize: 20 }),
      notificationApi.unreadCount()
    ]);
    items.value = listData.list || [];
    unread.value = unreadData;
  } catch {
    uni.showToast({ title: "通知加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function markReadAll() {
  try {
    await notificationApi.readAll();
    await loadData();
  } catch {
    uni.showToast({ title: "操作失败", icon: "none" });
  }
}

async function markRead(id: number) {
  try {
    await notificationApi.read(id);
    await loadData();
  } catch {
    uni.showToast({ title: "操作失败", icon: "none" });
  }
}

function goLogin() {
  uni.navigateTo({
    url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/notice/index")}`
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
      <view class="auth-title">登录后可查看通知</view>
      <view class="auth-sub">回复、点赞和系统通知仅登录后可查看</view>
      <button class="auth-btn" @click="goLogin">去登录</button>
    </view>

    <template v-else>
      <view class="head">
        <text>未读 {{ unread?.total || 0 }}</text>
        <text class="action" @click="markReadAll">全部已读</text>
      </view>

      <view v-if="loading" class="state">加载中...</view>
      <view v-else-if="!items.length" class="state">暂无通知</view>
      <view v-else>
        <view
          v-for="item in items"
          :key="item.id"
          class="app-card notify-item"
          :class="{ read: item.isRead }"
          @click="markRead(item.id)"
        >
          <view class="title">{{ item.title }}</view>
          <view class="content">{{ item.content }}</view>
          <view class="time">{{ item.createdAt }}</view>
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

.head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.action {
  color: #4ba7d9;
}

.notify-item {
  padding: 22rpx;
  margin-bottom: 12rpx;
}

.title {
  font-size: 30rpx;
  font-weight: 700;
}

.content,
.time {
  color: #8ba0b3;
  margin-top: 8rpx;
}

.read {
  opacity: 0.7;
}

.state {
  color: #8ba0b3;
  text-align: center;
  margin-top: 80rpx;
}
</style>
