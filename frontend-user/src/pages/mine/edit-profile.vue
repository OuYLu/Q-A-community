<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useAuthStore } from "@/stores/auth";
import { meApi, type AppMeOverviewVO } from "@/api/me";
import { ensurePageAuth } from "@/utils/auth-guard";
import { BASE_URL } from "@/utils/constants";

const authStore = useAuthStore();
const username = ref("");
const nickname = ref("");
const slogan = ref("");
const avatar = ref("");
const email = ref("");
const passwordSet = ref(0);
const submitting = ref(false);
const uploadingAvatar = ref(false);

const avatarPreview = computed(() => {
  if (!avatar.value) return "";
  if (avatar.value.startsWith("http://") || avatar.value.startsWith("https://")) return avatar.value;
  return `${BASE_URL}${avatar.value}`;
});

const passwordActionText = computed(() => (Number(passwordSet.value) === 1 ? "修改密码" : "设置密码"));

function fillFromOverview(data: AppMeOverviewVO) {
  username.value = data.username || authStore.user?.username || (data.userId ? `wx_${data.userId}` : "wx_user");
  nickname.value = data.nickname || authStore.user?.nickname || "";
  slogan.value = data.slogan || authStore.user?.slogan || "";
  avatar.value = data.avatar || authStore.user?.avatar || "";
  email.value = data.email || authStore.user?.email || "";
  passwordSet.value = Number(data.passwordSet || 0);
}

async function loadProfile() {
  const data = await meApi.overview();
  fillFromOverview(data);
}

onShow(async () => {
  if (!ensurePageAuth()) return;
  try {
    await loadProfile();
  } catch (err: any) {
    uni.showToast({ title: err?.message || "加载资料失败", icon: "none" });
  }
});

async function chooseAvatar() {
  uploadingAvatar.value = true;
  try {
    const choose = await uni.chooseImage({ count: 1, sizeType: ["compressed"], sourceType: ["album", "camera"] });
    const filePath = choose.tempFilePaths?.[0];
    if (!filePath) return;

    const upload = await uni.uploadFile({
      url: `${BASE_URL}/api/common/upload?bizType=avatar`,
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
    const url = body?.data?.url;
    if (!url) {
      throw new Error(body?.message || body?.msg || "头像上传失败");
    }
    avatar.value = url;
    uni.showToast({ title: "头像上传成功", icon: "success" });
  } catch (err: any) {
    uni.showToast({ title: err?.message || "头像上传失败", icon: "none" });
  } finally {
    uploadingAvatar.value = false;
  }
}

function validate() {
  const nick = nickname.value.trim();
  if (!nick || nick.length > 64) {
    uni.showToast({ title: "昵称长度不合法", icon: "none" });
    return false;
  }
  if (email.value.trim() && !/^\S+@\S+\.\S+$/.test(email.value.trim())) {
    uni.showToast({ title: "邮箱格式不正确", icon: "none" });
    return false;
  }
  return true;
}

async function submit() {
  if (!validate()) return;
  submitting.value = true;
  try {
    await meApi.updateProfile({
      nickname: nickname.value.trim(),
      slogan: slogan.value.trim() || undefined,
      avatar: avatar.value.trim() || undefined,
      email: email.value.trim() || undefined
    });

    authStore.user = {
      ...(authStore.user || {}),
      username: username.value.trim(),
      nickname: nickname.value.trim(),
      slogan: slogan.value.trim() || undefined,
      avatar: avatar.value.trim() || undefined,
      email: email.value.trim() || undefined
    };

    uni.showToast({ title: "资料已更新", icon: "success" });
    setTimeout(() => uni.navigateBack(), 250);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "更新失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}

function goPasswordPage() {
  const mode = Number(passwordSet.value) === 1 ? "modify" : "set";
  uni.navigateTo({ url: `/pages/mine/password?mode=${mode}` });
}
</script>

<template>
  <view class="page">
    <view class="panel app-card">
      <view class="label">头像</view>
      <view class="avatar-row">
        <view class="avatar-wrap" @click="chooseAvatar">
          <image v-if="avatarPreview" class="avatar" :src="avatarPreview" mode="aspectFill" />
          <view v-else class="avatar placeholder">健</view>
          <view class="avatar-tip">点击更换</view>
        </view>
      </view>

      <view class="label">用户名</view>
      <view class="readonly-input">
        <text class="readonly-text">{{ username || "-" }}</text>
        <text class="readonly-tag">不可修改</text>
      </view>

      <view class="label">昵称</view>
      <input v-model="nickname" class="input" maxlength="64" placeholder="请输入昵称" />

      <view class="label">个性签名</view>
      <textarea v-model="slogan" class="textarea" maxlength="200" placeholder="请输入个性签名" />

      <view class="label">邮箱（可选）</view>
      <input v-model="email" class="input" maxlength="100" placeholder="请输入邮箱" />

      <button class="password-btn" @click="goPasswordPage">{{ passwordActionText }}</button>
      <button class="submit" :disabled="submitting" :loading="submitting" @click="submit">保存</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 24rpx;
}

.panel {
  padding: 24rpx;
}

.label {
  margin: 14rpx 0 10rpx;
  color: #5d768a;
}

.avatar-row {
  display: flex;
  align-items: center;
}

.avatar-wrap {
  width: 140rpx;
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
  color: #2f9ad2;
  background: #f3eed7;
  font-size: 52rpx;
  font-weight: 700;
}

.avatar-tip {
  margin-top: 8rpx;
  color: #8ea1b2;
  font-size: 22rpx;
}

.input {
  height: 78rpx;
  border-radius: 18rpx;
  border: 2rpx solid #9bd1ef;
  background: #fff;
  padding: 0 20rpx;
}

.readonly-input {
  height: 78rpx;
  border-radius: 18rpx;
  border: 2rpx dashed #b7c9d8;
  background: #f4f8fb;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.readonly-text {
  color: #476277;
  font-size: 30rpx;
}

.readonly-tag {
  color: #7f97aa;
  font-size: 22rpx;
  background: #e7eff5;
  border-radius: 999rpx;
  padding: 6rpx 14rpx;
}

.textarea {
  min-height: 140rpx;
  border-radius: 18rpx;
  border: 2rpx solid #9bd1ef;
  background: #fff;
  padding: 16rpx 20rpx;
  width: 100%;
  box-sizing: border-box;
}

.password-btn {
  margin-top: 20rpx;
  height: 72rpx;
  line-height: 72rpx;
  border: 2rpx solid #9bd1ef;
  border-radius: 14rpx;
  background: #f2f8fc;
  color: #2f9ad2;
  font-size: 28rpx;
  font-weight: 500;
}

.submit {
  margin-top: 14rpx;
  border: none;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
}
</style>

