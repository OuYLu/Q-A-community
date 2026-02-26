<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { meApi } from "@/api/me";

const mode = ref<"set" | "modify">("set");
const oldPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const submitting = ref(false);

const title = computed(() => (mode.value === "modify" ? "修改密码" : "设置密码"));
const needOldPassword = computed(() => mode.value === "modify");

function validate() {
  if (needOldPassword.value && !oldPassword.value) {
    uni.showToast({ title: "请输入原密码", icon: "none" });
    return false;
  }
  if (!newPassword.value || newPassword.value.length < 6) {
    uni.showToast({ title: "新密码至少6位", icon: "none" });
    return false;
  }
  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({ title: "两次输入的新密码不一致", icon: "none" });
    return false;
  }
  return true;
}

async function submit() {
  if (!validate()) return;
  submitting.value = true;
  try {
    if (needOldPassword.value) {
      await meApi.changePassword({
        oldPassword: oldPassword.value,
        newPassword: newPassword.value,
        confirmPassword: confirmPassword.value
      });
    } else {
      await meApi.setFirstPassword({
        newPassword: newPassword.value,
        confirmPassword: confirmPassword.value
      });
    }
    uni.showToast({ title: `${title.value}成功`, icon: "success" });
    setTimeout(() => uni.navigateBack(), 220);
  } catch (err: any) {
    uni.showToast({ title: err?.message || `${title.value}失败`, icon: "none" });
  } finally {
    submitting.value = false;
  }
}

onLoad((options) => {
  mode.value = (options?.mode as "set" | "modify") || "set";
  uni.setNavigationBarTitle({ title: title.value });
});
</script>

<template>
  <view class="page">
    <view class="panel app-card">
      <view v-if="needOldPassword" class="label">原密码</view>
      <input v-if="needOldPassword" v-model="oldPassword" class="input" password maxlength="64" placeholder="请输入原密码" />

      <view class="label">新密码</view>
      <input v-model="newPassword" class="input" password maxlength="64" placeholder="至少6位" />

      <view class="label">确认新密码</view>
      <input v-model="confirmPassword" class="input" password maxlength="64" placeholder="再次输入新密码" />

      <button class="submit" :loading="submitting" @click="submit">{{ title }}</button>
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

.input {
  height: 78rpx;
  border-radius: 18rpx;
  border: 2rpx solid #9bd1ef;
  background: #fff;
  padding: 0 20rpx;
}

.submit {
  margin-top: 24rpx;
  border: none;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
}
</style>
