"use strict";
const common_vendor = require("../../common/vendor.js");
const api_me = require("../../api/me.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const stores_auth = require("../../stores/auth.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "settings",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const loading = common_vendor.ref(false);
    const savingPrivacy = common_vendor.ref(false);
    const canceling = common_vendor.ref(false);
    const privacy = common_vendor.ref({
      profileVisible: 1,
      statsVisible: 1,
      personalizedRecommend: 1
    });
    function toSwitch(value) {
      return Number(value || 0) === 1;
    }
    function updateSwitch(key, value) {
      privacy.value = {
        ...privacy.value,
        [key]: value ? 1 : 0
      };
    }
    async function loadData() {
      loading.value = true;
      try {
        const privacyData = await api_me.meApi.privacy();
        privacy.value = {
          profileVisible: Number((privacyData == null ? void 0 : privacyData.profileVisible) ?? 1),
          statsVisible: Number((privacyData == null ? void 0 : privacyData.statsVisible) ?? 1),
          personalizedRecommend: Number((privacyData == null ? void 0 : privacyData.personalizedRecommend) ?? 1),
          updatedAt: privacyData == null ? void 0 : privacyData.updatedAt
        };
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "设置加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function doSavePrivacy() {
      savingPrivacy.value = true;
      try {
        const payload = {
          profileVisible: Number(privacy.value.profileVisible || 0) === 1 ? 1 : 0,
          statsVisible: Number(privacy.value.statsVisible || 0) === 1 ? 1 : 0,
          personalizedRecommend: Number(privacy.value.personalizedRecommend || 0) === 1 ? 1 : 0
        };
        await api_me.meApi.updatePrivacy(payload);
        common_vendor.index.showToast({ title: "隐私设置已保存", icon: "success" });
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "保存失败", icon: "none" });
      } finally {
        savingPrivacy.value = false;
      }
    }
    function savePrivacy() {
      if (savingPrivacy.value)
        return;
      common_vendor.index.showModal({
        title: "确认保存",
        content: "是否确认保存当前隐私设置？",
        success: async (res) => {
          if (!res.confirm)
            return;
          await doSavePrivacy();
        }
      });
    }
    function cancelAccount() {
      if (canceling.value)
        return;
      common_vendor.index.showModal({
        title: "确认注销",
        content: "注销后账号将立即失效，且无法登录，是否继续？",
        confirmColor: "#d86c6c",
        success: async (res) => {
          if (!res.confirm)
            return;
          canceling.value = true;
          try {
            await api_me.meApi.submitCancelRequest({});
            common_vendor.index.showToast({ title: "账号已注销", icon: "success" });
            authStore.logout();
            setTimeout(() => {
              common_vendor.index.reLaunch({ url: "/pages/home/index" });
            }, 250);
          } catch (err) {
            common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "注销失败", icon: "none" });
          } finally {
            canceling.value = false;
          }
        }
      });
    }
    common_vendor.onShow(() => {
      if (!utils_authGuard.ensurePageAuth())
        return;
      loadData();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: toSwitch(privacy.value.profileVisible),
        b: common_vendor.o((e) => {
          var _a;
          return updateSwitch("profileVisible", !!((_a = e == null ? void 0 : e.detail) == null ? void 0 : _a.value));
        }),
        c: toSwitch(privacy.value.statsVisible),
        d: common_vendor.o((e) => {
          var _a;
          return updateSwitch("statsVisible", !!((_a = e == null ? void 0 : e.detail) == null ? void 0 : _a.value));
        }),
        e: toSwitch(privacy.value.personalizedRecommend),
        f: common_vendor.o((e) => {
          var _a;
          return updateSwitch("personalizedRecommend", !!((_a = e == null ? void 0 : e.detail) == null ? void 0 : _a.value));
        }),
        g: savingPrivacy.value,
        h: common_vendor.o(savePrivacy),
        i: canceling.value,
        j: common_vendor.o(cancelAccount),
        k: loading.value
      }, loading.value ? {} : {});
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-3f12c944"]]);
wx.createPage(MiniProgramPage);
