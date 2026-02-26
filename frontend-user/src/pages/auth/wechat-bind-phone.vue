<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { authApi } from "@/api/auth";
import { useAuthStore } from "@/stores/auth";
import { meApi } from "@/api/me";

const authStore = useAuthStore();
const bindTicket = ref("");
const redirect = ref("");
const nicknameHint = ref("微信用户");
const loading = ref(false);
const WX_PROFILE_CACHE_KEY = "wx_login_profile_cache";
const LOG_PREFIX = "[wechat-bind-phone]";

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

async function doBind(payload: { phoneCode: string }) {
  console.log(LOG_PREFIX, "doBind called", {
    hasBindTicket: Boolean(bindTicket.value),
    hasPhoneCode: Boolean(payload?.phoneCode),
    phoneCodePreview: payload?.phoneCode ? `${payload.phoneCode.slice(0, 6)}...` : ""
  });
  if (loading.value) {
    console.log(LOG_PREFIX, "doBind skipped because loading");
    return;
  }
  if (!bindTicket.value) {
    console.warn(LOG_PREFIX, "bindTicket missing");
    uni.showToast({ title: "缺少绑定凭证", icon: "none" });
    return;
  }
  loading.value = true;
  try {
    const cached = (uni.getStorageSync(WX_PROFILE_CACHE_KEY) || {}) as { nickname?: string; avatar?: string };
    const data = await authApi.wechatBindPhone({
      bindTicket: bindTicket.value,
      phoneCode: payload.phoneCode,
      nickname: cached.nickname,
      avatar: cached.avatar
    });
    console.log(LOG_PREFIX, "wechatBindPhone response", data);

    if (!data.token) {
      console.warn(LOG_PREFIX, "bind success but token missing", data);
      uni.showToast({ title: "绑定成功但登录信息缺失", icon: "none" });
      return;
    }

    authStore.applyLogin(
      {
        token: data.token,
        expiresAt: data.expiresAt,
        userId: data.userId
      },
      nicknameHint.value,
      data.username
    );

    if (cached.nickname || cached.avatar) {
      try {
        await meApi.updateProfile({
          nickname: cached.nickname,
          avatar: cached.avatar
        });
      } catch (e) {
        // non-blocking
      }
    }
    uni.removeStorageSync(WX_PROFILE_CACHE_KEY);

    uni.showToast({ title: "绑定并登录成功", icon: "success" });
    setTimeout(() => jumpAfterLogin(redirect.value), 180);
  } catch (err: any) {
    console.error(LOG_PREFIX, "wechatBindPhone failed", err);
    uni.showToast({ title: err?.message || "绑定失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function onGetPhoneNumber(e: any) {
  console.log(LOG_PREFIX, "getPhoneNumber callback", e?.detail);
  const phoneCode = e?.detail?.code;
  const errMsg = e?.detail?.errMsg;
  if (!phoneCode) {
    console.warn(LOG_PREFIX, "phone code missing", { errMsg, detail: e?.detail });
    uni.showToast({ title: errMsg || "未获取到手机号授权", icon: "none" });
    return;
  }
  doBind({ phoneCode });
}

onLoad((options) => {
  console.log(LOG_PREFIX, "onLoad options", options);
  bindTicket.value = decodeURIComponent((options?.bindTicket as string) || "");
  redirect.value = decodeURIComponent((options?.redirect as string) || "");
  nicknameHint.value = decodeURIComponent((options?.nickname as string) || "微信用户");
  console.log(LOG_PREFIX, "onLoad parsed", {
    hasBindTicket: Boolean(bindTicket.value),
    redirect: redirect.value,
    nicknameHint: nicknameHint.value
  });
});
</script>

<template>
  <view class="page">
    <view class="header-title">授权登录</view>

    <view class="mask" />

    <view class="sheet">
      <view class="brand-row">
        <view class="brand-avatar">健</view>
        <view class="brand-name">健康问问</view>
      </view>

      <view class="sheet-title">申请获取并验证你的手机号</view>
      <view class="sheet-sub">用于账号安全验证与服务通知</view>

      <button
        class="phone-btn"
        open-type="getPhoneNumber"
        :loading="loading"
        :disabled="loading"
        @getphonenumber="onGetPhoneNumber"
      >
        微信授权手机号
      </button>

      <view class="manage">管理号码</view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f7f5eb;
  position: relative;
  overflow: hidden;
}

.header-title {
  text-align: center;
  padding-top: 120rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: #1b2f43;
}

.mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.28);
}

.sheet {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-top-left-radius: 28rpx;
  border-top-right-radius: 28rpx;
  padding: 30rpx 28rpx 40rpx;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.brand-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 2rpx solid #9bd1ef;
  color: #2f9ad2;
  background: #f3eed7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.brand-name {
  font-size: 34rpx;
  font-weight: 700;
}

.sheet-title {
  margin-top: 22rpx;
  font-size: 52rpx;
  font-weight: 700;
}

.sheet-sub {
  margin-top: 10rpx;
  color: #909eaa;
}

.phone-btn {
  margin-top: 22rpx;
  border-radius: 20rpx;
  height: 90rpx;
  line-height: 90rpx;
  font-size: 34rpx;
}

.phone-btn {
  background: #4ba7d9;
  color: #fff;
  border: none;
}

.manage {
  margin-top: 24rpx;
  text-align: center;
  color: #5f6f96;
}
</style>
