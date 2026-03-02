<script setup lang="ts">
import { computed, ref } from "vue";
import { onPullDownRefresh, onReachBottom, onShow } from "@dcloudio/uni-app";
import {
  notificationApi,
  type AppNotificationItemVO,
  type AppNotificationUnreadCountVO
} from "@/api/notification";
import { useAuthStore } from "@/stores/auth";
import { refreshNoticeTabDot, syncNoticeTabDot } from "@/utils/notice-badge";

type TypeTabKey = "all" | "system" | "interaction" | "comment" | "answer";
type ReadTabKey = "all" | "unread" | "read";

const items = ref<AppNotificationItemVO[]>([]);
const unread = ref<AppNotificationUnreadCountVO | null>(null);
const loading = ref(false);
const page = ref(1);
const pageSize = 10;
const total = ref(0);
const hasMore = ref(true);
const selectedType = ref<TypeTabKey>("all");
const selectedRead = ref<ReadTabKey>("all");
const authStore = useAuthStore();
const needLogin = computed(() => !authStore.isLogin);

const typeTabs: Array<{ key: TypeTabKey; label: string; types?: string }> = [
  { key: "all", label: "全部" },
  { key: "system", label: "系统", types: "1,7" },
  { key: "interaction", label: "互动", types: "2,3" },
  { key: "comment", label: "评论", types: "5" },
  { key: "answer", label: "回答", types: "6" }
];

const readTabs: Array<{ key: ReadTabKey; label: string; isRead?: number }> = [
  { key: "all", label: "全部" },
  { key: "unread", label: "未读", isRead: 0 },
  { key: "read", label: "已读", isRead: 1 }
];

function currentTypeParams() {
  const tab = typeTabs.find((x) => x.key === selectedType.value);
  return tab?.types ? { types: tab.types } : {};
}

function currentReadParams() {
  const tab = readTabs.find((x) => x.key === selectedRead.value);
  return tab?.isRead === 0 || tab?.isRead === 1 ? { isRead: tab.isRead } : {};
}

async function loadUnread() {
  try {
    unread.value = await notificationApi.unreadCount();
    syncNoticeTabDot(Number(unread.value?.total || 0));
  } catch {
    unread.value = null;
    syncNoticeTabDot(0);
  } finally {
    await refreshNoticeTabDot();
  }
}

async function loadList(reset = false) {
  if (needLogin.value || loading.value || (!reset && !hasMore.value)) return;
  if (reset) {
    page.value = 1;
    total.value = 0;
    hasMore.value = true;
  }
  loading.value = true;
  try {
    const data = await notificationApi.list({
      page: page.value,
      pageSize,
      ...currentTypeParams(),
      ...currentReadParams()
    });
    const rows = data.list || [];
    items.value = reset ? rows : [...items.value, ...rows];
    total.value = Number(data.total || 0);
    hasMore.value = items.value.length < total.value;
    if (rows.length) page.value += 1;
  } catch {
    if (reset) items.value = [];
    uni.showToast({ title: "通知加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function refreshAll() {
  await Promise.all([loadUnread(), loadList(true)]);
}

function typeText(type?: number) {
  const map: Record<number, string> = {
    1: "系统",
    2: "点赞",
    3: "收藏",
    4: "关注",
    5: "评论",
    6: "回答",
    7: "举报反馈"
  };
  return type ? map[type] || "消息" : "消息";
}

async function markRead(id: number) {
  try {
    await notificationApi.read(id);
    const idx = items.value.findIndex((x) => x.id === id);
    if (idx >= 0) items.value[idx] = { ...items.value[idx], isRead: 1 };
    const nextTotal = Math.max(0, Number(unread.value?.total || 0) - 1);
    if (unread.value) unread.value = { ...unread.value, total: nextTotal };
    syncNoticeTabDot(nextTotal);
    await loadUnread();
  } catch {
    uni.showToast({ title: "操作失败", icon: "none" });
  }
}

function jumpBiz(item: AppNotificationItemVO) {
  if (item.type === 7 && item.bizId) {
    uni.navigateTo({ url: `/pages/notice/report-feedback?id=${item.bizId}` });
    return;
  }
  if (item.bizType === 1 && item.bizId) {
    uni.navigateTo({ url: `/pages/question/detail?id=${item.bizId}` });
    return;
  }
  if (item.bizType === 2 && item.bizId) {
    uni.navigateTo({ url: `/pages/question/answer-detail?id=${item.bizId}` });
  }
}

async function onClickItem(item: AppNotificationItemVO) {
  if (item.isRead !== 1) {
    await markRead(item.id);
  }
  jumpBiz(item);
}

async function markReadAll() {
  try {
    await notificationApi.readAll();
    items.value = items.value.map((x) => ({ ...x, isRead: 1 }));
    if (unread.value) unread.value = { ...unread.value, total: 0 };
    syncNoticeTabDot(0);
  } catch {
    uni.showToast({ title: "操作失败", icon: "none" });
  } finally {
    await loadUnread();
  }
}

function switchType(key: TypeTabKey) {
  if (selectedType.value === key) return;
  selectedType.value = key;
  loadList(true);
}

function switchRead(key: ReadTabKey) {
  if (selectedRead.value === key) return;
  selectedRead.value = key;
  loadList(true);
}

function goLogin() {
  uni.navigateTo({
    url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/notice/index")}`
  });
}

onShow(async () => {
  if (needLogin.value) {
    items.value = [];
    unread.value = null;
    syncNoticeTabDot(0);
    return;
  }
  await refreshAll();
});

onReachBottom(() => {
  if (!needLogin.value) loadList(false);
});

onPullDownRefresh(async () => {
  if (!needLogin.value) await refreshAll();
  uni.stopPullDownRefresh();
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

      <view class="line-tabs">
        <view
          v-for="tab in typeTabs"
          :key="tab.key"
          class="line-tab type-tab"
          :class="{ active: selectedType === tab.key }"
          @click="switchType(tab.key)"
        >
          {{ tab.label }}
        </view>
      </view>
      <view class="line-tabs read-tabs">
        <view
          v-for="tab in readTabs"
          :key="tab.key"
          class="line-tab read-tab-btn"
          :class="{ active: selectedRead === tab.key }"
          @click="switchRead(tab.key)"
        >
          {{ tab.label }}
        </view>
      </view>

      <view v-if="loading && !items.length" class="state">加载中...</view>
      <view v-else-if="!items.length" class="state">暂无通知</view>
      <view v-else>
        <view
          v-for="item in items"
          :key="item.id"
          class="app-card notify-item"
          :class="{ read: item.isRead === 1 }"
          @click="onClickItem(item)"
        >
          <view v-if="item.isRead !== 1" class="unread-dot"></view>
          <view class="title-row">
            <text class="title">{{ item.title }}</text>
            <text class="type">{{ typeText(item.type) }}</text>
          </view>
          <view class="content">{{ item.content }}</view>
          <view class="time">{{ item.createdAt }}</view>
        </view>
        <view class="state load-more">{{ hasMore ? "上拉加载更多" : "没有更多了" }}</view>
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
  margin-bottom: 16rpx;
}

.action {
  color: #4ba7d9;
}

.line-tabs {
  display: flex;
  align-items: center;
  border-bottom: 2rpx solid #d7e4ef;
  margin-bottom: 10rpx;
}

.read-tabs {
  border-bottom: none;
  margin-bottom: 16rpx;
  gap: 12rpx;
}

.line-tab {
  position: relative;
  padding: 10rpx 18rpx 12rpx;
  color: #7f94a8;
  font-size: 25rpx;
}

.type-tab {
  flex: 1;
  text-align: center;
}

.type-tab + .type-tab::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  width: 2rpx;
  height: 24rpx;
  transform: translateY(-50%);
  background: #d7e4ef;
}

.line-tab.active {
  color: #2f8fc3;
  font-weight: 700;
}

.read-tab-btn {
  min-width: 120rpx;
  text-align: center;
  padding: 10rpx 0;
  border-radius: 26rpx;
  border: 2rpx solid #d7e4ef;
  background: #f5f8fb;
}

.read-tab-btn + .read-tab-btn::before {
  display: none;
}

.read-tab-btn.active {
  color: #2f8fc3;
  border-color: #83bee2;
  background: #e9f4fb;
}

.notify-item {
  position: relative;
  padding: 22rpx;
  margin-bottom: 12rpx;
}

.unread-dot {
  position: absolute;
  right: 12rpx;
  top: 12rpx;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #ff5a5f;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.title {
  font-size: 30rpx;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type {
  font-size: 22rpx;
  color: #8ba0b3;
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

.state.load-more {
  margin-top: 12rpx;
  margin-bottom: 10rpx;
}
</style>
