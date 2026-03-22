"use strict";
const common_vendor = require("../common/vendor.js");
const stores_auth = require("../stores/auth.js");
const LOGIN_PAGE_PATH = "/pages/auth/login";
const LOGIN_ROUTE = "pages/auth/login";
const HOME_TAB_PATH = "/pages/home/index";
const LOGIN_NAV_LOCK_MS = 900;
let loginNavigating = false;
let loginNavTimer = null;
function normalizeRoute(route) {
  if (!route)
    return "";
  return route.startsWith("/") ? route.slice(1) : route;
}
function currentPage() {
  const pages = getCurrentPages();
  if (!pages.length)
    return null;
  return pages[pages.length - 1];
}
function getCurrentPath() {
  const page = currentPage();
  if (!(page == null ? void 0 : page.route))
    return HOME_TAB_PATH;
  return `/${normalizeRoute(page.route)}`;
}
function isCurrentLoginPage() {
  var _a;
  return normalizeRoute((_a = currentPage()) == null ? void 0 : _a.route) === LOGIN_ROUTE;
}
function lockLoginNav() {
  loginNavigating = true;
  if (loginNavTimer)
    clearTimeout(loginNavTimer);
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
function buildLoginUrl(redirect) {
  const raw = (redirect || getCurrentPath() || HOME_TAB_PATH).trim();
  const path = raw.startsWith("/") ? raw : `/${raw}`;
  const safeTarget = path.startsWith(LOGIN_PAGE_PATH) ? HOME_TAB_PATH : path;
  return `${LOGIN_PAGE_PATH}?redirect=${encodeURIComponent(safeTarget)}`;
}
function fallbackHome() {
  common_vendor.index.switchTab({ url: HOME_TAB_PATH });
}
function findLoginPageDeltaFromTop() {
  var _a;
  const pages = getCurrentPages();
  for (let i = pages.length - 1; i >= 0; i -= 1) {
    if (normalizeRoute((_a = pages[i]) == null ? void 0 : _a.route) === LOGIN_ROUTE) {
      return pages.length - 1 - i;
    }
  }
  return -1;
}
function openLoginByUrl(url, preferReplace = false) {
  const primary = preferReplace ? common_vendor.index.redirectTo : common_vendor.index.navigateTo;
  const secondary = preferReplace ? common_vendor.index.navigateTo : common_vendor.index.redirectTo;
  primary({
    url,
    fail: () => {
      secondary({
        url,
        fail: () => {
          common_vendor.index.reLaunch({
            url,
            fail: fallbackHome
          });
        }
      });
    }
  });
  unlockLoginNavSoon();
}
function openLoginPage(options) {
  if (isCurrentLoginPage() || loginNavigating)
    return false;
  lockLoginNav();
  const delta = findLoginPageDeltaFromTop();
  if (delta > 0) {
    common_vendor.index.navigateBack({
      delta,
      fail: () => {
        openLoginByUrl(buildLoginUrl(options == null ? void 0 : options.redirect), Boolean(options == null ? void 0 : options.preferReplace));
      }
    });
    unlockLoginNavSoon();
    return true;
  }
  openLoginByUrl(buildLoginUrl(options == null ? void 0 : options.redirect), Boolean(options == null ? void 0 : options.preferReplace));
  return true;
}
function requireAuth(redirect) {
  const authStore = stores_auth.useAuthStore();
  if (authStore.isLogin)
    return true;
  authStore.logout();
  openLoginPage({ redirect, preferReplace: true });
  return false;
}
function ensurePageAuth() {
  return requireAuth();
}
exports.ensurePageAuth = ensurePageAuth;
exports.openLoginPage = openLoginPage;
exports.requireAuth = requireAuth;
