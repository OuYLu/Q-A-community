"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const utils_constants = require("../../utils/constants.js");
const stores_auth = require("../../stores/auth.js");
const api_question = require("../../api/question.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "answer",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const questionId = common_vendor.ref(0);
    const questionTitle = common_vendor.ref("");
    const content = common_vendor.ref("");
    const imageUrls = common_vendor.ref([]);
    const uploading = common_vendor.ref(false);
    const publishing = common_vendor.ref(false);
    const isAnonymous = common_vendor.ref(false);
    const contentLength = common_vendor.computed(() => content.value.length);
    const canPublish = common_vendor.computed(() => !!content.value.trim() && !publishing.value);
    common_vendor.onLoad((options) => {
      questionId.value = Number((options == null ? void 0 : options.questionId) || 0);
      questionTitle.value = decodeURIComponent(String((options == null ? void 0 : options.title) || "")).trim();
    });
    common_vendor.onShow(() => {
      utils_authGuard.ensurePageAuth();
    });
    async function chooseImages() {
      const remain = 9 - imageUrls.value.length;
      if (remain <= 0) {
        common_vendor.index.showToast({ title: "最多上传9张图片", icon: "none" });
        return;
      }
      try {
        const choose = await common_vendor.index.chooseImage({
          count: remain,
          sizeType: ["compressed"],
          sourceType: ["album", "camera"]
        });
        const paths = choose.tempFilePaths || [];
        if (!paths.length)
          return;
        uploading.value = true;
        for (const filePath of paths) {
          const url = await uploadAnswerImage(filePath);
          imageUrls.value.push(url);
          if (imageUrls.value.length >= 9)
            break;
        }
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "图片上传失败", icon: "none" });
      } finally {
        uploading.value = false;
      }
    }
    function removeImage(index) {
      imageUrls.value.splice(index, 1);
    }
    async function uploadAnswerImage(filePath) {
      var _a;
      const upload = await common_vendor.index.uploadFile({
        url: `${utils_constants.BASE_URL}/api/common/upload?bizType=answer`,
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
      const url = (_a = body == null ? void 0 : body.data) == null ? void 0 : _a.url;
      if (!url) {
        throw new Error((body == null ? void 0 : body.message) || (body == null ? void 0 : body.msg) || "上传失败");
      }
      const text = String(url);
      if (text.startsWith("http://") || text.startsWith("https://"))
        return text;
      if (text.startsWith("/"))
        return `${utils_constants.BASE_URL}${text}`;
      return `${utils_constants.BASE_URL}/${text}`;
    }
    async function submitAnswer() {
      if (!questionId.value) {
        common_vendor.index.showToast({ title: "参数错误", icon: "none" });
        return;
      }
      if (!content.value.trim()) {
        common_vendor.index.showToast({ title: "请输入回答内容", icon: "none" });
        return;
      }
      if (publishing.value)
        return;
      publishing.value = true;
      try {
        await api_question.questionApi.createAnswer(questionId.value, {
          content: content.value.trim(),
          imageUrls: imageUrls.value.length ? imageUrls.value : void 0,
          isAnonymous: isAnonymous.value ? 1 : 0
        });
        common_vendor.index.showToast({ title: "回答已发布", icon: "success" });
        setTimeout(() => {
          common_vendor.index.navigateBack({
            delta: 1,
            fail: () => {
              common_vendor.index.redirectTo({ url: `/pages/question/detail?id=${questionId.value}` });
            }
          });
        }, 250);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "发布失败", icon: "none" });
      } finally {
        publishing.value = false;
      }
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(questionTitle.value),
        b: common_vendor.t(publishing.value ? "发布中" : "发布"),
        c: !canPublish.value ? 1 : "",
        d: common_vendor.o(submitAnswer),
        e: content.value,
        f: common_vendor.o(($event) => content.value = $event.detail.value),
        g: imageUrls.value.length
      }, imageUrls.value.length ? {
        h: common_vendor.f(imageUrls.value, (url, idx, i0) => {
          return {
            a: url,
            b: common_vendor.o(($event) => removeImage(idx), url + idx),
            c: url + idx
          };
        })
      } : {}, {
        i: uploading.value ? "/static/tabbar/image-active.png" : "/static/tabbar/image.png",
        j: common_vendor.o(chooseImages),
        k: common_vendor.t(isAnonymous.value ? "✓" : ""),
        l: common_vendor.t(contentLength.value),
        m: common_vendor.o(($event) => isAnonymous.value = !isAnonymous.value)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-5de4d0c5"]]);
wx.createPage(MiniProgramPage);
