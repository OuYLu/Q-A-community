"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const utils_nav = require("../../utils/nav.js");
const api_question = require("../../api/question.js");
const stores_auth = require("../../stores/auth.js");
const defaultAvatarText = "用户";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "detail",
  setup(__props) {
    const questionId = common_vendor.ref(0);
    const authStore = stores_auth.useAuthStore();
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
    const isMine = common_vendor.computed(() => {
      var _a, _b;
      const currentId = (_a = authStore.user) == null ? void 0 : _a.userId;
      if (!currentId || !((_b = question.value) == null ? void 0 : _b.authorId))
        return false;
      return Number(currentId) === Number(question.value.authorId);
    });
    const isSelfOnly = common_vendor.computed(() => {
      var _a;
      return Number(((_a = question.value) == null ? void 0 : _a.status) || 0) === 5;
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
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "加载失败", icon: "none" });
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
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "点赞失败", icon: "none" });
      }
    }
    async function toggleFavorite() {
      if (!question.value)
        return;
      try {
        const updated = await api_question.questionApi.toggleFavorite(question.value.id);
        question.value = { ...question.value, favoriteCount: updated.favoriteCount, favorited: updated.favorited };
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "收藏失败", icon: "none" });
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
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "点赞失败", icon: "none" });
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
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "收藏失败", icon: "none" });
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
    async function setSelfOnly() {
      if (!question.value || actionLoading.value)
        return;
      actionLoading.value = true;
      try {
        await api_question.questionApi.setQuestionSelfOnly(question.value.id);
        common_vendor.index.showToast({ title: "已设为仅自己可见", icon: "success" });
        await loadDetail();
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "设置失败", icon: "none" });
      } finally {
        actionLoading.value = false;
      }
    }
    async function setPublic() {
      if (!question.value || actionLoading.value)
        return;
      actionLoading.value = true;
      try {
        await api_question.questionApi.setQuestionPublic(question.value.id);
        common_vendor.index.showToast({ title: "已设为公开", icon: "success" });
        await loadDetail();
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "设置失败", icon: "none" });
      } finally {
        actionLoading.value = false;
      }
    }
    async function deleteQuestion() {
      if (!question.value || actionLoading.value)
        return;
      common_vendor.index.showModal({
        title: "删除问题",
        content: "删除后不可恢复，是否继续？",
        success: async (res) => {
          if (!res.confirm)
            return;
          actionLoading.value = true;
          try {
            await api_question.questionApi.deleteQuestion(question.value.id);
            common_vendor.index.showToast({ title: "删除成功", icon: "success" });
            setTimeout(() => common_vendor.index.navigateBack(), 220);
          } catch (err) {
            common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "删除失败", icon: "none" });
          } finally {
            actionLoading.value = false;
          }
        }
      });
    }
    function reportQuestion() {
      if (!question.value)
        return;
      const title = encodeURIComponent(question.value.title || "");
      common_vendor.index.navigateTo({ url: `/pages/question/report?questionId=${question.value.id}&title=${title}` });
    }
    function showMoreMenu() {
      if (!question.value)
        return;
      if (isMine.value) {
        const visibilityAction = isSelfOnly.value ? "设为公开" : "仅自己可见";
        common_vendor.index.showActionSheet({
          itemList: [visibilityAction, "删除问题"],
          success: (res) => {
            if (res.tapIndex === 0) {
              if (isSelfOnly.value)
                setPublic();
              else
                setSelfOnly();
              return;
            }
            if (res.tapIndex === 1)
              deleteQuestion();
          }
        });
        return;
      }
      common_vendor.index.showActionSheet({
        itemList: ["举报问题"],
        success: (res) => {
          if (res.tapIndex === 0)
            reportQuestion();
        }
      });
    }
    function openAuthorHome() {
      var _a;
      if (!((_a = question.value) == null ? void 0 : _a.authorId))
        return;
      if (isMine.value) {
        common_vendor.index.switchTab({ url: "/pages/mine/index" });
        return;
      }
      utils_nav.openUserHomePage(Number(question.value.authorId));
    }
    function openAnswerAuthorHome(item) {
      if (!(item == null ? void 0 : item.authorId))
        return;
      utils_nav.openUserHomePage(Number(item.authorId));
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
        c: common_vendor.t(loadFailed.value ? "加载失败，请重试" : "问题不存在")
      } : common_vendor.e({
        d: common_vendor.o(showMoreMenu),
        e: question.value.authorAvatar
      }, question.value.authorAvatar ? {
        f: question.value.authorAvatar
      } : {
        g: common_vendor.t(defaultAvatarText)
      }, {
        h: common_vendor.t(question.value.authorName || "匿名用户"),
        i: common_vendor.t(question.value.createdAt || ""),
        j: common_vendor.o(openAuthorHome),
        k: bannerImages.value.length
      }, bannerImages.value.length ? {
        l: common_vendor.f(bannerImages.value, (url, idx, i0) => {
          return {
            a: url,
            b: common_vendor.o(($event) => previewImages(idx, bannerImages.value), url + idx),
            c: url + idx
          };
        })
      } : {}, {
        m: common_vendor.t(question.value.title),
        n: common_vendor.t(question.value.content || "暂无内容"),
        o: (_a = question.value.tags) == null ? void 0 : _a.length
      }, ((_b = question.value.tags) == null ? void 0 : _b.length) ? {
        p: common_vendor.f(question.value.tags || [], (tag, k0, i0) => {
          return {
            a: common_vendor.t(tag),
            b: tag
          };
        })
      } : {}, {
        q: common_vendor.t(question.value.answerCount || 0),
        r: common_vendor.t(question.value.viewCount || 0),
        s: common_vendor.t(question.value.likeCount || 0),
        t: question.value.liked ? 1 : "",
        v: common_vendor.o(toggleLike),
        w: common_vendor.t(question.value.favoriteCount || 0),
        x: question.value.favorited ? 1 : "",
        y: common_vendor.o(toggleFavorite),
        z: common_vendor.o(($event) => common_vendor.unref(utils_nav.openAnswerPage)(question.value.id, question.value.title)),
        A: common_vendor.t(answers.value.length),
        B: !answers.value.length
      }, !answers.value.length ? {} : {}, {
        C: common_vendor.f(answers.value, (item, k0, i0) => {
          return common_vendor.e({
            a: item.canRecommend
          }, item.canRecommend ? {
            b: common_vendor.t(item.bestAnswer ? "取消最佳" : "设为最佳"),
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
            k: common_vendor.o(($event) => openAnswerAuthorHome(item), item.id),
            l: firstImage(item)
          }, firstImage(item) ? {
            m: firstImage(item)
          } : {}, {
            n: common_vendor.t(item.content),
            o: common_vendor.t(item.likeCount || 0),
            p: item.liked ? 1 : "",
            q: common_vendor.o(($event) => toggleAnswerLike(item.id), item.id),
            r: common_vendor.t(item.commentCount || 0),
            s: common_vendor.o(($event) => openAnswerDetail(item.id), item.id),
            t: common_vendor.t(item.favoriteCount || 0),
            v: item.favorited ? 1 : "",
            w: common_vendor.o(($event) => toggleAnswerFavorite(item.id), item.id),
            x: common_vendor.o(() => {
            }, item.id),
            y: item.id,
            z: common_vendor.o(($event) => openAnswerDetail(item.id), item.id)
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
