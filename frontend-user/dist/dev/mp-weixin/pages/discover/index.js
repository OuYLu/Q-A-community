"use strict";
const common_vendor = require("../../common/vendor.js");
const api_discover = require("../../api/discover.js");
const stores_auth = require("../../stores/auth.js");
const utils_nav = require("../../utils/nav.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const needLogin = common_vendor.computed(() => !authStore.isLogin);
    const loading = common_vendor.ref(false);
    const activeTab = common_vendor.ref("category");
    const categories = common_vendor.ref([]);
    const topics = common_vendor.ref([]);
    const hotQuestions = common_vendor.ref([]);
    const experts = common_vendor.ref([]);
    const failedCategoryIconIds = common_vendor.ref([]);
    const failedTopicCoverIds = common_vendor.ref([]);
    const failedAvatarIds = common_vendor.ref([]);
    const tabs = [
      { key: "category", label: "分类", icon: "/static/tabbar/topic.png", activeIcon: "/static/tabbar/topic-active.png" },
      { key: "rank", label: "热榜", icon: "/static/tabbar/hot.png", activeIcon: "/static/tabbar/hot-active.png" },
      { key: "expert", label: "专家", icon: "/static/tabbar/expert.png", activeIcon: "/static/tabbar/expert-active.png" }
    ];
    async function loadData() {
      loading.value = true;
      try {
        const [categoryData, topicData, rankData, expertData] = await Promise.all([
          api_discover.discoverApi.getCategories(),
          api_discover.discoverApi.getHotTopics(12),
          api_discover.discoverApi.getHotQuestions(20),
          api_discover.discoverApi.getExperts(20)
        ]);
        categories.value = categoryData || [];
        topics.value = topicData || [];
        hotQuestions.value = rankData || [];
        experts.value = expertData || [];
        failedCategoryIconIds.value = [];
        failedTopicCoverIds.value = [];
        failedAvatarIds.value = [];
      } catch {
        common_vendor.index.showToast({ title: "发现页加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function setTab(tab) {
      activeTab.value = tab;
    }
    function hasCategoryIcon(item) {
      return !!item.icon && !failedCategoryIconIds.value.includes(item.id);
    }
    function onCategoryIconError(categoryId) {
      if (failedCategoryIconIds.value.includes(categoryId))
        return;
      failedCategoryIconIds.value = [...failedCategoryIconIds.value, categoryId];
    }
    function openCategory(item) {
      common_vendor.index.navigateTo({
        url: `/pages/discover/category-detail?categoryId=${item.id}&categoryName=${encodeURIComponent(item.name || "")}`
      });
    }
    function hasTopicCover(item) {
      return !!item.coverImg && !failedTopicCoverIds.value.includes(item.id);
    }
    function onTopicCoverError(topicId) {
      if (failedTopicCoverIds.value.includes(topicId))
        return;
      failedTopicCoverIds.value = [...failedTopicCoverIds.value, topicId];
    }
    function avatarAvailable(userId, avatar) {
      return !!avatar && !failedAvatarIds.value.includes(userId);
    }
    function onAvatarError(userId) {
      if (failedAvatarIds.value.includes(userId))
        return;
      failedAvatarIds.value = [...failedAvatarIds.value, userId];
    }
    function openTopic(item) {
      common_vendor.index.navigateTo({
        url: `/pages/discover/topic-detail?topicId=${item.id}&topicTitle=${encodeURIComponent(item.title || "")}`
      });
    }
    function openHotQuestion(item) {
      utils_nav.openQuestionDetail(item.id);
    }
    function openExpert(item) {
      if (!(item == null ? void 0 : item.userId))
        return;
      utils_nav.openUserHomePage(Number(item.userId));
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
        c: common_vendor.f(tabs, (tab, k0, i0) => {
          return {
            a: activeTab.value === tab.key ? tab.activeIcon : tab.icon,
            b: common_vendor.t(tab.label),
            c: tab.key,
            d: activeTab.value === tab.key ? 1 : "",
            e: common_vendor.o(($event) => setTab(tab.key), tab.key)
          };
        }),
        d: loading.value
      }, loading.value ? {} : common_vendor.e({
        e: activeTab.value === "category"
      }, activeTab.value === "category" ? common_vendor.e({
        f: !categories.value.length
      }, !categories.value.length ? {} : {
        g: common_vendor.f(categories.value, (item, k0, i0) => {
          return common_vendor.e({
            a: hasCategoryIcon(item)
          }, hasCategoryIcon(item) ? {
            b: item.icon,
            c: common_vendor.o(($event) => onCategoryIconError(item.id), item.id)
          } : {}, {
            d: common_vendor.t(item.name),
            e: item.id,
            f: common_vendor.o(($event) => openCategory(item), item.id)
          });
        })
      }, {
        h: !topics.value.length
      }, !topics.value.length ? {} : {
        i: common_vendor.f(topics.value, (item, k0, i0) => {
          return common_vendor.e({
            a: hasTopicCover(item)
          }, hasTopicCover(item) ? {
            b: item.coverImg,
            c: common_vendor.o(($event) => onTopicCoverError(item.id), item.id)
          } : {}, {
            d: common_vendor.t(item.title),
            e: common_vendor.t(item.subtitle || "点击查看该专题热门问题"),
            f: common_vendor.t(item.questionCount),
            g: common_vendor.t(item.followCount),
            h: item.id,
            i: common_vendor.o(($event) => openTopic(item), item.id)
          });
        })
      }) : activeTab.value === "rank" ? common_vendor.e({
        k: !hotQuestions.value.length
      }, !hotQuestions.value.length ? {} : {
        l: common_vendor.f(hotQuestions.value, (item, idx, i0) => {
          return {
            a: common_vendor.t(idx + 1),
            b: idx < 3 ? 1 : "",
            c: common_vendor.t(item.title),
            d: common_vendor.t(item.authorName),
            e: common_vendor.t(item.answerCount),
            f: common_vendor.t(item.viewCount),
            g: common_vendor.t(item.likeCount),
            h: item.id,
            i: common_vendor.o(($event) => openHotQuestion(item), item.id)
          };
        })
      }) : common_vendor.e({
        m: !experts.value.length
      }, !experts.value.length ? {} : {
        n: common_vendor.f(experts.value, (item, k0, i0) => {
          return common_vendor.e({
            a: avatarAvailable(item.userId, item.avatar)
          }, avatarAvailable(item.userId, item.avatar) ? {
            b: item.avatar,
            c: common_vendor.o(($event) => onAvatarError(item.userId), item.userId)
          } : {
            d: common_vendor.t((item.nickname || "专").slice(0, 1))
          }, {
            e: common_vendor.t(item.nickname),
            f: common_vendor.t(item.title || item.organization || "认证专家"),
            g: common_vendor.t(item.expertise || "擅长健康问答与科普"),
            h: item.userId,
            i: common_vendor.o(($event) => openExpert(item), item.userId)
          });
        })
      }), {
        j: activeTab.value === "rank"
      })));
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-20534a7c"]]);
wx.createPage(MiniProgramPage);
