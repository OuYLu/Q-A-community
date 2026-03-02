<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { notificationApi, type AppReportFeedbackDetailVO } from "@/api/notification";

const loading = ref(true);
const detail = ref<AppReportFeedbackDetailVO | null>(null);

async function loadDetail(id: number) {
  loading.value = true;
  try {
    detail.value = await notificationApi.reportFeedbackDetail(id);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function bizTypeText(type?: number) {
  const map: Record<number, string> = {
    1: "问题",
    2: "回答",
    3: "评论",
    4: "科普"
  };
  return type ? map[type] || "内容" : "内容";
}

function actionText(action?: number) {
  const map: Record<number, string> = {
    1: "下架",
    2: "警告",
    3: "封禁",
    4: "不处理"
  };
  return action ? map[action] || "已处理" : "待处理";
}

onLoad((options) => {
  const id = Number(options?.id || 0);
  if (!id) {
    loading.value = false;
    uni.showToast({ title: "参数错误", icon: "none" });
    return;
  }
  loadDetail(id);
});
</script>

<template>
  <view class="page">
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!detail" class="state">暂无数据</view>
    <template v-else>
      <view class="card">
        <view class="label">被举报{{ bizTypeText(detail.bizType) }}</view>
        <view class="title">{{ detail.contentTitle || "-" }}</view>
        <view class="meta">处理动作：{{ actionText(detail.handleAction) }}</view>
        <view class="meta">处理人：{{ detail.handlerName || "-" }}</view>
      </view>

      <view class="card">
        <view class="label">处理结果</view>
        <view class="content">{{ detail.handleResult || "暂无处理结果" }}</view>
      </view>

      <view class="card">
        <view class="label">时间节点</view>
        <view class="timeline-row">
          <text class="dot"></text>
          <view>
            <view class="node-title">提交举报</view>
            <view class="node-time">{{ detail.createdAt || "-" }}</view>
          </view>
        </view>
        <view class="timeline-row">
          <text class="dot done"></text>
          <view>
            <view class="node-title">处理完成</view>
            <view class="node-time">{{ detail.handledAt || "待处理" }}</view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
  box-sizing: border-box;
}

.card {
  background: #fff;
  border: 2rpx solid #d7e4ef;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 14rpx;
}

.label {
  font-size: 24rpx;
  color: #8ba0b3;
}

.title {
  margin-top: 10rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #31495f;
}

.meta {
  margin-top: 8rpx;
  color: #5d7488;
}

.content {
  margin-top: 10rpx;
  color: #31495f;
  line-height: 1.6;
  white-space: pre-wrap;
}

.timeline-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-top: 14rpx;
}

.dot {
  margin-top: 8rpx;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #c8d7e4;
}

.dot.done {
  background: #4ba7d9;
}

.node-title {
  color: #31495f;
  font-weight: 600;
}

.node-time {
  margin-top: 4rpx;
  color: #8ba0b3;
}

.state {
  text-align: center;
  margin-top: 200rpx;
  color: #8ba0b3;
}
</style>
