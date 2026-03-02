"use strict";
const common_vendor = require("../../common/vendor.js");
const api_notification = require("../../api/notification.js");
const stores_auth = require("../../stores/auth.js");
const utils_noticeBadge = require("../../utils/notice-badge.js");
const pageSize = 10;
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const items = common_vendor.ref([]);
    const unread = common_vendor.ref(null);
    const loading = common_vendor.ref(false);
    const page = common_vendor.ref(1);
    const total = common_vendor.ref(0);
    const hasMore = common_vendor.ref(true);
    const selectedType = common_vendor.ref("all");
    const selectedRead = common_vendor.ref("all");
    const authStore = stores_auth.useAuthStore();
    const needLogin = common_vendor.computed(() => !authStore.isLogin);
    const typeTabs = [
      { key: "all", label: "全部" },
      { key: "system", label: "系统", types: "1,7" },
      { key: "interaction", label: "互动", types: "2,3" },
      { key: "comment", label: "评论", types: "5" },
      { key: "answer", label: "回答", types: "6" }
    ];
    const readTabs = [
      { key: "all", label: "全部" },
      { key: "unread", label: "未读", isRead: 0 },
      { key: "read", label: "已读", isRead: 1 }
    ];
    function currentTypeParams() {
      const tab = typeTabs.find((x) => x.key === selectedType.value);
      return (tab == null ? void 0 : tab.types) ? { types: tab.types } : {};
    }
    function currentReadParams() {
      const tab = readTabs.find((x) => x.key === selectedRead.value);
      return (tab == null ? void 0 : tab.isRead) === 0 || (tab == null ? void 0 : tab.isRead) === 1 ? { isRead: tab.isRead } : {};
    }
    async function loadUnread() {
      var _a;
      try {
        unread.value = await api_notification.notificationApi.unreadCount();
        utils_noticeBadge.syncNoticeTabDot(Number(((_a = unread.value) == null ? void 0 : _a.total) || 0));
      } catch {
        unread.value = null;
        utils_noticeBadge.syncNoticeTabDot(0);
      } finally {
        await utils_noticeBadge.refreshNoticeTabDot();
      }
    }
    async function loadList(reset = false) {
      if (needLogin.value || loading.value || !reset && !hasMore.value)
        return;
      if (reset) {
        page.value = 1;
        total.value = 0;
        hasMore.value = true;
      }
      loading.value = true;
      try {
        const data = await api_notification.notificationApi.list({
          page: page.value,
          pageSize,
          ...currentTypeParams(),
          ...currentReadParams()
        });
        const rows = data.list || [];
        items.value = reset ? rows : [...items.value, ...rows];
        total.value = Number(data.total || 0);
        hasMore.value = items.value.length < total.value;
        if (rows.length)
          page.value += 1;
      } catch {
        if (reset)
          items.value = [];
        common_vendor.index.showToast({ title: "通知加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function refreshAll() {
      await Promise.all([loadUnread(), loadList(true)]);
    }
    function typeText(type) {
      const map = {
        1: "系统",
        2: "点赞",
        3: "收藏",
        4: "关注",
        5: "评论",
        6: "回答",
        7: "举报反馈"
      };
      return type ? map[type] || "消息" : "消息";
    }
    async function markRead(id) {
      var _a;
      try {
        await api_notification.notificationApi.read(id);
        const idx = items.value.findIndex((x) => x.id === id);
        if (idx >= 0)
          items.value[idx] = { ...items.value[idx], isRead: 1 };
        const nextTotal = Math.max(0, Number(((_a = unread.value) == null ? void 0 : _a.total) || 0) - 1);
        if (unread.value)
          unread.value = { ...unread.value, total: nextTotal };
        utils_noticeBadge.syncNoticeTabDot(nextTotal);
        await loadUnread();
      } catch {
        common_vendor.index.showToast({ title: "操作失败", icon: "none" });
      }
    }
    function jumpBiz(item) {
      if (item.type === 7 && item.bizId) {
        common_vendor.index.navigateTo({ url: `/pages/notice/report-feedback?id=${item.bizId}` });
        return;
      }
      if (item.bizType === 1 && item.bizId) {
        common_vendor.index.navigateTo({ url: `/pages/question/detail?id=${item.bizId}` });
        return;
      }
      if (item.bizType === 2 && item.bizId) {
        common_vendor.index.navigateTo({ url: `/pages/question/answer-detail?id=${item.bizId}` });
      }
    }
    async function onClickItem(item) {
      if (item.isRead !== 1) {
        await markRead(item.id);
      }
      jumpBiz(item);
    }
    async function markReadAll() {
      try {
        await api_notification.notificationApi.readAll();
        items.value = items.value.map((x) => ({ ...x, isRead: 1 }));
        if (unread.value)
          unread.value = { ...unread.value, total: 0 };
        utils_noticeBadge.syncNoticeTabDot(0);
      } catch {
        common_vendor.index.showToast({ title: "操作失败", icon: "none" });
      } finally {
        await loadUnread();
      }
    }
    function switchType(key) {
      if (selectedType.value === key)
        return;
      selectedType.value = key;
      loadList(true);
    }
    function switchRead(key) {
      if (selectedRead.value === key)
        return;
      selectedRead.value = key;
      loadList(true);
    }
    function goLogin() {
      common_vendor.index.navigateTo({
        url: `/pages/auth/login?redirect=${encodeURIComponent("/pages/notice/index")}`
      });
    }
    common_vendor.onShow(async () => {
      if (needLogin.value) {
        items.value = [];
        unread.value = null;
        utils_noticeBadge.syncNoticeTabDot(0);
        return;
      }
      await refreshAll();
    });
    common_vendor.onReachBottom(() => {
      if (!needLogin.value)
        loadList(false);
    });
    common_vendor.onPullDownRefresh(async () => {
      if (!needLogin.value)
        await refreshAll();
      common_vendor.index.stopPullDownRefresh();
    });
    return (_ctx, _cache) => {
      var _a;
      return common_vendor.e({
        a: needLogin.value
      }, needLogin.value ? {
        b: common_vendor.o(goLogin)
      } : common_vendor.e({
        c: common_vendor.t(((_a = unread.value) == null ? void 0 : _a.total) || 0),
        d: common_vendor.o(markReadAll),
        e: common_vendor.f(typeTabs, (tab, k0, i0) => {
          return {
            a: common_vendor.t(tab.label),
            b: tab.key,
            c: selectedType.value === tab.key ? 1 : "",
            d: common_vendor.o(($event) => switchType(tab.key), tab.key)
          };
        }),
        f: common_vendor.f(readTabs, (tab, k0, i0) => {
          return {
            a: common_vendor.t(tab.label),
            b: tab.key,
            c: selectedRead.value === tab.key ? 1 : "",
            d: common_vendor.o(($event) => switchRead(tab.key), tab.key)
          };
        }),
        g: loading.value && !items.value.length
      }, loading.value && !items.value.length ? {} : !items.value.length ? {} : {
        i: common_vendor.f(items.value, (item, k0, i0) => {
          return common_vendor.e({
            a: item.isRead !== 1
          }, item.isRead !== 1 ? {} : {}, {
            b: common_vendor.t(item.title),
            c: common_vendor.t(typeText(item.type)),
            d: common_vendor.t(item.content),
            e: common_vendor.t(item.createdAt),
            f: item.id,
            g: item.isRead === 1 ? 1 : "",
            h: common_vendor.o(($event) => onClickItem(item), item.id)
          });
        }),
        j: common_vendor.t(hasMore.value ? "上拉加载更多" : "没有更多了")
      }, {
        h: !items.value.length
      }));
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-8e21c599"]]);
wx.createPage(MiniProgramPage);
