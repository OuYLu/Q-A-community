"use strict";
const common_vendor = require("../../common/vendor.js");
const api_auth = require("../../api/auth.js");
const stores_auth = require("../../stores/auth.js");
const utils_constants = require("../../utils/constants.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "register",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const avatar = common_vendor.ref("");
    const nickname = common_vendor.ref("");
    const username = common_vendor.ref("");
    const password = common_vendor.ref("");
    const confirmPassword = common_vendor.ref("");
    const phone = common_vendor.ref("");
    const email = common_vendor.ref("");
    const loading = common_vendor.ref(false);
    const uploadingAvatar = common_vendor.ref(false);
    const avatarPreview = common_vendor.computed(() => {
      if (!avatar.value)
        return "";
      if (avatar.value.startsWith("http://") || avatar.value.startsWith("https://"))
        return avatar.value;
      return `${utils_constants.BASE_URL}${avatar.value}`;
    });
    function valid() {
      const uname = username.value.trim();
      const pwd = password.value;
      const cpwd = confirmPassword.value;
      const mobile = phone.value.trim();
      if (!nickname.value.trim()) {
        common_vendor.index.showToast({ title: "昵称必填", icon: "none" });
        return false;
      }
      if (!uname) {
        common_vendor.index.showToast({ title: "用户名必填", icon: "none" });
        return false;
      }
      if (!/^[A-Za-z0-9_]{8,26}$/.test(uname)) {
        common_vendor.index.showToast({ title: "用户名需8-26位字母数字或下划线", icon: "none" });
        return false;
      }
      if (!pwd) {
        common_vendor.index.showToast({ title: "密码必填", icon: "none" });
        return false;
      }
      if (pwd.length < 6) {
        common_vendor.index.showToast({ title: "密码至少6位", icon: "none" });
        return false;
      }
      if (pwd !== cpwd) {
        common_vendor.index.showToast({ title: "两次密码不一致", icon: "none" });
        return false;
      }
      if (!mobile) {
        common_vendor.index.showToast({ title: "手机号必填", icon: "none" });
        return false;
      }
      if (!/^1\d{10}$/.test(mobile)) {
        common_vendor.index.showToast({ title: "手机号格式不正确", icon: "none" });
        return false;
      }
      if (email.value.trim() && !/^\S+@\S+\.\S+$/.test(email.value.trim())) {
        common_vendor.index.showToast({ title: "邮箱格式不正确", icon: "none" });
        return false;
      }
      return true;
    }
    async function chooseAvatar() {
      var _a, _b;
      uploadingAvatar.value = true;
      try {
        const choose = await common_vendor.index.chooseImage({ count: 1, sizeType: ["compressed"], sourceType: ["album", "camera"] });
        const filePath = (_a = choose.tempFilePaths) == null ? void 0 : _a[0];
        if (!filePath)
          return;
        const upload = await common_vendor.index.uploadFile({
          url: `${utils_constants.BASE_URL}/api/common/avatar/upload`,
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
        const url = ((_b = body == null ? void 0 : body.data) == null ? void 0 : _b.url) || (body == null ? void 0 : body.data);
        if (!url) {
          throw new Error((body == null ? void 0 : body.message) || (body == null ? void 0 : body.msg) || "上传失败");
        }
        avatar.value = url;
        common_vendor.index.showToast({ title: "头像上传成功", icon: "success" });
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "头像上传失败", icon: "none" });
      } finally {
        uploadingAvatar.value = false;
      }
    }
    async function submit() {
      if (!valid())
        return;
      loading.value = true;
      try {
        await api_auth.authApi.register({
          username: username.value.trim(),
          password: password.value,
          nickname: nickname.value.trim(),
          phone: phone.value.trim(),
          email: email.value.trim() || void 0,
          avatar: avatar.value || void 0
        });
        const login = await api_auth.authApi.login({
          username: username.value.trim(),
          password: password.value
        });
        authStore.applyLogin(login, nickname.value.trim() || username.value.trim(), username.value.trim());
        common_vendor.index.showToast({ title: "注册成功", icon: "success" });
        setTimeout(() => {
          common_vendor.index.switchTab({ url: "/pages/home/index" });
        }, 180);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "注册失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function goLogin() {
      common_vendor.index.navigateBack();
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: avatarPreview.value
      }, avatarPreview.value ? {
        b: avatarPreview.value
      } : {}, {
        c: uploadingAvatar.value ? 1 : "",
        d: common_vendor.o(chooseAvatar),
        e: nickname.value,
        f: common_vendor.o(($event) => nickname.value = $event.detail.value),
        g: username.value,
        h: common_vendor.o(($event) => username.value = $event.detail.value),
        i: password.value,
        j: common_vendor.o(($event) => password.value = $event.detail.value),
        k: confirmPassword.value,
        l: common_vendor.o(($event) => confirmPassword.value = $event.detail.value),
        m: phone.value,
        n: common_vendor.o(($event) => phone.value = $event.detail.value),
        o: email.value,
        p: common_vendor.o(($event) => email.value = $event.detail.value),
        q: loading.value,
        r: common_vendor.o(submit),
        s: loading.value,
        t: common_vendor.o(goLogin)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-3d5ab0d5"]]);
wx.createPage(MiniProgramPage);
