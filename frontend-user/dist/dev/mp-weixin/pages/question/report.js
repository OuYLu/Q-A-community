"use strict";
const common_vendor = require("../../common/vendor.js");
const api_question = require("../../api/question.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "report",
  setup(__props) {
    const targetType = common_vendor.ref("question");
    const targetId = common_vendor.ref(0);
    const targetTitle = common_vendor.ref("");
    const reasonCode = common_vendor.ref("");
    const reasonDetail = common_vendor.ref("");
    const submitting = common_vendor.ref(false);
    const reasons = [
      { code: "illegal", label: "违法违规" },
      { code: "porn", label: "低俗色情" },
      { code: "abuse", label: "辱骂攻击" },
      { code: "ad", label: "广告营销" },
      { code: "privacy", label: "隐私泄露" },
      { code: "other", label: "其他" }
    ];
    const targetLabel = common_vendor.computed(() => targetType.value === "answer" ? "回答" : "问题");
    const fallbackTitle = common_vendor.computed(() => `${targetLabel.value} #${targetId.value}`);
    const canSubmit = common_vendor.computed(() => !!targetId.value && !!reasonCode.value && !submitting.value);
    async function submitReport() {
      if (!canSubmit.value) {
        common_vendor.index.showToast({ title: "请选择举报原因", icon: "none" });
        return;
      }
      submitting.value = true;
      try {
        const payload = {
          reasonCode: reasonCode.value,
          reasonDetail: reasonDetail.value.trim() || void 0
        };
        if (targetType.value === "answer") {
          await api_question.questionApi.reportAnswer(targetId.value, payload);
        } else {
          await api_question.questionApi.reportQuestion(targetId.value, payload);
        }
        common_vendor.index.showToast({ title: "举报已提交", icon: "success" });
        setTimeout(() => common_vendor.index.navigateBack(), 220);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "举报失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    }
    common_vendor.onLoad((options) => {
      targetType.value = (options == null ? void 0 : options.targetType) === "answer" ? "answer" : "question";
      targetId.value = Number((options == null ? void 0 : options.questionId) || (options == null ? void 0 : options.answerId) || (options == null ? void 0 : options.id) || 0);
      targetTitle.value = decodeURIComponent(String((options == null ? void 0 : options.title) || ""));
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(targetLabel.value),
        b: common_vendor.t(targetTitle.value || fallbackTitle.value),
        c: common_vendor.f(reasons, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.label),
            b: item.code,
            c: reasonCode.value === item.code ? 1 : "",
            d: common_vendor.o(($event) => reasonCode.value = item.code, item.code)
          };
        }),
        d: reasonDetail.value,
        e: common_vendor.o(($event) => reasonDetail.value = $event.detail.value),
        f: common_vendor.t(reasonDetail.value.length),
        g: common_vendor.t(submitting.value ? "提交中..." : "提交举报"),
        h: !canSubmit.value,
        i: common_vendor.o(submitReport)
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a3cc35ab"]]);
wx.createPage(MiniProgramPage);
