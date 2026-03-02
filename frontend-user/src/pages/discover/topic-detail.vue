<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";
import { topicApi, type AppTopicDetailVO, type AppTopicQuestionItemVO } from "@/api/topic";
import { openQuestionDetail } from "@/utils/nav";

type SortKey = "hot" | "latest" | "unsolved";

const topicId = ref<number>(0);
const topic = ref<AppTopicDetailVO | null>(null);
const questions = ref<AppTopicQuestionItemVO[]>([]);
const pageNum = ref(1);
const pageSize = 10;
const finished = ref(false);
const loading = ref(false);
const activeSort = ref<SortKey>("hot");
const failedCover = ref(false);
const followLoading = ref(false);

const sortTabs: Array<{ key: SortKey; label: string }> = [
  { key: "hot", label: "最热" },
  { key: "latest", label: "最新" },
  { key: "unsolved", label: "待解决" }
];

const followText = computed(() => (topic.value?.followed ? "已关注" : "关注"));
const followBtnClass = computed(() => (topic.value?.followed ? "follow-btn active" : "follow-btn"));

async function loadTopicDetail() {
  topic.value = await topicApi.detail(topicId.value);
  failedCover.value = false;
}

async function loadQuestions(reset = false) {
  if (loading.value) return;
  if (!reset && finished.value) return;
  loading.value = true;
  try {
    const page = reset ? 1 : pageNum.value;
    const data = await topicApi.questions(topicId.value, {
      sortBy: activeSort.value,
      page,
      pageSize
    });
    const list = data?.list || [];
    if (reset) questions.value = list;
    else questions.value = questions.value.concat(list);
    pageNum.value = page + 1;
    finished.value = questions.value.length >= (data?.total || 0) || list.length < pageSize;
  } catch {
    if (reset) questions.value = [];
    uni.showToast({ title: "专题问题加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function switchSort(key: SortKey) {
  if (activeSort.value === key) return;
  activeSort.value = key;
  pageNum.value = 1;
  finished.value = false;
  await loadQuestions(true);
}

async function toggleFollow() {
  if (!topic.value || followLoading.value) return;
  followLoading.value = true;
  try {
    if (topic.value.followed) {
      await topicApi.unfollow(topic.value.id);
      topic.value = {
        ...topic.value,
        followed: false,
        followCount: Math.max(0, Number(topic.value.followCount || 0) - 1)
      };
      uni.showToast({ title: "已取消关注", icon: "none" });
    } else {
      await topicApi.follow(topic.value.id);
      topic.value = {
        ...topic.value,
        followed: true,
        followCount: Number(topic.value.followCount || 0) + 1
      };
      uni.showToast({ title: "关注成功", icon: "success" });
    }
  } catch (err: any) {
    uni.showToast({ title: err?.message || "操作失败", icon: "none" });
  } finally {
    followLoading.value = false;
  }
}

function goAskInTopic() {
  if (!topicId.value) return;
  const t = encodeURIComponent(topic.value?.title || "");
  uni.navigateTo({ url: `/pages/question/ask?topicId=${topicId.value}&topicTitle=${t}` });
}

onLoad(async (options) => {
  topicId.value = Number(options?.topicId || 0);
  const title = decodeURIComponent(String(options?.topicTitle || "专题"));
  if (!topicId.value) {
    uni.showToast({ title: "专题参数错误", icon: "none" });
    setTimeout(() => uni.navigateBack(), 120);
    return;
  }
  uni.setNavigationBarTitle({ title });
  await loadTopicDetail();
  if (topic.value?.title) {
    uni.setNavigationBarTitle({ title: topic.value.title });
  }
  await loadQuestions(true);
});

onReachBottom(() => {
  loadQuestions(false);
});
</script>

<template>
  <view class="page">
    <view v-if="topic" class="hero">
      <image
        v-if="topic.coverImg && !failedCover"
        class="hero-cover"
        :src="topic.coverImg"
        mode="aspectFill"
        @error="failedCover = true"
      />
      <view v-else class="hero-cover hero-cover--fallback">暂无封面</view>
      <view class="hero-mask" />
      <view class="hero-text">
        <view class="hero-title">{{ topic.title }}</view>
        <view class="hero-sub">{{ topic.subtitle || "专题下的相关问题集合" }}</view>
      </view>
    </view>

    <view v-if="topic" class="intro app-card">
      <view class="meta-row">
        <view class="meta-item"><text class="meta-label">问答</text><text>{{ topic.questionCount || 0 }}</text></view>
        <view class="meta-item"><text class="meta-label">关注</text><text>{{ topic.followCount || 0 }}</text></view>
        <view class="meta-item"><text class="meta-label">今日新增</text><text>{{ topic.todayNewCount || 0 }}</text></view>
      </view>
      <view class="divider" />
      <view class="intro-title">专题介绍</view>
      <view class="intro-text">{{ topic.intro || topic.subtitle || "暂无介绍" }}</view>
      <view v-if="topic.tags?.length" class="tag-row">
        <text v-for="(tag, idx) in topic.tags" :key="tag + idx" class="tag-chip">#{{ tag }}</text>
      </view>
      <view class="follow-row">
        <button class="follow-btn" :class="{ active: topic.followed }" :disabled="followLoading" @click="toggleFollow">
          {{ followText }}
        </button>
      </view>
    </view>

    <view class="sort-tabs">
      <view
        v-for="tab in sortTabs"
        :key="tab.key"
        class="sort-tab"
        :class="{ active: activeSort === tab.key }"
        @click="switchSort(tab.key)"
      >
        {{ tab.label }}
      </view>
    </view>

    <view v-if="!questions.length && !loading" class="state">该专题暂无问题</view>
    <view v-else>
      <view
        v-for="q in questions"
        :key="q.id"
        class="app-card question-item"
        @click="openQuestionDetail(q.id)"
      >
        <view class="q-title">{{ q.title }}</view>
        <view class="q-meta">
          <text>{{ q.answerCount || 0 }}回答</text>
          <text>{{ q.viewCount || 0 }}浏览</text>
          <text>{{ q.likeCount || 0 }}点赞</text>
        </view>
      </view>
    </view>

    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="finished && questions.length" class="state">没有更多了</view>
  </view>
  <view class="topic-plus" @click="goAskInTopic">+</view>
</template>

<style scoped lang="scss">
.page {
  padding: 18rpx 16rpx 24rpx;
  min-height: 100vh;
  box-sizing: border-box;
}

.hero {
  position: relative;
  height: 300rpx;
  border-radius: 18rpx;
  overflow: hidden;
}

.hero-cover {
  width: 100%;
  height: 100%;
  background: #e9f1f8;
}

.hero-cover--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5f7386;
  font-size: 28rpx;
}

.hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.2), rgba(0, 0, 0, 0.55));
}

.hero-text {
  position: absolute;
  left: 16rpx;
  right: 16rpx;
  bottom: 16rpx;
  color: #fff;
}

.hero-title {
  font-size: 34rpx;
  font-weight: 700;
}

.hero-sub {
  margin-top: 8rpx;
  font-size: 24rpx;
  opacity: 0.95;
}

.meta-row {
  padding: 2rpx 0 10rpx;
  display: flex;
  justify-content: space-between;
}

.meta-item {
  display: flex;
  gap: 6rpx;
  color: #557188;
  font-size: 24rpx;
}

.meta-label {
  color: #8ba0b3;
}

.intro {
  margin-top: 12rpx;
  padding: 14rpx;
}

.divider {
  height: 1rpx;
  background: #dfeaf3;
  margin: 2rpx 0 12rpx;
}

.intro-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1f3f55;
}

.intro-text {
  margin-top: 8rpx;
  color: #6f879b;
  font-size: 24rpx;
  line-height: 1.5;
}

.tag-row {
  margin-top: 10rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag-chip {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: #edf6fb;
  color: #3f7ea2;
  font-size: 22rpx;
}

.follow-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12rpx;
}

.follow-btn {
  min-width: 160rpx;
  height: 56rpx;
  border-radius: 28rpx;
  border: 2rpx solid #4ba7d9;
  background: #4ba7d9;
  color: #fff;
  font-size: 24rpx;
  font-weight: 700;
  padding: 0 20rpx;
}

.follow-btn.active {
  border-color: #cfdbe4;
  background: #eef3f7;
  color: #6f879b;
}

.sort-tabs {
  margin-top: 14rpx;
  display: flex;
  gap: 10rpx;
}

.sort-tab {
  flex: 1;
  height: 62rpx;
  border-radius: 12rpx;
  border: 1rpx solid #c9dceb;
  color: #6b8297;
  font-size: 25rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7fbff;
}

.sort-tab.active {
  color: #1f3f55;
  border-color: #84c5ea;
  background: #dff0fb;
  font-weight: 700;
}

.question-item {
  padding: 16rpx;
  margin-top: 12rpx;
}

.q-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f3f55;
  line-height: 1.35;
}

.q-meta {
  margin-top: 10rpx;
  display: flex;
  gap: 16rpx;
  color: #7f95a8;
  font-size: 24rpx;
}

.state {
  text-align: center;
  color: #8ba0b3;
  padding: 20rpx 0;
}

.topic-plus {
  position: fixed;
  right: 30rpx;
  bottom: 140rpx;
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  background: #4ba7d9;
  color: #fff;
  font-size: 54rpx;
  line-height: 86rpx;
  text-align: center;
  box-shadow: 0 10rpx 24rpx rgba(75, 167, 217, 0.28);
}
</style>