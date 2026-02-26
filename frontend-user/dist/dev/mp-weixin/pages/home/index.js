"use strict";
const common_vendor = require("../../common/vendor.js");
const api_discover = require("../../api/discover.js");
const stores_qa = require("../../stores/qa.js");
const stores_auth = require("../../stores/auth.js");
const utils_nav = require("../../utils/nav.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const qaStore = stores_qa.useQaStore();
    const authStore = stores_auth.useAuthStore();
    const loading = common_vendor.ref(false);
    const errorText = common_vendor.ref("");
    const categories = common_vendor.ref([]);
    const hotTopics = common_vendor.ref([]);
    const hotQuestions = common_vendor.ref([]);
    const experts = common_vendor.ref([]);
    const questionCount = common_vendor.computed(() => hotQuestions.value.length);
    async function fetchData() {
      loading.value = true;
      errorText.value = "";
      try {
        const home = await api_discover.discoverApi.getHome();
        categories.value = home.categories || [];
        hotTopics.value = home.hotTopics || [];
        hotQuestions.value = home.hotQuestions || [];
        experts.value = home.experts || [];
        qaStore.upsertFromHotRank(hotQuestions.value);
      } catch {
        errorText.value = "首页加载失败，请检查后端服务地址";
      } finally {
        loading.value = false;
      }
    }
    function goSearch() {
      common_vendor.index.switchTab({
        url: "/pages/search/index"
      });
    }
    function openCategory(item) {
      if (authStore.isLogin) {
        common_vendor.index.switchTab({ url: "/pages/discover/index" });
        return;
      }
      common_vendor.index.showModal({
        title: "提示",
        content: `登录后才能查看“${item.name}”分类内容`,
        confirmText: "去登录",
        cancelText: "取消",
        success: (res) => {
          if (!res.confirm)
            return;
          const redirect = encodeURIComponent("/pages/discover/index");
          common_vendor.index.navigateTo({ url: `/pages/auth/login?redirect=${redirect}` });
        }
      });
    }
    common_vendor.onShow(fetchData);
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goSearch),
        b: loading.value
      }, loading.value ? {} : errorText.value ? {
        d: common_vendor.t(errorText.value)
      } : common_vendor.e({
        e: common_vendor.f(categories.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: item.id,
            c: common_vendor.o(($event) => openCategory(item), item.id)
          };
        }),
        f: common_vendor.t(questionCount.value),
        g: !hotQuestions.value.length
      }, !hotQuestions.value.length ? {} : {
        h: common_vendor.f(hotQuestions.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.authorName),
            b: common_vendor.t(item.createdAt),
            c: common_vendor.t(item.title),
            d: common_vendor.t(item.answerCount),
            e: common_vendor.t(item.viewCount),
            f: common_vendor.t(item.likeCount),
            g: item.id,
            h: common_vendor.o(($event) => common_vendor.unref(utils_nav.openQuestionDetail)(item.id), item.id)
          };
        })
      }, {
        i: common_vendor.f(hotTopics.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(item.subtitle),
            c: common_vendor.t(item.questionCount),
            d: common_vendor.t(item.followCount),
            e: item.id
          };
        }),
        j: common_vendor.f(experts.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.nickname),
            b: common_vendor.t(item.title || item.organization),
            c: common_vendor.t(item.expertise),
            d: item.userId
          };
        })
      }), {
        c: errorText.value,
        k: common_vendor.o(
          //@ts-ignore
          (...args) => common_vendor.unref(utils_nav.openAskPage) && common_vendor.unref(utils_nav.openAskPage)(...args)
        )
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2c5296db"]]);
wx.createPage(MiniProgramPage);
