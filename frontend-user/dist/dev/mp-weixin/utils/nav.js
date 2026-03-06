"use strict";
const common_vendor = require("../common/vendor.js");
const utils_authGuard = require("./auth-guard.js");
const stores_auth = require("../stores/auth.js");
function openQuestionDetail(id) {
  const authStore = stores_auth.useAuthStore();
  const detailUrl = `/pages/question/detail?id=${id}`;
  if (authStore.isLogin) {
    common_vendor.index.navigateTo({ url: detailUrl });
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
function openAskPage(params) {
  const askUrl = (() => {
    if (!(params == null ? void 0 : params.topicId)) {
      return "/pages/question/ask";
    }
    const topicId = encodeURIComponent(String(params.topicId));
    const topicTitle = encodeURIComponent(params.topicTitle || "");
    return `/pages/question/ask?topicId=${topicId}&topicTitle=${topicTitle}`;
  })();
  const authStore = stores_auth.useAuthStore();
  if (!authStore.isLogin) {
    common_vendor.index.showModal({
      title: "提示",
      content: "未登录，登录后才能发起提问",
      confirmText: "去登录",
      cancelText: "取消",
      success: (res) => {
        if (!res.confirm)
          return;
        const target = encodeURIComponent(askUrl);
        common_vendor.index.navigateTo({ url: `/pages/auth/login?redirect=${target}` });
      }
    });
    return;
  }
  if (!utils_authGuard.requireAuth(askUrl))
    return;
  common_vendor.index.navigateTo({ url: askUrl });
}
function openExpertPostCreatePage() {
  if (!utils_authGuard.requireAuth("/pages/expert/post-create"))
    return;
  common_vendor.index.navigateTo({ url: "/pages/expert/post-create" });
}
function openExpertPostDetailPage(id) {
  const authStore = stores_auth.useAuthStore();
  const detailUrl = `/pages/expert/post-detail?id=${id}`;
  if (authStore.isLogin) {
    common_vendor.index.navigateTo({
      url: detailUrl,
      fail: () => {
        common_vendor.index.showToast({ title: "打开科普详情失败", icon: "none" });
      }
    });
    return;
  }
  common_vendor.index.showModal({
    title: "提示",
    content: "登录后才能查看科普详情",
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
function openUserHomePage(userId) {
  if (!utils_authGuard.requireAuth(`/pages/user/home?userId=${userId}`))
    return;
  common_vendor.index.navigateTo({ url: `/pages/user/home?userId=${userId}` });
}
exports.openAnswerDetailPage = openAnswerDetailPage;
exports.openAnswerPage = openAnswerPage;
exports.openAskPage = openAskPage;
exports.openExpertPostCreatePage = openExpertPostCreatePage;
exports.openExpertPostDetailPage = openExpertPostDetailPage;
exports.openQuestionDetail = openQuestionDetail;
exports.openUserHomePage = openUserHomePage;
