"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const stores_auth = require("../../stores/auth.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const api_me = require("../../api/me.js");
const utils_constants = require("../../utils/constants.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const authStore = stores_auth.useAuthStore();
    const loading = common_vendor.ref(false);
    const overview = common_vendor.ref(null);
    const needLogin = common_vendor.computed(() => !authStore.isLogin);
    const displayNickname = common_vendor.computed(() => {
      var _a, _b;
      return ((_a = overview.value) == null ? void 0 : _a.nickname) || ((_b = authStore.user) == null ? void 0 : _b.nickname) || "微信用户";
    });
    const displaySlogan = common_vendor.computed(
      () => {
        var _a, _b;
        return ((_a = overview.value) == null ? void 0 : _a.slogan) || ((_b = authStore.user) == null ? void 0 : _b.slogan) || "这个人很懒，还没有填写个性签名";
      }
    );
    const isVerifiedExpert = common_vendor.computed(() => {
      var _a;
      return ((_a = overview.value) == null ? void 0 : _a.expertStatus) === 3;
    });
    const expertCapsule = common_vendor.computed(() => {
      var _a;
      const text = (((_a = overview.value) == null ? void 0 : _a.expertExpertise) || "").trim();
      if (!text)
        return "已认证专家";
      return text.length > 14 ? `${text.slice(0, 14)}...` : text;
    });
    const avatarUrl = common_vendor.computed(() => {
      var _a, _b;
      const avatar = ((_a = overview.value) == null ? void 0 : _a.avatar) || ((_b = authStore.user) == null ? void 0 : _b.avatar) || "";
      if (!avatar)
        return "";
      if (avatar.startsWith("http://") || avatar.startsWith("https://"))
        return avatar;
      return `${utils_constants.BASE_URL}${avatar}`;
    });
    const profileStats = common_vendor.computed(() => {
      var _a, _b, _c, _d, _e;
      const base = [
        { label: "提问", value: ((_a = overview.value) == null ? void 0 : _a.questionCount) ?? 0 },
        { label: "回答", value: ((_b = overview.value) == null ? void 0 : _b.answerCount) ?? 0 },
        { label: "获赞", value: ((_c = overview.value) == null ? void 0 : _c.likeReceivedCount) ?? 0 },
        { label: "粉丝", value: ((_d = overview.value) == null ? void 0 : _d.followerCount) ?? 0 }
      ];
      if (isVerifiedExpert.value) {
        base.push({ label: "科普", value: ((_e = overview.value) == null ? void 0 : _e.expertPostCount) ?? 0 });
      }
      return base;
    });
    const myContent = common_vendor.computed(() => {
      var _a, _b, _c, _d, _e, _f;
      const rows = [
        { icon: "🔖", title: "我的收藏", value: String(((_a = overview.value) == null ? void 0 : _a.favoriteCount) ?? 0), type: "favorites" },
        { icon: "🕒", title: "浏览历史", value: String(((_b = overview.value) == null ? void 0 : _b.historyCount) ?? 0), type: "history" },
        { icon: "💬", title: "我的提问", value: String(((_c = overview.value) == null ? void 0 : _c.questionCount) ?? 0), type: "questions" },
        { icon: "⭐", title: "我的回答", value: String(((_d = overview.value) == null ? void 0 : _d.answerCount) ?? 0), type: "answers" },
        { icon: "🗂️", title: "专题关注", value: String(((_e = overview.value) == null ? void 0 : _e.topicFollowCount) ?? 0), type: "topic-following" }
      ];
      if (isVerifiedExpert.value) {
        rows.push({
          icon: "📚",
          title: "我的科普",
          value: String(((_f = overview.value) == null ? void 0 : _f.expertPostCount) ?? 0),
          type: "expert-posts"
        });
      }
      return rows;
    });
    const social = common_vendor.computed(() => {
      var _a, _b, _c;
      return [
        { icon: "👥", title: "关注", value: String(((_a = overview.value) == null ? void 0 : _a.followingCount) ?? 0), type: "following" },
        { icon: "🤍", title: "粉丝", value: String(((_b = overview.value) == null ? void 0 : _b.followerCount) ?? 0), type: "followers" },
        buildExpertSocialItem((_c = overview.value) == null ? void 0 : _c.expertStatus)
      ];
    });
    const others = [
      { icon: "⚙️", title: "设置", type: "settings" },
      { icon: "❓", title: "帮助与反馈", type: "help" },
      { icon: "📄", title: "用户协议", type: "user-agreement" },
      { icon: "🛡️", title: "隐私政策", type: "privacy-policy" }
    ];
    function logout() {
      authStore.logout();
      common_vendor.index.reLaunch({ url: "/pages/home/index" });
    }
    function goLogin() {
      utils_authGuard.openLoginPage({ redirect: "/pages/mine/index", preferReplace: true });
    }
    function editProfile() {
      common_vendor.index.navigateTo({ url: "/pages/mine/edit-profile" });
    }
    function openList(type) {
      common_vendor.index.navigateTo({ url: `/pages/mine/list?type=${type}` });
    }
    function openSocial(type) {
      var _a;
      if (type === "expert-apply") {
        const status = (_a = overview.value) == null ? void 0 : _a.expertStatus;
        if (status === 2) {
          common_vendor.index.showToast({ title: "专家认证审核中", icon: "none" });
          return;
        }
        if (status === 3) {
          common_vendor.index.showToast({ title: "专家认证已通过", icon: "none" });
          return;
        }
        common_vendor.index.navigateTo({ url: "/pages/mine/expert-apply" });
        return;
      }
      openList(type);
    }
    function buildExpertSocialItem(expertStatus) {
      if (expertStatus === 2) {
        return {
          iconImage: "/static/tabbar/expert.png",
          title: "专家认证",
          value: "审核中",
          type: "expert-apply",
          disabled: true
        };
      }
      if (expertStatus === 3) {
        return {
          iconImage: "/static/tabbar/expert.png",
          title: "专家认证",
          value: "已通过",
          type: "expert-apply",
          disabled: true
        };
      }
      if (expertStatus === 4) {
        return {
          iconImage: "/static/tabbar/expert.png",
          title: "重新申请专家",
          value: "已驳回",
          type: "expert-apply"
        };
      }
      return {
        iconImage: "/static/tabbar/expert.png",
        title: "申请专家",
        value: "未申请",
        type: "expert-apply"
      };
    }
    function openDoc(type) {
      common_vendor.index.navigateTo({ url: `/pages/mine/doc?type=${type}` });
    }
    function openOther(type) {
      if (type === "settings") {
        common_vendor.index.navigateTo({ url: "/pages/mine/settings" });
        return;
      }
      openDoc(type);
    }
    function formatJoinedAt(value) {
      if (!value)
        return "--";
      const normalized = value.replace("T", " ").replace(/-/g, "/");
      const date = new Date(normalized);
      if (Number.isNaN(date.getTime()))
        return value;
      const y = date.getFullYear();
      const m = `${date.getMonth() + 1}`.padStart(2, "0");
      return `${y}年${m}月`;
    }
    async function loadOverview() {
      var _a, _b, _c, _d;
      loading.value = true;
      try {
        overview.value = await api_me.meApi.overview();
        authStore.user = {
          ...authStore.user || {},
          userId: overview.value.userId,
          username: overview.value.username || ((_a = authStore.user) == null ? void 0 : _a.username),
          nickname: overview.value.nickname || ((_b = authStore.user) == null ? void 0 : _b.nickname) || "微信用户",
          avatar: overview.value.avatar || ((_c = authStore.user) == null ? void 0 : _c.avatar),
          slogan: overview.value.slogan || ((_d = authStore.user) == null ? void 0 : _d.slogan)
        };
      } catch (err) {
        if (String((err == null ? void 0 : err.message) || "").includes("401")) {
          authStore.logout();
          return;
        }
        common_vendor.index.showToast({ title: "我的页面加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    common_vendor.onShow(() => {
      if (needLogin.value)
        return;
      loadOverview();
    });
    return (_ctx, _cache) => {
      var _a;
      return common_vendor.e({
        a: needLogin.value
      }, needLogin.value ? {
        b: common_vendor.o(goLogin)
      } : common_vendor.e({
        c: avatarUrl.value
      }, avatarUrl.value ? {
        d: avatarUrl.value
      } : {}, {
        e: common_vendor.t(displayNickname.value),
        f: isVerifiedExpert.value
      }, isVerifiedExpert.value ? {
        g: common_assets._imports_0
      } : {}, {
        h: isVerifiedExpert.value
      }, isVerifiedExpert.value ? {
        i: common_vendor.t(expertCapsule.value)
      } : {}, {
        j: common_vendor.t(displaySlogan.value),
        k: common_vendor.o(editProfile),
        l: common_vendor.f(profileStats.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.value),
            b: common_vendor.t(item.label),
            c: item.label
          };
        }),
        m: common_vendor.f(myContent.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.icon),
            b: common_vendor.t(item.title),
            c: common_vendor.t(item.value),
            d: item.title,
            e: common_vendor.o(($event) => openList(item.type), item.title)
          };
        }),
        n: common_vendor.f(social.value, (item, k0, i0) => {
          return common_vendor.e({
            a: item.iconImage
          }, item.iconImage ? {
            b: item.iconImage
          } : {
            c: common_vendor.t(item.icon)
          }, {
            d: common_vendor.t(item.title),
            e: item.value !== void 0
          }, item.value !== void 0 ? {
            f: common_vendor.t(item.value)
          } : {}, {
            g: item.title,
            h: item.disabled ? 1 : "",
            i: common_vendor.o(($event) => openSocial(item.type), item.title)
          });
        }),
        o: common_vendor.f(others, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.icon),
            b: common_vendor.t(item.title),
            c: item.title,
            d: common_vendor.o(($event) => openOther(item.type), item.title)
          };
        }),
        p: common_vendor.t(formatJoinedAt((_a = overview.value) == null ? void 0 : _a.joinedAt)),
        q: common_vendor.o(logout),
        r: loading.value
      }));
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-9023ef44"]]);
wx.createPage(MiniProgramPage);
