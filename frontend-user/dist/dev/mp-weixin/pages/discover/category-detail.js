"use strict";
const common_vendor = require("../../common/vendor.js");
const api_discover = require("../../api/discover.js");
const api_expert = require("../../api/expert.js");
const api_question = require("../../api/question.js");
const utils_nav = require("../../utils/nav.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "category-detail",
  setup(__props) {
    const rootCategoryId = common_vendor.ref(0);
    const rootCategoryName = common_vendor.ref("分类");
    const categoryType = common_vendor.ref("qa");
    const childCategories = common_vendor.ref([]);
    const activeCategoryId = common_vendor.ref(0);
    const questions = common_vendor.ref([]);
    const kbPosts = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    async function loadChildren() {
      if (categoryType.value === "qa") {
        try {
          const rows = await api_discover.discoverApi.getCategoryTree(rootCategoryId.value);
          childCategories.value = (rows || []).map((item) => ({
            id: Number(item.id),
            name: item.name || ""
          }));
        } catch {
          childCategories.value = [];
        }
        return;
      }
      try {
        const categories = await api_expert.expertApi.categories();
        const rows = (categories || []).filter(
          (item) => Number(item.parentId || 0) === rootCategoryId.value
        );
        childCategories.value = rows.map((item) => ({
          id: Number(item.id),
          name: item.name || ""
        }));
      } catch {
        childCategories.value = [];
      }
    }
    async function loadQuestions(categoryId) {
      if (!categoryId)
        return;
      activeCategoryId.value = categoryId;
      loading.value = true;
      try {
        const page = await api_question.questionApi.page({
          page: 1,
          pageSize: 20,
          categoryId
        });
        questions.value = (page == null ? void 0 : page.list) || [];
      } catch {
        questions.value = [];
        common_vendor.index.showToast({ title: "分类问题加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function loadKbPosts(categoryId) {
      if (!categoryId)
        return;
      activeCategoryId.value = categoryId;
      loading.value = true;
      try {
        const page = await api_expert.expertApi.page({
          page: 1,
          pageSize: 20,
          categoryId,
          sortBy: "hot"
        });
        kbPosts.value = (page == null ? void 0 : page.list) || [];
      } catch {
        kbPosts.value = [];
        common_vendor.index.showToast({ title: "分类文章加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function loadByCategory(categoryId) {
      if (categoryType.value === "qa") {
        await loadQuestions(categoryId);
        return;
      }
      await loadKbPosts(categoryId);
    }
    async function initPage() {
      await loadChildren();
      const firstChildId = childCategories.value.length ? childCategories.value[0].id : 0;
      const targetId = firstChildId || rootCategoryId.value;
      await loadByCategory(targetId);
    }
    common_vendor.onLoad(async (options) => {
      const id = Number((options == null ? void 0 : options.categoryId) || 0);
      const name = decodeURIComponent(String((options == null ? void 0 : options.categoryName) || "分类"));
      const type = String((options == null ? void 0 : options.categoryType) || "qa").toLowerCase();
      if (!id) {
        common_vendor.index.showToast({ title: "分类参数错误", icon: "none" });
        setTimeout(() => common_vendor.index.navigateBack(), 120);
        return;
      }
      categoryType.value = type === "kb" ? "kb" : "qa";
      rootCategoryId.value = id;
      rootCategoryName.value = name || "分类";
      common_vendor.index.setNavigationBarTitle({ title: rootCategoryName.value });
      await initPage();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: childCategories.value.length
      }, childCategories.value.length ? {
        b: common_vendor.f(childCategories.value, (sub, k0, i0) => {
          return {
            a: common_vendor.t(sub.name),
            b: sub.id,
            c: activeCategoryId.value === Number(sub.id) ? 1 : "",
            d: common_vendor.o(($event) => loadByCategory(Number(sub.id)), sub.id)
          };
        })
      } : {}, {
        c: common_vendor.t(categoryType.value === "qa" ? "问题列表" : "科普文章"),
        d: loading.value
      }, loading.value ? {} : categoryType.value === "qa" && !questions.value.length ? {} : categoryType.value === "kb" && !kbPosts.value.length ? {} : categoryType.value === "qa" ? {
        h: common_vendor.f(questions.value, (q, k0, i0) => {
          return {
            a: common_vendor.t(q.title),
            b: common_vendor.t(q.answerCount || 0),
            c: common_vendor.t(q.viewCount || 0),
            d: common_vendor.t(q.likeCount || 0),
            e: common_vendor.t(q.createdAt || ""),
            f: q.id,
            g: common_vendor.o(($event) => common_vendor.unref(utils_nav.openQuestionDetail)(q.id), q.id)
          };
        })
      } : {
        i: common_vendor.f(kbPosts.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.title || "未命名文章"),
            b: common_vendor.t(item.summary || "暂无摘要"),
            c: common_vendor.t(item.viewCount || 0),
            d: common_vendor.t(item.likeCount || 0),
            e: common_vendor.t(item.favoriteCount || 0),
            f: common_vendor.t(item.createdAt || ""),
            g: item.id,
            h: common_vendor.o(($event) => common_vendor.unref(utils_nav.openExpertPostDetailPage)(item.id), item.id)
          };
        })
      }, {
        e: categoryType.value === "qa" && !questions.value.length,
        f: categoryType.value === "kb" && !kbPosts.value.length,
        g: categoryType.value === "qa"
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-226451ce"]]);
wx.createPage(MiniProgramPage);
