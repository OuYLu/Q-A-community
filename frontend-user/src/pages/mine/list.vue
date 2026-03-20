<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onReachBottom, onShow } from "@dcloudio/uni-app";
import {
  meApi,
  type AppMyFavoriteItemVO,
  type AppMyHistoryItemVO,
  type AppMyAnswerItemVO,
  type AppFollowUserItemVO,
  type AppFollowTopicItemVO
} from "@/api/me";
import { expertApi, type AppExpertPostItemVO } from "@/api/expert";
import { ensurePageAuth } from "@/utils/auth-guard";
import { questionApi, type AppMyQuestionItemVO } from "@/api/question";
import { openAnswerDetailPage, openExpertPostDetailPage, openQuestionDetail, openUserHomePage } from "@/utils/nav";
import { BASE_URL } from "@/utils/constants";

type ListType =
  | "favorites"
  | "history"
  | "questions"
  | "answers"
  | "following"
  | "followers"
  | "topic-following"
  | "expert-posts";
type MixedItem =
  | AppMyFavoriteItemVO
  | AppMyHistoryItemVO
  | AppMyQuestionItemVO
  | AppMyAnswerItemVO
  | AppFollowUserItemVO
  | AppFollowTopicItemVO
  | AppExpertPostItemVO;

const type = ref<ListType>("favorites");
const title = ref("列表");
const loading = ref(false);
const finished = ref(false);
const pageNum = ref(1);
const pageSize = 10;
const total = ref(0);
const items = ref<MixedItem[]>([]);
const entered = ref(false);
const answerEffectiveCount = ref(0);
const questionEffectiveCount = ref(0);

const titleMap: Record<ListType, string> = {
  favorites: "我的收藏",
  history: "浏览历史",
  questions: "我的提问",
  answers: "我的回答",
  following: "关注",
  followers: "粉丝",
  "topic-following": "专题关注",
  "expert-posts": "我的科普"
};

const isQuestionList = computed(() => type.value === "questions");
const isFollowList = computed(() => type.value === "following" || type.value === "followers");
const questionItems = computed(() => (items.value as AppMyQuestionItemVO[]) || []);
const followItems = computed(() => (items.value as AppFollowUserItemVO[]) || []);
const favoriteItems = computed(() => (items.value as AppMyFavoriteItemVO[]) || []);
const favoriteGroups = computed(() => {
  if (type.value !== "favorites") return [] as Array<{ key: number; title: string; items: AppMyFavoriteItemVO[] }>;
  const source = favoriteItems.value;
  const groups = [
    { key: 1, title: "问题收藏", items: source.filter((x) => Number(x?.bizType || 1) === 1) },
    { key: 3, title: "回答收藏", items: source.filter((x) => Number(x?.bizType || 1) === 3) },
    { key: 2, title: "科普收藏", items: source.filter((x) => Number(x?.bizType || 1) === 2) }
  ];
  return groups.filter((g) => g.items.length > 0);
});
const headText = computed(() => {
  if (type.value === "answers") return "共 " + answerEffectiveCount.value + " 条（有效）";
  if (type.value === "questions") return "共 " + questionEffectiveCount.value + " 条（有效）";
  return "共 " + total.value + " 条";
});

function normalizeType(raw?: string): ListType {
  const valid: ListType[] = [
    "favorites",
    "history",
    "questions",
    "answers",
    "following",
    "followers",
    "topic-following",
    "expert-posts"
  ];
  if (raw && valid.includes(raw as ListType)) return raw as ListType;
  return "favorites";
}

function resolveAvatar(avatar?: string) {
  if (!avatar) return "";
  const raw = String(avatar).trim();
  if (!raw) return "";
  if (raw.startsWith("http://") || raw.startsWith("https://")) return raw;
  return `${BASE_URL}${raw}`;
}

function isInvalidAnswerRow(item: any) {
  if (type.value !== "answers") return false;
  return Number(item?.effective || 0) !== 1;
}

function isInvalidQuestionRow(item: any) {
  if (type.value !== "questions") return false;
  return Number(item?.status || 0) === 4;
}


function rowMainText(item: any) {
  if (type.value === "favorites") {
    const bizType = Number(item?.bizType || 1);
    if (bizType === 3) return item.questionTitle || item.title || "";
    return item.title || "";
  }
  if (type.value === "answers") return `问题：${item.questionTitle || ""}`;
  if (type.value === "following" || type.value === "followers") return item.nickname || `用户 ${item.userId}`;
  return item.title || "";
}

function rowSubText(item: any) {
  if (type.value === "favorites") {
    const bizType = Number(item?.bizType || 1);
    if (bizType === 2) {
      return item.contentPreview || `${item.likeCount || 0} 点赞 ${item.favoriteCount || 0} 收藏`;
    }
    if (bizType === 3) {
      return item.contentPreview || "暂无回答内容";
    }
    return `${item.answerCount || 0} 回答 ${item.likeCount || 0} 点赞`;
  }
  if (type.value === "history") return item.subTitle || "";
  if (type.value === "questions") return `状态：${item.status}，${item.answerCount || 0} 回答`;
  if (type.value === "answers") return isInvalidAnswerRow(item) ? "该回答因违规已删除" : item.contentPreview || "";
  if (type.value === "topic-following") return item.subtitle || "点击查看专题详情";
  if (type.value === "expert-posts") return `${item.likeCount || 0} 点赞 ${item.viewCount || 0} 浏览`;
  return `专家状态：${item.expertStatus ?? "普通用户"}`;
}

function rowTimeText(item: any) {
  if (type.value === "favorites") return item.favoriteAt;
  if (type.value === "history") return item.viewedAt;
  if (type.value === "following" || type.value === "followers") return item.followedAt;
  if (type.value === "topic-following") return item.followedAt;
  return item.createdAt;
}

function formatDate(input?: string) {
  if (!input) return "";
  const value = String(input).trim();
  if (value.length >= 10) return value.slice(0, 10);
  return value;
}


function questionStatusText(item: AppMyQuestionItemVO) {
  if ((item as any).acceptedAnswerId) return "已采纳最佳答案";
  const count = item.answerCount || 0;
  if (count > 0) return `已有 ${count} 条回答`;
  return "暂无回答";
}

function questionStatusClass(item: AppMyQuestionItemVO) {
  if ((item as any).acceptedAnswerId) return "solved";
  const count = item.answerCount || 0;
  if (count > 0) return "waiting";
  return "empty-answer";
}

function questionTags(item: AppMyQuestionItemVO) {
  const tags: string[] = [];
  if ((item as any).categoryName) tags.push((item as any).categoryName);
  (((item as any).tags || []) as string[]).slice(0, 3).forEach((x) => tags.push(`#${x}`));
  return tags;
}

function isQuestionSelfOnly(item: AppMyQuestionItemVO) {
  return Number((item as any).status || 0) === 5;
}

async function loadAnswerEffectiveCount() {
  if (type.value !== "answers") return;
  try {
    const ov = await meApi.overview();
    answerEffectiveCount.value = Number(ov.answerCount || 0);
  } catch {
    answerEffectiveCount.value = 0;
  }
}

async function loadQuestionEffectiveCount() {
  if (type.value !== "questions") return;
  try {
    const ov = await meApi.overview();
    questionEffectiveCount.value = Number(ov.questionCount || 0);
  } catch {
    questionEffectiveCount.value = 0;
  }
}

async function fetchPage(reset = false) {
  if (loading.value || (!reset && finished.value)) return;
  loading.value = true;
  try {
    const page = reset ? 1 : pageNum.value;
    let resp: any;
    const query = { page, pageSize };
    switch (type.value) {
      case "favorites":
        resp = await meApi.favorites(query);
        break;
      case "history":
        resp = await meApi.history(query);
        break;
      case "questions":
        resp = await questionApi.myQuestions(query);
        break;
      case "answers":
        resp = await meApi.answers(query);
        break;
      case "following":
        resp = await meApi.following(query);
        break;
      case "followers":
        resp = await meApi.followers(query);
        break;
      case "topic-following":
        resp = await meApi.followedTopics(query);
        break;
      case "expert-posts":
        resp = await expertApi.myPosts(query);
        break;
    }

    total.value = Number(resp.total || 0);
    const list = (resp.list || []) as MixedItem[];
    if (reset) items.value = list;
    else items.value = items.value.concat(list);
    pageNum.value = page + 1;
    finished.value = items.value.length >= total.value || list.length < pageSize;
  } catch {
    uni.showToast({ title: "列表加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function openRow(item: any) {
  if (type.value === "questions" && item?.id) {
    if (isInvalidQuestionRow(item)) {
      uni.showToast({ title: "该提问因违规已下架", icon: "none" });
      return;
    }
    openQuestionDetail(Number(item.id));
    return;
  }
  if (type.value === "favorites") {
    const bizType = Number(item?.bizType || 1);
    if (bizType === 2 && item?.bizId) {
      openExpertPostDetailPage(Number(item.bizId));
      return;
    }
    if (bizType === 3 && (item?.answerId || item?.bizId)) {
      openAnswerDetailPage(Number(item.answerId || item.bizId));
      return;
    }
    if ((item?.questionId || item?.bizId) && bizType === 1) {
      openQuestionDetail(Number(item.questionId || item.bizId));
      return;
    }
  }
  if (type.value === "answers" && item?.questionId) {
    if (isInvalidAnswerRow(item)) {
      uni.showToast({ title: "该回答因违规已删除", icon: "none" });
      return;
    }
    openQuestionDetail(Number(item.questionId));
    return;
  }
  if (type.value === "history" && item?.bizType === 1 && item?.bizId) {
    openQuestionDetail(Number(item.bizId));
    return;
  }
  if (type.value === "history" && item?.bizType === 2 && item?.bizId) {
    openExpertPostDetailPage(Number(item.bizId));
    return;
  }
  if (type.value === "expert-posts" && item?.id) {
    openExpertPostDetailPage(Number(item.id));
    return;
  }
  if (type.value === "topic-following" && item?.topicId) {
    uni.navigateTo({
      url: `/pages/discover/topic-detail?topicId=${item.topicId}&topicTitle=${encodeURIComponent(item.title || "")}`
    });
    return;
  }
  if ((type.value === "following" || type.value === "followers") && item?.userId) {
    openUserHomePage(Number(item.userId));
  }
}

onLoad(async (options) => {
  if (!ensurePageAuth()) return;
  type.value = normalizeType(options?.type);
  title.value = titleMap[type.value];
  uni.setNavigationBarTitle({ title: title.value });
  if (type.value === "answers") await loadAnswerEffectiveCount();
  if (type.value === "questions") await loadQuestionEffectiveCount();
  fetchPage(true);
});

onShow(async () => {
  if (!entered.value) {
    entered.value = true;
    return;
  }
  if (!ensurePageAuth()) return;
  if (type.value === "answers") await loadAnswerEffectiveCount();
  if (type.value === "questions") await loadQuestionEffectiveCount();
  fetchPage(true);
});

onReachBottom(() => {
  fetchPage(false);
});
</script>

<template>
  <view class="page">
    <view class="head">{{ headText }}</view>
    <view v-if="!items.length && !loading" class="empty">暂无数据</view>

    <template v-if="isQuestionList">
      <view
        v-for="item in questionItems"
        :key="item.id"
        class="question-card"
        :class="{ 'question-card-disabled': isInvalidQuestionRow(item) }"
        @tap="openRow(item)"
      >
        <view class="question-row">
          <view class="dot" :class="questionStatusClass(item)" />
          <view class="question-title">{{ item.title }}</view>
          <text v-if="isInvalidQuestionRow(item)" class="invalid-badge">因违规已下架</text>
          <text v-if="isQuestionSelfOnly(item)" class="lock-flag">&#128274;</text>
          <text class="chevron">›</text>
        </view>

        <view v-if="questionTags(item).length" class="tags-row">
          <text
            v-for="(tag, tagIndex) in questionTags(item)"
            :key="`${item.id}-${tag}-${tagIndex}`"
            :class="['chip', tagIndex === 0 ? 'chip-main' : 'chip-sub']"
          >
            {{ tag }}
          </text>
        </view>

        <view class="meta-row">
          <view class="meta-left">
            <text class="meta-item">赞 {{ item.likeCount || 0 }}</text>
            <text class="meta-item">浏览 {{ (item as any).viewCount || 0 }}</text>
            <text class="meta-item">{{ item.createdAt || "" }}</text>
          </view>
          <text v-if="isInvalidQuestionRow(item)" class="status-text status-offline">因违规已下架</text>
          <text v-else :class="['status-text', questionStatusClass(item)]">{{ questionStatusText(item) }}</text>
        </view>
      </view>
    </template>

    <template v-else-if="isFollowList">
      <view
        v-for="item in followItems"
        :key="item.userId"
        class="follow-card"
        @tap="openRow(item)"
      >
        <image v-if="resolveAvatar(item.avatar)" class="follow-avatar" :src="resolveAvatar(item.avatar)" mode="aspectFill" />
        <view v-else class="follow-avatar follow-avatar--fallback">{{ (item.nickname || "用户").slice(0, 1) }}</view>
        <view class="follow-info">
          <view class="follow-name">{{ item.nickname || `用户 ${item.userId}` }}</view>
          <view class="follow-time">关注日期：{{ formatDate(item.followedAt) || "-" }}</view>
        </view>
      </view>
    </template>

    <template v-else-if="type === 'favorites'">
      <view v-for="group in favoriteGroups" :key="group.key" class="fav-group">
        <view class="fav-group-head">
          <text class="fav-group-title">{{ group.title }}</text>
          <text class="fav-group-count">{{ group.items.length }}</text>
        </view>
        <view
          v-for="(item, idx) in group.items"
          :key="`${group.key}-${item.bizId || idx}`"
          class="card app-card"
          @tap="openRow(item)"
        >
          <view class="main-row">
            <view class="main">{{ rowMainText(item) }}</view>
          </view>
          <view class="sub">{{ rowSubText(item) }}</view>
          <view class="time">{{ rowTimeText(item) }}</view>
        </view>
      </view>
    </template>

    <template v-else>
      <view
        v-for="(item, idx) in items"
        :key="idx"
        class="card app-card"
        :class="{ 'card-disabled': isInvalidAnswerRow(item) }"
        @tap="openRow(item)"
      >
        <view class="main-row">
          <view class="main">{{ rowMainText(item) }}</view>
          <text v-if="isInvalidAnswerRow(item)" class="invalid-badge">因违规已删除</text>
        </view>
        <view class="sub" :class="{ 'sub-danger': isInvalidAnswerRow(item) }">{{ rowSubText(item) }}</view>
        <view class="time">{{ rowTimeText(item) }}</view>
      </view>
    </template>

    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="finished && items.length" class="loading">没有更多了</view>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 18rpx 16rpx 24rpx;
}

.head {
  color: #8ea1b2;
  margin-bottom: 14rpx;
  font-size: 26rpx;
}

.question-card {
  background: #fff;
  border: 2rpx solid #d8e8f8;
  border-radius: 20rpx;
  padding: 18rpx 18rpx 14rpx;
  margin-bottom: 14rpx;
}

.question-card-disabled {
  opacity: 0.55;
}

.question-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  flex: 0 0 12rpx;
}

.dot.solved {
  background: #35b16a;
}

.dot.waiting,
.dot.empty-answer {
  background: #f2b230;
}

.question-title {
  flex: 1;
  font-size: 38rpx;
  line-height: 1.35;
  font-weight: 700;
  color: #22364d;
}

.chevron {
  color: #97aab9;
  font-size: 34rpx;
}

.lock-flag {
  font-size: 24rpx;
  color: #7c8ea1;
  margin-right: 4rpx;
}

.tags-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 14rpx;
}

.chip {
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
}

.chip-main {
  background: #eaf6ff;
  color: #5f8eb2;
  font-weight: 600;
}

.chip-sub {
  color: #8ea1b2;
}

.meta-row {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  color: #8ea1b2;
  font-size: 26rpx;
}

.meta-item {
  white-space: nowrap;
}

.status-text {
  font-size: 28rpx;
  font-weight: 700;
  white-space: nowrap;
}

.status-text.solved {
  color: #54ae76;
}

.status-text.waiting,
.status-text.empty-answer {
  color: #e1a43f;
}

.status-text.status-offline {
  color: #a96f6f;
}

.card {
  padding: 20rpx;
  margin-bottom: 12rpx;
}

.fav-group + .fav-group {
  margin-top: 4rpx;
}

.fav-group-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4rpx 2rpx 10rpx;
}

.fav-group-title {
  font-size: 27rpx;
  color: #5f7388;
  font-weight: 700;
}

.fav-group-count {
  font-size: 22rpx;
  color: #7e93a7;
  background: #edf3f8;
  border-radius: 999rpx;
  padding: 3rpx 12rpx;
}

.card-disabled {
  opacity: 0.55;
}

.main-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.main {
  flex: 1;
  font-size: 32rpx;
  font-weight: 700;
}

.invalid-badge {
  font-size: 22rpx;
  color: #a96f6f;
  background: #f7e9e9;
  border: 1rpx solid #e8caca;
  border-radius: 999rpx;
  padding: 4rpx 12rpx;
  white-space: nowrap;
}

.sub {
  margin-top: 8rpx;
  color: #6e8599;
}

.sub-danger {
  color: #a96f6f;
}

.time {
  margin-top: 10rpx;
  color: #9ab0bf;
  font-size: 24rpx;
}

.follow-card {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 16rpx;
  border-radius: 18rpx;
  background: #fff;
  border: 2rpx solid #e3edf7;
  margin-bottom: 12rpx;
}

.follow-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #f0f3f8;
}

.follow-avatar--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6f7d8c;
  font-size: 28rpx;
  font-weight: 700;
}

.follow-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.follow-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #2a3340;
}

.follow-time {
  font-size: 24rpx;
  color: #8ea1b2;
}

.loading,
.empty {
  text-align: center;
  color: #9ab0bf;
  padding: 24rpx 0;
}
</style>

