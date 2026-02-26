"use strict";
const common_vendor = require("../../common/vendor.js");
const api_search = require("../../api/search.js");
const stores_qa = require("../../stores/qa.js");
const stores_auth = require("../../stores/auth.js");
const utils_nav = require("../../utils/nav.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const qaStore = stores_qa.useQaStore();
    const authStore = stores_auth.useAuthStore();
    const query = common_vendor.ref("");
    const loading = common_vendor.ref(false);
    const hotList = common_vendor.ref([]);
    const historyList = common_vendor.ref([]);
    const result = common_vendor.ref(null);
    const needLogin = common_vendor.computed(() => !authStore.isLogin);
    async function loadPanels() {
      try {
        const [hot, history] = await Promise.all([api_search.searchApi.hot(10), api_search.searchApi.history(10)]);
        hotList.value = hot;
        historyList.value = history;
      } catch (err) {
        common_vendor.index.showToast({ title: "搜索面板加载失败", icon: "none" });
      }
    }
    async function doSearch(forceText) {
      var _a, _b, _c;
      const text = (forceText ?? query.value).trim();
      query.value = text;
      if (!text)
        return;
      loading.value = true;
      try {
        const data = await api_search.searchApi.search({ query: text, type: "all", page: 1, pageSize: 10 });
        result.value = data;
        qaStore.upsertFromSearch(data.questions || []);
        await api_search.searchApi.logSearch({
          queryText: text,
          searchType: "all",
          hitCount: (((_a = data.questions) == null ? void 0 : _a.length) || 0) + (((_b = data.topics) == null ? void 0 : _b.length) || 0) + (((_c = data.tags) == null ? void 0 : _c.length) || 0)
        });
        await loadPanels();
      } catch (err) {
        common_vendor.index.showToast({ title: "搜索失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function clearHistory() {
      try {
        await api_search.searchApi.clearHistory();
        historyList.value = [];
      } catch (err) {
        common_vendor.index.showToast({ title: "清空失败", icon: "none" });
      }
    }
    function openQuestion(id) {
      utils_nav.openQuestionDetail(id);
    }
    common_vendor.onShow(async () => {
      if (needLogin.value)
        return;
      await loadPanels();
    });
    function goLogin() {
      common_vendor.index.navigateTo({
        url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/search/index")}`
      });
    }
    return (_ctx, _cache) => {
      var _a;
      return common_vendor.e({
        a: needLogin.value
      }, needLogin.value ? {
        b: common_vendor.o(goLogin)
      } : common_vendor.e({
        c: common_vendor.o(($event) => doSearch()),
        d: query.value,
        e: common_vendor.o(($event) => query.value = $event.detail.value),
        f: common_vendor.o(($event) => doSearch()),
        g: !result.value
      }, !result.value ? {
        h: common_vendor.o(clearHistory),
        i: common_vendor.f(historyList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.queryText),
            b: item.queryText,
            c: common_vendor.o(($event) => doSearch(item.queryText), item.queryText)
          };
        }),
        j: common_vendor.f(hotList.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.queryText),
            b: item.queryText,
            c: common_vendor.o(($event) => doSearch(item.queryText), item.queryText)
          };
        })
      } : common_vendor.e({
        k: common_vendor.t(((_a = result.value.questions) == null ? void 0 : _a.length) || 0),
        l: loading.value
      }, loading.value ? {} : !(result.value.questions && result.value.questions.length) ? {} : {
        n: common_vendor.f(result.value.questions, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(item.summary || "暂无摘要"),
            c: common_vendor.t(item.answerCount || 0),
            d: common_vendor.t(item.viewCount || 0),
            e: item.id,
            f: common_vendor.o(($event) => openQuestion(item.id), item.id)
          };
        })
      }, {
        m: !(result.value.questions && result.value.questions.length)
      })));
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-308a4d57"]]);
wx.createPage(MiniProgramPage);
