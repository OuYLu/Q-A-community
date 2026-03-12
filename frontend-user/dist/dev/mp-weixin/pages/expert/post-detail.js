"use strict";
const common_vendor = require("../../common/vendor.js");
const api_expert = require("../../api/expert.js");
const utils_nav = require("../../utils/nav.js");
const utils_constants = require("../../utils/constants.js");
const defaultAvatarText = "用户";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "post-detail",
  setup(__props) {
    const postId = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const detail = common_vendor.ref(null);
    const interaction = common_vendor.ref(null);
    const comments = common_vendor.ref([]);
    const commentInput = common_vendor.ref("");
    const posting = common_vendor.ref(false);
    const replyParentId = common_vendor.ref(null);
    const replyToName = common_vendor.ref("");
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
    const liked = common_vendor.computed(() => {
      var _a;
      return Boolean((_a = interaction.value) == null ? void 0 : _a.liked);
    });
    const favorited = common_vendor.computed(() => {
      var _a;
      return Boolean((_a = interaction.value) == null ? void 0 : _a.favorited);
    });
    const likeCount = common_vendor.computed(() => {
      var _a, _b;
      return Number(((_a = interaction.value) == null ? void 0 : _a.likeCount) ?? ((_b = detail.value) == null ? void 0 : _b.likeCount) ?? 0);
    });
    const favoriteCount = common_vendor.computed(() => {
      var _a, _b;
      return Number(((_a = interaction.value) == null ? void 0 : _a.favoriteCount) ?? ((_b = detail.value) == null ? void 0 : _b.favoriteCount) ?? 0);
    });
    const commentCount = common_vendor.computed(() => {
      var _a;
      return Number(((_a = interaction.value) == null ? void 0 : _a.commentCount) ?? comments.value.length);
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
    const commentMap = common_vendor.computed(() => {
      const map = /* @__PURE__ */ new Map();
      comments.value.forEach((item) => map.set(item.id, item));
      return map;
    });
    const rootComments = common_vendor.computed(() => {
      return comments.value.filter((item) => !item.parentId || !commentMap.value.has(item.parentId));
    });
    function childrenOf(parentId) {
      return comments.value.filter((item) => item.parentId === parentId);
    }
    const commentPlaceholder = common_vendor.computed(() => {
      return replyParentId.value ? `回复 ${replyToName.value}...` : "写评论...";
    });
    function applyInteraction(data) {
      if (!data)
        return;
      interaction.value = {
        kbEntryId: Number(data.kbEntryId || postId.value),
        likeCount: Number(data.likeCount ?? 0),
        favoriteCount: Number(data.favoriteCount ?? 0),
        commentCount: Number(data.commentCount ?? comments.value.length),
        liked: Boolean(data.liked),
        favorited: Boolean(data.favorited)
      };
    }
    async function loadComments() {
      comments.value = await api_expert.expertApi.kbComments(postId.value);
    }
    async function loadDetail() {
      var _a;
      if (!postId.value)
        return;
      loading.value = true;
      try {
        const post = await api_expert.expertApi.detail(postId.value);
        detail.value = post;
        const [interactRes, commentRes] = await Promise.all([
          api_expert.expertApi.kbInteraction(postId.value).catch(() => null),
          api_expert.expertApi.kbComments(postId.value).catch(() => [])
        ]);
        comments.value = commentRes || [];
        if (interactRes) {
          applyInteraction(interactRes);
        } else {
          interaction.value = {
            kbEntryId: postId.value,
            likeCount: Number(post.likeCount || 0),
            favoriteCount: Number(post.favoriteCount || 0),
            commentCount: comments.value.length,
            liked: false,
            favorited: false
          };
        }
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
    async function toggleLike() {
      if (!postId.value)
        return;
      try {
        const data = await api_expert.expertApi.toggleKbLike(postId.value);
        applyInteraction(data);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "点赞失败", icon: "none" });
      }
    }
    async function toggleFavorite() {
      if (!postId.value)
        return;
      try {
        const data = await api_expert.expertApi.toggleKbFavorite(postId.value);
        applyInteraction(data);
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "收藏失败", icon: "none" });
      }
    }
    function openPostAuthor() {
      var _a;
      if (!((_a = detail.value) == null ? void 0 : _a.authorUserId))
        return;
      utils_nav.openUserHomePage(Number(detail.value.authorUserId));
    }
    function openCommentAuthor(comment) {
      if (!(comment == null ? void 0 : comment.authorId))
        return;
      utils_nav.openUserHomePage(Number(comment.authorId));
    }
    function startReply(comment) {
      replyParentId.value = comment.id;
      replyToName.value = comment.authorName || "用户";
    }
    function cancelReply() {
      replyParentId.value = null;
      replyToName.value = "";
    }
    async function submitComment() {
      const content = commentInput.value.trim();
      if (!content) {
        common_vendor.index.showToast({ title: "请输入评论内容", icon: "none" });
        return;
      }
      if (posting.value)
        return;
      posting.value = true;
      try {
        await api_expert.expertApi.createKbComment(postId.value, content, replyParentId.value || void 0);
        commentInput.value = "";
        cancelReply();
        await loadComments();
        try {
          const latest = await api_expert.expertApi.kbInteraction(postId.value);
          applyInteraction(latest);
        } catch {
          if (interaction.value)
            interaction.value.commentCount = comments.value.length;
        }
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "评论失败", icon: "none" });
      } finally {
        posting.value = false;
      }
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
        i: common_vendor.o(openPostAuthor),
        j: common_vendor.t(detail.value.title),
        k: detail.value.summary
      }, detail.value.summary ? {
        l: common_vendor.t(detail.value.summary)
      } : {}, {
        m: common_vendor.t(detail.value.createdAt || ""),
        n: common_vendor.t(detail.value.viewCount || 0),
        o: contentBlocks.value.length
      }, contentBlocks.value.length ? {
        p: common_vendor.f(contentBlocks.value, (block, idx, i0) => {
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
        q: common_vendor.t(detail.value.content || "暂无正文")
      }, {
        r: common_vendor.t(likeCount.value),
        s: liked.value ? 1 : "",
        t: common_vendor.o(toggleLike),
        v: common_vendor.t(favoriteCount.value),
        w: favorited.value ? 1 : "",
        x: common_vendor.o(toggleFavorite),
        y: common_vendor.t(commentCount.value),
        z: common_vendor.t(commentCount.value),
        A: !comments.value.length
      }, !comments.value.length ? {} : {}, {
        B: common_vendor.f(rootComments.value, (root, k0, i0) => {
          return common_vendor.e({
            a: root.authorAvatar
          }, root.authorAvatar ? {
            b: root.authorAvatar
          } : {
            c: common_vendor.t(defaultAvatarText)
          }, {
            d: common_vendor.t(root.authorName || "匿名用户"),
            e: common_vendor.t(root.createdAt || ""),
            f: common_vendor.o(($event) => startReply(root), root.id),
            g: common_vendor.o(($event) => openCommentAuthor(root), root.id),
            h: common_vendor.t(root.content),
            i: childrenOf(root.id).length
          }, childrenOf(root.id).length ? {
            j: common_vendor.f(childrenOf(root.id), (child, k1, i1) => {
              return common_vendor.e({
                a: child.authorAvatar
              }, child.authorAvatar ? {
                b: child.authorAvatar
              } : {
                c: common_vendor.t(defaultAvatarText)
              }, {
                d: common_vendor.t(child.authorName || "匿名用户"),
                e: common_vendor.t(child.createdAt || ""),
                f: common_vendor.o(($event) => startReply(child), child.id),
                g: common_vendor.o(($event) => openCommentAuthor(child), child.id),
                h: common_vendor.t(child.content),
                i: child.id
              });
            })
          } : {}, {
            k: root.id
          });
        }),
        C: replyParentId.value
      }, replyParentId.value ? {
        D: common_vendor.t(replyToName.value),
        E: common_vendor.o(cancelReply)
      } : {}, {
        F: commentPlaceholder.value,
        G: commentInput.value,
        H: common_vendor.o(($event) => commentInput.value = $event.detail.value),
        I: common_vendor.t(posting.value ? "发送中" : "发送"),
        J: common_vendor.o(submitComment)
      }), {
        b: !detail.value
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b25f2227"]]);
wx.createPage(MiniProgramPage);
