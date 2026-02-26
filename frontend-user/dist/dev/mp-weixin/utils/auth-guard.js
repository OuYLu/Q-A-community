"use strict";
const common_vendor = require("../common/vendor.js");
const stores_auth = require("../stores/auth.js");
function getCurrentPath() {
  const pages = getCurrentPages();
  if (!pages.length)
    return "/pages/home/index";
  return `/${pages[pages.length - 1].route}`;
}
function requireAuth(redirect) {
  const authStore = stores_auth.useAuthStore();
  if (authStore.isLogin)
    return true;
  authStore.logout();
  const target = encodeURIComponent(redirect || getCurrentPath());
  common_vendor.index.navigateTo({
    url: `/pages/auth/login?redirect=${target}`
  });
  return false;
}
function ensurePageAuth() {
  return requireAuth();
}
exports.ensurePageAuth = ensurePageAuth;
exports.requireAuth = requireAuth;
