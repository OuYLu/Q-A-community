"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/home/index.js";
  "./pages/search/index.js";
  "./pages/discover/index.js";
  "./pages/notice/index.js";
  "./pages/mine/index.js";
  "./pages/question/detail.js";
  "./pages/question/ask.js";
  "./pages/question/answer.js";
  "./pages/question/answer-detail.js";
  "./pages/auth/login.js";
  "./pages/auth/register.js";
  "./pages/auth/wechat-authorize.js";
  "./pages/auth/wechat-bind-phone.js";
  "./pages/mine/list.js";
  "./pages/mine/doc.js";
  "./pages/mine/edit-profile.js";
  "./pages/mine/password.js";
}
const _sfc_main = {
  onLaunch() {
  },
  onShow() {
  },
  onHide() {
  }
};
function createApp() {
  const app = common_vendor.createSSRApp(_sfc_main);
  const pinia = common_vendor.createPinia();
  app.use(pinia);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
