"use strict";
const common_vendor = require("../../common/vendor.js");
const api_auth = require("../../api/auth.js");
const stores_auth = require("../../stores/auth.js");
const api_me = require("../../api/me.js");
const WX_PROFILE_CACHE_KEY = "wx_login_profile_cache";
const LOG_PREFIX = "[wechat-bind-phone]";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "wechat-bind-phone",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const bindTicket = common_vendor.ref("");
    const redirect = common_vendor.ref("");
    const nicknameHint = common_vendor.ref("微信用户");
    const loading = common_vendor.ref(false);
    function jumpAfterLogin(target) {
      const to = target || "";
      if (to.startsWith("/pages/search/")) {
        common_vendor.index.switchTab({ url: "/pages/search/index" });
        return;
      }
      if (to.startsWith("/pages/discover/")) {
        common_vendor.index.switchTab({ url: "/pages/discover/index" });
        return;
      }
      if (to.startsWith("/pages/notice/")) {
        common_vendor.index.switchTab({ url: "/pages/notice/index" });
        return;
      }
      if (to.startsWith("/pages/mine/")) {
        common_vendor.index.switchTab({ url: "/pages/mine/index" });
        return;
      }
      if (to) {
        common_vendor.index.redirectTo({ url: to });
        return;
      }
      common_vendor.index.switchTab({ url: "/pages/home/index" });
    }
    async function doBind(payload) {
      console.log(LOG_PREFIX, "doBind called", {
        hasBindTicket: Boolean(bindTicket.value),
        hasPhoneCode: Boolean(payload == null ? void 0 : payload.phoneCode),
        phoneCodePreview: (payload == null ? void 0 : payload.phoneCode) ? `${payload.phoneCode.slice(0, 6)}...` : ""
      });
      if (loading.value) {
        console.log(LOG_PREFIX, "doBind skipped because loading");
        return;
      }
      if (!bindTicket.value) {
        console.warn(LOG_PREFIX, "bindTicket missing");
        common_vendor.index.showToast({ title: "缺少绑定凭证", icon: "none" });
        return;
      }
      loading.value = true;
      try {
        const cached = common_vendor.index.getStorageSync(WX_PROFILE_CACHE_KEY) || {};
        const data = await api_auth.authApi.wechatBindPhone({
          bindTicket: bindTicket.value,
          phoneCode: payload.phoneCode,
          nickname: cached.nickname,
          avatar: cached.avatar
        });
        console.log(LOG_PREFIX, "wechatBindPhone response", data);
        if (!data.token) {
          console.warn(LOG_PREFIX, "bind success but token missing", data);
          common_vendor.index.showToast({ title: "绑定成功但登录信息缺失", icon: "none" });
          return;
        }
        authStore.applyLogin(
          {
            token: data.token,
            expiresAt: data.expiresAt,
            userId: data.userId
          },
          nicknameHint.value,
          data.username
        );
        if (cached.nickname || cached.avatar) {
          try {
            await api_me.meApi.updateProfile({
              nickname: cached.nickname,
              avatar: cached.avatar
            });
          } catch (e) {
          }
        }
        common_vendor.index.removeStorageSync(WX_PROFILE_CACHE_KEY);
        common_vendor.index.showToast({ title: "绑定并登录成功", icon: "success" });
        setTimeout(() => jumpAfterLogin(redirect.value), 180);
      } catch (err) {
        console.error(LOG_PREFIX, "wechatBindPhone failed", err);
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "绑定失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function onGetPhoneNumber(e) {
      var _a, _b;
      console.log(LOG_PREFIX, "getPhoneNumber callback", e == null ? void 0 : e.detail);
      const phoneCode = (_a = e == null ? void 0 : e.detail) == null ? void 0 : _a.code;
      const errMsg = (_b = e == null ? void 0 : e.detail) == null ? void 0 : _b.errMsg;
      if (!phoneCode) {
        console.warn(LOG_PREFIX, "phone code missing", { errMsg, detail: e == null ? void 0 : e.detail });
        common_vendor.index.showToast({ title: errMsg || "未获取到手机号授权", icon: "none" });
        return;
      }
      doBind({ phoneCode });
    }
    common_vendor.onLoad((options) => {
      console.log(LOG_PREFIX, "onLoad options", options);
      bindTicket.value = decodeURIComponent((options == null ? void 0 : options.bindTicket) || "");
      redirect.value = decodeURIComponent((options == null ? void 0 : options.redirect) || "");
      nicknameHint.value = decodeURIComponent((options == null ? void 0 : options.nickname) || "微信用户");
      console.log(LOG_PREFIX, "onLoad parsed", {
        hasBindTicket: Boolean(bindTicket.value),
        redirect: redirect.value,
        nicknameHint: nicknameHint.value
      });
    });
    return (_ctx, _cache) => {
      return {
        a: loading.value,
        b: loading.value,
        c: common_vendor.o(onGetPhoneNumber)
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-26f93713"]]);
wx.createPage(MiniProgramPage);
