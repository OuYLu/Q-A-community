"use strict";
const common_vendor = require("../../common/vendor.js");
const api_discover = require("../../api/discover.js");
const api_expert = require("../../api/expert.js");
const stores_auth = require("../../stores/auth.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const utils_nav = require("../../utils/nav.js");
const CLICK_GUARD_MS = 450;
const NAV_LOCK_MS = 700;
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const needLogin = common_vendor.computed(() => !authStore.isLogin);
    const loading = common_vendor.ref(false);
    const activeTab = common_vendor.ref("category");
    const categoryBizType = common_vendor.ref("qa");
    const qaCategories = common_vendor.ref([]);
    const kbCategories = common_vendor.ref([]);
    const topics = common_vendor.ref([]);
    const hotQuestions = common_vendor.ref([]);
    const experts = common_vendor.ref([]);
    const failedCategoryIconIds = common_vendor.ref([]);
    const failedTopicCoverIds = common_vendor.ref([]);
    const failedAvatarIds = common_vendor.ref([]);
    const showAt = common_vendor.ref(0);
    const navLock = common_vendor.ref(false);
    const tabs = [
      { key: "category", label: "分类", icon: "/static/tabbar/topic.png", activeIcon: "/static/tabbar/topic-active.png" },
      { key: "rank", label: "热榜", icon: "/static/tabbar/hot.png", activeIcon: "/static/tabbar/hot-active.png" },
      { key: "expert", label: "专家", icon: "/static/tabbar/expert.png", activeIcon: "/static/tabbar/expert-active.png" }
    ];
    const categoryCards = common_vendor.computed(() => {
      if (categoryBizType.value === "qa") {
        return qaCategories.value || [];
      }
      const rows = kbCategories.value || [];
      const hasParent = rows.some((item) => Number(item.parentId || 0) > 0);
      return hasParent ? rows.filter((item) => Number(item.parentId || 0) <= 0) : rows;
    });
    async function loadData() {
      loading.value = true;
      try {
        const [categoryData, kbCategoryData, topicData, rankData, expertData] = await Promise.all([
          api_discover.discoverApi.getAllCategories(),
          api_expert.expertApi.categories(),
          api_discover.discoverApi.getHotTopics(12),
          api_discover.discoverApi.getHotQuestions(20),
          api_discover.discoverApi.getExperts(20)
        ]);
        qaCategories.value = categoryData || [];
        kbCategories.value = kbCategoryData || [];
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
    function setCategoryBizType(type) {
      categoryBizType.value = type;
    }
    function hasCategoryIcon(item) {
      if (categoryBizType.value !== "qa")
        return false;
      const row = item;
      return !!row.icon && !failedCategoryIconIds.value.includes(Number(row.id));
    }
    function categoryIcon(item) {
      if (categoryBizType.value !== "qa")
        return "";
      return item.icon || "";
    }
    function onCategoryIconError(categoryId) {
      const id = Number(categoryId);
      if (!id)
        return;
      if (failedCategoryIconIds.value.includes(id))
        return;
      failedCategoryIconIds.value = [...failedCategoryIconIds.value, id];
    }
    function openCategory(item) {
      if (!canNavigateByTap())
        return;
      const type = categoryBizType.value;
      common_vendor.index.navigateTo({
        url: `/pages/discover/category-detail?categoryId=${item.id}&categoryName=${encodeURIComponent(item.name || "")}&categoryType=${type}`
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
      if (!canNavigateByTap())
        return;
      common_vendor.index.navigateTo({
        url: `/pages/discover/topic-detail?topicId=${item.id}&topicTitle=${encodeURIComponent(item.title || "")}`
      });
    }
    function openHotQuestion(item) {
      if (!canNavigateByTap())
        return;
      utils_nav.openQuestionDetail(item.id);
    }
    function openExpert(item) {
      if (!canNavigateByTap())
        return;
      if (!(item == null ? void 0 : item.userId))
        return;
      utils_nav.openUserHomePage(Number(item.userId));
    }
    function goLogin() {
      utils_authGuard.openLoginPage({ redirect: "/pages/discover/index", preferReplace: true });
    }
    common_vendor.onShow(async () => {
      showAt.value = Date.now();
      navLock.value = false;
      if (needLogin.value)
        return;
      await loadData();
    });
    function canNavigateByTap() {
      const now = Date.now();
      if (now - showAt.value < CLICK_GUARD_MS) {
        return false;
      }
      if (navLock.value) {
        return false;
      }
      navLock.value = true;
      setTimeout(() => {
        navLock.value = false;
      }, NAV_LOCK_MS);
      return true;
    }
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
        f: categoryBizType.value === "qa" ? 1 : "",
        g: common_vendor.o(($event) => setCategoryBizType("qa")),
        h: categoryBizType.value === "kb" ? 1 : "",
        i: common_vendor.o(($event) => setCategoryBizType("kb")),
        j: !categoryCards.value.length
      }, !categoryCards.value.length ? {} : {
        k: common_vendor.f(categoryCards.value, (item, k0, i0) => {
          return common_vendor.e({
            a: hasCategoryIcon(item)
          }, hasCategoryIcon(item) ? {
            b: categoryIcon(item),
            c: common_vendor.o(($event) => onCategoryIconError(item.id), `${categoryBizType.value}-${item.id}`)
          } : {
            d: common_vendor.t((item.name || "分").slice(0, 1))
          }, {
            e: common_vendor.t(item.name),
            f: `${categoryBizType.value}-${item.id}`,
            g: common_vendor.o(($event) => openCategory(item), `${categoryBizType.value}-${item.id}`)
          });
        })
      }, {
        l: !topics.value.length
      }, !topics.value.length ? {} : {
        m: common_vendor.f(topics.value, (item, k0, i0) => {
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
        o: !hotQuestions.value.length
      }, !hotQuestions.value.length ? {} : {
        p: common_vendor.f(hotQuestions.value, (item, idx, i0) => {
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
        q: !experts.value.length
      }, !experts.value.length ? {} : {
        r: common_vendor.f(experts.value, (item, k0, i0) => {
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
        n: activeTab.value === "rank"
      })));
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-20534a7c"]]);
wx.createPage(MiniProgramPage);
