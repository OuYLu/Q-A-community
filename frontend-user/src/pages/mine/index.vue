﻿﻿<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useAuthStore } from "@/stores/auth";
import { meApi, type AppMeOverviewVO } from "@/api/me";
import { BASE_URL } from "@/utils/constants";

const authStore = useAuthStore();
const loading = ref(false);
const overview = ref<AppMeOverviewVO | null>(null);
const needLogin = computed(() => !authStore.isLogin);

const displayNickname = computed(() => overview.value?.nickname || authStore.user?.nickname || "微信用户");
const displaySlogan = computed(
  () => overview.value?.slogan || authStore.user?.slogan || "这个人很懒，还没有填写个性签名"
);
const isVerifiedExpert = computed(() => overview.value?.expertStatus === 3);
const expertCapsule = computed(() => {
  const text = (overview.value?.expertExpertise || "").trim();
  if (!text) return "已认证专家";
  return text.length > 14 ? `${text.slice(0, 14)}...` : text;
});
const avatarUrl = computed(() => {
  const avatar = overview.value?.avatar || authStore.user?.avatar || "";
  if (!avatar) return "";
  if (avatar.startsWith("http://") || avatar.startsWith("https://")) return avatar;
  return `${BASE_URL}${avatar}`;
});

const profileStats = computed(() => {
  const base = [
    { label: "提问", value: overview.value?.questionCount ?? 0 },
    { label: "回答", value: overview.value?.answerCount ?? 0 },
    { label: "获赞", value: overview.value?.likeReceivedCount ?? 0 },
    { label: "粉丝", value: overview.value?.followerCount ?? 0 }
  ];
  if (isVerifiedExpert.value) {
    base.push({ label: "科普", value: overview.value?.expertPostCount ?? 0 });
  }
  return base;
});

const myContent = computed(() => {
  const rows = [
    { icon: "🔖", title: "我的收藏", value: String(overview.value?.favoriteCount ?? 0), type: "favorites" },
    { icon: "🕒", title: "浏览历史", value: String(overview.value?.historyCount ?? 0), type: "history" },
    { icon: "💬", title: "我的提问", value: String(overview.value?.questionCount ?? 0), type: "questions" },
    { icon: "⭐", title: "我的回答", value: String(overview.value?.answerCount ?? 0), type: "answers" },
    { icon: "🗂️", title: "专题关注", value: String(overview.value?.topicFollowCount ?? 0), type: "topic-following" }
  ];
  if (isVerifiedExpert.value) {
    rows.push({
      icon: "📚",
      title: "我的科普",
      value: String(overview.value?.expertPostCount ?? 0),
      type: "expert-posts"
    });
  }
  return rows;
});

type SocialItem = {
  icon?: string;
  iconImage?: string;
  title: string;
  value?: string;
  type: "following" | "followers" | "topic-following" | "expert-apply";
  disabled?: boolean;
};

const social = computed<SocialItem[]>(() => [
  { icon: "👥", title: "关注", value: String(overview.value?.followingCount ?? 0), type: "following" },
  { icon: "🤍", title: "粉丝", value: String(overview.value?.followerCount ?? 0), type: "followers" },
  buildExpertSocialItem(overview.value?.expertStatus)
]);

const others = [
  { icon: "⚙️", title: "设置", type: "settings" },
  { icon: "❓", title: "帮助与反馈", type: "help" },
  { icon: "📄", title: "用户协议", type: "user-agreement" },
  { icon: "🛡️", title: "隐私政策", type: "privacy-policy" }
];

function logout() {
  authStore.logout();
  uni.reLaunch({ url: "/pages/home/index" });
}

function goLogin() {
  uni.navigateTo({
    url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/mine/index")}`
  });
}

function editProfile() {
  uni.navigateTo({ url: "/pages/mine/edit-profile" });
}

function openList(type: string) {
  uni.navigateTo({ url: `/pages/mine/list?type=${type}` });
}

function openSocial(type: string) {
  if (type === "expert-apply") {
    const status = overview.value?.expertStatus;
    if (status === 2) {
      uni.showToast({ title: "专家认证审核中", icon: "none" });
      return;
    }
    if (status === 3) {
      uni.showToast({ title: "专家认证已通过", icon: "none" });
      return;
    }
    uni.navigateTo({ url: "/pages/mine/expert-apply" });
    return;
  }
  openList(type);
}

function buildExpertSocialItem(expertStatus?: number | null): SocialItem {
  if (expertStatus === 2) {
    return {
      iconImage: "/static/tabbar/expert.png",
      title: "专家认证",
      value: "审核中",
      type: "expert-apply",
      disabled: true
    };
  }
  if (expertStatus === 3) {
    return {
      iconImage: "/static/tabbar/expert.png",
      title: "专家认证",
      value: "已通过",
      type: "expert-apply",
      disabled: true
    };
  }
  if (expertStatus === 4) {
    return {
      iconImage: "/static/tabbar/expert.png",
      title: "重新申请专家",
      value: "已驳回",
      type: "expert-apply"
    };
  }
  return {
    iconImage: "/static/tabbar/expert.png",
    title: "申请专家",
    value: "未申请",
    type: "expert-apply"
  };
}

function openDoc(type: string) {
  uni.navigateTo({ url: `/pages/mine/doc?type=${type}` });
}

function openOther(type: string) {
  if (type === "settings") {
    uni.navigateTo({ url: "/pages/mine/settings" });
    return;
  }
  openDoc(type);
}

function formatJoinedAt(value?: string) {
  if (!value) return "--";
  const normalized = value.replace("T", " ").replace(/-/g, "/");
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return value;
  const y = date.getFullYear();
  const m = `${date.getMonth() + 1}`.padStart(2, "0");
  return `${y}年${m}月`;
}

async function loadOverview() {
  loading.value = true;
  try {
    overview.value = await meApi.overview();
    authStore.user = {
      ...(authStore.user || {}),
      userId: overview.value.userId,
      username: overview.value.username || authStore.user?.username,
      nickname: overview.value.nickname || authStore.user?.nickname || "微信用户",
      avatar: overview.value.avatar || authStore.user?.avatar,
      slogan: overview.value.slogan || authStore.user?.slogan
    };
  } catch (err: any) {
    if (String(err?.message || "").includes("401")) {
      authStore.logout();
      return;
    }
    uni.showToast({ title: "我的页面加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (needLogin.value) return;
  loadOverview();
});
</script>

<template>
  <view class="page">
    <view v-if="needLogin" class="auth-card app-card">
      <view class="auth-title">登录后可查看我的页面</view>
      <view class="auth-sub">收藏、历史、提问和个人资料仅登录后可用</view>
      <button class="auth-btn" @click="goLogin">去登录</button>
    </view>

    <template v-else>
    <view class="profile-card app-card">
      <view class="head-row">
        <image v-if="avatarUrl" class="avatar-img" :src="avatarUrl" mode="aspectFill" />
        <view v-else class="avatar">健</view>
        <view class="user-meta">
          <view class="name-row">
            <view class="name">{{ displayNickname }}</view>
            <image v-if="isVerifiedExpert" class="expert-badge" src="/static/tabbar/expert-active.png" mode="aspectFit" />
            <view v-if="isVerifiedExpert" class="expert-pill">{{ expertCapsule }}</view>
          </view>
          <view class="desc">{{ displaySlogan }}</view>
        </view>
        <button class="edit-btn" @click="editProfile">编辑资料</button>
      </view>

      <view class="stat-row">
        <view v-for="item in profileStats" :key="item.label" class="stat-item">
          <view class="stat-value">{{ item.value }}</view>
          <view class="stat-label">{{ item.label }}</view>
        </view>
      </view>

    </view>

    <view class="section-title">我的内容</view>
    <view class="list-card">
      <view v-for="item in myContent" :key="item.title" class="list-row" @click="openList(item.type)">
        <view class="row-left">
          <view class="icon">{{ item.icon }}</view>
          <text class="row-title">{{ item.title }}</text>
        </view>
        <view class="row-right">
          <text class="pill">{{ item.value }}</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <view class="section-title">社交</view>
    <view class="list-card">
      <view v-for="item in social" :key="item.title" class="list-row" :class="{ 'row-disabled': item.disabled }" @click="openSocial(item.type)">
        <view class="row-left">
          <view class="icon">
            <image v-if="item.iconImage" class="icon-image" :src="item.iconImage" mode="aspectFit" />
            <text v-else>{{ item.icon }}</text>
          </view>
          <text class="row-title">{{ item.title }}</text>
        </view>
        <view class="row-right">
          <text v-if="item.value !== undefined" class="pill">{{ item.value }}</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <view class="section-title">其他</view>
    <view class="list-card">
      <view v-for="item in others" :key="item.title" class="list-row" @click="openOther(item.type)">
        <view class="row-left">
          <view class="icon">{{ item.icon }}</view>
          <text class="row-title">{{ item.title }}</text>
        </view>
        <view class="row-right">
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <view class="join-time">{{ formatJoinedAt(overview?.joinedAt) }}加入</view>
    <button class="logout-bottom" @click="logout" :loading="loading">退出登录</button>
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

.expert-badge {
  width: 32rpx;
  height: 32rpx;
}

.expert-pill {
  max-width: 300rpx;
  height: 34rpx;
  line-height: 34rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background: #eaf4fb;
  color: #2f6f99;
  font-size: 20rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.desc {
  margin-top: 6rpx;
  color: #7f95a8;
  line-height: 1.3;
}

.edit-btn {
  margin: 0;
  height: 68rpx;
  line-height: 68rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  border: 2rpx solid #eadfbf;
  border-radius: 18rpx;
  background: #f3eed7;
  color: #3e5b72;
}

.stat-row {
  margin-top: 22rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f0ead7;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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

.section-title {
  margin: 24rpx 0 10rpx;
  font-size: 36rpx;
  font-weight: 700;
  color: #8ea1b2;
}

.list-card {
  background: #fff;
  border: 2rpx solid #eadfbf;
  border-radius: 24rpx;
  overflow: hidden;
}

.list-row {
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22rpx;
  border-bottom: 1rpx solid #f3efdf;
}

.row-disabled {
  opacity: 0.9;
}

.list-row:last-child {
  border-bottom: none;
}

.row-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.icon {
  width: 54rpx;
  height: 54rpx;
  border-radius: 50%;
  background: #f3eed7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.icon-image {
  width: 34rpx;
  height: 34rpx;
}

.row-title {
  font-size: 36rpx;
  font-weight: 600;
}

.row-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.pill {
  min-width: 50rpx;
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  background: #f3eed7;
  text-align: center;
  color: #8ea1b2;
}

.arrow {
  color: #8ea1b2;
  font-size: 36rpx;
}

.join-time {
  text-align: center;
  color: #8ea1b2;
  margin: 32rpx 0 10rpx;
}

.logout-bottom {
  margin-top: 10rpx;
  border: none;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
}
</style>
