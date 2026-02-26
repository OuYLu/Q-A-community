"use strict";
const common_vendor = require("../../common/vendor.js");
const api_me = require("../../api/me.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const api_question = require("../../api/question.js");
const utils_nav = require("../../utils/nav.js");
const pageSize = 10;
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "list",
  setup(__props) {
    const type = common_vendor.ref("favorites");
    const title = common_vendor.ref("列表");
    const loading = common_vendor.ref(false);
    const finished = common_vendor.ref(false);
    const pageNum = common_vendor.ref(1);
    const total = common_vendor.ref(0);
    const items = common_vendor.ref([]);
    const titleMap = {
      favorites: "我的收藏",
      history: "浏览历史",
      questions: "我的提问",
      answers: "我的回答",
      following: "关注",
      followers: "粉丝"
    };
    const isQuestionList = common_vendor.computed(() => type.value === "questions");
    const questionItems = common_vendor.computed(() => items.value || []);
    function normalizeType(raw) {
      const valid = ["favorites", "history", "questions", "answers", "following", "followers"];
      if (raw && valid.includes(raw))
        return raw;
      return "favorites";
    }
    function rowMainText(item) {
      if (type.value === "favorites")
        return item.title;
      if (type.value === "history")
        return item.title;
      if (type.value === "questions")
        return item.title;
      if (type.value === "answers")
        return `问题：${item.questionTitle || ""}`;
      return item.nickname || `用户 ${item.userId}`;
    }
    function rowSubText(item) {
      if (type.value === "favorites")
        return `${item.answerCount}回答 ${item.likeCount}点赞`;
      if (type.value === "history")
        return item.subTitle || "";
      if (type.value === "questions")
        return `状态:${item.status} ${item.answerCount}回答`;
      if (type.value === "answers")
        return item.contentPreview || "";
      return `专家状态:${item.expertStatus ?? "普通"}`;
    }
    function rowTimeText(item) {
      if (type.value === "favorites")
        return item.favoriteAt;
      if (type.value === "history")
        return item.viewedAt;
      if (type.value === "questions")
        return item.createdAt;
      if (type.value === "answers")
        return item.createdAt;
      return item.followedAt;
    }
    function questionStatusText(item) {
      if (item.acceptedAnswerId)
        return "已采纳最佳答案";
      const count = item.answerCount || 0;
      if (count > 0)
        return `已有${count}条回答`;
      return "尚无回答";
    }
    function questionStatusClass(item) {
      if (item.acceptedAnswerId)
        return "solved";
      const count = item.answerCount || 0;
      if (count > 0)
        return "waiting";
      return "empty-answer";
    }
    function questionTags(item) {
      const tags = [];
      if (item.categoryName)
        tags.push(item.categoryName);
      (item.tags || []).slice(0, 3).forEach((x) => tags.push(`#${x}`));
      return tags;
    }
    async function fetchPage(reset = false) {
      if (loading.value || finished.value)
        return;
      loading.value = true;
      try {
        const page = reset ? 1 : pageNum.value;
        let resp;
        const query = { page, pageSize };
        switch (type.value) {
          case "favorites":
            resp = await api_me.meApi.favorites(query);
            break;
          case "history":
            resp = await api_me.meApi.history(query);
            break;
          case "questions":
            resp = await api_question.questionApi.myQuestions(query);
            break;
          case "answers":
            resp = await api_me.meApi.answers(query);
            break;
          case "following":
            resp = await api_me.meApi.following(query);
            break;
          case "followers":
            resp = await api_me.meApi.followers(query);
            break;
        }
        total.value = resp.total;
        if (reset)
          items.value = resp.list;
        else
          items.value = items.value.concat(resp.list);
        pageNum.value = page + 1;
        finished.value = items.value.length >= resp.total || resp.list.length < pageSize;
      } catch (err) {
        common_vendor.index.showToast({ title: "列表加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function openRow(item) {
      if (type.value === "questions" && (item == null ? void 0 : item.id)) {
        utils_nav.openQuestionDetail(Number(item.id));
      }
    }
    common_vendor.onLoad((options) => {
      if (!utils_authGuard.ensurePageAuth())
        return;
      type.value = normalizeType(options == null ? void 0 : options.type);
      title.value = titleMap[type.value];
      common_vendor.index.setNavigationBarTitle({ title: title.value });
      fetchPage(true);
    });
    common_vendor.onReachBottom(() => {
      fetchPage(false);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(total.value),
        b: !items.value.length && !loading.value
      }, !items.value.length && !loading.value ? {} : {}, {
        c: isQuestionList.value
      }, isQuestionList.value ? {
        d: common_vendor.f(questionItems.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.n(questionStatusClass(item)),
            b: common_vendor.t(item.title),
            c: questionTags(item).length
          }, questionTags(item).length ? {
            d: common_vendor.f(questionTags(item), (tag, tagIndex, i1) => {
              return {
                a: common_vendor.t(tag),
                b: `${item.id}-${tag}-${tagIndex}`,
                c: common_vendor.n(tagIndex === 0 ? "chip-main" : "chip-sub")
              };
            })
          } : {}, {
            e: common_vendor.t(item.likeCount || 0),
            f: common_vendor.t(item.viewCount || 0),
            g: common_vendor.t(item.createdAt || ""),
            h: common_vendor.t(questionStatusText(item)),
            i: common_vendor.n(questionStatusClass(item)),
            j: item.id,
            k: common_vendor.o(($event) => openRow(item), item.id)
          });
        })
      } : {
        e: common_vendor.f(items.value, (item, idx, i0) => {
          return {
            a: common_vendor.t(rowMainText(item)),
            b: common_vendor.t(rowSubText(item)),
            c: common_vendor.t(rowTimeText(item)),
            d: idx,
            e: common_vendor.o(($event) => openRow(item), idx)
          };
        })
      }, {
        f: loading.value
      }, loading.value ? {} : finished.value && items.value.length ? {} : {}, {
        g: finished.value && items.value.length
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-688829fb"]]);
wx.createPage(MiniProgramPage);
