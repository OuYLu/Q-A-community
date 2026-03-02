"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const stores_auth = require("../../stores/auth.js");
const api_expert = require("../../api/expert.js");
const utils_constants = require("../../utils/constants.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "expert-apply",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const realName = common_vendor.ref("");
    const organization = common_vendor.ref("");
    const title = common_vendor.ref("");
    const expertise = common_vendor.ref("");
    const licenseFiles = common_vendor.ref([]);
    const employmentFiles = common_vendor.ref([]);
    const titleFiles = common_vendor.ref([]);
    const educationFiles = common_vendor.ref([]);
    const otherFiles = common_vendor.ref([]);
    const submitting = common_vendor.ref(false);
    const uploading = common_vendor.ref(false);
    const canSubmit = common_vendor.computed(() => {
      return !!realName.value.trim() && !!expertise.value.trim() && licenseFiles.value.length > 0 && employmentFiles.value.length > 0 && !submitting.value;
    });
    common_vendor.onShow(() => {
      utils_authGuard.ensurePageAuth();
    });
    function filesByType(type) {
      if (type === "license")
        return licenseFiles.value;
      if (type === "employment")
        return employmentFiles.value;
      if (type === "title")
        return titleFiles.value;
      if (type === "education")
        return educationFiles.value;
      return otherFiles.value;
    }
    async function pickProof(type) {
      const current = filesByType(type);
      const remain = 9 - current.length;
      if (remain <= 0) {
        common_vendor.index.showToast({ title: "最多上传9张", icon: "none" });
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
          const file = await uploadProof(filePath);
          current.push(file);
          if (current.length >= 9)
            break;
        }
        common_vendor.index.showToast({ title: "上传成功", icon: "success" });
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "上传失败", icon: "none" });
      } finally {
        uploading.value = false;
      }
    }
    function removeProof(type, index) {
      const current = filesByType(type);
      current.splice(index, 1);
    }
    async function uploadProof(filePath) {
      const upload = await common_vendor.index.uploadFile({
        url: `${utils_constants.BASE_URL}/api/common/upload?bizType=expert-proof`,
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
      const data = (body == null ? void 0 : body.data) || {};
      const url = String(data.url || "");
      if (!url) {
        throw new Error((body == null ? void 0 : body.message) || (body == null ? void 0 : body.msg) || "上传失败");
      }
      const fullUrl = url.startsWith("http://") || url.startsWith("https://") ? url : url.startsWith("/") ? `${utils_constants.BASE_URL}${url}` : `${utils_constants.BASE_URL}/${url}`;
      return {
        url: fullUrl,
        name: String(data.originalName || data.filename || "proof"),
        size: Number(data.size || 0),
        mime: String(data.contentType || "image/jpeg")
      };
    }
    async function submit() {
      if (!canSubmit.value) {
        common_vendor.index.showToast({ title: "请补齐必填项", icon: "none" });
        return;
      }
      submitting.value = true;
      try {
        await api_expert.expertApi.apply({
          realName: realName.value.trim(),
          organization: organization.value.trim() || void 0,
          title: title.value.trim() || void 0,
          expertise: expertise.value.trim(),
          proofUrls: {
            LICENSE: licenseFiles.value,
            EMPLOYMENT: employmentFiles.value,
            TITLE: titleFiles.value.length ? titleFiles.value : void 0,
            EDUCATION: educationFiles.value.length ? educationFiles.value : void 0,
            OTHER: otherFiles.value.length ? otherFiles.value : void 0
          }
        });
        common_vendor.index.showToast({ title: "申请已提交", icon: "success" });
        setTimeout(() => {
          common_vendor.index.navigateBack();
        }, 280);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "提交失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    }
    return (_ctx, _cache) => {
      return {
        a: realName.value,
        b: common_vendor.o(($event) => realName.value = $event.detail.value),
        c: organization.value,
        d: common_vendor.o(($event) => organization.value = $event.detail.value),
        e: title.value,
        f: common_vendor.o(($event) => title.value = $event.detail.value),
        g: expertise.value,
        h: common_vendor.o(($event) => expertise.value = $event.detail.value),
        i: common_vendor.f(licenseFiles.value, (item, idx, i0) => {
          return {
            a: item.url,
            b: common_vendor.o(($event) => removeProof("license", idx), item.url + idx),
            c: item.url + idx
          };
        }),
        j: common_vendor.t(uploading.value ? "上传中..." : "+ 上传"),
        k: common_vendor.o(($event) => pickProof("license")),
        l: common_vendor.f(employmentFiles.value, (item, idx, i0) => {
          return {
            a: item.url,
            b: common_vendor.o(($event) => removeProof("employment", idx), item.url + idx),
            c: item.url + idx
          };
        }),
        m: common_vendor.t(uploading.value ? "上传中..." : "+ 上传"),
        n: common_vendor.o(($event) => pickProof("employment")),
        o: common_vendor.f(titleFiles.value, (item, idx, i0) => {
          return {
            a: item.url,
            b: common_vendor.o(($event) => removeProof("title", idx), item.url + idx),
            c: item.url + idx
          };
        }),
        p: common_vendor.t(uploading.value ? "上传中..." : "+ 上传"),
        q: common_vendor.o(($event) => pickProof("title")),
        r: common_vendor.f(educationFiles.value, (item, idx, i0) => {
          return {
            a: item.url,
            b: common_vendor.o(($event) => removeProof("education", idx), item.url + idx),
            c: item.url + idx
          };
        }),
        s: common_vendor.t(uploading.value ? "上传中..." : "+ 上传"),
        t: common_vendor.o(($event) => pickProof("education")),
        v: common_vendor.f(otherFiles.value, (item, idx, i0) => {
          return {
            a: item.url,
            b: common_vendor.o(($event) => removeProof("other", idx), item.url + idx),
            c: item.url + idx
          };
        }),
        w: common_vendor.t(uploading.value ? "上传中..." : "+ 上传"),
        x: common_vendor.o(($event) => pickProof("other")),
        y: common_vendor.t(submitting.value ? "提交中..." : "提交申请"),
        z: !canSubmit.value ? 1 : "",
        A: common_vendor.o(submit)
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2e121e50"]]);
wx.createPage(MiniProgramPage);
