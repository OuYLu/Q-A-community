"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const api_question = require("../../api/question.js");
const defaultAvatarText = "用户";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "answer-detail",
  setup(__props) {
    const answerId = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const loadFailed = common_vendor.ref(false);
    const loadErrorText = common_vendor.ref("");
    const detail = common_vendor.ref(null);
    const commentInput = common_vendor.ref("");
    const posting = common_vendor.ref(false);
    const replyParentId = common_vendor.ref(null);
    const replyToName = common_vendor.ref("");
    const comments = common_vendor.computed(() => {
      var _a;
      return ((_a = detail.value) == null ? void 0 : _a.comments) || [];
    });
    const commentMap = common_vendor.computed(() => {
      const map = /* @__PURE__ */ new Map();
      comments.value.forEach((item) => map.set(item.id, item));
      return map;
    });
    const rootComments = common_vendor.computed(() => {
      return comments.value.filter((item) => !item.parentId || !commentMap.value.has(item.parentId));
    });
    function childrenOf(parentId) {
      return comments.value.filter((item) => item.parentId === parentId);
    }
    const commentPlaceholder = common_vendor.computed(() => {
      return replyParentId.value ? `回复 ${replyToName.value}...` : "写评论...";
    });
    function previewImages(index, urls) {
      if (!urls.length)
        return;
      common_vendor.index.previewImage({ current: urls[index], urls });
    }
    function startReply(comment) {
      replyParentId.value = comment.id;
      replyToName.value = comment.authorName || "用户";
    }
    function cancelReply() {
      replyParentId.value = null;
      replyToName.value = "";
    }
    async function loadDetail() {
      if (!answerId.value)
        return;
      loading.value = true;
      loadFailed.value = false;
      loadErrorText.value = "";
      try {
        detail.value = await api_question.questionApi.answerDetail(answerId.value);
      } catch (err) {
        loadFailed.value = true;
        loadErrorText.value = (err == null ? void 0 : err.message) || "加载回答失败";
        common_vendor.index.showToast({ title: loadErrorText.value, icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function toggleLike() {
      if (!detail.value)
        return;
      try {
        detail.value = await api_question.questionApi.toggleAnswerLike(answerId.value);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      }
    }
    async function toggleFavorite() {
      if (!detail.value)
        return;
      try {
        detail.value = await api_question.questionApi.toggleAnswerFavorite(answerId.value);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      }
    }
    async function submitComment() {
      const content = commentInput.value.trim();
      if (!content) {
        common_vendor.index.showToast({ title: "请输入评论内容", icon: "none" });
        return;
      }
      if (posting.value)
        return;
      posting.value = true;
      try {
        await api_question.questionApi.createAnswerComment(answerId.value, content, replyParentId.value || void 0);
        commentInput.value = "";
        cancelReply();
        await loadDetail();
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "评论失败", icon: "none" });
      } finally {
        posting.value = false;
      }
    }
    common_vendor.onLoad((options) => {
      answerId.value = Number((options == null ? void 0 : options.id) || (options == null ? void 0 : options.answerId) || 0);
      if (!answerId.value) {
        loadFailed.value = true;
        loadErrorText.value = "回答ID无效";
        return;
      }
      loadDetail();
    });
    common_vendor.onShow(() => {
      if (!utils_authGuard.ensurePageAuth())
        return;
      if (answerId.value) {
        loadDetail();
      }
    });
    return (_ctx, _cache) => {
      var _a, _b;
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : !detail.value ? {
        c: common_vendor.t(loadFailed.value ? loadErrorText.value || "回答不存在或已删除" : "未找到回答")
      } : common_vendor.e({
        d: common_vendor.t(detail.value.questionTitle),
        e: detail.value.answer.authorAvatar
      }, detail.value.answer.authorAvatar ? {
        f: detail.value.answer.authorAvatar
      } : {
        g: common_vendor.t(defaultAvatarText)
      }, {
        h: common_vendor.t(detail.value.answer.authorName || "匿名用户"),
        i: detail.value.answer.bestAnswer
      }, detail.value.answer.bestAnswer ? {} : {}, {
        j: common_vendor.t(detail.value.answer.createdAt || ""),
        k: common_vendor.t(detail.value.answer.content),
        l: (_a = detail.value.answer.imageUrls) == null ? void 0 : _a.length
      }, ((_b = detail.value.answer.imageUrls) == null ? void 0 : _b.length) ? {
        m: common_vendor.f(detail.value.answer.imageUrls, (url, idx, i0) => {
          return {
            a: url + idx,
            b: url,
            c: common_vendor.o(($event) => previewImages(idx, detail.value.answer.imageUrls || []), url + idx)
          };
        })
      } : {}, {
        n: common_vendor.t(detail.value.answer.likeCount || 0),
        o: detail.value.answer.liked ? 1 : "",
        p: common_vendor.o(toggleLike),
        q: common_vendor.t(detail.value.answer.favoriteCount || 0),
        r: detail.value.answer.favorited ? 1 : "",
        s: common_vendor.o(toggleFavorite),
        t: common_vendor.t(detail.value.answer.commentCount || 0),
        v: common_vendor.t(comments.value.length),
        w: !comments.value.length
      }, !comments.value.length ? {} : {}, {
        x: common_vendor.f(rootComments.value, (root, k0, i0) => {
          return common_vendor.e({
            a: root.authorAvatar
          }, root.authorAvatar ? {
            b: root.authorAvatar
          } : {
            c: common_vendor.t(defaultAvatarText)
          }, {
            d: common_vendor.t(root.authorName || "匿名用户"),
            e: common_vendor.t(root.createdAt || ""),
            f: common_vendor.o(($event) => startReply(root), root.id),
            g: common_vendor.t(root.content),
            h: childrenOf(root.id).length
          }, childrenOf(root.id).length ? {
            i: common_vendor.f(childrenOf(root.id), (child, k1, i1) => {
              return common_vendor.e({
                a: child.authorAvatar
              }, child.authorAvatar ? {
                b: child.authorAvatar
              } : {
                c: common_vendor.t(defaultAvatarText)
              }, {
                d: common_vendor.t(child.authorName || "匿名用户"),
                e: common_vendor.t(child.createdAt || ""),
                f: common_vendor.o(($event) => startReply(child), child.id),
                g: common_vendor.t(child.content),
                h: child.id
              });
            })
          } : {}, {
            j: root.id
          });
        }),
        y: replyParentId.value
      }, replyParentId.value ? {
        z: common_vendor.t(replyToName.value),
        A: common_vendor.o(cancelReply)
      } : {}, {
        B: commentPlaceholder.value,
        C: commentInput.value,
        D: common_vendor.o(($event) => commentInput.value = $event.detail.value),
        E: common_vendor.t(posting.value ? "发送中" : "发送"),
        F: common_vendor.o(submitComment)
      }), {
        b: !detail.value
      });
    };
  }
});
wx.createPage(_sfc_main);
