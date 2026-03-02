"use strict";
const common_vendor = require("../../common/vendor.js");
const api_expert = require("../../api/expert.js");
const utils_constants = require("../../utils/constants.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "post-detail",
  setup(__props) {
    const postId = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const detail = common_vendor.ref(null);
    const officialAvatar = `${utils_constants.BASE_URL}/api/common/avatar/staff.png`;
    function resolveMediaUrl(raw) {
      const url = String(raw || "").trim();
      if (!url)
        return "";
      if (url.startsWith("http://") || url.startsWith("https://"))
        return url;
      if (url.startsWith("/"))
        return `${utils_constants.BASE_URL}${url}`;
      return `${utils_constants.BASE_URL}/${url}`;
    }
    const authorAvatarUrl = common_vendor.computed(() => {
      var _a;
      return resolveMediaUrl((_a = detail.value) == null ? void 0 : _a.authorAvatar);
    });
    const isOfficial = common_vendor.computed(() => {
      var _a;
      return ((_a = detail.value) == null ? void 0 : _a.source) && detail.value.source !== "expert_post";
    });
    const showAvatar = common_vendor.computed(() => isOfficial.value ? officialAvatar : authorAvatarUrl.value);
    const authorName = common_vendor.computed(() => {
      var _a;
      return isOfficial.value ? "问问官方" : ((_a = detail.value) == null ? void 0 : _a.authorName) || "匿名用户";
    });
    const contentBlocks = common_vendor.computed(() => {
      var _a, _b;
      if (!((_b = (_a = detail.value) == null ? void 0 : _a.contentBlocks) == null ? void 0 : _b.length))
        return [];
      return detail.value.contentBlocks.map((x) => ({
        ...x,
        url: x.url ? resolveMediaUrl(x.url) : x.url
      }));
    });
    const imageUrls = common_vendor.computed(() => {
      var _a;
      const list = ((_a = detail.value) == null ? void 0 : _a.imageUrls) || [];
      return list.map((x) => resolveMediaUrl(x)).filter(Boolean);
    });
    async function loadDetail() {
      var _a;
      if (!postId.value)
        return;
      loading.value = true;
      try {
        detail.value = await api_expert.expertApi.detail(postId.value);
        if ((_a = detail.value) == null ? void 0 : _a.title) {
          common_vendor.index.setNavigationBarTitle({ title: detail.value.title });
        }
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "加载科普详情失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function previewImage(current) {
      if (!current)
        return;
      const urls = imageUrls.value.length ? imageUrls.value : [current];
      common_vendor.index.previewImage({ current, urls });
    }
    common_vendor.onLoad((options) => {
      postId.value = Number((options == null ? void 0 : options.id) || 0);
      if (!postId.value) {
        common_vendor.index.showToast({ title: "科普ID无效", icon: "none" });
        return;
      }
      loadDetail();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : !detail.value ? {} : common_vendor.e({
        c: showAvatar.value
      }, showAvatar.value ? {
        d: showAvatar.value
      } : {
        e: common_vendor.t((authorName.value || "问").slice(0, 1))
      }, {
        f: common_vendor.t(authorName.value),
        g: isOfficial.value
      }, isOfficial.value ? {} : {}, {
        h: common_vendor.t(isOfficial.value ? "官方科普" : detail.value.authorTitle || detail.value.authorExpertise || "健康科普作者"),
        i: common_vendor.t(detail.value.title),
        j: detail.value.summary
      }, detail.value.summary ? {
        k: common_vendor.t(detail.value.summary)
      } : {}, {
        l: common_vendor.t(detail.value.createdAt || ""),
        m: common_vendor.t(detail.value.viewCount || 0),
        n: contentBlocks.value.length
      }, contentBlocks.value.length ? {
        o: common_vendor.f(contentBlocks.value, (block, idx, i0) => {
          return common_vendor.e({
            a: block.type === "text"
          }, block.type === "text" ? {
            b: common_vendor.t(block.text || "")
          } : block.type === "image" && block.url ? {
            d: block.url,
            e: common_vendor.o(($event) => previewImage(block.url), `b-${idx}`)
          } : {}, {
            c: block.type === "image" && block.url,
            f: `b-${idx}`
          });
        })
      } : {
        p: common_vendor.t(detail.value.content || "暂无正文")
      }), {
        b: !detail.value
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b25f2227"]]);
wx.createPage(MiniProgramPage);
