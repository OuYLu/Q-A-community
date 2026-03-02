"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const stores_auth = require("../../stores/auth.js");
const api_expert = require("../../api/expert.js");
const api_me = require("../../api/me.js");
const utils_constants = require("../../utils/constants.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "post-create",
  setup(__props) {
    const vm = common_vendor.getCurrentInstance();
    const authStore = stores_auth.useAuthStore();
    const title = common_vendor.ref("");
    const categoryId = common_vendor.ref(null);
    const categoryName = common_vendor.ref("");
    const categories = common_vendor.ref([]);
    const tagsInput = common_vendor.ref("");
    const coverImage = common_vendor.ref("");
    const summary = common_vendor.ref("");
    const checkingRole = common_vendor.ref(false);
    const submitting = common_vendor.ref(false);
    const uploading = common_vendor.ref(false);
    const editorCtx = common_vendor.ref(null);
    const editorTextLen = common_vendor.ref(0);
    const canSubmit = common_vendor.computed(() => !!title.value.trim() && !!categoryId.value && !checkingRole.value && !submitting.value);
    common_vendor.onLoad(async () => {
      await initPage();
    });
    common_vendor.onShow(async () => {
      await initPage();
    });
    async function initPage() {
      if (!utils_authGuard.ensurePageAuth())
        return;
      await Promise.all([guardExpertRole(), loadCategories()]);
    }
    async function guardExpertRole() {
      checkingRole.value = true;
      try {
        const info = await api_me.meApi.overview();
        if (info.expertStatus !== 3) {
          common_vendor.index.showToast({ title: "仅认证专家可发科普帖", icon: "none" });
          setTimeout(() => common_vendor.index.navigateBack(), 260);
        }
      } catch {
        common_vendor.index.showToast({ title: "身份校验失败", icon: "none" });
        setTimeout(() => common_vendor.index.navigateBack(), 260);
      } finally {
        checkingRole.value = false;
      }
    }
    async function loadCategories() {
      try {
        const list = await api_expert.expertApi.categories();
        categories.value = (list || []).map((x) => ({ id: x.id, name: x.name })).filter((x) => !!x.name);
      } catch {
        categories.value = [];
      }
    }
    function chooseCategory() {
      if (!categories.value.length) {
        common_vendor.index.showToast({ title: "分类加载中，请稍后", icon: "none" });
        return;
      }
      common_vendor.index.showActionSheet({
        itemList: categories.value.map((x) => x.name),
        success: (res) => {
          const selected = categories.value[res.tapIndex];
          if (!selected)
            return;
          categoryId.value = selected.id;
          categoryName.value = selected.name;
        }
      });
    }
    function onEditorReady() {
      common_vendor.index.createSelectorQuery().in(vm == null ? void 0 : vm.proxy).select("#postEditor").context((res) => {
        editorCtx.value = (res == null ? void 0 : res.context) || null;
      }).exec();
    }
    function onEditorInput(e) {
      var _a;
      const text = ((_a = e == null ? void 0 : e.detail) == null ? void 0 : _a.text) || "";
      editorTextLen.value = String(text).trim().length;
    }
    async function insertImageToEditor() {
      var _a;
      if (!editorCtx.value) {
        common_vendor.index.showToast({ title: "编辑器未就绪", icon: "none" });
        return;
      }
      try {
        const choose = await common_vendor.index.chooseImage({
          count: 1,
          sizeType: ["compressed"],
          sourceType: ["album", "camera"]
        });
        const filePath = (_a = choose.tempFilePaths) == null ? void 0 : _a[0];
        if (!filePath)
          return;
        uploading.value = true;
        const url = await uploadExpertPostImage(filePath);
        editorCtx.value.insertImage({ src: url });
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "插图失败", icon: "none" });
      } finally {
        uploading.value = false;
      }
    }
    async function chooseCover() {
      var _a;
      try {
        const choose = await common_vendor.index.chooseImage({
          count: 1,
          sizeType: ["compressed"],
          sourceType: ["album", "camera"]
        });
        const filePath = (_a = choose.tempFilePaths) == null ? void 0 : _a[0];
        if (!filePath)
          return;
        uploading.value = true;
        coverImage.value = await uploadExpertPostImage(filePath);
        common_vendor.index.showToast({ title: "封面上传成功", icon: "success" });
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "封面上传失败", icon: "none" });
      } finally {
        uploading.value = false;
      }
    }
    async function uploadExpertPostImage(filePath) {
      var _a;
      const upload = await common_vendor.index.uploadFile({
        url: `${utils_constants.BASE_URL}/api/common/upload?bizType=expert-post`,
        filePath,
        name: "file",
        header: authStore.token ? { Authorization: `Bearer ${authStore.token}` } : void 0
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
    function getEditorContents() {
      return new Promise((resolve, reject) => {
        if (!editorCtx.value) {
          reject(new Error("编辑器未就绪"));
          return;
        }
        editorCtx.value.getContents({
          success: resolve,
          fail: reject
        });
      });
    }
    function buildBlocksFromDelta(ops) {
      var _a;
      const blocks = [];
      for (const op of ops || []) {
        if (typeof (op == null ? void 0 : op.insert) === "string") {
          const text = op.insert.replace(/\n+/g, "\n").trim();
          if (!text)
            continue;
          blocks.push({ type: "text", text });
          continue;
        }
        if ((_a = op == null ? void 0 : op.insert) == null ? void 0 : _a.image) {
          blocks.push({ type: "image", url: String(op.insert.image) });
        }
      }
      return blocks;
    }
    function normalizeTagNames() {
      return tagsInput.value.split(/[，, ]+/).map((x) => x.trim()).filter(Boolean).map((x) => x.startsWith("#") ? x.slice(1) : x).filter(Boolean).slice(0, 10);
    }
    async function submit() {
      var _a;
      if (!canSubmit.value) {
        common_vendor.index.showToast({ title: "请先填写标题和分类", icon: "none" });
        return;
      }
      let contents;
      try {
        contents = await getEditorContents();
      } catch {
        common_vendor.index.showToast({ title: "正文编辑器未就绪", icon: "none" });
        return;
      }
      const blocks = buildBlocksFromDelta(((_a = contents == null ? void 0 : contents.delta) == null ? void 0 : _a.ops) || []);
      if (!blocks.length) {
        common_vendor.index.showToast({ title: "请填写正文", icon: "none" });
        return;
      }
      const html = String((contents == null ? void 0 : contents.html) || "");
      const text = String((contents == null ? void 0 : contents.text) || "").trim();
      const imageUrls = blocks.filter((x) => x.type === "image" && x.url).map((x) => x.url);
      submitting.value = true;
      try {
        await api_expert.expertApi.createPost({
          title: title.value.trim(),
          categoryId: categoryId.value,
          summary: summary.value.trim() || text.slice(0, 120) || void 0,
          content: html || text || void 0,
          contentBlocks: blocks,
          tagNames: normalizeTagNames(),
          coverImage: coverImage.value || imageUrls[0] || void 0,
          imageUrls: imageUrls.length ? imageUrls : void 0
        });
        common_vendor.index.showToast({ title: "科普帖发布成功", icon: "success" });
        setTimeout(() => common_vendor.index.navigateBack(), 260);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "发布失败", icon: "none" });
      } finally {
        submitting.value = false;
      }
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: title.value,
        b: common_vendor.o(($event) => title.value = $event.detail.value),
        c: common_vendor.t(title.value.length),
        d: common_vendor.t(categoryName.value || "请选择分类"),
        e: !categoryName.value ? 1 : "",
        f: common_vendor.o(chooseCategory),
        g: summary.value,
        h: common_vendor.o(($event) => summary.value = $event.detail.value),
        i: common_vendor.t(summary.value.length),
        j: common_vendor.o(onEditorReady),
        k: common_vendor.o(onEditorInput),
        l: common_vendor.t(uploading.value ? "上传中..." : "相册"),
        m: common_vendor.o(insertImageToEditor),
        n: common_vendor.t(editorTextLen.value),
        o: tagsInput.value,
        p: common_vendor.o(($event) => tagsInput.value = $event.detail.value),
        q: coverImage.value
      }, coverImage.value ? {
        r: coverImage.value,
        s: common_vendor.o(($event) => coverImage.value = "")
      } : {
        t: common_vendor.o(chooseCover)
      }, {
        v: common_vendor.t(submitting.value ? "发布中..." : "发布科普帖"),
        w: !canSubmit.value ? 1 : "",
        x: common_vendor.o(submit)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-7e83fed9"]]);
wx.createPage(MiniProgramPage);
