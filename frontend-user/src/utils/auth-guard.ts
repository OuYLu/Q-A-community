import { useAuthStore } from "@/stores/auth";

function getCurrentPath() {
  const pages = getCurrentPages();
  if (!pages.length) return "/pages/home/index";
  return `/${pages[pages.length - 1].route}`;
}

export function requireAuth(redirect?: string) {
  const authStore = useAuthStore();
  if (authStore.isLogin) return true;
  authStore.logout();
  const target = encodeURIComponent(redirect || getCurrentPath());
  uni.navigateTo({
    url: `/pages/auth/login?redirect=${target}`
  });
  return false;
}

export function ensurePageAuth() {
  return requireAuth();
}

