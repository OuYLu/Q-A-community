"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const api_user = require("../../api/user.js");
const utils_nav = require("../../utils/nav.js");
const answerPageSize = 10;
const postPageSize = 10;
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "home",
  setup(__props) {
    const userId = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const actionLoading = common_vendor.ref(false);
    const failedAvatar = common_vendor.ref(false);
    const userHome = common_vendor.ref(null);
    const entered = common_vendor.ref(false);
    const activeTab = common_vendor.ref("answers");
    const answerLoading = common_vendor.ref(false);
    const answerPage = common_vendor.ref(1);
    const answerTotal = common_vendor.ref(0);
    const answerFinished = common_vendor.ref(false);
    const answers = common_vendor.ref([]);
    const postLoading = common_vendor.ref(false);
    const postPage = common_vendor.ref(1);
    const postTotal = common_vendor.ref(0);
    const postFinished = common_vendor.ref(false);
    const posts = common_vendor.ref([]);
    const isExpert = common_vendor.computed(() => {
      var _a;
      return ((_a = userHome.value) == null ? void 0 : _a.expertStatus) === 3;
    });
    const statColumns = common_vendor.computed(() => isExpert.value ? 5 : 4);
    async function loadHome() {
      if (!userId.value)
        return;
      loading.value = true;
      try {
        userHome.value = await api_user.userApi.home(userId.value);
        failedAvatar.value = false;
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function loadAnswers(reset = false) {
      if (!userId.value)
        return;
      if (answerLoading.value || !reset && answerFinished.value)
        return;
      answerLoading.value = true;
      try {
        const page = reset ? 1 : answerPage.value;
        const resp = await api_user.userApi.answers(userId.value, { page, pageSize: answerPageSize });
        const list = (resp == null ? void 0 : resp.list) || [];
        if (reset)
          answers.value = list;
        else
          answers.value = answers.value.concat(list);
        answerTotal.value = Number((resp == null ? void 0 : resp.total) || 0);
        answerPage.value = page + 1;
        answerFinished.value = answers.value.length >= answerTotal.value || list.length < answerPageSize;
      } catch (err) {
        if (reset)
          answers.value = [];
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "回答加载失败", icon: "none" });
      } finally {
        answerLoading.value = false;
      }
    }
    async function loadPosts(reset = false) {
      if (!userId.value)
        return;
      if (postLoading.value || !reset && postFinished.value)
        return;
      postLoading.value = true;
      try {
        const page = reset ? 1 : postPage.value;
        const resp = await api_user.userApi.expertPosts(userId.value, { page, pageSize: postPageSize });
        const list = (resp == null ? void 0 : resp.list) || [];
        if (reset)
          posts.value = list;
        else
          posts.value = posts.value.concat(list);
        postTotal.value = Number((resp == null ? void 0 : resp.total) || 0);
        postPage.value = page + 1;
        postFinished.value = posts.value.length >= postTotal.value || list.length < postPageSize;
      } catch (err) {
        if (reset)
          posts.value = [];
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "科普文章加载失败", icon: "none" });
      } finally {
        postLoading.value = false;
      }
    }
    async function toggleFollow() {
      if (!userHome.value || userHome.value.self || actionLoading.value)
        return;
      actionLoading.value = true;
      try {
        if (userHome.value.followed) {
          await api_user.userApi.unfollow(userHome.value.userId);
          userHome.value = {
            ...userHome.value,
            followed: false,
            followerCount: Math.max(0, Number(userHome.value.followerCount || 0) - 1)
          };
          common_vendor.index.showToast({ title: "已取消关注", icon: "none" });
        } else {
          await api_user.userApi.follow(userHome.value.userId);
          userHome.value = {
            ...userHome.value,
            followed: true,
            followerCount: Number(userHome.value.followerCount || 0) + 1
          };
          common_vendor.index.showToast({ title: "关注成功", icon: "success" });
        }
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      } finally {
        actionLoading.value = false;
      }
    }
    function openAnswer(item) {
      if (!(item == null ? void 0 : item.answerId))
        return;
      utils_nav.openAnswerDetailPage(Number(item.answerId));
    }
    function openPost(item) {
      if (!(item == null ? void 0 : item.id))
        return;
      utils_nav.openExpertPostDetailPage(Number(item.id));
    }
    function switchTab(tab) {
      if (activeTab.value === tab)
        return;
      activeTab.value = tab;
      if (tab === "answers") {
        answerPage.value = 1;
        answerTotal.value = 0;
        answerFinished.value = false;
        loadAnswers(true);
      } else {
        postPage.value = 1;
        postTotal.value = 0;
        postFinished.value = false;
        loadPosts(true);
      }
    }
    async function initPage() {
      await loadHome();
      answerPage.value = 1;
      answerTotal.value = 0;
      answerFinished.value = false;
      await loadAnswers(true);
      if (isExpert.value) {
        postPage.value = 1;
        postTotal.value = 0;
        postFinished.value = false;
        await loadPosts(true);
      } else {
        posts.value = [];
      }
    }
    common_vendor.onLoad(async (options) => {
      if (!utils_authGuard.ensurePageAuth())
        return;
      userId.value = Number((options == null ? void 0 : options.userId) || 0);
      if (!userId.value) {
        common_vendor.index.showToast({ title: "用户参数错误", icon: "none" });
        setTimeout(() => common_vendor.index.navigateBack(), 150);
        return;
      }
      await initPage();
    });
    common_vendor.onShow(async () => {
      if (!entered.value) {
        entered.value = true;
        return;
      }
      if (!utils_authGuard.ensurePageAuth())
        return;
      await initPage();
    });
    common_vendor.onReachBottom(() => {
      if (activeTab.value === "posts") {
        loadPosts(false);
      } else {
        loadAnswers(false);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : !userHome.value ? {} : common_vendor.e({
        c: userHome.value.avatar && !failedAvatar.value
      }, userHome.value.avatar && !failedAvatar.value ? {
        d: userHome.value.avatar,
        e: common_vendor.o(($event) => failedAvatar.value = true)
      } : {
        f: common_vendor.t((userHome.value.nickname || "用").slice(0, 1))
      }, {
        g: common_vendor.t(userHome.value.nickname || "用户"),
        h: userHome.value.expertStatus === 3
      }, userHome.value.expertStatus === 3 ? {} : {}, {
        i: common_vendor.t(userHome.value.slogan || "这个人很懒，还没有填写个性签名"),
        j: common_vendor.t(userHome.value.questionCount || 0),
        k: common_vendor.t(userHome.value.answerCount || 0),
        l: userHome.value.expertStatus === 3
      }, userHome.value.expertStatus === 3 ? {
        m: common_vendor.t(userHome.value.expertPostCount || 0)
      } : {}, {
        n: common_vendor.t(userHome.value.followingCount || 0),
        o: common_vendor.t(userHome.value.followerCount || 0),
        p: `repeat(${statColumns.value}, 1fr)`,
        q: !userHome.value.self
      }, !userHome.value.self ? {
        r: common_vendor.t(userHome.value.followed ? "取消关注" : "关注"),
        s: userHome.value.followed ? 1 : "",
        t: actionLoading.value,
        v: common_vendor.o(toggleFollow)
      } : {}, {
        w: userHome.value.expertStatus === 3
      }, userHome.value.expertStatus === 3 ? {
        x: activeTab.value === "posts" ? 1 : "",
        y: common_vendor.o(($event) => switchTab("posts")),
        z: activeTab.value === "answers" ? 1 : "",
        A: common_vendor.o(($event) => switchTab("answers"))
      } : {}, {
        B: common_vendor.t(activeTab.value === "posts" ? "科普文章" : "有效回答"),
        C: activeTab.value === "posts"
      }, activeTab.value === "posts" ? common_vendor.e({
        D: !posts.value.length && postLoading.value
      }, !posts.value.length && postLoading.value ? {} : !posts.value.length ? {} : common_vendor.e({
        F: common_vendor.f(posts.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(item.summary || "暂无摘要"),
            c: common_vendor.t(item.likeCount || 0),
            d: common_vendor.t(item.viewCount || 0),
            e: common_vendor.t(item.createdAt || ""),
            f: item.id,
            g: common_vendor.o(($event) => openPost(item), item.id)
          };
        }),
        G: postLoading.value
      }, postLoading.value ? {} : postFinished.value ? {} : {}, {
        H: postFinished.value
      }), {
        E: !posts.value.length
      }) : common_vendor.e({
        I: !answers.value.length && answerLoading.value
      }, !answers.value.length && answerLoading.value ? {} : !answers.value.length ? {} : common_vendor.e({
        K: common_vendor.f(answers.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.questionTitle),
            b: common_vendor.t(item.contentPreview || "暂无内容"),
            c: common_vendor.t(item.likeCount || 0),
            d: common_vendor.t(item.createdAt || ""),
            e: item.answerId,
            f: common_vendor.o(($event) => openAnswer(item), item.answerId)
          };
        }),
        L: answerLoading.value
      }, answerLoading.value ? {} : answerFinished.value ? {} : {}, {
        M: answerFinished.value
      }), {
        J: !answers.value.length
      })), {
        b: !userHome.value
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-3013224c"]]);
wx.createPage(MiniProgramPage);
