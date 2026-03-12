"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const utils_nav = require("../../utils/nav.js");
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
    function reportAnswer() {
      var _a, _b;
      if (!((_b = (_a = detail.value) == null ? void 0 : _a.answer) == null ? void 0 : _b.id))
        return;
      const title = encodeURIComponent(detail.value.questionTitle || "");
      common_vendor.index.navigateTo({
        url: `/pages/question/report?targetType=answer&answerId=${detail.value.answer.id}&title=${title}`
      });
    }
    function showMoreMenu() {
      common_vendor.index.showActionSheet({
        itemList: ["举报"],
        success: (res) => {
          if (res.tapIndex === 0) {
            reportAnswer();
          }
        }
      });
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
    function openAnswerAuthor() {
      var _a, _b;
      if (!((_b = (_a = detail.value) == null ? void 0 : _a.answer) == null ? void 0 : _b.authorId))
        return;
      utils_nav.openUserHomePage(Number(detail.value.answer.authorId));
    }
    function openCommentAuthor(comment) {
      if (!(comment == null ? void 0 : comment.authorId))
        return;
      utils_nav.openUserHomePage(Number(comment.authorId));
    }
    function openQuestionFromAnswer() {
      var _a;
      const qid = (_a = detail.value) == null ? void 0 : _a.questionId;
      if (!qid)
        return;
      utils_nav.openQuestionDetail(Number(qid));
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
        e: common_vendor.o(openQuestionFromAnswer),
        f: common_vendor.o(showMoreMenu),
        g: detail.value.answer.authorAvatar
      }, detail.value.answer.authorAvatar ? {
        h: detail.value.answer.authorAvatar
      } : {
        i: common_vendor.t(defaultAvatarText)
      }, {
        j: common_vendor.t(detail.value.answer.authorName || "匿名用户"),
        k: detail.value.answer.bestAnswer
      }, detail.value.answer.bestAnswer ? {} : {}, {
        l: common_vendor.t(detail.value.answer.createdAt || ""),
        m: common_vendor.o(openAnswerAuthor),
        n: common_vendor.t(detail.value.answer.content),
        o: (_a = detail.value.answer.imageUrls) == null ? void 0 : _a.length
      }, ((_b = detail.value.answer.imageUrls) == null ? void 0 : _b.length) ? {
        p: common_vendor.f(detail.value.answer.imageUrls, (url, idx, i0) => {
          return {
            a: url + idx,
            b: url,
            c: common_vendor.o(($event) => previewImages(idx, detail.value.answer.imageUrls || []), url + idx)
          };
        })
      } : {}, {
        q: common_vendor.t(detail.value.answer.likeCount || 0),
        r: detail.value.answer.liked ? 1 : "",
        s: common_vendor.o(toggleLike),
        t: common_vendor.t(detail.value.answer.favoriteCount || 0),
        v: detail.value.answer.favorited ? 1 : "",
        w: common_vendor.o(toggleFavorite),
        x: common_vendor.t(detail.value.answer.commentCount || 0),
        y: common_vendor.t(comments.value.length),
        z: !comments.value.length
      }, !comments.value.length ? {} : {}, {
        A: common_vendor.f(rootComments.value, (root, k0, i0) => {
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
            g: common_vendor.o(($event) => openCommentAuthor(root), root.id),
            h: common_vendor.t(root.content),
            i: childrenOf(root.id).length
          }, childrenOf(root.id).length ? {
            j: common_vendor.f(childrenOf(root.id), (child, k1, i1) => {
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
                g: common_vendor.o(($event) => openCommentAuthor(child), child.id),
                h: common_vendor.t(child.content),
                i: child.id
              });
            })
          } : {}, {
            k: root.id
          });
        }),
        B: replyParentId.value
      }, replyParentId.value ? {
        C: common_vendor.t(replyToName.value),
        D: common_vendor.o(cancelReply)
      } : {}, {
        E: commentPlaceholder.value,
        F: commentInput.value,
        G: common_vendor.o(($event) => commentInput.value = $event.detail.value),
        H: common_vendor.t(posting.value ? "发送中" : "发送"),
        I: common_vendor.o(submitComment)
      }), {
        b: !detail.value
      });
    };
  }
});
wx.createPage(_sfc_main);
