<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { searchApi, type AppSearchHistoryVO, type AppSearchHotVO, type AppSearchResultVO } from "@/api/search";
import { useQaStore } from "@/stores/qa";
import { useAuthStore } from "@/stores/auth";
import { openQuestionDetail } from "@/utils/nav";

const qaStore = useQaStore();
const authStore = useAuthStore();
const query = ref("");
const loading = ref(false);
const hotList = ref<AppSearchHotVO[]>([]);
const historyList = ref<AppSearchHistoryVO[]>([]);
const result = ref<AppSearchResultVO | null>(null);
const needLogin = computed(() => !authStore.isLogin);

async function loadPanels() {
  try {
    const [hot, history] = await Promise.all([searchApi.hot(10), searchApi.history(10)]);
    hotList.value = hot;
    historyList.value = history;
  } catch (err) {
    uni.showToast({ title: "搜索面板加载失败", icon: "none" });
  }
}

async function doSearch(forceText?: string) {
  const text = (forceText ?? query.value).trim();
  query.value = text;
  if (!text) return;
  loading.value = true;
  try {
    const data = await searchApi.search({ query: text, type: "all", page: 1, pageSize: 10 });
    result.value = data;
    qaStore.upsertFromSearch(data.questions || []);
    await searchApi.logSearch({
      queryText: text,
      searchType: "all",
      hitCount: (data.questions?.length || 0) + (data.topics?.length || 0) + (data.tags?.length || 0)
    });
    await loadPanels();
  } catch (err) {
    uni.showToast({ title: "搜索失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function clearHistory() {
  try {
    await searchApi.clearHistory();
    historyList.value = [];
  } catch (err) {
    uni.showToast({ title: "清空失败", icon: "none" });
  }
}

function openQuestion(id: number) {
  openQuestionDetail(id);
}

onShow(async () => {
  if (needLogin.value) return;
  await loadPanels();
});

function goLogin() {
  uni.navigateTo({
    url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/search/index")}`
  });
}
</script>

<template>
  <view class="page">
    <view v-if="needLogin" class="auth-card app-card">
      <view class="auth-title">登录后可查看搜索内容</view>
      <view class="auth-sub">历史记录、热门搜索和搜索结果仅登录后可用</view>
      <button class="auth-btn" @click="goLogin">去登录</button>
    </view>

    <template v-else>
    <view class="search-line">
      <input
        v-model="query"
        class="search-input"
        type="text"
        placeholder="输入症状或健康问题..."
        confirm-type="search"
        @confirm="doSearch()"
      />
      <button class="search-btn" @click="doSearch()">搜索</button>
    </view>

    <view v-if="!result" class="panel">
      <view class="head">
        <text class="title">搜索历史</text>
        <text class="action" @click="clearHistory">清空</text>
      </view>
      <view class="chips">
        <text v-for="item in historyList" :key="item.queryText" class="app-chip" @click="doSearch(item.queryText)">
          {{ item.queryText }}
        </text>
      </view>

      <view class="head second">
        <text class="title">热门搜索</text>
      </view>
      <view class="chips">
        <text v-for="item in hotList" :key="item.queryText" class="app-chip hot" @click="doSearch(item.queryText)">
          {{ item.queryText }}
        </text>
      </view>
    </view>

    <view v-else class="panel">
      <view class="head">
        <text class="title">问题结果 {{ result.questions?.length || 0 }}</text>
      </view>
      <view v-if="loading" class="state">搜索中...</view>
      <view v-else-if="!(result.questions && result.questions.length)" class="state">没有匹配问题</view>
      <view v-else>
        <view
          v-for="item in result.questions"
          :key="item.id"
          class="app-card result-item"
          @click="openQuestion(item.id)"
        >
          <view class="result-title">{{ item.title }}</view>
          <view class="result-desc">{{ item.summary || "暂无摘要" }}</view>
          <view class="meta">
            <text>{{ item.answerCount || 0 }}回答</text>
            <text>{{ item.viewCount || 0 }}浏览</text>
          </view>
        </view>
      </view>
    </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.page {
  padding: 24rpx;
  min-height: 100vh;
  box-sizing: border-box;
}

.auth-card {
  padding: 30rpx 24rpx;
  margin-top: 0;
}

.auth-title {
  font-size: 34rpx;
  font-weight: 700;
}

.auth-sub {
  margin-top: 10rpx;
  color: #8ba0b3;
}

.auth-btn {
  margin-top: 18rpx;
  border: none;
  border-radius: 16rpx;
  background: #4ba7d9;
  color: #fff;
}

.page > .auth-card {
  position: relative;
  top: 40vh;
  transform: translateY(-50%);
}

.search-line {
  display: flex;
  gap: 14rpx;
}

.search-input {
  flex: 1;
  height: 76rpx;
  background: #fff;
  border: 2rpx solid #9bd1ef;
  border-radius: 20rpx;
  padding: 0 24rpx;
}

.search-btn {
  width: 140rpx;
  margin: 0;
  border: none;
  background: #4ba7d9;
  color: #fff;
  border-radius: 20rpx;
  font-size: 28rpx;
}

.panel {
  margin-top: 24rpx;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.second {
  margin-top: 24rpx;
}

.title {
  font-size: 34rpx;
  font-weight: 700;
}

.action {
  color: #8ba0b3;
}

.chips {
  margin-top: 14rpx;
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.hot {
  background: #f3ecd1;
}

.result-item {
  padding: 22rpx;
  margin-top: 14rpx;
}

.result-title {
  font-size: 30rpx;
  font-weight: 700;
}

.result-desc {
  color: #5f7488;
  margin-top: 10rpx;
}

.meta {
  margin-top: 12rpx;
  display: flex;
  gap: 18rpx;
  color: #8ba0b3;
}

.state {
  color: #8ba0b3;
  text-align: center;
  padding: 40rpx 0;
}
</style>
