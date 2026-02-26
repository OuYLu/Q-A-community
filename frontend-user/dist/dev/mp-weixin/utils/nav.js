"use strict";
const common_vendor = require("../common/vendor.js");
const utils_authGuard = require("./auth-guard.js");
const stores_auth = require("../stores/auth.js");
function openQuestionDetail(id) {
  const authStore = stores_auth.useAuthStore();
  const detailUrl = `/pages/question/detail?id=${id}`;
  if (authStore.isLogin) {
    common_vendor.index.navigateTo({
      url: detailUrl
    });
    return;
  }
  common_vendor.index.showModal({
    title: "提示",
    content: "登录后才能查看问题详情",
    confirmText: "去登录",
    cancelText: "返回首页",
    success: (res) => {
      if (res.confirm) {
        const target = encodeURIComponent(detailUrl);
        common_vendor.index.navigateTo({ url: `/pages/auth/login?redirect=${target}` });
        return;
      }
      common_vendor.index.switchTab({ url: "/pages/home/index" });
    }
  });
}
function openAskPage() {
  if (!utils_authGuard.requireAuth("/pages/question/ask"))
    return;
  common_vendor.index.navigateTo({
    url: "/pages/question/ask"
  });
}
function openAnswerPage(questionId, title) {
  const encodedTitle = title ? `&title=${encodeURIComponent(title)}` : "";
  if (!utils_authGuard.requireAuth(`/pages/question/answer?questionId=${questionId}${encodedTitle}`))
    return;
  common_vendor.index.navigateTo({
    url: `/pages/question/answer?questionId=${questionId}${encodedTitle}`
  });
}
function openAnswerDetailPage(answerId) {
  if (!utils_authGuard.requireAuth(`/pages/question/answer-detail?id=${answerId}`))
    return;
  common_vendor.index.navigateTo({
    url: `/pages/question/answer-detail?id=${answerId}`,
    fail: (err) => {
      common_vendor.index.showToast({
        title: "打开回答详情失败",
        icon: "none"
      });
      console.error("openAnswerDetailPage failed:", err);
    }
  });
}
exports.openAnswerDetailPage = openAnswerDetailPage;
exports.openAnswerPage = openAnswerPage;
exports.openAskPage = openAskPage;
exports.openQuestionDetail = openQuestionDetail;
