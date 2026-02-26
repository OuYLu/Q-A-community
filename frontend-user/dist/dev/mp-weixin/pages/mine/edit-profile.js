"use strict";
const common_vendor = require("../../common/vendor.js");
const stores_auth = require("../../stores/auth.js");
const api_me = require("../../api/me.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const utils_constants = require("../../utils/constants.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "edit-profile",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const username = common_vendor.ref("");
    const nickname = common_vendor.ref("");
    const slogan = common_vendor.ref("");
    const avatar = common_vendor.ref("");
    const email = common_vendor.ref("");
    const passwordSet = common_vendor.ref(0);
    const submitting = common_vendor.ref(false);
    const uploadingAvatar = common_vendor.ref(false);
    const avatarPreview = common_vendor.computed(() => {
      if (!avatar.value)
        return "";
      if (avatar.value.startsWith("http://") || avatar.value.startsWith("https://"))
        return avatar.value;
      return `${utils_constants.BASE_URL}${avatar.value}`;
    });
    const passwordActionText = common_vendor.computed(() => Number(passwordSet.value) === 1 ? "修改密码" : "设置密码");
    function fillFromOverview(data) {
      var _a, _b, _c, _d, _e;
      username.value = data.username || ((_a = authStore.user) == null ? void 0 : _a.username) || (data.userId ? `wx_${data.userId}` : "wx_user");
      nickname.value = data.nickname || ((_b = authStore.user) == null ? void 0 : _b.nickname) || "";
      slogan.value = data.slogan || ((_c = authStore.user) == null ? void 0 : _c.slogan) || "";
      avatar.value = data.avatar || ((_d = authStore.user) == null ? void 0 : _d.avatar) || "";
      email.value = data.email || ((_e = authStore.user) == null ? void 0 : _e.email) || "";
      passwordSet.value = Number(data.passwordSet || 0);
    }
    async function loadProfile() {
      const data = await api_me.meApi.overview();
      fillFromOverview(data);
    }
    common_vendor.onShow(async () => {
      if (!utils_authGuard.ensurePageAuth())
        return;
      try {
        await loadProfile();
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "加载资料失败", icon: "none" });
      }
    });
    async function chooseAvatar() {
      var _a, _b;
      uploadingAvatar.value = true;
      try {
        const choose = await common_vendor.index.chooseImage({ count: 1, sizeType: ["compressed"], sourceType: ["album", "camera"] });
        const filePath = (_a = choose.tempFilePaths) == null ? void 0 : _a[0];
        if (!filePath)
          return;
        const upload = await common_vendor.index.uploadFile({
          url: `${utils_constants.BASE_URL}/api/common/upload?bizType=avatar`,
          filePath,
          name: "file",
          header: authStore.token ? {
            Authorization: `Bearer ${authStore.token}`
          } : void 0
        });
        if (upload.statusCode < 200 || upload.statusCode >= 300) {
          throw new Error(`HTTP ${upload.statusCode}`);
        }
        const body = JSON.parse(upload.data || "{}");
        const url = (_b = body == null ? void 0 : body.data) == null ? void 0 : _b.url;
        if (!url) {
          throw new Error((body == null ? void 0 : body.message) || (body == null ? void 0 : body.msg) || "头像上传失败");
        }
        avatar.value = url;
        common_vendor.index.showToast({ title: "头像上传成功", icon: "success" });
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "头像上传失败", icon: "none" });
      } finally {
        uploadingAvatar.value = false;
      }
    }
    function validate() {
      const nick = nickname.value.trim();
      if (!nick || nick.length > 64) {
        common_vendor.index.showToast({ title: "昵称长度不合法", icon: "none" });
        return false;
      }
      if (email.value.trim() && !/^\S+@\S+\.\S+$/.test(email.value.trim())) {
        common_vendor.index.showToast({ title: "邮箱格式不正确", icon: "none" });
        return false;
      }
      return true;
    }
    async function submit() {
      if (!validate())
        return;
      submitting.value = true;
      try {
        await api_me.meApi.updateProfile({
          nickname: nickname.value.trim(),
          slogan: slogan.value.trim() || void 0,
          avatar: avatar.value.trim() || void 0,
          email: email.value.trim() || void 0
        });
        authStore.user = {
          ...authStore.user || {},
          username: username.value.trim(),
          nickname: nickname.value.trim(),
          slogan: slogan.value.trim() || void 0,
          avatar: avatar.value.trim() || void 0,
          email: email.value.trim() || void 0
        };
        common_vendor.index.showToast({ title: "资料已更新", icon: "success" });
        setTimeout(() => common_vendor.index.navigateBack(), 250);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "更新失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    }
    function goPasswordPage() {
      const mode = Number(passwordSet.value) === 1 ? "modify" : "set";
      common_vendor.index.navigateTo({ url: `/pages/mine/password?mode=${mode}` });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: avatarPreview.value
      }, avatarPreview.value ? {
        b: avatarPreview.value
      } : {}, {
        c: common_vendor.o(chooseAvatar),
        d: common_vendor.t(username.value || "-"),
        e: nickname.value,
        f: common_vendor.o(($event) => nickname.value = $event.detail.value),
        g: slogan.value,
        h: common_vendor.o(($event) => slogan.value = $event.detail.value),
        i: email.value,
        j: common_vendor.o(($event) => email.value = $event.detail.value),
        k: common_vendor.t(passwordActionText.value),
        l: common_vendor.o(goPasswordPage),
        m: submitting.value,
        n: submitting.value,
        o: common_vendor.o(submit)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-5b2b31b9"]]);
wx.createPage(MiniProgramPage);
