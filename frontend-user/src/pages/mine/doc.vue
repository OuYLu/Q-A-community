<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { meApi, type AppDocType } from "@/api/me";
import { ensurePageAuth } from "@/utils/auth-guard";

const title = ref("文档");
const content = ref("");
const loading = ref(false);

const titleMap: Record<AppDocType, string> = {
  settings: "设置",
  help: "帮助与反馈",
  "user-agreement": "用户协议",
  "privacy-policy": "隐私政策"
};

onLoad(async (options) => {
  if (!ensurePageAuth()) return;
  const type = (options?.type || "help") as AppDocType;
  loading.value = true;
  try {
    const doc = await meApi.doc(type);
    title.value = doc.title || titleMap[type] || "文档";
    content.value = doc.content || "暂无内容";
    uni.setNavigationBarTitle({ title: title.value });
  } catch (err) {
    uni.showToast({ title: "文档加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <view class="page">
    <view class="doc-title">{{ title }}</view>
    <view v-if="loading" class="state">加载中...</view>
    <view v-else class="doc-body">{{ content }}</view>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 24rpx;
}

.doc-title {
  font-size: 38rpx;
  font-weight: 700;
  margin-bottom: 18rpx;
}

.doc-body {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #3f5e76;
}

.state {
  color: #9ab0bf;
}
</style>
