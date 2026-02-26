"use strict";
const common_vendor = require("../common/vendor.js");
const utils_constants = require("../utils/constants.js");
function readStorage(key, fallback) {
  const val = common_vendor.index.getStorageSync(key);
  if (!val)
    return fallback;
  return val;
}
const useAuthStore = common_vendor.defineStore("auth", {
  state: () => ({
    token: readStorage(utils_constants.TOKEN_KEY, ""),
    expiresAt: readStorage(utils_constants.AUTH_EXPIRES_KEY, 0),
    user: readStorage(utils_constants.USER_KEY, null)
  }),
  getters: {
    isLogin: (state) => Boolean(state.token) && state.expiresAt > Date.now()
  },
  actions: {
    applyLogin(login, nicknameHint, usernameHint) {
      var _a, _b, _c, _d, _e;
      this.token = login.token;
      this.expiresAt = login.expiresAt;
      this.user = {
        userId: login.userId,
        username: usernameHint || ((_a = this.user) == null ? void 0 : _a.username),
        nickname: nicknameHint || ((_b = this.user) == null ? void 0 : _b.nickname) || "用户",
        avatar: (_c = this.user) == null ? void 0 : _c.avatar,
        slogan: (_d = this.user) == null ? void 0 : _d.slogan,
        email: (_e = this.user) == null ? void 0 : _e.email
      };
      common_vendor.index.setStorageSync(utils_constants.TOKEN_KEY, this.token);
      common_vendor.index.setStorageSync(utils_constants.AUTH_EXPIRES_KEY, this.expiresAt);
      common_vendor.index.setStorageSync(utils_constants.USER_KEY, this.user);
    },
    mockLogin(nickname) {
      this.token = `mock-token-${Date.now()}`;
      this.expiresAt = Date.now() + 7 * 24 * 3600 * 1e3;
      this.user = {
        nickname
      };
      common_vendor.index.setStorageSync(utils_constants.TOKEN_KEY, this.token);
      common_vendor.index.setStorageSync(utils_constants.AUTH_EXPIRES_KEY, this.expiresAt);
      common_vendor.index.setStorageSync(utils_constants.USER_KEY, this.user);
    },
    logout() {
      this.token = "";
      this.expiresAt = 0;
      this.user = null;
      common_vendor.index.removeStorageSync(utils_constants.TOKEN_KEY);
      common_vendor.index.removeStorageSync(utils_constants.AUTH_EXPIRES_KEY);
      common_vendor.index.removeStorageSync(utils_constants.USER_KEY);
    }
  }
});
exports.useAuthStore = useAuthStore;
