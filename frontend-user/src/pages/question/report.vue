<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { questionApi } from "@/api/question";

type ReportTarget = "question" | "answer";

const targetType = ref<ReportTarget>("question");
const targetId = ref(0);
const targetTitle = ref("");
const reasonCode = ref("");
const reasonDetail = ref("");
const submitting = ref(false);

const reasons = [
  { code: "illegal", label: "违法违规" },
  { code: "porn", label: "低俗色情" },
  { code: "abuse", label: "辱骂攻击" },
  { code: "ad", label: "广告营销" },
  { code: "privacy", label: "隐私泄露" },
  { code: "other", label: "其他" }
];

const targetLabel = computed(() => (targetType.value === "answer" ? "回答" : "问题"));
const fallbackTitle = computed(() => `${targetLabel.value} #${targetId.value}`);
const canSubmit = computed(() => !!targetId.value && !!reasonCode.value && !submitting.value);

async function submitReport() {
  if (!canSubmit.value) {
    uni.showToast({ title: "请选择举报原因", icon: "none" });
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      reasonCode: reasonCode.value,
      reasonDetail: reasonDetail.value.trim() || undefined
    };
    if (targetType.value === "answer") {
      await questionApi.reportAnswer(targetId.value, payload);
    } else {
      await questionApi.reportQuestion(targetId.value, payload);
    }
    uni.showToast({ title: "举报已提交", icon: "success" });
    setTimeout(() => uni.navigateBack(), 220);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "举报失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}

onLoad((options) => {
  targetType.value = options?.targetType === "answer" ? "answer" : "question";
  targetId.value = Number(options?.questionId || options?.answerId || options?.id || 0);
  targetTitle.value = decodeURIComponent(String(options?.title || ""));
});
</script>

<template>
  <view class="page">
    <view class="app-card card">
      <view class="label">{{ targetLabel }}</view>
      <view class="title">{{ targetTitle || fallbackTitle }}</view>
    </view>

    <view class="app-card card">
      <view class="label">举报原因</view>
      <view class="reasons">
        <view
          v-for="item in reasons"
          :key="item.code"
          class="reason-item"
          :class="{ active: reasonCode === item.code }"
          @click="reasonCode = item.code"
        >
          {{ item.label }}
        </view>
      </view>

      <view class="label detail-label">补充说明（选填）</view>
      <textarea
        v-model="reasonDetail"
        class="detail-input"
        maxlength="500"
        placeholder="请描述举报原因，便于平台处理"
      />
      <view class="count">{{ reasonDetail.length }}/500</view>
    </view>

    <button class="submit-btn" :disabled="!canSubmit" @click="submitReport">
      {{ submitting ? "提交中..." : "提交举报" }}
    </button>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 16rpx;
}

.card {
  padding: 16rpx;
  margin-bottom: 12rpx;
}

.label {
  font-size: 26rpx;
  color: #5f7388;
  margin-bottom: 8rpx;
}

.title {
  font-size: 30rpx;
  color: #22364d;
  font-weight: 700;
  line-height: 1.45;
}

.reasons {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.reason-item {
  min-width: 140rpx;
  height: 58rpx;
  border-radius: 12rpx;
  border: 2rpx solid #d5e1ec;
  color: #607487;
  background: #f7fbff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}

.reason-item.active {
  border-color: #53abd9;
  color: #2a8cc1;
  background: #eaf7fe;
}

.detail-label {
  margin-top: 14rpx;
}

.detail-input {
  min-height: 180rpx;
  border-radius: 14rpx;
  border: 2rpx solid #d5e1ec;
  background: #fff;
  padding: 10rpx 12rpx;
}

.count {
  margin-top: 8rpx;
  text-align: right;
  color: #8ea1b2;
  font-size: 22rpx;
}

.submit-btn {
  margin-top: 14rpx;
  border-radius: 14rpx;
  border: none;
  background: #4aa9da;
  color: #fff;
}

.submit-btn[disabled] {
  opacity: 0.55;
}
</style>