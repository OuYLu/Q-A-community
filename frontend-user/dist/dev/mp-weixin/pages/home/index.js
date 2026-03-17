"use strict";
const common_vendor = require("../../common/vendor.js");
const api_discover = require("../../api/discover.js");
const api_expert = require("../../api/expert.js");
const stores_auth = require("../../stores/auth.js");
const utils_nav = require("../../utils/nav.js");
const api_me = require("../../api/me.js");
const utils_constants = require("../../utils/constants.js");
const sciencePageSize = 10;
const qaPageSize = 10;
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const isExpertUser = common_vendor.ref(false);
    const activeZone = common_vendor.ref("science");
    const loading = common_vendor.ref(false);
    const errorText = common_vendor.ref("");
    const scienceCategories = common_vendor.ref([]);
    const scienceSelectedCategoryId = common_vendor.ref(void 0);
    const scienceSortBy = common_vendor.ref("hot");
    const sciencePage = common_vendor.ref(1);
    const scienceTotal = common_vendor.ref(0);
    const scienceLoading = common_vendor.ref(false);
    const scienceHasMore = common_vendor.ref(true);
    const sciencePosts = common_vendor.ref([]);
    const qaCategories = common_vendor.ref([]);
    const qaSelectedCategoryId = common_vendor.ref(void 0);
    const qaSortBy = common_vendor.ref("hot");
    const qaPage = common_vendor.ref(1);
    const qaTotal = common_vendor.ref(0);
    const qaLoading = common_vendor.ref(false);
    const qaHasMore = common_vendor.ref(true);
    const qaQuestions = common_vendor.ref([]);
    const sortTabs = [
      { key: "hot", label: "推荐" },
      { key: "latest", label: "最新" }
    ];
    const zoneTitle = common_vendor.computed(() => activeZone.value === "science" ? "推荐文章" : "推荐问答");
    const officialAvatar = `${utils_constants.BASE_URL}/api/common/avatar/staff.png`;
    async function loadHomeBase() {
      loading.value = true;
      errorText.value = "";
      try {
        const [kbCategories, home] = await Promise.all([api_discover.discoverApi.getKbCategories(4), api_discover.discoverApi.getHome()]);
        scienceCategories.value = kbCategories || [];
        qaCategories.value = home.categories || [];
      } catch {
        errorText.value = "首页加载失败，请稍后重试";
      } finally {
        loading.value = false;
      }
    }
    async function refreshExpertStatus() {
      if (!authStore.isLogin) {
        isExpertUser.value = false;
        return;
      }
      try {
        const info = await api_me.meApi.overview();
        isExpertUser.value = (info == null ? void 0 : info.expertStatus) === 3;
      } catch {
        isExpertUser.value = false;
      }
    }
    async function loadSciencePosts(reset = false) {
      if (scienceLoading.value || !reset && !scienceHasMore.value)
        return;
      if (reset) {
        sciencePage.value = 1;
        scienceTotal.value = 0;
        scienceHasMore.value = true;
      }
      scienceLoading.value = true;
      try {
        const data = await api_expert.expertApi.page({
          page: sciencePage.value,
          pageSize: sciencePageSize,
          sortBy: scienceSortBy.value,
          categoryId: scienceSelectedCategoryId.value
        });
        const rows = data.list || [];
        sciencePosts.value = reset ? rows : [...sciencePosts.value, ...rows];
        scienceTotal.value = Number(data.total || 0);
        scienceHasMore.value = sciencePosts.value.length < scienceTotal.value;
        if (rows.length > 0)
          sciencePage.value += 1;
      } catch {
        common_vendor.index.showToast({ title: "科普文章加载失败", icon: "none" });
      } finally {
        scienceLoading.value = false;
      }
    }
    async function loadQaQuestions(reset = false) {
      if (qaLoading.value || !reset && !qaHasMore.value)
        return;
      if (reset) {
        qaPage.value = 1;
        qaTotal.value = 0;
        qaHasMore.value = true;
      }
      qaLoading.value = true;
      try {
        const data = await api_discover.discoverApi.getQuestionPage({
          page: qaPage.value,
          pageSize: qaPageSize,
          sortBy: qaSortBy.value,
          categoryId: qaSelectedCategoryId.value
        });
        const rows = data.list || [];
        qaQuestions.value = reset ? rows : [...qaQuestions.value, ...rows];
        qaTotal.value = Number(data.total || 0);
        qaHasMore.value = qaQuestions.value.length < qaTotal.value;
        if (rows.length > 0)
          qaPage.value += 1;
      } catch {
        common_vendor.index.showToast({ title: "问答加载失败", icon: "none" });
      } finally {
        qaLoading.value = false;
      }
    }
    function goSearch() {
      common_vendor.index.switchTab({ url: "/pages/search/index" });
    }
    function switchZone(zone) {
      if (activeZone.value === zone)
        return;
      activeZone.value = zone;
      if (zone === "science" && sciencePosts.value.length === 0) {
        loadSciencePosts(true);
      }
      if (zone === "qa" && qaQuestions.value.length === 0) {
        loadQaQuestions(true);
      }
    }
    function switchSort(key) {
      if (activeZone.value === "science") {
        if (scienceSortBy.value === key)
          return;
        scienceSortBy.value = key;
        loadSciencePosts(true);
        return;
      }
      if (qaSortBy.value === key)
        return;
      qaSortBy.value = key;
      loadQaQuestions(true);
    }
    function selectScienceCategory(item) {
      const next = item == null ? void 0 : item.id;
      if (scienceSelectedCategoryId.value === next)
        return;
      scienceSelectedCategoryId.value = next;
      loadSciencePosts(true);
    }
    function selectQaCategory(item) {
      const next = item == null ? void 0 : item.id;
      if (qaSelectedCategoryId.value === next)
        return;
      qaSelectedCategoryId.value = next;
      loadQaQuestions(true);
    }
    function resolveAvatar(avatar) {
      if (!avatar)
        return "";
      const raw = String(avatar).trim();
      if (!raw)
        return "";
      if (raw.startsWith("http://") || raw.startsWith("https://"))
        return raw;
      return `${utils_constants.BASE_URL}${raw}`;
    }
    function isOfficialPost(item) {
      return (item == null ? void 0 : item.source) !== "expert_post";
    }
    function postAuthorName(item) {
      if (isOfficialPost(item))
        return "问问官方";
      return item.authorName || "认证专家";
    }
    function postAuthorAvatar(item) {
      if (isOfficialPost(item))
        return officialAvatar;
      return resolveAvatar(item.authorAvatar || "");
    }
    function openScienceDetail(item) {
      if (!(item == null ? void 0 : item.id))
        return;
      utils_nav.openExpertPostDetailPage(item.id);
    }
    function handleFloatingAction() {
      if (isExpertUser.value) {
        common_vendor.index.showActionSheet({
          itemList: ["发科普帖子", "发起提问"],
          success: (res) => {
            if (res.tapIndex === 0) {
              utils_nav.openExpertPostCreatePage();
              return;
            }
            if (res.tapIndex === 1) {
              utils_nav.openAskPage();
            }
          }
        });
        return;
      }
      utils_nav.openAskPage();
    }
    common_vendor.onShow(async () => {
      await Promise.all([loadHomeBase(), refreshExpertStatus()]);
      if (activeZone.value === "science") {
        await loadSciencePosts(true);
        return;
      }
      await loadQaQuestions(true);
    });
    common_vendor.onReachBottom(() => {
      if (activeZone.value === "science") {
        loadSciencePosts(false);
        return;
      }
      loadQaQuestions(false);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goSearch),
        b: activeZone.value === "science" ? 1 : "",
        c: common_vendor.o(($event) => switchZone("science")),
        d: activeZone.value === "qa" ? 1 : "",
        e: common_vendor.o(($event) => switchZone("qa")),
        f: loading.value
      }, loading.value ? {} : errorText.value ? {
        h: common_vendor.t(errorText.value)
      } : common_vendor.e({
        i: activeZone.value === "science"
      }, activeZone.value === "science" ? {
        j: scienceSelectedCategoryId.value == null ? 1 : "",
        k: common_vendor.o(($event) => selectScienceCategory(void 0)),
        l: common_vendor.f(scienceCategories.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: item.id,
            c: scienceSelectedCategoryId.value === item.id ? 1 : "",
            d: common_vendor.o(($event) => selectScienceCategory(item), item.id)
          };
        })
      } : {
        m: qaSelectedCategoryId.value == null ? 1 : "",
        n: common_vendor.o(($event) => selectQaCategory(void 0)),
        o: common_vendor.f(qaCategories.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.name),
            b: item.id,
            c: qaSelectedCategoryId.value === item.id ? 1 : "",
            d: common_vendor.o(($event) => selectQaCategory(item), item.id)
          };
        })
      }, {
        p: common_vendor.t(zoneTitle.value),
        q: common_vendor.f(sortTabs, (tab, k0, i0) => {
          return {
            a: common_vendor.t(tab.label),
            b: tab.key,
            c: (activeZone.value === "science" ? scienceSortBy.value : qaSortBy.value) === tab.key ? 1 : "",
            d: common_vendor.o(($event) => switchSort(tab.key), tab.key)
          };
        }),
        r: activeZone.value === "science"
      }, activeZone.value === "science" ? common_vendor.e({
        s: !sciencePosts.value.length && scienceLoading.value
      }, !sciencePosts.value.length && scienceLoading.value ? {} : !sciencePosts.value.length ? {} : {
        v: common_vendor.f(sciencePosts.value, (item, k0, i0) => {
          return common_vendor.e({
            a: postAuthorAvatar(item)
          }, postAuthorAvatar(item) ? {
            b: postAuthorAvatar(item)
          } : {}, {
            c: common_vendor.t(postAuthorName(item)),
            d: isOfficialPost(item)
          }, isOfficialPost(item) ? {} : {}, {
            e: common_vendor.t(item.title),
            f: common_vendor.t(item.summary || "暂无摘要"),
            g: common_vendor.t(item.likeCount || 0),
            h: common_vendor.t(item.viewCount || 0),
            i: common_vendor.t(item.createdAt || ""),
            j: item.coverImage
          }, item.coverImage ? {
            k: item.coverImage
          } : {}, {
            l: item.id,
            m: common_vendor.o(($event) => openScienceDetail(item), item.id)
          });
        }),
        w: common_vendor.t(scienceHasMore.value ? "上拉加载更多" : "没有更多了")
      }, {
        t: !sciencePosts.value.length
      }) : common_vendor.e({
        x: !qaQuestions.value.length && qaLoading.value
      }, !qaQuestions.value.length && qaLoading.value ? {} : !qaQuestions.value.length ? {} : {
        z: common_vendor.f(qaQuestions.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.authorName || "匿名用户"),
            b: common_vendor.t(item.createdAt || ""),
            c: common_vendor.t(item.title),
            d: common_vendor.t(item.answerCount || 0),
            e: common_vendor.t(item.viewCount || 0),
            f: common_vendor.t(item.likeCount || 0),
            g: item.id,
            h: common_vendor.o(($event) => common_vendor.unref(utils_nav.openQuestionDetail)(item.id), item.id)
          };
        }),
        A: common_vendor.t(qaHasMore.value ? "上拉加载更多" : "没有更多了")
      }, {
        y: !qaQuestions.value.length
      })), {
        g: errorText.value,
        B: common_vendor.o(handleFloatingAction)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2c5296db"]]);
wx.createPage(MiniProgramPage);
