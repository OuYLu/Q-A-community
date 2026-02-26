"use strict";
const common_vendor = require("../../common/vendor.js");
const api_discover = require("../../api/discover.js");
const api_topic = require("../../api/topic.js");
const stores_auth = require("../../stores/auth.js");
const stores_qa = require("../../stores/qa.js");
const utils_nav = require("../../utils/nav.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const qaStore = stores_qa.useQaStore();
    const authStore = stores_auth.useAuthStore();
    const categories = common_vendor.ref([]);
    const topics = common_vendor.ref([]);
    const experts = common_vendor.ref([]);
    const failedTopicCoverIds = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const needLogin = common_vendor.computed(() => !authStore.isLogin);
    async function loadData() {
      loading.value = true;
      try {
        const [cateData, topicData, expertData] = await Promise.all([
          api_discover.discoverApi.getCategories(),
          api_discover.discoverApi.getHotTopics(6),
          api_discover.discoverApi.getExperts(6)
        ]);
        categories.value = cateData;
        topics.value = topicData;
        failedTopicCoverIds.value = [];
        experts.value = expertData;
        if (topicData.length > 0) {
          const firstTopicQuestions = await api_topic.topicApi.questions(topicData[0].id, {
            sortBy: "hot",
            page: 1,
            pageSize: 8
          });
          qaStore.upsertFromTopicQuestions(firstTopicQuestions.list || []);
        }
      } catch {
        common_vendor.index.showToast({ title: "发现页面加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function openTopicQuestion() {
      const q = qaStore.questions[0];
      if (!q)
        return;
      utils_nav.openQuestionDetail(q.id);
    }
    function hasTopicCover(item) {
      return !!item.coverImg && !failedTopicCoverIds.value.includes(item.id);
    }
    function onTopicCoverError(topicId) {
      if (failedTopicCoverIds.value.includes(topicId))
        return;
      failedTopicCoverIds.value = [...failedTopicCoverIds.value, topicId];
    }
    function goLogin() {
      common_vendor.index.navigateTo({
        url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/discover/index")}`
      });
    }
    common_vendor.onShow(async () => {
      if (needLogin.value)
        return;
      await loadData();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: needLogin.value
      }, needLogin.value ? {
        b: common_vendor.o(goLogin)
      } : common_vendor.e({
        c: loading.value
      }, loading.value ? {} : {
        d: common_vendor.f(categories.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: common_vendor.t(item.questionCount),
            c: item.id
          };
        }),
        e: common_vendor.f(topics.value, (item, k0, i0) => {
          return common_vendor.e({
            a: hasTopicCover(item)
          }, hasTopicCover(item) ? {
            b: item.coverImg,
            c: common_vendor.o(($event) => onTopicCoverError(item.id), item.id)
          } : {}, {
            d: common_vendor.t(item.title),
            e: common_vendor.t(item.subtitle),
            f: common_vendor.t(item.questionCount),
            g: common_vendor.t(item.followCount),
            h: common_vendor.t(item.todayNewCount || 0),
            i: item.id,
            j: common_vendor.o(openTopicQuestion, item.id)
          });
        }),
        f: common_vendor.f(experts.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.nickname),
            b: common_vendor.t(item.title || item.organization),
            c: item.userId
          };
        })
      }));
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-20534a7c"]]);
wx.createPage(MiniProgramPage);
