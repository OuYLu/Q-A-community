<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";
import {
  meApi,
  type AppMyFavoriteItemVO,
  type AppMyHistoryItemVO,
  type AppMyAnswerItemVO,
  type AppFollowUserItemVO
} from "@/api/me";
import { ensurePageAuth } from "@/utils/auth-guard";
import { questionApi, type AppMyQuestionItemVO } from "@/api/question";
import { openQuestionDetail } from "@/utils/nav";

type ListType = "favorites" | "history" | "questions" | "answers" | "following" | "followers";

type MixedItem = AppMyFavoriteItemVO | AppMyHistoryItemVO | AppMyQuestionItemVO | AppMyAnswerItemVO | AppFollowUserItemVO;

const type = ref<ListType>("favorites");
const title = ref("列表");
const loading = ref(false);
const finished = ref(false);
const pageNum = ref(1);
const pageSize = 10;
const total = ref(0);
const items = ref<MixedItem[]>([]);

const titleMap: Record<ListType, string> = {
  favorites: "我的收藏",
  history: "浏览历史",
  questions: "我的提问",
  answers: "我的回答",
  following: "关注",
  followers: "粉丝"
};

const isQuestionList = computed(() => type.value === "questions");
const questionItems = computed(() => (items.value as AppMyQuestionItemVO[]) || []);

function normalizeType(raw?: string): ListType {
  const valid: ListType[] = ["favorites", "history", "questions", "answers", "following", "followers"];
  if (raw && valid.includes(raw as ListType)) return raw as ListType;
  return "favorites";
}

function rowMainText(item: any) {
  if (type.value === "favorites") return item.title;
  if (type.value === "history") return item.title;
  if (type.value === "questions") return item.title;
  if (type.value === "answers") return `问题：${item.questionTitle || ""}`;
  return item.nickname || `用户 ${item.userId}`;
}

function rowSubText(item: any) {
  if (type.value === "favorites") return `${item.answerCount}回答 ${item.likeCount}点赞`;
  if (type.value === "history") return item.subTitle || "";
  if (type.value === "questions") return `状态:${item.status} ${item.answerCount}回答`;
  if (type.value === "answers") return item.contentPreview || "";
  return `专家状态:${item.expertStatus ?? "普通"}`;
}

function rowTimeText(item: any) {
  if (type.value === "favorites") return item.favoriteAt;
  if (type.value === "history") return item.viewedAt;
  if (type.value === "questions") return item.createdAt;
  if (type.value === "answers") return item.createdAt;
  return item.followedAt;
}

function questionStatusText(item: AppMyQuestionItemVO) {
  if (item.acceptedAnswerId) return "已采纳最佳答案";
  const count = item.answerCount || 0;
  if (count > 0) return `已有${count}条回答`;
  return "尚无回答";
}

function questionStatusClass(item: AppMyQuestionItemVO) {
  if (item.acceptedAnswerId) return "solved";
  const count = item.answerCount || 0;
  if (count > 0) return "waiting";
  return "empty-answer";
}

function questionTags(item: AppMyQuestionItemVO) {
  const tags: string[] = [];
  if (item.categoryName) tags.push(item.categoryName);
  (item.tags || []).slice(0, 3).forEach((x) => tags.push(`#${x}`));
  return tags;
}

async function fetchPage(reset = false) {
  if (loading.value || finished.value) return;
  loading.value = true;
  try {
    const page = reset ? 1 : pageNum.value;
    let resp;
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
    }

    total.value = resp.total;
    if (reset) items.value = resp.list;
    else items.value = items.value.concat(resp.list);
    pageNum.value = page + 1;
    finished.value = items.value.length >= resp.total || resp.list.length < pageSize;
  } catch (err) {
    uni.showToast({ title: "列表加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function openRow(item: any) {
  if (type.value === "questions" && item?.id) {
    openQuestionDetail(Number(item.id));
  }
}

onLoad((options) => {
  if (!ensurePageAuth()) return;
  type.value = normalizeType(options?.type);
  title.value = titleMap[type.value];
  uni.setNavigationBarTitle({ title: title.value });
  fetchPage(true);
});

onReachBottom(() => {
  fetchPage(false);
});
</script>

<template>
  <view class="page">
    <view class="head">共 {{ total }} 条</view>
    <view v-if="!items.length && !loading" class="empty">暂无数据</view>

    <template v-if="isQuestionList">
      <view
        v-for="item in questionItems"
        :key="item.id"
        class="question-card"
        @tap="openRow(item)"
      >
        <view class="question-row">
          <view class="dot" :class="questionStatusClass(item)" />
          <view class="question-title">{{ item.title }}</view>
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
            <text class="meta-item">❤ {{ item.likeCount || 0 }}</text>
            <text class="meta-item">👁 {{ item.viewCount || 0 }}</text>
            <text class="meta-item">{{ item.createdAt || "" }}</text>
          </view>
          <text :class="['status-text', questionStatusClass(item)]">{{ questionStatusText(item) }}</text>
        </view>
      </view>
    </template>

    <template v-else>
      <view v-for="(item, idx) in items" :key="idx" class="card app-card" @tap="openRow(item)">
        <view class="main">{{ rowMainText(item) }}</view>
        <view class="sub">{{ rowSubText(item) }}</view>
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

.dot.waiting {
  background: #f2b230;
}

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

.status-text.waiting {
  color: #e1a43f;
}

.status-text.empty-answer {
  color: #e1a43f;
}

.card {
  padding: 20rpx;
  margin-bottom: 12rpx;
}

.main {
  font-size: 32rpx;
  font-weight: 700;
}

.sub {
  margin-top: 8rpx;
  color: #6e8599;
}

.time {
  margin-top: 10rpx;
  color: #9ab0bf;
  font-size: 24rpx;
}

.loading,
.empty {
  text-align: center;
  color: #9ab0bf;
  padding: 24rpx 0;
}
</style>
