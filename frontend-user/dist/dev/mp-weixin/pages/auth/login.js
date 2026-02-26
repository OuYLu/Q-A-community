"use strict";
const common_vendor = require("../../common/vendor.js");
const stores_auth = require("../../stores/auth.js");
const api_auth = require("../../api/auth.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "login",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const username = common_vendor.ref("");
    const password = common_vendor.ref("");
    const accountLoading = common_vendor.ref(false);
    const wechatLoading = common_vendor.ref(false);
    function getRouteRedirect() {
      var _a;
      const pages = getCurrentPages();
      const route = pages[pages.length - 1];
      return decodeURIComponent(((_a = route.options) == null ? void 0 : _a.redirect) || "");
    }
    function jumpAfterLogin(redirectRaw) {
      const redirect = redirectRaw ?? getRouteRedirect();
      if (redirect.startsWith("/pages/search/")) {
        common_vendor.index.switchTab({ url: "/pages/search/index" });
        return;
      }
      if (redirect.startsWith("/pages/discover/")) {
        common_vendor.index.switchTab({ url: "/pages/discover/index" });
        return;
      }
      if (redirect.startsWith("/pages/notice/")) {
        common_vendor.index.switchTab({ url: "/pages/notice/index" });
        return;
      }
      if (redirect.startsWith("/pages/mine/")) {
        common_vendor.index.switchTab({ url: "/pages/mine/index" });
        return;
      }
      if (redirect) {
        common_vendor.index.redirectTo({ url: redirect });
        return;
      }
      common_vendor.index.switchTab({ url: "/pages/home/index" });
    }
    async function login() {
      if (!username.value.trim() || !password.value.trim()) {
        common_vendor.index.showToast({ title: "请输入账号和密码", icon: "none" });
        return;
      }
      if (accountLoading.value || wechatLoading.value)
        return;
      accountLoading.value = true;
      try {
        const data = await api_auth.authApi.login({
          username: username.value.trim(),
          password: password.value
        });
        authStore.applyLogin(data, void 0, username.value.trim());
        common_vendor.index.showToast({ title: "登录成功", icon: "success" });
        setTimeout(() => jumpAfterLogin(), 180);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "登录失败", icon: "none" });
      } finally {
        accountLoading.value = false;
      }
    }
    function goRegister() {
      common_vendor.index.navigateTo({ url: "/pages/auth/register" });
    }
    async function loginByWechat() {
      if (accountLoading.value || wechatLoading.value)
        return;
      wechatLoading.value = true;
      try {
        const loginResult = await common_vendor.index.login({ provider: "weixin" });
        if (!loginResult.code) {
          common_vendor.index.showToast({ title: "微信授权失败", icon: "none" });
          return;
        }
        const redirect = getRouteRedirect();
        const data = await api_auth.authApi.wechatLogin({
          code: loginResult.code
        });
        if (data.needPhoneBind) {
          if (!data.bindTicket) {
            common_vendor.index.showToast({ title: "缺少绑定凭证", icon: "none" });
            return;
          }
          const target = encodeURIComponent(redirect);
          common_vendor.index.navigateTo({
            url: `/pages/auth/wechat-bind-phone?bindTicket=${encodeURIComponent(data.bindTicket)}&redirect=${target}&nickname=${encodeURIComponent("微信用户")}`
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
          "微信用户",
          data.username
        );
        common_vendor.index.showToast({ title: data.newUser ? "注册并登录成功" : "微信登录成功", icon: "success" });
        setTimeout(() => jumpAfterLogin(redirect), 180);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "微信登录失败", icon: "none" });
      } finally {
        wechatLoading.value = false;
      }
    }
    return (_ctx, _cache) => {
      return {
        a: username.value,
        b: common_vendor.o(($event) => username.value = $event.detail.value),
        c: password.value,
        d: common_vendor.o(($event) => password.value = $event.detail.value),
        e: accountLoading.value,
        f: wechatLoading.value,
        g: common_vendor.o(login),
        h: wechatLoading.value,
        i: accountLoading.value,
        j: common_vendor.o(loginByWechat),
        k: accountLoading.value || wechatLoading.value,
        l: common_vendor.o(goRegister)
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-6c56cc25"]]);
wx.createPage(MiniProgramPage);
