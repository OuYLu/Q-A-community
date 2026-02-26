<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { authApi } from "@/api/auth";
import { meApi } from "@/api/me";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();
const loading = ref(false);
const redirect = ref("");
const WX_PROFILE_CACHE_KEY = "wx_login_profile_cache";

function jumpAfterLogin(target?: string) {
  const to = target || "";
  if (to.startsWith("/pages/search/")) {
    uni.switchTab({ url: "/pages/search/index" });
    return;
  }
  if (to.startsWith("/pages/discover/")) {
    uni.switchTab({ url: "/pages/discover/index" });
    return;
  }
  if (to.startsWith("/pages/notice/")) {
    uni.switchTab({ url: "/pages/notice/index" });
    return;
  }
  if (to.startsWith("/pages/mine/")) {
    uni.switchTab({ url: "/pages/mine/index" });
    return;
  }
  if (to) {
    uni.redirectTo({ url: to });
    return;
  }
  uni.switchTab({ url: "/pages/home/index" });
}

async function getWechatProfileSafe(): Promise<{ nickname?: string; avatar?: string }> {
  try {
    const profile = await new Promise<any>((resolve, reject) => {
      uni.getUserProfile({
        desc: "用于完善账号资料",
        success: resolve,
        fail: reject
      });
    });
    const info = profile?.userInfo || {};
    return {
      nickname: info.nickName,
      avatar: info.avatarUrl
    };
  } catch {
    return {};
  }
}

async function doWechatAuthorize() {
  loading.value = true;
  try {
    const loginResult = await uni.login({ provider: "weixin" });
    if (!loginResult.code) {
      uni.showToast({ title: "微信授权失败", icon: "none" });
      return;
    }

    const profile = await getWechatProfileSafe();
    uni.setStorageSync(WX_PROFILE_CACHE_KEY, profile);

    const data = await authApi.wechatLogin({
      code: loginResult.code,
      nickname: profile.nickname,
      avatar: profile.avatar
    });

    if (data.needPhoneBind) {
      if (!data.bindTicket) {
        uni.showToast({ title: "缺少绑定凭证", icon: "none" });
        return;
      }
      const target = encodeURIComponent(redirect.value);
      const nickname = profile.nickname || "微信用户";
      uni.redirectTo({
        url: `/pages/auth/wechat-bind-phone?bindTicket=${encodeURIComponent(data.bindTicket)}&redirect=${target}&nickname=${encodeURIComponent(nickname)}`
      });
      return;
    }

    if (!data.token) {
      uni.showToast({ title: "登录返回无 token", icon: "none" });
      return;
    }

    authStore.applyLogin(
      {
        token: data.token,
        expiresAt: data.expiresAt,
        userId: data.userId
      },
      profile.nickname || "微信用户",
      data.username
    );

    if (profile.nickname || profile.avatar) {
      try {
        await meApi.updateProfile({
          nickname: profile.nickname,
          avatar: profile.avatar
        });
      } catch {
        // non-blocking
      }
    }

    uni.showToast({ title: data.newUser ? "注册并登录成功" : "微信登录成功", icon: "success" });
    setTimeout(() => jumpAfterLogin(redirect.value), 180);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "微信登录失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function cancelAndBack() {
  uni.navigateBack();
}

onLoad((options) => {
  redirect.value = decodeURIComponent((options?.redirect as string) || "");
});
</script>

<template>
  <view class="page">
    <view class="header">授权登录</view>
    <view class="overlay" />

    <view class="sheet app-card">
      <view class="brand">健康问问</view>
      <view class="title">申请获取你的昵称、头像与手机号</view>
      <view class="desc">用于个性化展示、账号安全验证与服务通知</view>

      <button class="btn primary" :loading="loading" @click="doWechatAuthorize">微信授权并继续</button>
      <button class="btn ghost" :disabled="loading" @click="cancelAndBack">暂不授权</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.header {
  text-align: center;
  padding-top: 120rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: #1b2f43;
}

.overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
}

.sheet {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  bottom: 40rpx;
  padding: 26rpx;
}

.brand {
  font-size: 34rpx;
  font-weight: 700;
}

.title {
  margin-top: 14rpx;
  font-size: 42rpx;
  font-weight: 700;
}

.desc {
  margin-top: 10rpx;
  color: #8ea1b2;
  line-height: 1.5;
}

.btn {
  margin-top: 20rpx;
  border-radius: 20rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  border: none;
}

.primary {
  background: #4ba7d9;
  color: #fff;
}

.ghost {
  background: #f3f3f3;
  color: #2f2f2f;
}
</style>
