<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { ensurePageAuth } from "@/utils/auth-guard";
import { BASE_URL } from "@/utils/constants";
import { useAuthStore } from "@/stores/auth";
import { questionApi } from "@/api/question";

const authStore = useAuthStore();
const questionId = ref(0);
const questionTitle = ref("");
const content = ref("");
const imageUrls = ref<string[]>([]);
const uploading = ref(false);
const publishing = ref(false);
const isAnonymous = ref(false);

const contentLength = computed(() => content.value.length);
const canPublish = computed(() => !!content.value.trim() && !publishing.value);

onLoad((options) => {
  questionId.value = Number(options?.questionId || 0);
  questionTitle.value = decodeURIComponent(String(options?.title || "")).trim();
});

onShow(() => {
  ensurePageAuth();
});

async function chooseImages() {
  const remain = 9 - imageUrls.value.length;
  if (remain <= 0) {
    uni.showToast({ title: "最多上传9张图片", icon: "none" });
    return;
  }
  try {
    const choose = await uni.chooseImage({
      count: remain,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"]
    });
    const paths = choose.tempFilePaths || [];
    if (!paths.length) return;
    uploading.value = true;
    for (const filePath of paths) {
      const url = await uploadAnswerImage(filePath);
      imageUrls.value.push(url);
      if (imageUrls.value.length >= 9) break;
    }
  } catch (err: any) {
    uni.showToast({ title: err?.message || "图片上传失败", icon: "none" });
  } finally {
    uploading.value = false;
  }
}

function removeImage(index: number) {
  imageUrls.value.splice(index, 1);
}

async function uploadAnswerImage(filePath: string) {
  const upload = await uni.uploadFile({
    url: `${BASE_URL}/api/common/upload?bizType=answer`,
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
    throw new Error(body?.message || body?.msg || "上传失败");
  }
  const text = String(url);
  if (text.startsWith("http://") || text.startsWith("https://")) return text;
  if (text.startsWith("/")) return `${BASE_URL}${text}`;
  return `${BASE_URL}/${text}`;
}

async function submitAnswer() {
  if (!questionId.value) {
    uni.showToast({ title: "参数错误", icon: "none" });
    return;
  }
  if (!content.value.trim()) {
    uni.showToast({ title: "请输入回答内容", icon: "none" });
    return;
  }
  if (publishing.value) return;

  publishing.value = true;
  try {
    await questionApi.createAnswer(questionId.value, {
      content: content.value.trim(),
      imageUrls: imageUrls.value.length ? imageUrls.value : undefined,
      isAnonymous: isAnonymous.value ? 1 : 0
    });
    uni.showToast({ title: "回答已发布", icon: "success" });
    setTimeout(() => {
      uni.navigateBack({
        delta: 1,
        fail: () => {
          uni.redirectTo({ url: `/pages/question/detail?id=${questionId.value}` });
        }
      });
    }, 250);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "发布失败", icon: "none" });
  } finally {
    publishing.value = false;
  }
}
</script>

<template>
  <view class="page">
    <view class="question-line">
      <view class="question-main">
        <text class="question-label">回答问题：</text>
        <text class="question-text">{{ questionTitle }}</text>
      </view>
      <view class="publish-btn line-publish" :class="{ disabled: !canPublish }" @click="submitAnswer">
        {{ publishing ? "发布中" : "发布" }}
      </view>
    </view>

    <textarea
      v-model="content"
      class="editor"
      maxlength="5000"
      placeholder="分享你的专业知识或亲身经验，帮助提问者解决问题..."
      placeholder-style="font-size: 34rpx; font-weight: 700; color: #9aa8b7;"
    />

    <view v-if="imageUrls.length" class="images-wrap">
      <view v-for="(url, idx) in imageUrls" :key="url + idx" class="img-item">
        <image class="img" :src="url" mode="aspectFill" />
        <view class="img-del" @click="removeImage(idx)">×</view>
      </view>
    </view>

    <view class="suggest-card">
      <view class="suggest-title">优质回答建议</view>
      <view class="suggest-item">· 分享真实经验或专业知识</view>
      <view class="suggest-item">· 逻辑清晰，分点阐述</view>
      <view class="suggest-item">· 如引用资料，请注明来源</view>
      <view class="suggest-item">· 避免营销推广内容</view>
    </view>

    <view class="bottom-tools">
      <view class="tool-left">
        <view class="tool-image-btn" @click="chooseImages">
          <image
            class="tool-image-icon"
            :src="uploading ? '/static/tabbar/image-active.png' : '/static/tabbar/image.png'"
            mode="aspectFit"
          />
        </view>
      </view>
    </view>

    <view class="anonymous-row" @click="isAnonymous = !isAnonymous">
      <view class="check-box">{{ isAnonymous ? "✓" : "" }}</view>
      <text class="anonymous-text">匿名回答</text>
      <text class="tool-right">{{ contentLength }}/5000</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f3f1e7;
  padding: 16rpx 0 0;
  display: flex;
  flex-direction: column;
}

.question-line {
  margin-top: 8rpx;
  padding: 12rpx 22rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10rpx;
  background: #f0e8d1;
  border-top: 1rpx solid #edd04c;
  border-bottom: 1rpx solid #edd04c;
}

.question-main {
  display: flex;
  align-items: center;
  gap: 6rpx;
  min-width: 0;
  flex: 1;
}

.question-label {
  color: #6d849d;
  font-size: 30rpx;
  font-weight: 600;
}

.question-text {
  color: #30455d;
  font-size: 30rpx;
  font-weight: 700;
  flex: 1;
}

.publish-btn {
  min-width: 98rpx;
  height: 56rpx;
  border-radius: 28rpx;
  padding: 0 20rpx;
  background: #e4d7b4;
  color: #6f89a5;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.publish-btn.disabled {
  opacity: 0.6;
}

.line-publish {
  height: 50rpx;
  min-width: 90rpx;
  border-radius: 25rpx;
  font-size: 24rpx;
  padding: 0 16rpx;
  flex-shrink: 0;
}

.editor {
  flex: 1;
  width: auto;
  min-height: 540rpx;
  margin: 0 22rpx;
  padding: 20rpx 0;
  color: #3e5063;
  line-height: 1.7;
  font-size: 34rpx;
  background: transparent;
}

.images-wrap {
  margin: 0 22rpx 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.img-item {
  width: 148rpx;
  height: 148rpx;
  border-radius: 12rpx;
  overflow: hidden;
  position: relative;
}

.img {
  width: 100%;
  height: 100%;
}

.img-del {
  position: absolute;
  right: 4rpx;
  top: 4rpx;
  width: 28rpx;
  height: 28rpx;
  border-radius: 14rpx;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  text-align: center;
  line-height: 28rpx;
  font-size: 20rpx;
}

.suggest-card {
  margin: 0 22rpx 16rpx;
  padding: 20rpx 24rpx;
  border-radius: 28rpx;
  border: 2rpx solid #c9d8ea;
  background: #e6edf3;
}

.suggest-title {
  color: #3c536c;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.suggest-item {
  color: #5d748d;
  font-size: 24rpx;
  line-height: 1.8;
}

.bottom-tools {
  height: 84rpx;
  border-top: 1rpx solid #eadfbf;
  border-bottom: 1rpx solid #eadfbf;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tool-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  color: #8198ad;
}

.tool-image-btn {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-image-icon {
  width: 34rpx;
  height: 34rpx;
}

.anonymous-row {
  height: 76rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.check-box {
  width: 34rpx;
  height: 34rpx;
  border: 2rpx solid #a8b8c7;
  border-radius: 6rpx;
  color: #5a83a5;
  font-size: 24rpx;
  line-height: 32rpx;
  text-align: center;
}

.anonymous-text {
  color: #6e869e;
}

.tool-right {
  margin-left: auto;
  color: #8ea4b7;
  font-size: 28rpx;
}
</style>
