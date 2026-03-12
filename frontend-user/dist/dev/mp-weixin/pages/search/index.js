"use strict";
const common_vendor = require("../../common/vendor.js");
const api_search = require("../../api/search.js");
const stores_auth = require("../../stores/auth.js");
const utils_nav = require("../../utils/nav.js");
const utils_constants = require("../../utils/constants.js");
const KB_INIT_LIMIT = 5;
const QUESTION_INIT_LIMIT = 5;
const ANSWER_INIT_LIMIT = 5;
const LOAD_MORE_STEP = 10;
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
    const similarQuestionList = common_vendor.ref([]);
    const kbLimit = common_vendor.ref(KB_INIT_LIMIT);
    const questionLimit = common_vendor.ref(QUESTION_INIT_LIMIT);
    const answerLimit = common_vendor.ref(ANSWER_INIT_LIMIT);
    const kbHasMore = common_vendor.ref(false);
    const questionHasMore = common_vendor.ref(false);
    const answerHasMore = common_vendor.ref(false);
    const kbLoading = common_vendor.ref(false);
    const questionLoading = common_vendor.ref(false);
    const answerLoading = common_vendor.ref(false);
    const inResult = common_vendor.ref(false);
    const sortBy = common_vendor.ref("comprehensive");
    const officialAvatar = `${utils_constants.BASE_URL}/api/common/avatar/staff.png`;
    const sortTabs = [
      { key: "comprehensive", label: "综合" },
      { key: "latest", label: "最新" },
      { key: "hot", label: "最热" }
    ];
    const hasResult = common_vendor.computed(
      () => kbList.value.length > 0 || questionList.value.length > 0 || answerList.value.length > 0
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
    async function fetchKbRows() {
      const text = query.value.trim();
      if (!text) {
        kbList.value = [];
        kbHasMore.value = false;
        return 0;
      }
      kbLoading.value = true;
      try {
        const data = await api_search.searchApi.search({
          query: text,
          type: "kb",
          sortBy: sortBy.value,
          page: 1,
          pageSize: kbLimit.value + 1
        });
        const rows = data.kbEntries || [];
        kbList.value = rows.slice(0, kbLimit.value);
        kbHasMore.value = rows.length > kbLimit.value;
        return kbList.value.length;
      } finally {
        kbLoading.value = false;
      }
    }
    async function fetchQuestionRows() {
      const text = query.value.trim();
      if (!text) {
        questionList.value = [];
        questionHasMore.value = false;
        similarQuestionList.value = [];
        return 0;
      }
      questionLoading.value = true;
      try {
        const data = await api_search.searchApi.search({
          query: text,
          type: "question",
          sortBy: sortBy.value,
          page: 1,
          pageSize: questionLimit.value + 1
        });
        const rows = data.questions || [];
        questionList.value = rows.slice(0, questionLimit.value);
        questionHasMore.value = rows.length > questionLimit.value;
        similarQuestionList.value = data.similarQuestions || [];
        return questionList.value.length;
      } finally {
        questionLoading.value = false;
      }
    }
    async function fetchAnswerRows() {
      const text = query.value.trim();
      if (!text) {
        answerList.value = [];
        answerHasMore.value = false;
        return 0;
      }
      answerLoading.value = true;
      try {
        const data = await api_search.searchApi.search({
          query: text,
          type: "answer",
          sortBy: sortBy.value,
          page: 1,
          pageSize: answerLimit.value + 1
        });
        const rows = data.answers || [];
        answerList.value = rows.slice(0, answerLimit.value);
        answerHasMore.value = rows.length > answerLimit.value;
        return answerList.value.length;
      } finally {
        answerLoading.value = false;
      }
    }
    async function search(reset = true) {
      const text = query.value.trim();
      query.value = text;
      if (!text || searching.value)
        return;
      if (reset) {
        kbLimit.value = KB_INIT_LIMIT;
        questionLimit.value = QUESTION_INIT_LIMIT;
        answerLimit.value = ANSWER_INIT_LIMIT;
        questionList.value = [];
        answerList.value = [];
        kbList.value = [];
        similarQuestionList.value = [];
        kbHasMore.value = false;
        questionHasMore.value = false;
        answerHasMore.value = false;
        inResult.value = true;
      }
      searching.value = true;
      try {
        const [questionHit, answerHit, kbHit] = await Promise.all([
          fetchQuestionRows(),
          fetchAnswerRows(),
          fetchKbRows()
        ]);
        if (reset) {
          appendLocalHistory(text);
          historyList.value = mergeHistory(historyList.value, getLocalHistory());
          try {
            await api_search.searchApi.logSearch({
              queryText: text,
              searchType: 1,
              hitCount: questionHit + answerHit + kbHit
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
      similarQuestionList.value = [];
      kbLimit.value = KB_INIT_LIMIT;
      questionLimit.value = QUESTION_INIT_LIMIT;
      answerLimit.value = ANSWER_INIT_LIMIT;
      kbHasMore.value = false;
      questionHasMore.value = false;
      answerHasMore.value = false;
    }
    function isOfficialKb(item) {
      return (item == null ? void 0 : item.source) !== "expert_post";
    }
    function openKb(item) {
      if (!(item == null ? void 0 : item.id))
        return;
      utils_nav.openExpertPostDetailPage(item.id);
    }
    function openAnswerQuestion(questionId) {
      if (!questionId)
        return;
      utils_nav.openQuestionDetail(questionId);
    }
    async function loadMoreKb() {
      if (!kbHasMore.value || kbLoading.value)
        return;
      kbLimit.value += LOAD_MORE_STEP;
      try {
        await fetchKbRows();
      } catch {
        common_vendor.index.showToast({ title: "加载更多失败", icon: "none" });
      }
    }
    async function loadMoreQuestions() {
      if (!questionHasMore.value || questionLoading.value)
        return;
      questionLimit.value += LOAD_MORE_STEP;
      try {
        await fetchQuestionRows();
      } catch {
        common_vendor.index.showToast({ title: "加载更多失败", icon: "none" });
      }
    }
    async function loadMoreAnswers() {
      if (!answerHasMore.value || answerLoading.value)
        return;
      answerLimit.value += LOAD_MORE_STEP;
      try {
        await fetchAnswerRows();
      } catch {
        common_vendor.index.showToast({ title: "加载更多失败", icon: "none" });
      }
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
        l: kbList.value.length
      }, kbList.value.length ? common_vendor.e({
        m: common_vendor.f(kbList.value, (item, k0, i0) => {
          return common_vendor.e({
            a: isOfficialKb(item)
          }, isOfficialKb(item) ? {
            b: officialAvatar
          } : {}, {
            c: item.titleHighlight || item.title,
            d: item.summaryHighlight || item.summary || "No summary",
            e: common_vendor.t(item.viewCount || 0),
            f: common_vendor.t(item.likeCount || 0),
            g: item.id,
            h: common_vendor.o(($event) => openKb(item), item.id)
          });
        }),
        n: kbHasMore.value
      }, kbHasMore.value ? {
        o: common_vendor.t(kbLoading.value ? "加载中..." : "展示更多"),
        p: common_vendor.o(loadMoreKb)
      } : {}) : {}, {
        q: questionList.value.length
      }, questionList.value.length ? common_vendor.e({
        r: common_vendor.f(questionList.value, (item, k0, i0) => {
          return {
            a: item.titleHighlight || item.title,
            b: item.summaryHighlight || item.summary || "No summary",
            c: common_vendor.t(item.answerCount || 0),
            d: common_vendor.t(item.viewCount || 0),
            e: common_vendor.t(item.likeCount || 0),
            f: item.id,
            g: common_vendor.o(($event) => common_vendor.unref(utils_nav.openQuestionDetail)(item.id), item.id)
          };
        }),
        s: questionHasMore.value
      }, questionHasMore.value ? {
        t: common_vendor.t(questionLoading.value ? "加载中..." : "展示更多"),
        v: common_vendor.o(loadMoreQuestions)
      } : {}) : {}, {
        w: similarQuestionList.value.length
      }, similarQuestionList.value.length ? {
        x: common_vendor.f(similarQuestionList.value, (item, k0, i0) => {
          return {
            a: item.titleHighlight || item.title,
            b: item.id,
            c: common_vendor.o(($event) => common_vendor.unref(utils_nav.openQuestionDetail)(item.id), item.id)
          };
        })
      } : {}, {
        y: answerList.value.length
      }, answerList.value.length ? common_vendor.e({
        z: common_vendor.f(answerList.value, (item, k0, i0) => {
          return {
            a: item.questionTitleHighlight || item.questionTitle,
            b: common_vendor.o(($event) => openAnswerQuestion(item.questionId), item.answerId),
            c: item.contentPreviewHighlight || item.contentPreview || "No answer content",
            d: common_vendor.t(item.likeCount || 0),
            e: item.answerId,
            f: common_vendor.o(($event) => common_vendor.unref(utils_nav.openAnswerDetailPage)(item.answerId), item.answerId)
          };
        }),
        A: answerHasMore.value
      }, answerHasMore.value ? {
        B: common_vendor.t(answerLoading.value ? "加载中..." : "展示更多"),
        C: common_vendor.o(loadMoreAnswers)
      } : {}) : {}), {
        k: !hasResult.value
      }) : {
        D: common_vendor.o(clearHistory),
        E: common_vendor.f(historyList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.queryText),
            b: item.queryText,
            c: common_vendor.o(($event) => chooseWord(item.queryText), item.queryText)
          };
        }),
        F: common_vendor.f(hotList.value, (item, k0, i0) => {
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
