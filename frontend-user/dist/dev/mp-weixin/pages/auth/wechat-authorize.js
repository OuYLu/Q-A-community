"use strict";
const common_vendor = require("../../common/vendor.js");
const api_auth = require("../../api/auth.js");
const api_me = require("../../api/me.js");
const stores_auth = require("../../stores/auth.js");
const WX_PROFILE_CACHE_KEY = "wx_login_profile_cache";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "wechat-authorize",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const loading = common_vendor.ref(false);
    const redirect = common_vendor.ref("");
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
    async function getWechatProfileSafe() {
      try {
        const profile = await new Promise((resolve, reject) => {
          common_vendor.index.getUserProfile({
            desc: "用于完善账号资料",
            success: resolve,
            fail: reject
          });
        });
        const info = (profile == null ? void 0 : profile.userInfo) || {};
        return {
          nickname: info.nickName,
          avatar: info.avatarUrl
        };
      } catch {
        return {};
      }
    }
    async function doWechatAuthorize() {
      loading.value = true;
      try {
        const loginResult = await common_vendor.index.login({ provider: "weixin" });
        if (!loginResult.code) {
          common_vendor.index.showToast({ title: "微信授权失败", icon: "none" });
          return;
        }
        const profile = await getWechatProfileSafe();
        common_vendor.index.setStorageSync(WX_PROFILE_CACHE_KEY, profile);
        const data = await api_auth.authApi.wechatLogin({
          code: loginResult.code,
          nickname: profile.nickname,
          avatar: profile.avatar
        });
        if (data.needPhoneBind) {
          if (!data.bindTicket) {
            common_vendor.index.showToast({ title: "缺少绑定凭证", icon: "none" });
            return;
          }
          const target = encodeURIComponent(redirect.value);
          const nickname = profile.nickname || "微信用户";
          common_vendor.index.redirectTo({
            url: `/pages/auth/wechat-bind-phone?bindTicket=${encodeURIComponent(data.bindTicket)}&redirect=${target}&nickname=${encodeURIComponent(nickname)}`
          });
          return;
        }
        if (!data.token) {
          common_vendor.index.showToast({ title: "登录返回无 token", icon: "none" });
          return;
        }
        authStore.applyLogin(
          {
            token: data.token,
            expiresAt: data.expiresAt,
            userId: data.userId
          },
          profile.nickname || "微信用户",
          data.username
        );
        if (profile.nickname || profile.avatar) {
          try {
            await api_me.meApi.updateProfile({
              nickname: profile.nickname,
              avatar: profile.avatar
            });
          } catch {
          }
        }
        common_vendor.index.showToast({ title: data.newUser ? "注册并登录成功" : "微信登录成功", icon: "success" });
        setTimeout(() => jumpAfterLogin(redirect.value), 180);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "微信登录失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function cancelAndBack() {
      common_vendor.index.navigateBack();
    }
    common_vendor.onLoad((options) => {
      redirect.value = decodeURIComponent((options == null ? void 0 : options.redirect) || "");
    });
    return (_ctx, _cache) => {
      return {
        a: loading.value,
        b: common_vendor.o(doWechatAuthorize),
        c: loading.value,
        d: common_vendor.o(cancelAndBack)
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-77a971d0"]]);
wx.createPage(MiniProgramPage);
