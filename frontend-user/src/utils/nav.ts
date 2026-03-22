import { openLoginPage, requireAuth } from "@/utils/auth-guard";
import { useAuthStore } from "@/stores/auth";

export function openQuestionDetail(id: number) {
  const authStore = useAuthStore();
  const detailUrl = `/pages/question/detail?id=${id}`;
  if (authStore.isLogin) {
    uni.navigateTo({ url: detailUrl });
    return;
  }

  uni.showModal({
    title: "提示",
    content: "登录后才能查看问题详情",
    confirmText: "去登录",
    cancelText: "返回首页",
    success: (res) => {
      if (res.confirm) {
        openLoginPage({ redirect: detailUrl, preferReplace: true });
        return;
      }
      uni.switchTab({ url: "/pages/home/index" });
    }
  });
}

export function openAskPage(params?: { topicId?: number | string; topicTitle?: string }) {
  const askUrl = (() => {
    if (!params?.topicId) {
      return "/pages/question/ask";
    }
    const topicId = encodeURIComponent(String(params.topicId));
    const topicTitle = encodeURIComponent(params.topicTitle || "");
    return `/pages/question/ask?topicId=${topicId}&topicTitle=${topicTitle}`;
  })();

  const authStore = useAuthStore();
  if (!authStore.isLogin) {
    uni.showModal({
      title: "提示",
      content: "未登录，登录后才能发起提问",
      confirmText: "去登录",
      cancelText: "取消",
      success: (res) => {
        if (!res.confirm) return;
        openLoginPage({ redirect: askUrl, preferReplace: true });
      }
    });
    return;
  }
  if (!requireAuth(askUrl)) return;
  uni.navigateTo({ url: askUrl });
}

export function openExpertPostCreatePage() {
  if (!requireAuth("/pages/expert/post-create")) return;
  uni.navigateTo({ url: "/pages/expert/post-create" });
}

export function openExpertPostDetailPage(id: number) {
  const authStore = useAuthStore();
  const detailUrl = `/pages/expert/post-detail?id=${id}`;
  if (authStore.isLogin) {
    uni.navigateTo({
      url: detailUrl,
      fail: () => {
        uni.showToast({ title: "打开科普详情失败", icon: "none" });
      }
    });
    return;
  }

  uni.showModal({
    title: "提示",
    content: "登录后才能查看科普详情",
    confirmText: "去登录",
    cancelText: "返回首页",
    success: (res) => {
      if (res.confirm) {
        openLoginPage({ redirect: detailUrl, preferReplace: true });
        return;
      }
      uni.switchTab({ url: "/pages/home/index" });
    }
  });
}

export function openAnswerPage(questionId: number, title?: string) {
  const encodedTitle = title ? `&title=${encodeURIComponent(title)}` : "";
  if (!requireAuth(`/pages/question/answer?questionId=${questionId}${encodedTitle}`)) return;
  uni.navigateTo({
    url: `/pages/question/answer?questionId=${questionId}${encodedTitle}`
  });
}

export function openAnswerDetailPage(answerId: number) {
  if (!requireAuth(`/pages/question/answer-detail?id=${answerId}`)) return;
  uni.navigateTo({
    url: `/pages/question/answer-detail?id=${answerId}`,
    fail: (err) => {
      uni.showToast({
        title: "打开回答详情失败",
        icon: "none"
      });
      console.error("openAnswerDetailPage failed:", err);
    }
  });
}

export function openUserHomePage(userId: number) {
  if (!requireAuth(`/pages/user/home?userId=${userId}`)) return;
  uni.navigateTo({ url: `/pages/user/home?userId=${userId}` });
}
