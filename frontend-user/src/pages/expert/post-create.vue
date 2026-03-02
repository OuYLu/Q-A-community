<script setup lang="ts">
import { computed, getCurrentInstance, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { ensurePageAuth } from "@/utils/auth-guard";
import { useAuthStore } from "@/stores/auth";
import { expertApi } from "@/api/expert";
import { meApi } from "@/api/me";
import { BASE_URL } from "@/utils/constants";

type EditorCtx = {
  insertImage: (options: { src: string; alt?: string; success?: () => void; fail?: (err: any) => void }) => void;
  getContents: (options: { success: (res: any) => void; fail: (err: any) => void }) => void;
};

const vm = getCurrentInstance();
const authStore = useAuthStore();
const title = ref("");
const categoryId = ref<number | null>(null);
const categoryName = ref("");
const categories = ref<Array<{ id: number; name: string }>>([]);
const tagsInput = ref("");
const coverImage = ref("");
const summary = ref("");
const checkingRole = ref(false);
const submitting = ref(false);
const uploading = ref(false);
const editorCtx = ref<EditorCtx | null>(null);
const editorTextLen = ref(0);

const canSubmit = computed(() => !!title.value.trim() && !!categoryId.value && !checkingRole.value && !submitting.value);

onLoad(async () => {
  await initPage();
});

onShow(async () => {
  await initPage();
});

async function initPage() {
  if (!ensurePageAuth()) return;
  await Promise.all([guardExpertRole(), loadCategories()]);
}

async function guardExpertRole() {
  checkingRole.value = true;
  try {
    const info = await meApi.overview();
    if (info.expertStatus !== 3) {
      uni.showToast({ title: "仅认证专家可发科普帖", icon: "none" });
      setTimeout(() => uni.navigateBack(), 260);
    }
  } catch {
    uni.showToast({ title: "身份校验失败", icon: "none" });
    setTimeout(() => uni.navigateBack(), 260);
  } finally {
    checkingRole.value = false;
  }
}

async function loadCategories() {
  try {
    const list = await expertApi.categories();
    categories.value = (list || []).map((x) => ({ id: x.id, name: x.name })).filter((x) => !!x.name);
  } catch {
    categories.value = [];
  }
}

function chooseCategory() {
  if (!categories.value.length) {
    uni.showToast({ title: "分类加载中，请稍后", icon: "none" });
    return;
  }
  uni.showActionSheet({
    itemList: categories.value.map((x) => x.name),
    success: (res) => {
      const selected = categories.value[res.tapIndex];
      if (!selected) return;
      categoryId.value = selected.id;
      categoryName.value = selected.name;
    }
  });
}

function onEditorReady() {
  uni.createSelectorQuery()
    .in(vm?.proxy as any)
    .select("#postEditor")
    .context((res: any) => {
      editorCtx.value = res?.context || null;
    })
    .exec();
}

function onEditorInput(e: any) {
  const text = e?.detail?.text || "";
  editorTextLen.value = String(text).trim().length;
}

async function insertImageToEditor() {
  if (!editorCtx.value) {
    uni.showToast({ title: "编辑器未就绪", icon: "none" });
    return;
  }
  try {
    const choose = await uni.chooseImage({
      count: 1,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"]
    });
    const filePath = choose.tempFilePaths?.[0];
    if (!filePath) return;
    uploading.value = true;
    const url = await uploadExpertPostImage(filePath);
    editorCtx.value.insertImage({ src: url });
  } catch (err: any) {
    uni.showToast({ title: err?.message || "插图失败", icon: "none" });
  } finally {
    uploading.value = false;
  }
}

async function chooseCover() {
  try {
    const choose = await uni.chooseImage({
      count: 1,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"]
    });
    const filePath = choose.tempFilePaths?.[0];
    if (!filePath) return;
    uploading.value = true;
    coverImage.value = await uploadExpertPostImage(filePath);
    uni.showToast({ title: "封面上传成功", icon: "success" });
  } catch (err: any) {
    uni.showToast({ title: err?.message || "封面上传失败", icon: "none" });
  } finally {
    uploading.value = false;
  }
}

async function uploadExpertPostImage(filePath: string) {
  const upload = await uni.uploadFile({
    url: `${BASE_URL}/api/common/upload?bizType=expert-post`,
    filePath,
    name: "file",
    header: authStore.token ? { Authorization: `Bearer ${authStore.token}` } : undefined
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

function getEditorContents() {
  return new Promise<any>((resolve, reject) => {
    if (!editorCtx.value) {
      reject(new Error("编辑器未就绪"));
      return;
    }
    editorCtx.value.getContents({
      success: resolve,
      fail: reject
    });
  });
}

function buildBlocksFromDelta(ops: any[]) {
  const blocks: Array<{ type: "text" | "image"; text?: string; url?: string }> = [];
  for (const op of ops || []) {
    if (typeof op?.insert === "string") {
      const text = op.insert.replace(/\n+/g, "\n").trim();
      if (!text) continue;
      blocks.push({ type: "text", text });
      continue;
    }
    if (op?.insert?.image) {
      blocks.push({ type: "image", url: String(op.insert.image) });
    }
  }
  return blocks;
}

function normalizeTagNames() {
  return tagsInput.value
    .split(/[，, ]+/)
    .map((x) => x.trim())
    .filter(Boolean)
    .map((x) => (x.startsWith("#") ? x.slice(1) : x))
    .filter(Boolean)
    .slice(0, 10);
}

async function submit() {
  if (!canSubmit.value) {
    uni.showToast({ title: "请先填写标题和分类", icon: "none" });
    return;
  }
  let contents: any;
  try {
    contents = await getEditorContents();
  } catch {
    uni.showToast({ title: "正文编辑器未就绪", icon: "none" });
    return;
  }

  const blocks = buildBlocksFromDelta(contents?.delta?.ops || []);
  if (!blocks.length) {
    uni.showToast({ title: "请填写正文", icon: "none" });
    return;
  }

  const html = String(contents?.html || "");
  const text = String(contents?.text || "").trim();
  const imageUrls = blocks.filter((x) => x.type === "image" && x.url).map((x) => x.url as string);

  submitting.value = true;
  try {
    await expertApi.createPost({
      title: title.value.trim(),
      categoryId: categoryId.value as number,
      summary: summary.value.trim() || text.slice(0, 120) || undefined,
      content: html || text || undefined,
      contentBlocks: blocks,
      tagNames: normalizeTagNames(),
      coverImage: coverImage.value || imageUrls[0] || undefined,
      imageUrls: imageUrls.length ? imageUrls : undefined
    });
    uni.showToast({ title: "科普帖发布成功", icon: "success" });
    setTimeout(() => uni.navigateBack(), 260);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "发布失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <view class="page">
    <view class="panel">
      <view class="label">标题 *</view>
      <textarea v-model="title" class="title-input" maxlength="80" placeholder="输入标题（建议 20~40 字）" />
      <view class="counter">{{ title.length }}/80</view>

      <view class="label">分类 *</view>
      <view class="picker" @click="chooseCategory">
        <text :class="{ placeholder: !categoryName }">{{ categoryName || "请选择分类" }}</text>
        <text class="arrow">></text>
      </view>
    </view>

    <view class="panel">
      <view class="label">摘要（选填）</view>
      <textarea v-model="summary" class="summary-input" maxlength="120" placeholder="一句话摘要（不填则自动截取正文）" />
      <view class="counter">{{ summary.length }}/120</view>

      <view class="label">正文 *</view>
      <editor
        id="postEditor"
        class="editor"
        placeholder="先输入文字，再用下方相册按钮插图，继续输入文字..."
        @ready="onEditorReady"
        @input="onEditorInput"
      />

      <view class="toolbar">
        <view class="tool-btn wide" @click="insertImageToEditor">{{ uploading ? "上传中..." : "相册" }}</view>
        <view class="tool-len">{{ editorTextLen }}/5000</view>
      </view>
    </view>

    <view class="panel">
      <view class="label">标签（选填，最多10个）</view>
      <input v-model="tagsInput" class="line-input" placeholder="如：睡眠 调理 焦虑" />

      <view class="label">封面图（选填）</view>
      <view class="cover-wrap">
        <view v-if="coverImage" class="cover-item">
          <image class="cover-img" :src="coverImage" mode="aspectFill" />
          <view class="cover-del" @click="coverImage = ''">x</view>
        </view>
        <view v-else class="cover-pick" @click="chooseCover">+ 选择封面</view>
      </view>
    </view>

    <view class="bottom-bar">
      <button class="submit" :class="{ disabled: !canSubmit }" @click="submit">
        {{ submitting ? "发布中..." : "发布科普帖" }}
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 16rpx 20rpx 140rpx;
  box-sizing: border-box;
}

.panel {
  background: #fff;
  border: 2rpx solid #eadfbf;
  border-radius: 20rpx;
  padding: 16rpx;
  margin-bottom: 14rpx;
}

.label {
  margin: 10rpx 0 8rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #354f68;
}

.title-input {
  min-height: 92rpx;
  max-height: 132rpx;
  width: auto;
  border: 2rpx solid #d8e8f8;
  border-radius: 14rpx;
  background: #fdfdfb;
  padding: 10rpx 14rpx;
  line-height: 1.45;
}

.summary-input {
  min-height: 76rpx;
  width: auto;
  border: 2rpx solid #d8e8f8;
  border-radius: 14rpx;
  background: #fdfdfb;
  padding: 10rpx 14rpx;
}

.counter {
  margin-top: 6rpx;
  text-align: right;
  color: #8ba0b3;
  font-size: 22rpx;
}

.picker {
  height: 72rpx;
  border: 2rpx solid #d8e8f8;
  border-radius: 14rpx;
  background: #fdfdfb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14rpx;
}

.placeholder {
  color: #8ea1b2;
}

.arrow {
  color: #8ea1b2;
  font-size: 32rpx;
}

.editor {
  min-height: 360rpx;
  border: 2rpx solid #d8e8f8;
  border-radius: 14rpx;
  background: #fff;
  padding: 8rpx 10rpx;
}

.toolbar {
  margin-top: 10rpx;
  border: 2rpx solid #d8e8f8;
  border-radius: 14rpx;
  background: #f8fbff;
  padding: 8rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.tool-btn {
  min-width: 52rpx;
  height: 52rpx;
  border-radius: 10rpx;
  border: 1rpx solid #c8dbec;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #446482;
  font-weight: 700;
  background: #fff;
}

.tool-btn.wide {
  min-width: 92rpx;
  padding: 0 10rpx;
}

.tool-len {
  margin-left: auto;
  color: #8ea1b2;
  font-size: 22rpx;
}

.line-input {
  height: 70rpx;
  border: 2rpx solid #d8e8f8;
  border-radius: 14rpx;
  background: #fdfdfb;
  padding: 0 14rpx;
}

.cover-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.cover-item,
.cover-pick {
  width: 176rpx;
  height: 176rpx;
  border-radius: 12rpx;
  overflow: hidden;
  border: 2rpx solid #eadfbf;
  position: relative;
}

.cover-pick {
  border: 2rpx dashed #d6cba8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7d8f9f;
  background: #fffdf7;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.cover-del {
  position: absolute;
  right: 6rpx;
  top: 4rpx;
  width: 30rpx;
  height: 30rpx;
  line-height: 30rpx;
  text-align: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 14rpx 20rpx 20rpx;
  background: #f7f5eb;
  border-top: 1rpx solid #ece5cf;
}

.submit {
  border: none;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
}

.submit.disabled {
  opacity: 0.65;
}
</style>
