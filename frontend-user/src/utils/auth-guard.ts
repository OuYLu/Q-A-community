import { useAuthStore } from "@/stores/auth";

const LOGIN_PAGE_PATH = "/pages/auth/login";
const LOGIN_ROUTE = "pages/auth/login";
const HOME_TAB_PATH = "/pages/home/index";
const LOGIN_NAV_LOCK_MS = 900;

let loginNavigating = false;
let loginNavTimer: ReturnType<typeof setTimeout> | null = null;

function normalizeRoute(route?: string) {
  if (!route) return "";
  return route.startsWith("/") ? route.slice(1) : route;
}

function currentPage() {
  const pages = getCurrentPages();
  if (!pages.length) return null;
  return pages[pages.length - 1];
}

function getCurrentPath() {
  const page = currentPage();
  if (!page?.route) return HOME_TAB_PATH;
  return `/${normalizeRoute(page.route)}`;
}

function isCurrentLoginPage() {
  return normalizeRoute(currentPage()?.route) === LOGIN_ROUTE;
}

function lockLoginNav() {
  loginNavigating = true;
  if (loginNavTimer) clearTimeout(loginNavTimer);
  loginNavTimer = setTimeout(() => {
    loginNavigating = false;
    loginNavTimer = null;
  }, LOGIN_NAV_LOCK_MS);
}

function unlockLoginNavSoon() {
  setTimeout(() => {
    loginNavigating = false;
  }, 260);
}

function buildLoginUrl(redirect?: string) {
  const raw = (redirect || getCurrentPath() || HOME_TAB_PATH).trim();
  const path = raw.startsWith("/") ? raw : `/${raw}`;
  const safeTarget = path.startsWith(LOGIN_PAGE_PATH) ? HOME_TAB_PATH : path;
  return `${LOGIN_PAGE_PATH}?redirect=${encodeURIComponent(safeTarget)}`;
}

function fallbackHome() {
  uni.switchTab({ url: HOME_TAB_PATH });
}

function findLoginPageDeltaFromTop() {
  const pages = getCurrentPages();
  for (let i = pages.length - 1; i >= 0; i -= 1) {
    if (normalizeRoute(pages[i]?.route) === LOGIN_ROUTE) {
      return pages.length - 1 - i;
    }
  }
  return -1;
}

function openLoginByUrl(url: string, preferReplace = false) {
  const primary = preferReplace ? uni.redirectTo : uni.navigateTo;
  const secondary = preferReplace ? uni.navigateTo : uni.redirectTo;
  primary({
    url,
    fail: () => {
      secondary({
        url,
        fail: () => {
          uni.reLaunch({
            url,
            fail: fallbackHome
          });
        }
      });
    }
  });
  unlockLoginNavSoon();
}

export function openLoginPage(options?: { redirect?: string; preferReplace?: boolean }) {
  if (isCurrentLoginPage() || loginNavigating) return false;
  lockLoginNav();

  const delta = findLoginPageDeltaFromTop();
  if (delta > 0) {
    uni.navigateBack({
      delta,
      fail: () => {
        openLoginByUrl(buildLoginUrl(options?.redirect), Boolean(options?.preferReplace));
      }
    });
    unlockLoginNavSoon();
    return true;
  }

  openLoginByUrl(buildLoginUrl(options?.redirect), Boolean(options?.preferReplace));
  return true;
}

export function requireAuth(redirect?: string) {
  const authStore = useAuthStore();
  if (authStore.isLogin) return true;
  authStore.logout();
  openLoginPage({ redirect, preferReplace: true });
  return false;
}

export function ensurePageAuth() {
  return requireAuth();
}
