<script setup lang="ts">
import { ref } from "vue";
import { useAuthStore } from "@/stores/auth";
import { authApi } from "@/api/auth";

const authStore = useAuthStore();
const username = ref("");
const password = ref("");
const accountLoading = ref(false);
const wechatLoading = ref(false);

function getRouteRedirect() {
  const pages = getCurrentPages();
  const route = pages[pages.length - 1];
  return decodeURIComponent((route.options?.redirect as string) || "");
}

function jumpAfterLogin(redirectRaw?: string) {
  const redirect = redirectRaw ?? getRouteRedirect();

  if (redirect.startsWith("/pages/search/")) {
    uni.switchTab({ url: "/pages/search/index" });
    return;
  }
  if (redirect.startsWith("/pages/discover/")) {
    uni.switchTab({ url: "/pages/discover/index" });
    return;
  }
  if (redirect.startsWith("/pages/notice/")) {
    uni.switchTab({ url: "/pages/notice/index" });
    return;
  }
  if (redirect.startsWith("/pages/mine/")) {
    uni.switchTab({ url: "/pages/mine/index" });
    return;
  }
  if (redirect) {
    uni.redirectTo({ url: redirect });
    return;
  }
  uni.switchTab({ url: "/pages/home/index" });
}

async function login() {
  if (!username.value.trim() || !password.value.trim()) {
    uni.showToast({ title: "请输入账号和密码", icon: "none" });
    return;
  }

  if (accountLoading.value || wechatLoading.value) return;
  accountLoading.value = true;
  try {
    const data = await authApi.login({
      username: username.value.trim(),
      password: password.value
    });
    authStore.applyLogin(data, undefined, username.value.trim());
    uni.showToast({ title: "登录成功", icon: "success" });
    setTimeout(() => jumpAfterLogin(), 180);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "登录失败", icon: "none" });
  } finally {
    accountLoading.value = false;
  }
}

function goRegister() {
  uni.navigateTo({ url: "/pages/auth/register" });
}

async function loginByWechat() {
  if (accountLoading.value || wechatLoading.value) return;
  wechatLoading.value = true;
  try {
    const loginResult = await uni.login({ provider: "weixin" });
    if (!loginResult.code) {
      uni.showToast({ title: "微信授权失败", icon: "none" });
      return;
    }

    const redirect = getRouteRedirect();
    const data = await authApi.wechatLogin({
      code: loginResult.code
    });

    if (data.needPhoneBind) {
      if (!data.bindTicket) {
        uni.showToast({ title: "缺少绑定凭证", icon: "none" });
        return;
      }
      const target = encodeURIComponent(redirect);
      uni.navigateTo({
        url: `/pages/auth/wechat-bind-phone?bindTicket=${encodeURIComponent(data.bindTicket)}&redirect=${target}&nickname=${encodeURIComponent("微信用户")}`
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
      "微信用户",
      data.username
    );

    uni.showToast({ title: data.newUser ? "注册并登录成功" : "微信登录成功", icon: "success" });
    setTimeout(() => jumpAfterLogin(redirect), 180);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "微信登录失败", icon: "none" });
  } finally {
    wechatLoading.value = false;
  }
}
</script>

<template>
  <view class="page">
    <view class="panel app-card">
      <view class="logo">健康问问</view>
      <view class="title">登录后使用完整功能</view>
      <view class="sub">搜索、发现、通知、我的页面都需要登录后访问</view>

      <view class="label">账号</view>
      <input v-model="username" class="input" maxlength="64" placeholder="请输入用户名或手机号" />

      <view class="label">密码</view>
      <input v-model="password" class="input" password maxlength="64" placeholder="请输入密码" />

      <button class="btn" :loading="accountLoading" :disabled="wechatLoading" @click="login">账号登录</button>
      <button class="btn ghost" :loading="wechatLoading" :disabled="accountLoading" @click="loginByWechat">微信登录</button>
      <button class="link" :disabled="accountLoading || wechatLoading" @click="goRegister">还没有账号？去注册</button>

      <view class="agreement">登录即代表同意《用户协议》与《隐私政策》</view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
  display: flex;
  align-items: center;
}

.panel {
  width: 100%;
  padding: 34rpx 28rpx;
}

.logo {
  font-size: 42rpx;
  font-weight: 800;
  color: #2b5f86;
}

.title {
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 700;
}

.sub {
  margin-top: 8rpx;
  color: #8ba0b3;
  line-height: 1.5;
}

.label {
  margin-top: 24rpx;
  color: #5d768a;
}

.input {
  margin-top: 10rpx;
  height: 78rpx;
  border-radius: 18rpx;
  border: 2rpx solid #9bd1ef;
  background: #fff;
  padding: 0 20rpx;
}

.btn {
  margin-top: 24rpx;
  height: 78rpx;
  line-height: 78rpx;
  border: none;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
}

.ghost {
  margin-top: 12rpx;
  background: #e9f5fc;
  color: #2f9ad2;
}

.link {
  margin-top: 14rpx;
  border: none;
  background: transparent;
  color: #2f9ad2;
  font-size: 26rpx;
}

.agreement {
  margin-top: 12rpx;
  color: #9bb0c0;
  font-size: 24rpx;
  text-align: center;
}
</style>
