"use strict";
const common_vendor = require("../../common/vendor.js");
const api_search = require("../../api/search.js");
const stores_auth = require("../../stores/auth.js");
const utils_nav = require("../../utils/nav.js");
const utils_constants = require("../../utils/constants.js");
const pageSize = 10;
const LOCAL_HISTORY_KEY_PREFIX = "search_history_cache_";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const needLogin = common_vendor.computed(() => !authStore.isLogin);
    const query = common_vendor.ref("");
    const loading = common_vendor.ref(false);
    const searching = common_vendor.ref(false);
    const hotList = common_vendor.ref([]);
    const historyList = common_vendor.ref([]);
    const questionList = common_vendor.ref([]);
    const answerList = common_vendor.ref([]);
    const kbList = common_vendor.ref([]);
    const topicList = common_vendor.ref([]);
    const tagList = common_vendor.ref([]);
    const page = common_vendor.ref(1);
    const hasMore = common_vendor.ref(false);
    const inResult = common_vendor.ref(false);
    const sortBy = common_vendor.ref("comprehensive");
    const officialAvatar = `${utils_constants.BASE_URL}/api/common/avatar/staff.png`;
    const sortTabs = [
      { key: "comprehensive", label: "综合" },
      { key: "latest", label: "最新" },
      { key: "hot", label: "最热" }
    ];
    const hasResult = common_vendor.computed(
      () => kbList.value.length > 0 || questionList.value.length > 0 || answerList.value.length > 0 || topicList.value.length > 0 || tagList.value.length > 0
    );
    function localHistoryKey() {
      var _a;
      return `${LOCAL_HISTORY_KEY_PREFIX}${((_a = authStore.user) == null ? void 0 : _a.userId) || "guest"}`;
    }
    function getLocalHistory() {
      try {
        const raw = common_vendor.index.getStorageSync(localHistoryKey());
        if (!raw)
          return [];
        const list = JSON.parse(raw);
        if (!Array.isArray(list))
          return [];
        return list.filter((item) => item && typeof item.queryText === "string" && item.queryText.trim()).slice(0, 20);
      } catch {
        return [];
      }
    }
    function saveLocalHistory(list) {
      try {
        common_vendor.index.setStorageSync(localHistoryKey(), JSON.stringify((list || []).slice(0, 20)));
      } catch {
      }
    }
    function appendLocalHistory(queryText) {
      const text = (queryText || "").trim();
      if (!text)
        return;
      const now = (/* @__PURE__ */ new Date()).toISOString();
      const current = getLocalHistory().filter((item) => item.queryText !== text);
      current.unshift({ queryText: text, lastTime: now });
      saveLocalHistory(current);
    }
    function mergeHistory(remote, local) {
      const map = /* @__PURE__ */ new Map();
      [...remote, ...local].forEach((item) => {
        if (!(item == null ? void 0 : item.queryText))
          return;
        const key = item.queryText.trim();
        if (!key)
          return;
        const existing = map.get(key);
        if (!existing) {
          map.set(key, { queryText: key, lastTime: item.lastTime || "" });
          return;
        }
        const oldTime = new Date(existing.lastTime || 0).getTime();
        const newTime = new Date(item.lastTime || 0).getTime();
        if (newTime > oldTime) {
          map.set(key, { queryText: key, lastTime: item.lastTime || "" });
        }
      });
      return Array.from(map.values()).sort((a, b) => new Date(b.lastTime || 0).getTime() - new Date(a.lastTime || 0).getTime()).slice(0, 10);
    }
    async function loadPanels() {
      loading.value = true;
      try {
        const [hot, history] = await Promise.all([api_search.searchApi.hot(10), api_search.searchApi.history(10)]);
        hotList.value = hot || [];
        historyList.value = mergeHistory(history || [], getLocalHistory());
      } catch {
        historyList.value = getLocalHistory();
        common_vendor.index.showToast({ title: "搜索面板加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function search(reset = true) {
      const text = query.value.trim();
      query.value = text;
      if (!text || searching.value)
        return;
      if (reset) {
        page.value = 1;
        questionList.value = [];
        answerList.value = [];
        kbList.value = [];
        topicList.value = [];
        tagList.value = [];
        hasMore.value = false;
        inResult.value = true;
      }
      searching.value = true;
      try {
        const data = await api_search.searchApi.search({
          query: text,
          type: "all",
          sortBy: sortBy.value,
          page: page.value,
          pageSize
        });
        const questionRows = data.questions || [];
        const answerRows = data.answers || [];
        const kbRows = data.kbEntries || [];
        const topicRows = data.topics || [];
        const tagRows = data.tags || [];
        questionList.value = reset ? questionRows : [...questionList.value, ...questionRows];
        answerList.value = reset ? answerRows : [...answerList.value, ...answerRows];
        kbList.value = reset ? kbRows : [...kbList.value, ...kbRows];
        topicList.value = reset ? topicRows : topicList.value;
        tagList.value = reset ? tagRows : tagList.value;
        hasMore.value = questionRows.length >= pageSize || kbRows.length >= pageSize || answerRows.length >= pageSize;
        if (questionRows.length > 0 || kbRows.length > 0 || answerRows.length > 0) {
          page.value += 1;
        }
        if (reset) {
          appendLocalHistory(text);
          historyList.value = mergeHistory(historyList.value, getLocalHistory());
          try {
            await api_search.searchApi.logSearch({
              queryText: text,
              searchType: 1,
              hitCount: questionRows.length + kbRows.length + answerRows.length + topicRows.length + tagRows.length
            });
            await loadPanels();
          } catch {
          }
        }
      } catch {
        common_vendor.index.showToast({ title: "搜索失败", icon: "none" });
      } finally {
        searching.value = false;
      }
    }
    async function clearHistory() {
      try {
        await api_search.searchApi.clearHistory();
      } catch {
      }
      historyList.value = [];
      saveLocalHistory([]);
    }
    function chooseWord(word) {
      query.value = word;
      search(true);
    }
    function switchSort(key) {
      if (sortBy.value === key)
        return;
      sortBy.value = key;
      if (inResult.value && query.value.trim()) {
        search(true);
      }
    }
    function resetPanel() {
      inResult.value = false;
      questionList.value = [];
      answerList.value = [];
      kbList.value = [];
      topicList.value = [];
      tagList.value = [];
      page.value = 1;
      hasMore.value = false;
    }
    function isOfficialKb(item) {
      return (item == null ? void 0 : item.source) !== "expert_post";
    }
    function openKb(item) {
      if (!(item == null ? void 0 : item.id))
        return;
      utils_nav.openExpertPostDetailPage(item.id);
    }
    function openTopic(topicId) {
      if (!topicId)
        return;
      common_vendor.index.navigateTo({
        url: `/pages/discover/topic-detail?topicId=${topicId}`
      });
    }
    function chooseTag(tagName) {
      if (!tagName)
        return;
      query.value = tagName;
      search(true);
    }
    function openAnswerQuestion(questionId) {
      if (!questionId)
        return;
      utils_nav.openQuestionDetail(questionId);
    }
    function goLogin() {
      common_vendor.index.navigateTo({
        url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/search/index")}`
      });
    }
    common_vendor.onShow(async () => {
      if (needLogin.value)
        return;
      if (inResult.value && query.value.trim()) {
        await search(true);
      } else {
        await loadPanels();
      }
    });
    common_vendor.onReachBottom(() => {
      if (!needLogin.value && inResult.value && hasMore.value) {
        search(false);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: needLogin.value
      }, needLogin.value ? {
        b: common_vendor.o(goLogin)
      } : common_vendor.e({
        c: common_vendor.o(($event) => search(true)),
        d: query.value,
        e: common_vendor.o(($event) => query.value = $event.detail.value),
        f: common_vendor.o(($event) => search(true)),
        g: inResult.value
      }, inResult.value ? common_vendor.e({
        h: common_vendor.f(sortTabs, (tab, k0, i0) => {
          return {
            a: common_vendor.t(tab.label),
            b: tab.key,
            c: sortBy.value === tab.key ? 1 : "",
            d: common_vendor.o(($event) => switchSort(tab.key), tab.key)
          };
        }),
        i: common_vendor.o(resetPanel),
        j: searching.value && !hasResult.value
      }, searching.value && !hasResult.value ? {} : !hasResult.value ? {} : common_vendor.e({
        l: !kbList.value.length
      }, !kbList.value.length ? {} : {
        m: common_vendor.f(kbList.value, (item, k0, i0) => {
          return common_vendor.e({
            a: isOfficialKb(item)
          }, isOfficialKb(item) ? {
            b: officialAvatar
          } : {}, {
            c: common_vendor.t(item.title),
            d: common_vendor.t(item.summary || "暂无摘要"),
            e: common_vendor.t(item.viewCount || 0),
            f: common_vendor.t(item.likeCount || 0),
            g: item.id,
            h: common_vendor.o(($event) => openKb(item), item.id)
          });
        })
      }, {
        n: !questionList.value.length
      }, !questionList.value.length ? {} : {
        o: common_vendor.f(questionList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(item.summary || "暂无摘要"),
            c: common_vendor.t(item.answerCount || 0),
            d: common_vendor.t(item.viewCount || 0),
            e: common_vendor.t(item.likeCount || 0),
            f: item.id,
            g: common_vendor.o(($event) => common_vendor.unref(utils_nav.openQuestionDetail)(item.id), item.id)
          };
        })
      }, {
        p: !answerList.value.length
      }, !answerList.value.length ? {} : {
        q: common_vendor.f(answerList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.questionTitle),
            b: common_vendor.o(($event) => openAnswerQuestion(item.questionId), item.answerId),
            c: common_vendor.t(item.contentPreview || "暂无回答内容"),
            d: common_vendor.t(item.likeCount || 0),
            e: item.answerId,
            f: common_vendor.o(($event) => common_vendor.unref(utils_nav.openAnswerDetailPage)(item.answerId), item.answerId)
          };
        })
      }, {
        r: !topicList.value.length
      }, !topicList.value.length ? {} : {
        s: common_vendor.f(topicList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(item.subtitle || "暂无简介"),
            c: common_vendor.t(item.followCount || 0),
            d: common_vendor.t(item.questionCount || 0),
            e: item.id,
            f: common_vendor.o(($event) => openTopic(item.id), item.id)
          };
        })
      }, {
        t: !tagList.value.length
      }, !tagList.value.length ? {} : {
        v: common_vendor.f(tagList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: common_vendor.t(item.useCount || 0),
            c: item.id,
            d: common_vendor.o(($event) => chooseTag(item.name), item.id)
          };
        })
      }, {
        w: common_vendor.t(hasMore.value ? "上拉加载更多" : "没有更多了")
      }), {
        k: !hasResult.value
      }) : {
        x: common_vendor.o(clearHistory),
        y: common_vendor.f(historyList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.queryText),
            b: item.queryText,
            c: common_vendor.o(($event) => chooseWord(item.queryText), item.queryText)
          };
        }),
        z: common_vendor.f(hotList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.queryText),
            b: item.queryText,
            c: common_vendor.o(($event) => chooseWord(item.queryText), item.queryText)
          };
        })
      }));
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-308a4d57"]]);
wx.createPage(MiniProgramPage);
