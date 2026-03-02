"use strict";
const common_vendor = require("../../common/vendor.js");
const api_discover = require("../../api/discover.js");
const api_question = require("../../api/question.js");
const utils_nav = require("../../utils/nav.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "category-detail",
  setup(__props) {
    const rootCategoryId = common_vendor.ref(0);
    const rootCategoryName = common_vendor.ref("分类");
    const childCategories = common_vendor.ref([]);
    const activeCategoryId = common_vendor.ref(0);
    const questions = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    async function loadChildren() {
      try {
        childCategories.value = await api_discover.discoverApi.getCategoryTree(rootCategoryId.value);
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
    async function initPage() {
      await loadChildren();
      const firstChildId = childCategories.value.length ? Number(childCategories.value[0].id) : 0;
      const targetId = firstChildId || rootCategoryId.value;
      await loadQuestions(targetId);
    }
    common_vendor.onLoad(async (options) => {
      const id = Number((options == null ? void 0 : options.categoryId) || 0);
      const name = decodeURIComponent(String((options == null ? void 0 : options.categoryName) || "分类"));
      if (!id) {
        common_vendor.index.showToast({ title: "分类参数错误", icon: "none" });
        setTimeout(() => common_vendor.index.navigateBack(), 120);
        return;
      }
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
            d: common_vendor.o(($event) => loadQuestions(Number(sub.id)), sub.id)
          };
        })
      } : {}, {
        c: loading.value
      }, loading.value ? {} : !questions.value.length ? {} : {
        e: common_vendor.f(questions.value, (q, k0, i0) => {
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
      }, {
        d: !questions.value.length
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-226451ce"]]);
wx.createPage(MiniProgramPage);
