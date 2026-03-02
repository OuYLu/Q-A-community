"use strict";
const common_vendor = require("../../common/vendor.js");
const api_topic = require("../../api/topic.js");
const utils_nav = require("../../utils/nav.js");
const pageSize = 10;
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "topic-detail",
  setup(__props) {
    const topicId = common_vendor.ref(0);
    const topic = common_vendor.ref(null);
    const questions = common_vendor.ref([]);
    const pageNum = common_vendor.ref(1);
    const finished = common_vendor.ref(false);
    const loading = common_vendor.ref(false);
    const activeSort = common_vendor.ref("hot");
    const failedCover = common_vendor.ref(false);
    const followLoading = common_vendor.ref(false);
    const sortTabs = [
      { key: "hot", label: "最热" },
      { key: "latest", label: "最新" },
      { key: "unsolved", label: "待解决" }
    ];
    const followText = common_vendor.computed(() => {
      var _a;
      return ((_a = topic.value) == null ? void 0 : _a.followed) ? "已关注" : "关注";
    });
    common_vendor.computed(() => {
      var _a;
      return ((_a = topic.value) == null ? void 0 : _a.followed) ? "follow-btn active" : "follow-btn";
    });
    async function loadTopicDetail() {
      topic.value = await api_topic.topicApi.detail(topicId.value);
      failedCover.value = false;
    }
    async function loadQuestions(reset = false) {
      if (loading.value)
        return;
      if (!reset && finished.value)
        return;
      loading.value = true;
      try {
        const page = reset ? 1 : pageNum.value;
        const data = await api_topic.topicApi.questions(topicId.value, {
          sortBy: activeSort.value,
          page,
          pageSize
        });
        const list = (data == null ? void 0 : data.list) || [];
        if (reset)
          questions.value = list;
        else
          questions.value = questions.value.concat(list);
        pageNum.value = page + 1;
        finished.value = questions.value.length >= ((data == null ? void 0 : data.total) || 0) || list.length < pageSize;
      } catch {
        if (reset)
          questions.value = [];
        common_vendor.index.showToast({ title: "专题问题加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    async function switchSort(key) {
      if (activeSort.value === key)
        return;
      activeSort.value = key;
      pageNum.value = 1;
      finished.value = false;
      await loadQuestions(true);
    }
    async function toggleFollow() {
      if (!topic.value || followLoading.value)
        return;
      followLoading.value = true;
      try {
        if (topic.value.followed) {
          await api_topic.topicApi.unfollow(topic.value.id);
          topic.value = {
            ...topic.value,
            followed: false,
            followCount: Math.max(0, Number(topic.value.followCount || 0) - 1)
          };
          common_vendor.index.showToast({ title: "已取消关注", icon: "none" });
        } else {
          await api_topic.topicApi.follow(topic.value.id);
          topic.value = {
            ...topic.value,
            followed: true,
            followCount: Number(topic.value.followCount || 0) + 1
          };
          common_vendor.index.showToast({ title: "关注成功", icon: "success" });
        }
      } catch (err) {
        common_vendor.index.showToast({ title: (err == null ? void 0 : err.message) || "操作失败", icon: "none" });
      } finally {
        followLoading.value = false;
      }
    }
    function goAskInTopic() {
      var _a;
      if (!topicId.value)
        return;
      const t = encodeURIComponent(((_a = topic.value) == null ? void 0 : _a.title) || "");
      common_vendor.index.navigateTo({ url: `/pages/question/ask?topicId=${topicId.value}&topicTitle=${t}` });
    }
    common_vendor.onLoad(async (options) => {
      var _a;
      topicId.value = Number((options == null ? void 0 : options.topicId) || 0);
      const title = decodeURIComponent(String((options == null ? void 0 : options.topicTitle) || "专题"));
      if (!topicId.value) {
        common_vendor.index.showToast({ title: "专题参数错误", icon: "none" });
        setTimeout(() => common_vendor.index.navigateBack(), 120);
        return;
      }
      common_vendor.index.setNavigationBarTitle({ title });
      await loadTopicDetail();
      if ((_a = topic.value) == null ? void 0 : _a.title) {
        common_vendor.index.setNavigationBarTitle({ title: topic.value.title });
      }
      await loadQuestions(true);
    });
    common_vendor.onReachBottom(() => {
      loadQuestions(false);
    });
    return (_ctx, _cache) => {
      var _a, _b;
      return common_vendor.e({
        a: topic.value
      }, topic.value ? common_vendor.e({
        b: topic.value.coverImg && !failedCover.value
      }, topic.value.coverImg && !failedCover.value ? {
        c: topic.value.coverImg,
        d: common_vendor.o(($event) => failedCover.value = true)
      } : {}, {
        e: common_vendor.t(topic.value.title),
        f: common_vendor.t(topic.value.subtitle || "专题下的相关问题集合")
      }) : {}, {
        g: topic.value
      }, topic.value ? common_vendor.e({
        h: common_vendor.t(topic.value.questionCount || 0),
        i: common_vendor.t(topic.value.followCount || 0),
        j: common_vendor.t(topic.value.todayNewCount || 0),
        k: common_vendor.t(topic.value.intro || topic.value.subtitle || "暂无介绍"),
        l: (_a = topic.value.tags) == null ? void 0 : _a.length
      }, ((_b = topic.value.tags) == null ? void 0 : _b.length) ? {
        m: common_vendor.f(topic.value.tags, (tag, idx, i0) => {
          return {
            a: common_vendor.t(tag),
            b: tag + idx
          };
        })
      } : {}, {
        n: common_vendor.t(followText.value),
        o: topic.value.followed ? 1 : "",
        p: followLoading.value,
        q: common_vendor.o(toggleFollow)
      }) : {}, {
        r: common_vendor.f(sortTabs, (tab, k0, i0) => {
          return {
            a: common_vendor.t(tab.label),
            b: tab.key,
            c: activeSort.value === tab.key ? 1 : "",
            d: common_vendor.o(($event) => switchSort(tab.key), tab.key)
          };
        }),
        s: !questions.value.length && !loading.value
      }, !questions.value.length && !loading.value ? {} : {
        t: common_vendor.f(questions.value, (q, k0, i0) => {
          return {
            a: common_vendor.t(q.title),
            b: common_vendor.t(q.answerCount || 0),
            c: common_vendor.t(q.viewCount || 0),
            d: common_vendor.t(q.likeCount || 0),
            e: q.id,
            f: common_vendor.o(($event) => common_vendor.unref(utils_nav.openQuestionDetail)(q.id), q.id)
          };
        })
      }, {
        v: loading.value
      }, loading.value ? {} : finished.value && questions.value.length ? {} : {}, {
        w: finished.value && questions.value.length,
        x: common_vendor.o(goAskInTopic)
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-5a41a422"]]);
wx.createPage(MiniProgramPage);
