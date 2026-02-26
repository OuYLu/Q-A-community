<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { ensurePageAuth } from "@/utils/auth-guard";
import { discoverApi, type AppCategoryTreeNodeVO } from "@/api/discover";
import { questionApi } from "@/api/question";
import { BASE_URL } from "@/utils/constants";
import { useAuthStore } from "@/stores/auth";

const title = ref("");
const categoryId = ref<number | null>(null);
const categoryName = ref("");
const content = ref("");
const tagsInput = ref("");
const imageUrls = ref<string[]>([]);
const uploadingImages = ref(false);
const categoryLoading = ref(false);
const categoryTree = ref<AppCategoryTreeNodeVO[]>([]);
const showCategoryPanel = ref(false);
const selectedRootId = ref<number | null>(null);
const childCategoryMap = ref<Record<number, AppCategoryTreeNodeVO[]>>({});
const categorySearch = ref("");
const categorySearchLoading = ref(false);
const authStore = useAuthStore();

const categoryPlaceholder = computed(() => {
  if (categoryLoading.value) return "分类加载中...";
  return categoryName.value || "例如：睡眠调理";
});

const selectedRoot = computed(() => categoryTree.value.find((x) => x.id === selectedRootId.value) || null);
const normalizedSearch = computed(() => categorySearch.value.trim().toLowerCase());
const currentChildren = computed(() => {
  const children = childCategoryMap.value[selectedRootId.value || -1] || [];
  if (!normalizedSearch.value) return children;
  return children.filter((x) => getCategoryText(x).toLowerCase().includes(normalizedSearch.value));
});
const filteredRoots = computed(() => {
  if (!normalizedSearch.value) return categoryTree.value;
  return categoryTree.value.filter((root) => {
    const rootHit = getCategoryText(root).toLowerCase().includes(normalizedSearch.value);
    const children = childCategoryMap.value[root.id] || root.children || [];
    const childHit = children.some((x) => getCategoryText(x).toLowerCase().includes(normalizedSearch.value));
    return rootHit || childHit;
  });
});
const searchMatches = computed(() => {
  if (!normalizedSearch.value) return [] as Array<{ node: AppCategoryTreeNodeVO; parent?: AppCategoryTreeNodeVO }>;
  const result: Array<{ node: AppCategoryTreeNodeVO; parent?: AppCategoryTreeNodeVO }> = [];
  const seen = new Set<string>();
  for (const root of categoryTree.value) {
    const children = childCategoryMap.value[root.id] || root.children || [];
    const rootHit = getCategoryText(root).toLowerCase().includes(normalizedSearch.value);
    if (rootHit) {
      const rootKey = `root-${root.id}`;
      if (!seen.has(rootKey)) {
        result.push({ node: root });
        seen.add(rootKey);
      }
      for (const child of children) {
        const childKey = `child-${root.id}-${child.id}`;
        if (!seen.has(childKey)) {
          result.push({ node: child, parent: root });
          seen.add(childKey);
        }
      }
      continue;
    }
    for (const child of children) {
      if (getCategoryText(child).toLowerCase().includes(normalizedSearch.value)) {
        const childKey = `child-${root.id}-${child.id}`;
        if (seen.has(childKey)) continue;
        result.push({ node: child, parent: root });
        seen.add(childKey);
      }
    }
  }
  return result;
});

onShow(() => {
  ensurePageAuth();
});

async function submit() {
  if (!title.value.trim()) {
    uni.showToast({ title: "请输入问题标题", icon: "none" });
    return;
  }
  if (!categoryName.value.trim()) {
    uni.showToast({ title: "请选择问题分类", icon: "none" });
    return;
  }

  const tags = tagsInput.value
    .split(/[，, ]+/)
    .map((x) => x.trim())
    .filter(Boolean)
    .slice(0, 5);

  try {
    const questionId = await questionApi.createQuestion({
      title: title.value.trim(),
      content: content.value.trim() || undefined,
      categoryId: categoryId.value || undefined,
      tagNames: tags.length ? tags : undefined,
      imageUrls: imageUrls.value.length ? imageUrls.value : undefined
    });

    uni.showToast({ title: "提问成功", icon: "success" });
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/question/detail?id=${questionId}`
      });
    }, 250);
  } catch (err: any) {
    uni.showToast({ title: err?.message || "发布失败", icon: "none" });
  }
}

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
    uploadingImages.value = true;
    for (const filePath of paths) {
      const url = await uploadQuestionImage(filePath);
      imageUrls.value.push(url);
      if (imageUrls.value.length >= 9) break;
    }
    uni.showToast({ title: "图片上传成功", icon: "success" });
  } catch (err: any) {
    uni.showToast({ title: err?.message || "图片上传失败", icon: "none" });
  } finally {
    uploadingImages.value = false;
  }
}

async function uploadQuestionImage(filePath: string) {
  const upload = await uni.uploadFile({
    url: `${BASE_URL}/api/common/upload?bizType=question`,
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
  if (text.startsWith("http://") || text.startsWith("https://")) {
    return text;
  }
  if (text.startsWith("/")) {
    return `${BASE_URL}${text}`;
  }
  return `${BASE_URL}/${text}`;
}

function removeImage(index: number) {
  imageUrls.value.splice(index, 1);
}

async function fetchCategoryTree(parentId?: number) {
  return discoverApi.getCategoryTree(parentId);
}

function hasChildren(node: AppCategoryTreeNodeVO) {
  return Array.isArray(node.children) && node.children.length > 0;
}

function getCategoryText(node: AppCategoryTreeNodeVO) {
  return node.name || node.label || "";
}

async function ensureRootCategories() {
  if (categoryTree.value.length) return;
  categoryLoading.value = true;
  try {
    categoryTree.value = await fetchCategoryTree();
  } finally {
    categoryLoading.value = false;
  }
}

function applyCategory(node: AppCategoryTreeNodeVO, parent?: AppCategoryTreeNodeVO) {
  categoryId.value = node.id;
  const currentText = getCategoryText(node);
  const parentText = parent ? getCategoryText(parent) : "";
  categoryName.value = parentText ? `${parentText} / ${currentText}` : currentText;
  showCategoryPanel.value = false;
}

async function loadChildren(root: AppCategoryTreeNodeVO) {
  if (hasChildren(root)) {
    childCategoryMap.value[root.id] = root.children || [];
    return;
  }
  if (childCategoryMap.value[root.id]) return;
  try {
    const children = await fetchCategoryTree(root.id);
    childCategoryMap.value[root.id] = children || [];
  } catch {
    childCategoryMap.value[root.id] = [];
  }
}

async function preloadChildrenForSearch() {
  if (!normalizedSearch.value || categorySearchLoading.value) return;
  categorySearchLoading.value = true;
  try {
    for (const root of categoryTree.value) {
      if (!childCategoryMap.value[root.id]) {
        await loadChildren(root);
      }
    }
  } finally {
    categorySearchLoading.value = false;
  }
}

async function chooseCategory() {
  try {
    await ensureRootCategories();
    if (!categoryTree.value.length) {
      uni.showToast({ title: "暂无可选分类", icon: "none" });
      return;
    }
    showCategoryPanel.value = true;
    if (selectedRootId.value === null && categoryTree.value.length) {
      selectedRootId.value = categoryTree.value[0].id;
      await loadChildren(categoryTree.value[0]);
    }
  } catch {
    uni.showToast({ title: "分类加载失败", icon: "none" });
  }
}

async function toggleCategoryPanel() {
  if (showCategoryPanel.value) {
    closeCategoryPanel();
    return;
  }
  await chooseCategory();
}

async function selectRoot(node: AppCategoryTreeNodeVO) {
  selectedRootId.value = node.id;
  await loadChildren(node);
  const children = childCategoryMap.value[node.id] || [];
  if (!children.length) {
    applyCategory(node);
  }
}

function selectChild(node: AppCategoryTreeNodeVO, parent: AppCategoryTreeNodeVO) {
  applyCategory(node, parent);
}

function closeCategoryPanel() {
  showCategoryPanel.value = false;
  categorySearch.value = "";
}

watch(categorySearch, () => {
  if (normalizedSearch.value) {
    preloadChildrenForSearch();
  }
});
</script>

<template>
  <view class="page">
    <view class="label">问题标题 *</view>
    <textarea v-model="title" class="input area short" maxlength="100" placeholder="简明描述你的问题" />
    <view class="counter">{{ title.length }}/100</view>

    <view class="label">问题分类 *</view>
    <view class="input category-picker" @click="toggleCategoryPanel">
      <text :class="{ placeholder: !categoryName }">{{ categoryPlaceholder }}</text>
      <text :class="['arrow', { up: showCategoryPanel }]" />
    </view>

    <view v-if="showCategoryPanel" class="category-panel">
      <view class="panel-search-wrap">
        <input v-model="categorySearch" class="panel-search-input" placeholder="搜索分类关键词" />
      </view>

      <scroll-view v-if="normalizedSearch" class="search-result-list" scroll-y>
        <view
          v-for="item in searchMatches"
          :key="`${item.parent?.id || 'root'}-${item.node.id}`"
          class="search-item"
          @click="applyCategory(item.node, item.parent)"
        >
          {{ item.parent ? `${getCategoryText(item.parent)} / ${getCategoryText(item.node)}` : getCategoryText(item.node) }}
        </view>
        <view v-if="!searchMatches.length && !categorySearchLoading" class="empty-child">未找到匹配分类</view>
        <view v-if="categorySearchLoading" class="empty-child">正在加载分类...</view>
      </scroll-view>

      <view v-else class="category-body">
        <scroll-view class="root-list" scroll-y>
          <view
            v-for="root in filteredRoots"
            :key="root.id"
            :class="['root-item', { active: selectedRootId === root.id }]"
            @click="selectRoot(root)"
          >
            {{ getCategoryText(root) }}
          </view>
        </scroll-view>
        <scroll-view class="child-list" scroll-y>
          <view
            v-for="child in currentChildren"
            :key="child.id"
            class="child-item"
            @click="selectedRoot && selectChild(child, selectedRoot)"
          >
            {{ getCategoryText(child) }}
          </view>
          <view v-if="!currentChildren.length" class="empty-child">暂无子分类</view>
        </scroll-view>
      </view>
    </view>

    <view class="label">问题详情（选填）</view>
    <textarea
      v-model="content"
      class="input area"
      maxlength="2000"
      placeholder="详细描述你的问题背景、症状、持续时间、已尝试方法等"
    />
    <view class="counter">{{ content.length }}/2000</view>

    <view class="label">问题图片（最多9张）</view>
    <view class="images-wrap">
      <view v-for="(url, idx) in imageUrls" :key="url + idx" class="img-item">
        <image class="img" :src="url" mode="aspectFill" />
        <view class="img-del" @click="removeImage(idx)">×</view>
      </view>
      <view class="img-uploader" @click="chooseImages">
        <text v-if="uploadingImages">上传中...</text>
        <text v-else>+ 添加图片</text>
      </view>
    </view>

    <view class="label">标签（最多5个）</view>
    <input v-model="tagsInput" class="input" placeholder="多个标签用空格或逗号分隔" />

    <button class="submit" @click="submit">发布问题</button>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 24rpx;
}

.label {
  margin: 18rpx 0 12rpx;
  font-size: 30rpx;
  font-weight: 700;
}

.input {
  min-height: 76rpx;
  border: 2rpx solid #eadfbf;
  border-radius: 20rpx;
  background: #fff;
  padding: 16rpx 20rpx;
}

.category-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.category-panel {
  margin-top: 12rpx;
  border: 2rpx solid #eadfbf;
  border-radius: 20rpx;
  background: #fff;
  overflow: hidden;
}

.panel-search-wrap {
  padding: 14rpx 16rpx;
}

.panel-search-input {
  height: 64rpx;
  border: 1rpx solid #d8e8f8;
  border-radius: 12rpx;
  padding: 0 16rpx;
  font-size: 26rpx;
  color: #1d4468;
}

.search-result-list {
  max-height: 420rpx;
}

.search-item {
  padding: 18rpx 20rpx;
  border-bottom: 1rpx solid #f7f0dd;
  font-size: 26rpx;
  color: #1d4468;
}

.category-body {
  display: flex;
  height: 360rpx;
}

.root-list {
  width: 42%;
  border-right: 1rpx solid #f1e8d0;
}

.child-list {
  width: 58%;
}

.root-item,
.child-item,
.empty-child {
  padding: 18rpx 20rpx;
  font-size: 26rpx;
  color: #1d4468;
}

.root-item.active {
  background: #edf7fd;
  color: #2f8ec3;
  font-weight: 700;
}

.child-item {
  border-bottom: 1rpx solid #f7f0dd;
}

.empty-child {
  color: #8ba0b3;
}

.placeholder {
  color: #8ba0b3;
}

.arrow {
  width: 14rpx;
  height: 14rpx;
  border-right: 3rpx solid #8ba0b3;
  border-bottom: 3rpx solid #8ba0b3;
  transform: rotate(45deg);
  transition: transform 0.2s ease;
  margin-right: 4rpx;
}

.arrow.up {
  transform: rotate(-135deg);
}

.area {
  min-height: 220rpx;
  width: auto;
}

.short {
  min-height: 120rpx;
}

.counter {
  margin-top: 8rpx;
  text-align: right;
  color: #8ba0b3;
  font-size: 24rpx;
}

.images-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.img-item {
  width: 180rpx;
  height: 180rpx;
  position: relative;
  border-radius: 14rpx;
  overflow: hidden;
  border: 2rpx solid #eadfbf;
}

.img {
  width: 100%;
  height: 100%;
}

.img-del {
  position: absolute;
  right: 6rpx;
  top: 2rpx;
  width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 26rpx;
}

.img-uploader {
  width: 180rpx;
  height: 180rpx;
  border: 2rpx dashed #d6cba8;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7d8f9f;
  font-size: 24rpx;
  background: #fffdf7;
}

.submit {
  margin-top: 28rpx;
  border: none;
  border-radius: 18rpx;
  background: #4ba7d9;
  color: #fff;
}
</style>
