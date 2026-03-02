"use strict";
const common_vendor = require("../../common/vendor.js");
const api_notification = require("../../api/notification.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "report-feedback",
  setup(__props) {
    const loading = common_vendor.ref(true);
    const detail = common_vendor.ref(null);
    async function loadDetail(id) {
      loading.value = true;
      try {
        detail.value = await api_notification.notificationApi.reportFeedbackDetail(id);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function bizTypeText(type) {
      const map = {
        1: "问题",
        2: "回答",
        3: "评论",
        4: "科普"
      };
      return type ? map[type] || "内容" : "内容";
    }
    function actionText(action) {
      const map = {
        1: "下架",
        2: "警告",
        3: "封禁",
        4: "不处理"
      };
      return action ? map[action] || "已处理" : "待处理";
    }
    common_vendor.onLoad((options) => {
      const id = Number((options == null ? void 0 : options.id) || 0);
      if (!id) {
        loading.value = false;
        common_vendor.index.showToast({ title: "参数错误", icon: "none" });
        return;
      }
      loadDetail(id);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : !detail.value ? {} : {
        c: common_vendor.t(bizTypeText(detail.value.bizType)),
        d: common_vendor.t(detail.value.contentTitle || "-"),
        e: common_vendor.t(actionText(detail.value.handleAction)),
        f: common_vendor.t(detail.value.handlerName || "-"),
        g: common_vendor.t(detail.value.handleResult || "暂无处理结果"),
        h: common_vendor.t(detail.value.createdAt || "-"),
        i: common_vendor.t(detail.value.handledAt || "待处理")
      }, {
        b: !detail.value
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-93447804"]]);
wx.createPage(MiniProgramPage);
