"use strict";
const common_vendor = require("../../common/vendor.js");
const api_me = require("../../api/me.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "password",
  setup(__props) {
    const mode = common_vendor.ref("set");
    const oldPassword = common_vendor.ref("");
    const newPassword = common_vendor.ref("");
    const confirmPassword = common_vendor.ref("");
    const submitting = common_vendor.ref(false);
    const title = common_vendor.computed(() => mode.value === "modify" ? "修改密码" : "设置密码");
    const needOldPassword = common_vendor.computed(() => mode.value === "modify");
    function validate() {
      if (needOldPassword.value && !oldPassword.value) {
        common_vendor.index.showToast({ title: "请输入原密码", icon: "none" });
        return false;
      }
      if (!newPassword.value || newPassword.value.length < 6) {
        common_vendor.index.showToast({ title: "新密码至少6位", icon: "none" });
        return false;
      }
      if (newPassword.value !== confirmPassword.value) {
        common_vendor.index.showToast({ title: "两次输入的新密码不一致", icon: "none" });
        return false;
      }
      return true;
    }
    async function submit() {
      if (!validate())
        return;
      submitting.value = true;
      try {
        if (needOldPassword.value) {
          await api_me.meApi.changePassword({
            oldPassword: oldPassword.value,
            newPassword: newPassword.value,
            confirmPassword: confirmPassword.value
          });
        } else {
          await api_me.meApi.setFirstPassword({
            newPassword: newPassword.value,
            confirmPassword: confirmPassword.value
          });
        }
        common_vendor.index.showToast({ title: `${title.value}成功`, icon: "success" });
        setTimeout(() => common_vendor.index.navigateBack(), 220);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || `${title.value}失败`, icon: "none" });
      } finally {
        submitting.value = false;
      }
    }
    common_vendor.onLoad((options) => {
      mode.value = (options == null ? void 0 : options.mode) || "set";
      common_vendor.index.setNavigationBarTitle({ title: title.value });
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: needOldPassword.value
      }, needOldPassword.value ? {} : {}, {
        b: needOldPassword.value
      }, needOldPassword.value ? {
        c: oldPassword.value,
        d: common_vendor.o(($event) => oldPassword.value = $event.detail.value)
      } : {}, {
        e: newPassword.value,
        f: common_vendor.o(($event) => newPassword.value = $event.detail.value),
        g: confirmPassword.value,
        h: common_vendor.o(($event) => confirmPassword.value = $event.detail.value),
        i: common_vendor.t(title.value),
        j: submitting.value,
        k: common_vendor.o(submit)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-3d5cae68"]]);
wx.createPage(MiniProgramPage);
