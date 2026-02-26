"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const utils_nav = require("../../utils/nav.js");
const api_question = require("../../api/question.js");
const defaultAvatarText = "用户";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "detail",
  setup(__props) {
    const questionId = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const loadFailed = common_vendor.ref(false);
    const actionLoading = common_vendor.ref(false);
    const question = common_vendor.ref(null);
    const entered = common_vendor.ref(false);
    const answers = common_vendor.computed(() => {
      var _a;
      return ((_a = question.value) == null ? void 0 : _a.answers) || [];
    });
    const bannerImages = common_vendor.computed(() => {
      var _a;
      return ((_a = question.value) == null ? void 0 : _a.imageUrls) || [];
    });
    function previewImages(index, urls) {
      if (!urls.length)
        return;
      common_vendor.index.previewImage({ current: urls[index], urls });
    }
    function firstImage(item) {
      return item.imageUrls && item.imageUrls.length ? item.imageUrls[0] : "";
    }
    async function loadDetail() {
      if (!questionId.value)
        return;
      loading.value = true;
      loadFailed.value = false;
      try {
        question.value = await api_question.questionApi.detail(questionId.value);
      } catch (err) {
        loadFailed.value = true;
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "加载问题失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function toggleLike() {
      if (!question.value)
        return;
      try {
        const updated = await api_question.questionApi.toggleLike(question.value.id);
        question.value = { ...question.value, likeCount: updated.likeCount, liked: updated.liked };
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      }
    }
    async function toggleFavorite() {
      if (!question.value)
        return;
      try {
        const updated = await api_question.questionApi.toggleFavorite(question.value.id);
        question.value = { ...question.value, favoriteCount: updated.favoriteCount, favorited: updated.favorited };
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      }
    }
    async function toggleAnswerLike(answerId) {
      if (actionLoading.value)
        return;
      actionLoading.value = true;
      try {
        await api_question.questionApi.toggleAnswerLike(answerId);
        await loadDetail();
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      } finally {
        actionLoading.value = false;
      }
    }
    async function toggleAnswerFavorite(answerId) {
      if (actionLoading.value)
        return;
      actionLoading.value = true;
      try {
        await api_question.questionApi.toggleAnswerFavorite(answerId);
        await loadDetail();
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      } finally {
        actionLoading.value = false;
      }
    }
    async function toggleBest(answerId, isBest) {
      if (!question.value || actionLoading.value)
        return;
      actionLoading.value = true;
      try {
        await api_question.questionApi.recommendBest(question.value.id, answerId);
        common_vendor.index.showToast({ title: isBest ? "已取消最佳" : "已设为最佳", icon: "success" });
        await loadDetail();
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      } finally {
        actionLoading.value = false;
      }
    }
    function openAnswerDetail(answerId) {
      utils_nav.openAnswerDetailPage(answerId);
    }
    common_vendor.onLoad((options) => {
      questionId.value = Number((options == null ? void 0 : options.id) || 0);
      loadDetail();
    });
    common_vendor.onShow(() => {
      utils_authGuard.ensurePageAuth();
      if (!entered.value) {
        entered.value = true;
        return;
      }
      loadDetail();
    });
    return (_ctx, _cache) => {
      var _a, _b;
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : !question.value ? {
        c: common_vendor.t(loadFailed.value ? "问题不存在或已删除" : "未找到问题")
      } : common_vendor.e({
        d: question.value.authorAvatar
      }, question.value.authorAvatar ? {
        e: question.value.authorAvatar
      } : {
        f: common_vendor.t(defaultAvatarText)
      }, {
        g: common_vendor.t(question.value.authorName || "匿名用户"),
        h: common_vendor.t(question.value.createdAt || ""),
        i: bannerImages.value.length
      }, bannerImages.value.length ? {
        j: common_vendor.f(bannerImages.value, (url, idx, i0) => {
          return {
            a: url,
            b: common_vendor.o(($event) => previewImages(idx, bannerImages.value), url + idx),
            c: url + idx
          };
        })
      } : {}, {
        k: common_vendor.t(question.value.title),
        l: common_vendor.t(question.value.content || "暂无详细描述"),
        m: (_a = question.value.tags) == null ? void 0 : _a.length
      }, ((_b = question.value.tags) == null ? void 0 : _b.length) ? {
        n: common_vendor.f(question.value.tags || [], (tag, k0, i0) => {
          return {
            a: common_vendor.t(tag),
            b: tag
          };
        })
      } : {}, {
        o: common_vendor.t(question.value.answerCount || 0),
        p: common_vendor.t(question.value.viewCount || 0),
        q: common_vendor.t(question.value.likeCount || 0),
        r: question.value.liked ? 1 : "",
        s: common_vendor.o(toggleLike),
        t: common_vendor.t(question.value.favoriteCount || 0),
        v: question.value.favorited ? 1 : "",
        w: common_vendor.o(toggleFavorite),
        x: common_vendor.o(($event) => common_vendor.unref(utils_nav.openAnswerPage)(question.value.id, question.value.title)),
        y: common_vendor.t(answers.value.length),
        z: !answers.value.length
      }, !answers.value.length ? {} : {}, {
        A: common_vendor.f(answers.value, (item, k0, i0) => {
          return common_vendor.e({
            a: item.canRecommend
          }, item.canRecommend ? {
            b: common_vendor.t(item.bestAnswer ? "取消" : "最佳"),
            c: item.bestAnswer ? 1 : "",
            d: common_vendor.o(($event) => toggleBest(item.id, item.bestAnswer), item.id)
          } : {}, {
            e: item.authorAvatar
          }, item.authorAvatar ? {
            f: item.authorAvatar
          } : {
            g: common_vendor.t(defaultAvatarText)
          }, {
            h: common_vendor.t(item.authorName || "匿名用户"),
            i: item.bestAnswer
          }, item.bestAnswer ? {} : {}, {
            j: common_vendor.t(item.createdAt || ""),
            k: firstImage(item)
          }, firstImage(item) ? {
            l: firstImage(item)
          } : {}, {
            m: common_vendor.t(item.content),
            n: common_vendor.t(item.likeCount || 0),
            o: item.liked ? 1 : "",
            p: common_vendor.o(($event) => toggleAnswerLike(item.id), item.id),
            q: common_vendor.t(item.commentCount || 0),
            r: common_vendor.o(($event) => openAnswerDetail(item.id), item.id),
            s: common_vendor.t(item.favoriteCount || 0),
            t: item.favorited ? 1 : "",
            v: common_vendor.o(($event) => toggleAnswerFavorite(item.id), item.id),
            w: common_vendor.o(() => {
            }, item.id),
            x: item.id,
            y: common_vendor.o(($event) => openAnswerDetail(item.id), item.id)
          });
        })
      }), {
        b: !question.value
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-76570c52"]]);
wx.createPage(MiniProgramPage);
