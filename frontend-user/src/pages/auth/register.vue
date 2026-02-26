<script setup lang="ts">
import { computed, ref } from "vue";
import { authApi } from "@/api/auth";
import { useAuthStore } from "@/stores/auth";
import { BASE_URL } from "@/utils/constants";

const authStore = useAuthStore();

const avatar = ref("");
const nickname = ref("");
const username = ref("");
const password = ref("");
const confirmPassword = ref("");
const phone = ref("");
const email = ref("");
const loading = ref(false);
const uploadingAvatar = ref(false);

const avatarPreview = computed(() => {
  if (!avatar.value) return "";
  if (avatar.value.startsWith("http://") || avatar.value.startsWith("https://")) return avatar.value;
  return `${BASE_URL}${avatar.value}`;
});

function valid() {
  const uname = username.value.trim();
  const pwd = password.value;
  const cpwd = confirmPassword.value;
  const mobile = phone.value.trim();

  if (!nickname.value.trim()) {
    uni.showToast({ title: "昵称必填", icon: "none" });
    return false;
  }
  if (!uname) {
    uni.showToast({ title: "用户名必填", icon: "none" });
    return false;
  }
  if (!/^[A-Za-z0-9_]{8,26}$/.test(uname)) {
    uni.showToast({ title: "用户名需8-26位字母数字或下划线", icon: "none" });
    return false;
  }
  if (!pwd) {
    uni.showToast({ title: "密码必填", icon: "none" });
    return false;
  }
  if (pwd.length < 6) {
    uni.showToast({ title: "密码至少6位", icon: "none" });
    return false;
  }
  if (pwd !== cpwd) {
    uni.showToast({ title: "两次密码不一致", icon: "none" });
    return false;
  }
  if (!mobile) {
    uni.showToast({ title: "手机号必填", icon: "none" });
    return false;
  }
  if (!/^1\d{10}$/.test(mobile)) {
    uni.showToast({ title: "手机号格式不正确", icon: "none" });
    return false;
  }
  if (email.value.trim() && !/^\S+@\S+\.\S+$/.test(email.value.trim())) {
    uni.showToast({ title: "邮箱格式不正确", icon: "none" });
    return false;
  }
  return true;
}

async function chooseAvatar() {
  uploadingAvatar.value = true;
  try {
    const choose = await uni.chooseImage({ count: 1, sizeType: ["compressed"], sourceType: ["album", "camera"] });
    const filePath = choose.tempFilePaths?.[0];
    if (!filePath) return;

    const upload = await uni.uploadFile({
      url: `${BASE_URL}/api/common/avatar/upload`,
      filePath,
      name: "file",
      header: authStore.token
        ? {
            Authorization: `Bearer ${authStore.token}`
          }
        : undefined
    });

    if (upload.statusCode < 200 || upload.statusCode >= 300) {
      throw new Error(`HTTP ${upload.statusCode}`);
    }

    const body = JSON.parse(upload.data || "{}");
    const url = body?.data?.url || body?.data;
    if (!url) {
      throw new Error(body?.message || body?.msg || "上传失败");
    }
    avatar.value = url;
    uni.showToast({ title: "头像上传成功", icon: "success" });
  } catch (err: any) {
    uni.showToast({ title: err?.message || "头像上传失败", icon: "none" });
  } finally {
    uploadingAvatar.value = false;
  }
}

async function submit() {
  if (!valid()) return;
  loading.value = true;
  try {
    await authApi.register({
      username: username.value.trim(),
      password: password.value,
      nickname: nickname.value.trim(),
      phone: phone.value.trim(),
      email: email.value.trim() || undefined,
      avatar: avatar.value || undefined
    });

    const login = await authApi.login({
      username: username.value.trim(),
      password: password.value
    });
    authStore.applyLogin(login, nickname.value.trim() || username.value.trim(), username.value.trim());

    uni.showToast({ title: "注册成功", icon: "success" });
    setTimeout(() => {
      uni.switchTab({ url: "/pages/home/index" });
    }, 180);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "注册失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function goLogin() {
  uni.navigateBack();
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <view class="hero-title">创建你的账号</view>
      <view class="hero-sub">先完成注册，再自动登录进入健康问问</view>
    </view>

    <view class="panel app-card">
      <view class="label">头像</view>
      <view class="avatar-row">
        <view class="avatar-click" :class="{ disabled: uploadingAvatar }" @click="chooseAvatar">
          <image v-if="avatarPreview" :src="avatarPreview" class="avatar" mode="aspectFill" />
          <view v-else class="avatar placeholder">+</view>
        </view>
      </view>

      <view class="label">昵称 *</view>
      <input v-model="nickname" class="input" maxlength="64" placeholder="请输入昵称" />

      <view class="label">用户名 *</view>
      <input v-model="username" class="input" maxlength="26" placeholder="8-26位，仅字母数字和下划线" />

      <view class="label">密码 *</view>
      <input v-model="password" class="input" password maxlength="64" placeholder="至少 6 位" />

      <view class="label">确认密码 *</view>
      <input v-model="confirmPassword" class="input" password maxlength="64" placeholder="再次输入密码" />

      <view class="label">手机号 *</view>
      <input v-model="phone" class="input" maxlength="11" placeholder="请输入手机号" />

      <view class="label">邮箱（可选）</view>
      <input v-model="email" class="input" maxlength="100" placeholder="请输入邮箱" />

      <button class="btn" :loading="loading" @click="submit">注册并登录</button>
      <button class="link" :disabled="loading" @click="goLogin">已有账号？返回登录</button>

      <view class="tip">注册即代表同意《用户协议》与《隐私政策》</view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 28rpx;
}

.hero {
  margin: 10rpx 0 20rpx;
}

.hero-title {
  font-size: 46rpx;
  font-weight: 800;
  color: #264f73;
}

.hero-sub {
  margin-top: 10rpx;
  color: #8ea2b3;
  line-height: 1.5;
}

.panel {
  padding: 26rpx;
}

.label {
  margin-top: 16rpx;
  color: #5d768a;
  font-size: 26rpx;
}

.avatar-row {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
}

.avatar-click {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.avatar-click.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  border: 2rpx solid #9bd1ef;
  background: #fff;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52rpx;
  color: #9bb0c0;
}

.input {
  margin-top: 8rpx;
  height: 78rpx;
  border-radius: 18rpx;
  border: 2rpx solid #9bd1ef;
  background: #fff;
  padding: 0 20rpx;
}

.btn {
  margin-top: 28rpx;
  height: 78rpx;
  line-height: 78rpx;
  border: none;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
}

.link {
  margin-top: 10rpx;
  border: none;
  background: transparent;
  color: #2f9ad2;
  font-size: 26rpx;
}

.tip {
  margin-top: 10rpx;
  text-align: center;
  color: #9bb0c0;
  font-size: 24rpx;
}
</style>
