<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { discoverApi, type AppCategoryTreeNodeVO } from "@/api/discover";
import { questionApi, type AppQuestionListItemVO } from "@/api/question";
import { openQuestionDetail } from "@/utils/nav";

const rootCategoryId = ref<number>(0);
const rootCategoryName = ref("分类");
const childCategories = ref<AppCategoryTreeNodeVO[]>([]);
const activeCategoryId = ref<number>(0);
const questions = ref<AppQuestionListItemVO[]>([]);
const loading = ref(false);

async function loadChildren() {
  try {
    childCategories.value = await discoverApi.getCategoryTree(rootCategoryId.value);
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

async function initPage() {
  await loadChildren();
  const firstChildId = childCategories.value.length ? Number(childCategories.value[0].id) : 0;
  const targetId = firstChildId || rootCategoryId.value;
  await loadQuestions(targetId);
}

onLoad(async (options) => {
  const id = Number(options?.categoryId || 0);
  const name = decodeURIComponent(String(options?.categoryName || "分类"));
  if (!id) {
    uni.showToast({ title: "分类参数错误", icon: "none" });
    setTimeout(() => uni.navigateBack(), 120);
    return;
  }
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
        @click="loadQuestions(Number(sub.id))"
      >
        {{ sub.name }}
      </view>
    </view>

    <view class="section-title">问题列表</view>
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!questions.length" class="state">该分类暂无问题</view>
    <view v-else>
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
