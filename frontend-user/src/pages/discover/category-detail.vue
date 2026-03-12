<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { discoverApi, type AppCategoryTreeNodeVO } from "@/api/discover";
import { expertApi, type AppExpertPostCategoryVO, type AppExpertPostItemVO } from "@/api/expert";
import { questionApi, type AppQuestionListItemVO } from "@/api/question";
import { openExpertPostDetailPage, openQuestionDetail } from "@/utils/nav";

const rootCategoryId = ref<number>(0);
const rootCategoryName = ref("分类");
const categoryType = ref<"qa" | "kb">("qa");
const childCategories = ref<Array<{ id: number; name: string }>>([]);
const activeCategoryId = ref<number>(0);
const questions = ref<AppQuestionListItemVO[]>([]);
const kbPosts = ref<AppExpertPostItemVO[]>([]);
const loading = ref(false);

async function loadChildren() {
  if (categoryType.value === "qa") {
    try {
      const rows = await discoverApi.getCategoryTree(rootCategoryId.value);
      childCategories.value = (rows || []).map((item: AppCategoryTreeNodeVO) => ({
        id: Number(item.id),
        name: item.name || ""
      }));
    } catch {
      childCategories.value = [];
    }
    return;
  }
  try {
    const categories = await expertApi.categories();
    const rows = (categories || []).filter(
      (item: AppExpertPostCategoryVO) => Number(item.parentId || 0) === rootCategoryId.value
    );
    childCategories.value = rows.map((item) => ({
      id: Number(item.id),
      name: item.name || ""
    }));
  } catch {
    childCategories.value = [];
  }
}

async function loadQuestions(categoryId: number) {
  if (!categoryId) return;
  activeCategoryId.value = categoryId;
  loading.value = true;
  try {
    const page = await questionApi.page({
      page: 1,
      pageSize: 20,
      categoryId
    });
    questions.value = page?.list || [];
  } catch {
    questions.value = [];
    uni.showToast({ title: "分类问题加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function loadKbPosts(categoryId: number) {
  if (!categoryId) return;
  activeCategoryId.value = categoryId;
  loading.value = true;
  try {
    const page = await expertApi.page({
      page: 1,
      pageSize: 20,
      categoryId,
      sortBy: "hot"
    });
    kbPosts.value = page?.list || [];
  } catch {
    kbPosts.value = [];
    uni.showToast({ title: "分类文章加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function loadByCategory(categoryId: number) {
  if (categoryType.value === "qa") {
    await loadQuestions(categoryId);
    return;
  }
  await loadKbPosts(categoryId);
}

async function initPage() {
  await loadChildren();
  const firstChildId = childCategories.value.length ? childCategories.value[0].id : 0;
  const targetId = firstChildId || rootCategoryId.value;
  await loadByCategory(targetId);
}

onLoad(async (options) => {
  const id = Number(options?.categoryId || 0);
  const name = decodeURIComponent(String(options?.categoryName || "分类"));
  const type = String(options?.categoryType || "qa").toLowerCase();
  if (!id) {
    uni.showToast({ title: "分类参数错误", icon: "none" });
    setTimeout(() => uni.navigateBack(), 120);
    return;
  }
  categoryType.value = type === "kb" ? "kb" : "qa";
  rootCategoryId.value = id;
  rootCategoryName.value = name || "分类";
  uni.setNavigationBarTitle({ title: rootCategoryName.value });
  await initPage();
});
</script>

<template>
  <view class="page">
    <view v-if="childCategories.length" class="child-row">
      <view
        v-for="sub in childCategories"
        :key="sub.id"
        class="child-chip"
        :class="{ active: activeCategoryId === Number(sub.id) }"
        @click="loadByCategory(Number(sub.id))"
      >
        {{ sub.name }}
      </view>
    </view>

    <view class="section-title">{{ categoryType === "qa" ? "问题列表" : "科普文章" }}</view>
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="categoryType === 'qa' && !questions.length" class="state">该分类暂无问题</view>
    <view v-else-if="categoryType === 'kb' && !kbPosts.length" class="state">该分类暂无文章</view>
    <view v-else-if="categoryType === 'qa'">
      <view
        v-for="q in questions"
        :key="q.id"
        class="app-card question-item"
        @click="openQuestionDetail(q.id)"
      >
        <view class="question-title">{{ q.title }}</view>
        <view class="question-meta">
          <text>{{ q.answerCount || 0 }}回答</text>
          <text>{{ q.viewCount || 0 }}浏览</text>
          <text>{{ q.likeCount || 0 }}点赞</text>
        </view>
        <view class="question-time">{{ q.createdAt || "" }}</view>
      </view>
    </view>
    <view v-else>
      <view
        v-for="item in kbPosts"
        :key="item.id"
        class="app-card question-item"
        @click="openExpertPostDetailPage(item.id)"
      >
        <view class="question-title">{{ item.title || "未命名文章" }}</view>
        <view class="article-summary">{{ item.summary || "暂无摘要" }}</view>
        <view class="question-meta">
          <text>{{ item.viewCount || 0 }}浏览</text>
          <text>{{ item.likeCount || 0 }}点赞</text>
          <text>{{ item.favoriteCount || 0 }}收藏</text>
        </view>
        <view class="question-time">{{ item.createdAt || "" }}</view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 18rpx 16rpx 24rpx;
  min-height: 100vh;
  box-sizing: border-box;
}

.child-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 14rpx;
}

.child-chip {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  border: 1rpx solid #c9dceb;
  color: #6b8297;
  font-size: 24rpx;
  background: #f7fbff;
}

.child-chip.active {
  color: #1f3f55;
  border-color: #84c5ea;
  background: #dff0fb;
  font-weight: 700;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f3f55;
  margin: 8rpx 0 12rpx;
}

.question-item {
  padding: 18rpx 16rpx;
  margin-bottom: 12rpx;
}

.question-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f3f55;
  line-height: 1.35;
}

.question-meta {
  margin-top: 10rpx;
  display: flex;
  gap: 16rpx;
  color: #7f95a8;
  font-size: 24rpx;
}

.article-summary {
  margin-top: 10rpx;
  color: #7f95a8;
  font-size: 24rpx;
  line-height: 1.4;
}

.question-time {
  margin-top: 8rpx;
  color: #98abba;
  font-size: 22rpx;
}

.state {
  text-align: center;
  color: #8ba0b3;
  margin-top: 80rpx;
}
</style>
