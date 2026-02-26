"use strict";
const common_vendor = require("../../common/vendor.js");
const api_me = require("../../api/me.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "doc",
  setup(__props) {
    const title = common_vendor.ref("文档");
    const content = common_vendor.ref("");
    const loading = common_vendor.ref(false);
    const titleMap = {
      settings: "设置",
      help: "帮助与反馈",
      "user-agreement": "用户协议",
      "privacy-policy": "隐私政策"
    };
    common_vendor.onLoad(async (options) => {
      if (!utils_authGuard.ensurePageAuth())
        return;
      const type = (options == null ? void 0 : options.type) || "help";
      loading.value = true;
      try {
        const doc = await api_me.meApi.doc(type);
        title.value = doc.title || titleMap[type] || "文档";
        content.value = doc.content || "暂无内容";
        common_vendor.index.setNavigationBarTitle({ title: title.value });
      } catch (err) {
        common_vendor.index.showToast({ title: "文档加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(title.value),
        b: loading.value
      }, loading.value ? {} : {
        c: common_vendor.t(content.value)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2aba08ea"]]);
wx.createPage(MiniProgramPage);
