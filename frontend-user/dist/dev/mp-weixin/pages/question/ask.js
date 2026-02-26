"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const api_discover = require("../../api/discover.js");
const api_question = require("../../api/question.js");
const utils_constants = require("../../utils/constants.js");
const stores_auth = require("../../stores/auth.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "ask",
  setup(__props) {
    const title = common_vendor.ref("");
    const categoryId = common_vendor.ref(null);
    const categoryName = common_vendor.ref("");
    const content = common_vendor.ref("");
    const tagsInput = common_vendor.ref("");
    const imageUrls = common_vendor.ref([]);
    const uploadingImages = common_vendor.ref(false);
    const categoryLoading = common_vendor.ref(false);
    const categoryTree = common_vendor.ref([]);
    const showCategoryPanel = common_vendor.ref(false);
    const selectedRootId = common_vendor.ref(null);
    const childCategoryMap = common_vendor.ref({});
    const categorySearch = common_vendor.ref("");
    const categorySearchLoading = common_vendor.ref(false);
    const authStore = stores_auth.useAuthStore();
    const categoryPlaceholder = common_vendor.computed(() => {
      if (categoryLoading.value)
        return "分类加载中...";
      return categoryName.value || "例如：睡眠调理";
    });
    const selectedRoot = common_vendor.computed(() => categoryTree.value.find((x) => x.id === selectedRootId.value) || null);
    const normalizedSearch = common_vendor.computed(() => categorySearch.value.trim().toLowerCase());
    const currentChildren = common_vendor.computed(() => {
      const children = childCategoryMap.value[selectedRootId.value || -1] || [];
      if (!normalizedSearch.value)
        return children;
      return children.filter((x) => getCategoryText(x).toLowerCase().includes(normalizedSearch.value));
    });
    const filteredRoots = common_vendor.computed(() => {
      if (!normalizedSearch.value)
        return categoryTree.value;
      return categoryTree.value.filter((root) => {
        const rootHit = getCategoryText(root).toLowerCase().includes(normalizedSearch.value);
        const children = childCategoryMap.value[root.id] || root.children || [];
        const childHit = children.some((x) => getCategoryText(x).toLowerCase().includes(normalizedSearch.value));
        return rootHit || childHit;
      });
    });
    const searchMatches = common_vendor.computed(() => {
      if (!normalizedSearch.value)
        return [];
      const result = [];
      const seen = /* @__PURE__ */ new Set();
      for (const root of categoryTree.value) {
        const children = childCategoryMap.value[root.id] || root.children || [];
        const rootHit = getCategoryText(root).toLowerCase().includes(normalizedSearch.value);
        if (rootHit) {
          const rootKey = `root-${root.id}`;
          if (!seen.has(rootKey)) {
            result.push({ node: root });
            seen.add(rootKey);
          }
          for (const child of children) {
            const childKey = `child-${root.id}-${child.id}`;
            if (!seen.has(childKey)) {
              result.push({ node: child, parent: root });
              seen.add(childKey);
            }
          }
          continue;
        }
        for (const child of children) {
          if (getCategoryText(child).toLowerCase().includes(normalizedSearch.value)) {
            const childKey = `child-${root.id}-${child.id}`;
            if (seen.has(childKey))
              continue;
            result.push({ node: child, parent: root });
            seen.add(childKey);
          }
        }
      }
      return result;
    });
    common_vendor.onShow(() => {
      utils_authGuard.ensurePageAuth();
    });
    async function submit() {
      if (!title.value.trim()) {
        common_vendor.index.showToast({ title: "请输入问题标题", icon: "none" });
        return;
      }
      if (!categoryName.value.trim()) {
        common_vendor.index.showToast({ title: "请选择问题分类", icon: "none" });
        return;
      }
      const tags = tagsInput.value.split(/[，, ]+/).map((x) => x.trim()).filter(Boolean).slice(0, 5);
      try {
        const questionId = await api_question.questionApi.createQuestion({
          title: title.value.trim(),
          content: content.value.trim() || void 0,
          categoryId: categoryId.value || void 0,
          tagNames: tags.length ? tags : void 0,
          imageUrls: imageUrls.value.length ? imageUrls.value : void 0
        });
        common_vendor.index.showToast({ title: "提问成功", icon: "success" });
        setTimeout(() => {
          common_vendor.index.redirectTo({
            url: `/pages/question/detail?id=${questionId}`
          });
        }, 250);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "发布失败", icon: "none" });
      }
    }
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
        uploadingImages.value = true;
        for (const filePath of paths) {
          const url = await uploadQuestionImage(filePath);
          imageUrls.value.push(url);
          if (imageUrls.value.length >= 9)
            break;
        }
        common_vendor.index.showToast({ title: "图片上传成功", icon: "success" });
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "图片上传失败", icon: "none" });
      } finally {
        uploadingImages.value = false;
      }
    }
    async function uploadQuestionImage(filePath) {
      var _a;
      const upload = await common_vendor.index.uploadFile({
        url: `${utils_constants.BASE_URL}/api/common/upload?bizType=question`,
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
      if (text.startsWith("http://") || text.startsWith("https://")) {
        return text;
      }
      if (text.startsWith("/")) {
        return `${utils_constants.BASE_URL}${text}`;
      }
      return `${utils_constants.BASE_URL}/${text}`;
    }
    function removeImage(index) {
      imageUrls.value.splice(index, 1);
    }
    async function fetchCategoryTree(parentId) {
      return api_discover.discoverApi.getCategoryTree(parentId);
    }
    function hasChildren(node) {
      return Array.isArray(node.children) && node.children.length > 0;
    }
    function getCategoryText(node) {
      return node.name || node.label || "";
    }
    async function ensureRootCategories() {
      if (categoryTree.value.length)
        return;
      categoryLoading.value = true;
      try {
        categoryTree.value = await fetchCategoryTree();
      } finally {
        categoryLoading.value = false;
      }
    }
    function applyCategory(node, parent) {
      categoryId.value = node.id;
      const currentText = getCategoryText(node);
      const parentText = parent ? getCategoryText(parent) : "";
      categoryName.value = parentText ? `${parentText} / ${currentText}` : currentText;
      showCategoryPanel.value = false;
    }
    async function loadChildren(root) {
      if (hasChildren(root)) {
        childCategoryMap.value[root.id] = root.children || [];
        return;
      }
      if (childCategoryMap.value[root.id])
        return;
      try {
        const children = await fetchCategoryTree(root.id);
        childCategoryMap.value[root.id] = children || [];
      } catch {
        childCategoryMap.value[root.id] = [];
      }
    }
    async function preloadChildrenForSearch() {
      if (!normalizedSearch.value || categorySearchLoading.value)
        return;
      categorySearchLoading.value = true;
      try {
        for (const root of categoryTree.value) {
          if (!childCategoryMap.value[root.id]) {
            await loadChildren(root);
          }
        }
      } finally {
        categorySearchLoading.value = false;
      }
    }
    async function chooseCategory() {
      try {
        await ensureRootCategories();
        if (!categoryTree.value.length) {
          common_vendor.index.showToast({ title: "暂无可选分类", icon: "none" });
          return;
        }
        showCategoryPanel.value = true;
        if (selectedRootId.value === null && categoryTree.value.length) {
          selectedRootId.value = categoryTree.value[0].id;
          await loadChildren(categoryTree.value[0]);
        }
      } catch {
        common_vendor.index.showToast({ title: "分类加载失败", icon: "none" });
      }
    }
    async function toggleCategoryPanel() {
      if (showCategoryPanel.value) {
        closeCategoryPanel();
        return;
      }
      await chooseCategory();
    }
    async function selectRoot(node) {
      selectedRootId.value = node.id;
      await loadChildren(node);
      const children = childCategoryMap.value[node.id] || [];
      if (!children.length) {
        applyCategory(node);
      }
    }
    function selectChild(node, parent) {
      applyCategory(node, parent);
    }
    function closeCategoryPanel() {
      showCategoryPanel.value = false;
      categorySearch.value = "";
    }
    common_vendor.watch(categorySearch, () => {
      if (normalizedSearch.value) {
        preloadChildrenForSearch();
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: title.value,
        b: common_vendor.o(($event) => title.value = $event.detail.value),
        c: common_vendor.t(title.value.length),
        d: common_vendor.t(categoryPlaceholder.value),
        e: !categoryName.value ? 1 : "",
        f: common_vendor.n({
          up: showCategoryPanel.value
        }),
        g: common_vendor.o(toggleCategoryPanel),
        h: showCategoryPanel.value
      }, showCategoryPanel.value ? common_vendor.e({
        i: categorySearch.value,
        j: common_vendor.o(($event) => categorySearch.value = $event.detail.value),
        k: normalizedSearch.value
      }, normalizedSearch.value ? common_vendor.e({
        l: common_vendor.f(searchMatches.value, (item, k0, i0) => {
          var _a, _b;
          return {
            a: common_vendor.t(item.parent ? `${getCategoryText(item.parent)} / ${getCategoryText(item.node)}` : getCategoryText(item.node)),
            b: `${((_a = item.parent) == null ? void 0 : _a.id) || "root"}-${item.node.id}`,
            c: common_vendor.o(($event) => applyCategory(item.node, item.parent), `${((_b = item.parent) == null ? void 0 : _b.id) || "root"}-${item.node.id}`)
          };
        }),
        m: !searchMatches.value.length && !categorySearchLoading.value
      }, !searchMatches.value.length && !categorySearchLoading.value ? {} : {}, {
        n: categorySearchLoading.value
      }, categorySearchLoading.value ? {} : {}) : common_vendor.e({
        o: common_vendor.f(filteredRoots.value, (root, k0, i0) => {
          return {
            a: common_vendor.t(getCategoryText(root)),
            b: root.id,
            c: common_vendor.n({
              active: selectedRootId.value === root.id
            }),
            d: common_vendor.o(($event) => selectRoot(root), root.id)
          };
        }),
        p: common_vendor.f(currentChildren.value, (child, k0, i0) => {
          return {
            a: common_vendor.t(getCategoryText(child)),
            b: child.id,
            c: common_vendor.o(($event) => selectedRoot.value && selectChild(child, selectedRoot.value), child.id)
          };
        }),
        q: !currentChildren.value.length
      }, !currentChildren.value.length ? {} : {})) : {}, {
        r: content.value,
        s: common_vendor.o(($event) => content.value = $event.detail.value),
        t: common_vendor.t(content.value.length),
        v: common_vendor.f(imageUrls.value, (url, idx, i0) => {
          return {
            a: url,
            b: common_vendor.o(($event) => removeImage(idx), url + idx),
            c: url + idx
          };
        }),
        w: uploadingImages.value
      }, uploadingImages.value ? {} : {}, {
        x: common_vendor.o(chooseImages),
        y: tagsInput.value,
        z: common_vendor.o(($event) => tagsInput.value = $event.detail.value),
        A: common_vendor.o(submit)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-7aad2763"]]);
wx.createPage(MiniProgramPage);
