<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { meApi, type AppMePrivacyUpdateDTO, type AppMePrivacyVO } from "@/api/me";
import { ensurePageAuth } from "@/utils/auth-guard";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();

const loading = ref(false);
const savingPrivacy = ref(false);
const canceling = ref(false);

const privacy = ref<AppMePrivacyVO>({
  profileVisible: 1,
  statsVisible: 1,
  personalizedRecommend: 1
});

function toSwitch(value?: number) {
  return Number(value || 0) === 1;
}

function updateSwitch(key: keyof AppMePrivacyUpdateDTO, value: boolean) {
  privacy.value = {
    ...privacy.value,
    [key]: value ? 1 : 0
  };
}

async function loadData() {
  loading.value = true;
  try {
    const privacyData = await meApi.privacy();
    privacy.value = {
      profileVisible: Number(privacyData?.profileVisible ?? 1),
      statsVisible: Number(privacyData?.statsVisible ?? 1),
      personalizedRecommend: Number(privacyData?.personalizedRecommend ?? 1),
      updatedAt: privacyData?.updatedAt
    };
  } catch (err: any) {
    uni.showToast({ title: err?.message || "设置加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function doSavePrivacy() {
  savingPrivacy.value = true;
  try {
    const payload: AppMePrivacyUpdateDTO = {
      profileVisible: Number(privacy.value.profileVisible || 0) === 1 ? 1 : 0,
      statsVisible: Number(privacy.value.statsVisible || 0) === 1 ? 1 : 0,
      personalizedRecommend: Number(privacy.value.personalizedRecommend || 0) === 1 ? 1 : 0
    };
    await meApi.updatePrivacy(payload);
    uni.showToast({ title: "隐私设置已保存", icon: "success" });
  } catch (err: any) {
    uni.showToast({ title: err?.message || "保存失败", icon: "none" });
  } finally {
    savingPrivacy.value = false;
  }
}

function savePrivacy() {
  if (savingPrivacy.value) return;
  uni.showModal({
    title: "确认保存",
    content: "是否确认保存当前隐私设置？",
    success: async (res) => {
      if (!res.confirm) return;
      await doSavePrivacy();
    }
  });
}

function cancelAccount() {
  if (canceling.value) return;
  uni.showModal({
    title: "确认注销",
    content: "注销后账号将立即失效，且无法登录，是否继续？",
    confirmColor: "#d86c6c",
    success: async (res) => {
      if (!res.confirm) return;
      canceling.value = true;
      try {
        await meApi.submitCancelRequest({});
        uni.showToast({ title: "账号已注销", icon: "success" });
        authStore.logout();
        setTimeout(() => {
          uni.reLaunch({ url: "/pages/home/index" });
        }, 250);
      } catch (err: any) {
        uni.showToast({ title: err?.message || "注销失败", icon: "none" });
      } finally {
        canceling.value = false;
      }
    }
  });
}

onShow(() => {
  if (!ensurePageAuth()) return;
  loadData();
});
</script>

<template>
  <view class="page">
    <view class="group-card">
      <view class="group-title">隐私设置</view>

      <view class="group-row">
        <view class="item-main">
          <view class="item-title">公开个人主页</view>
          <view class="item-desc">关闭后，他人看不到你的问答与科普列表</view>
        </view>
        <switch
          :checked="toSwitch(privacy.profileVisible)"
          color="#4ba7d9"
          @change="(e: any) => updateSwitch('profileVisible', !!e?.detail?.value)"
        />
      </view>

      <view class="group-row">
        <view class="item-main">
          <view class="item-title">公开主页统计</view>
          <view class="item-desc">关闭后，他人主页中不会显示你的统计数</view>
        </view>
        <switch
          :checked="toSwitch(privacy.statsVisible)"
          color="#4ba7d9"
          @change="(e: any) => updateSwitch('statsVisible', !!e?.detail?.value)"
        />
      </view>

      <view class="group-row">
        <view class="item-main">
          <view class="item-title">个性化推荐</view>
          <view class="item-desc">根据你的行为偏好优化首页内容排序</view>
        </view>
        <switch
          :checked="toSwitch(privacy.personalizedRecommend)"
          color="#4ba7d9"
          @change="(e: any) => updateSwitch('personalizedRecommend', !!e?.detail?.value)"
        />
      </view>

      <view class="save-row">
        <button class="btn btn-primary" :loading="savingPrivacy" @click="savePrivacy">保存隐私设置</button>
      </view>
    </view>

    <view class="item-card">
      <view class="item-main">
        <view class="item-title">立即注销账号</view>
        <view class="item-desc">无需审核，提交后立即注销并退出登录</view>
      </view>
      <button class="side-btn side-btn--danger" :loading="canceling" @click="cancelAccount">立即注销</button>
    </view>

    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 20rpx;
}

.group-card {
  background: #fff;
  border: 2rpx solid #eadfbf;
  border-radius: 20rpx;
  margin-bottom: 12rpx;
  overflow: hidden;
}

.group-title {
  padding: 16rpx 20rpx 10rpx;
  font-size: 31rpx;
  color: #2f4f68;
  font-weight: 700;
}

.group-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  padding: 12rpx 20rpx;
  border-top: 1rpx solid #f0ece0;
}

.save-row {
  padding: 12rpx 20rpx 18rpx;
  border-top: 1rpx solid #f0ece0;
}

.item-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 16rpx 20rpx;
  background: #fff;
  border: 2rpx solid #eadfbf;
  border-radius: 20rpx;
  margin-bottom: 12rpx;
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 30rpx;
  color: #2f4f68;
  font-weight: 700;
}

.item-desc {
  margin-top: 6rpx;
  color: #7f95a8;
  line-height: 1.45;
  font-size: 24rpx;
}

.btn {
  margin: 0;
  width: 100%;
  height: 64rpx;
  line-height: 64rpx;
  border: none;
  border-radius: 14rpx;
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
}

.btn::after {
  border: none;
}

.btn-primary {
  background: #4ba7d9;
}

.btn-danger {
  background: #d86c6c;
}

.side-btn {
  margin: 0;
  min-width: 176rpx;
  height: 58rpx;
  line-height: 58rpx;
  padding: 0 18rpx;
  border: none;
  border-radius: 12rpx;
  color: #fff;
  font-size: 25rpx;
  font-weight: 600;
}

.side-btn::after {
  border: none;
}

.side-btn--danger {
  background: #d86c6c;
}

.loading {
  text-align: center;
  color: #8ea1b2;
  padding: 10rpx 0 20rpx;
}
</style>
