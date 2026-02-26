import { requireAuth } from "@/utils/auth-guard";
import { useAuthStore } from "@/stores/auth";

export function openQuestionDetail(id: number) {
  const authStore = useAuthStore();
  const detailUrl = `/pages/question/detail?id=${id}`;
  if (authStore.isLogin) {
    uni.navigateTo({
      url: detailUrl
    });
    return;
  }

  uni.showModal({
    title: "提示",
    content: "登录后才能查看问题详情",
    confirmText: "去登录",
    cancelText: "返回首页",
    success: (res) => {
      if (res.confirm) {
        const target = encodeURIComponent(detailUrl);
        uni.navigateTo({ url: `/pages/auth/login?redirect=${target}` });
        return;
      }
      uni.switchTab({ url: "/pages/home/index" });
    }
  });
}

export function openAskPage() {
  if (!requireAuth("/pages/question/ask")) return;
  uni.navigateTo({
    url: "/pages/question/ask"
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
