"use strict";
const common_vendor = require("../../common/vendor.js");
const api_me = require("../../api/me.js");
const api_expert = require("../../api/expert.js");
const utils_authGuard = require("../../utils/auth-guard.js");
const api_question = require("../../api/question.js");
const utils_nav = require("../../utils/nav.js");
const utils_constants = require("../../utils/constants.js");
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
    const entered = common_vendor.ref(false);
    const answerEffectiveCount = common_vendor.ref(0);
    const questionEffectiveCount = common_vendor.ref(0);
    const titleMap = {
      favorites: "我的收藏",
      history: "浏览历史",
      questions: "我的提问",
      answers: "我的回答",
      following: "关注",
      followers: "粉丝",
      "topic-following": "专题关注",
      "expert-posts": "我的科普"
    };
    const isQuestionList = common_vendor.computed(() => type.value === "questions");
    const isFollowList = common_vendor.computed(() => type.value === "following" || type.value === "followers");
    const questionItems = common_vendor.computed(() => items.value || []);
    const followItems = common_vendor.computed(() => items.value || []);
    const favoriteItems = common_vendor.computed(() => items.value || []);
    const favoriteGroups = common_vendor.computed(() => {
      if (type.value !== "favorites")
        return [];
      const source = favoriteItems.value;
      const groups = [
        { key: 1, title: "问题收藏", items: source.filter((x) => Number((x == null ? void 0 : x.bizType) || 1) === 1) },
        { key: 3, title: "回答收藏", items: source.filter((x) => Number((x == null ? void 0 : x.bizType) || 1) === 3) },
        { key: 2, title: "科普收藏", items: source.filter((x) => Number((x == null ? void 0 : x.bizType) || 1) === 2) }
      ];
      return groups.filter((g) => g.items.length > 0);
    });
    const headText = common_vendor.computed(() => {
      if (type.value === "answers")
        return "共 " + answerEffectiveCount.value + " 条（有效）";
      if (type.value === "questions")
        return "共 " + questionEffectiveCount.value + " 条（有效）";
      return "共 " + total.value + " 条";
    });
    function normalizeType(raw) {
      const valid = [
        "favorites",
        "history",
        "questions",
        "answers",
        "following",
        "followers",
        "topic-following",
        "expert-posts"
      ];
      if (raw && valid.includes(raw))
        return raw;
      return "favorites";
    }
    function resolveAvatar(avatar) {
      if (!avatar)
        return "";
      const raw = String(avatar).trim();
      if (!raw)
        return "";
      if (raw.startsWith("http://") || raw.startsWith("https://"))
        return raw;
      return `${utils_constants.BASE_URL}${raw}`;
    }
    function isInvalidAnswerRow(item) {
      if (type.value !== "answers")
        return false;
      return Number((item == null ? void 0 : item.effective) || 0) !== 1;
    }
    function isInvalidQuestionRow(item) {
      if (type.value !== "questions")
        return false;
      return Number((item == null ? void 0 : item.status) || 0) === 4;
    }
    function rowMainText(item) {
      if (type.value === "favorites") {
        const bizType = Number((item == null ? void 0 : item.bizType) || 1);
        if (bizType === 3)
          return item.questionTitle || item.title || "";
        return item.title || "";
      }
      if (type.value === "answers")
        return `问题：${item.questionTitle || ""}`;
      if (type.value === "following" || type.value === "followers")
        return item.nickname || `用户 ${item.userId}`;
      return item.title || "";
    }
    function rowSubText(item) {
      if (type.value === "favorites") {
        const bizType = Number((item == null ? void 0 : item.bizType) || 1);
        if (bizType === 2) {
          return item.contentPreview || `${item.likeCount || 0} 点赞 ${item.favoriteCount || 0} 收藏`;
        }
        if (bizType === 3) {
          return item.contentPreview || "暂无回答内容";
        }
        return `${item.answerCount || 0} 回答 ${item.likeCount || 0} 点赞`;
      }
      if (type.value === "history")
        return item.subTitle || "";
      if (type.value === "questions")
        return `状态：${item.status}，${item.answerCount || 0} 回答`;
      if (type.value === "answers")
        return isInvalidAnswerRow(item) ? "该回答因违规已删除" : item.contentPreview || "";
      if (type.value === "topic-following")
        return item.subtitle || "点击查看专题详情";
      if (type.value === "expert-posts")
        return `${item.likeCount || 0} 点赞 ${item.viewCount || 0} 浏览`;
      return `专家状态：${item.expertStatus ?? "普通用户"}`;
    }
    function rowTimeText(item) {
      if (type.value === "favorites")
        return item.favoriteAt;
      if (type.value === "history")
        return item.viewedAt;
      if (type.value === "following" || type.value === "followers")
        return item.followedAt;
      if (type.value === "topic-following")
        return item.followedAt;
      return item.createdAt;
    }
    function formatDate(input) {
      if (!input)
        return "";
      const value = String(input).trim();
      if (value.length >= 10)
        return value.slice(0, 10);
      return value;
    }
    function questionStatusText(item) {
      if (item.acceptedAnswerId)
        return "已采纳最佳答案";
      const count = item.answerCount || 0;
      if (count > 0)
        return `已有 ${count} 条回答`;
      return "暂无回答";
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
    function isQuestionSelfOnly(item) {
      return Number(item.status || 0) === 5;
    }
    async function loadAnswerEffectiveCount() {
      if (type.value !== "answers")
        return;
      try {
        const ov = await api_me.meApi.overview();
        answerEffectiveCount.value = Number(ov.answerCount || 0);
      } catch {
        answerEffectiveCount.value = 0;
      }
    }
    async function loadQuestionEffectiveCount() {
      if (type.value !== "questions")
        return;
      try {
        const ov = await api_me.meApi.overview();
        questionEffectiveCount.value = Number(ov.questionCount || 0);
      } catch {
        questionEffectiveCount.value = 0;
      }
    }
    async function fetchPage(reset = false) {
      if (loading.value || !reset && finished.value)
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
          case "topic-following":
            resp = await api_me.meApi.followedTopics(query);
            break;
          case "expert-posts":
            resp = await api_expert.expertApi.myPosts(query);
            break;
        }
        total.value = Number(resp.total || 0);
        const list = resp.list || [];
        if (reset)
          items.value = list;
        else
          items.value = items.value.concat(list);
        pageNum.value = page + 1;
        finished.value = items.value.length >= total.value || list.length < pageSize;
      } catch {
        common_vendor.index.showToast({ title: "列表加载失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    }
    function openRow(item) {
      if (type.value === "questions" && (item == null ? void 0 : item.id)) {
        if (isInvalidQuestionRow(item)) {
          common_vendor.index.showToast({ title: "该提问因违规已下架", icon: "none" });
          return;
        }
        utils_nav.openQuestionDetail(Number(item.id));
        return;
      }
      if (type.value === "favorites") {
        const bizType = Number((item == null ? void 0 : item.bizType) || 1);
        if (bizType === 2 && (item == null ? void 0 : item.bizId)) {
          utils_nav.openExpertPostDetailPage(Number(item.bizId));
          return;
        }
        if (bizType === 3 && ((item == null ? void 0 : item.answerId) || (item == null ? void 0 : item.bizId))) {
          utils_nav.openAnswerDetailPage(Number(item.answerId || item.bizId));
          return;
        }
        if (((item == null ? void 0 : item.questionId) || (item == null ? void 0 : item.bizId)) && bizType === 1) {
          utils_nav.openQuestionDetail(Number(item.questionId || item.bizId));
          return;
        }
      }
      if (type.value === "answers" && (item == null ? void 0 : item.questionId)) {
        if (isInvalidAnswerRow(item)) {
          common_vendor.index.showToast({ title: "该回答因违规已删除", icon: "none" });
          return;
        }
        utils_nav.openQuestionDetail(Number(item.questionId));
        return;
      }
      if (type.value === "history" && (item == null ? void 0 : item.bizType) === 1 && (item == null ? void 0 : item.bizId)) {
        utils_nav.openQuestionDetail(Number(item.bizId));
        return;
      }
      if (type.value === "history" && (item == null ? void 0 : item.bizType) === 2 && (item == null ? void 0 : item.bizId)) {
        utils_nav.openExpertPostDetailPage(Number(item.bizId));
        return;
      }
      if (type.value === "expert-posts" && (item == null ? void 0 : item.id)) {
        utils_nav.openExpertPostDetailPage(Number(item.id));
        return;
      }
      if (type.value === "topic-following" && (item == null ? void 0 : item.topicId)) {
        common_vendor.index.navigateTo({
          url: `/pages/discover/topic-detail?topicId=${item.topicId}&topicTitle=${encodeURIComponent(item.title || "")}`
        });
        return;
      }
      if ((type.value === "following" || type.value === "followers") && (item == null ? void 0 : item.userId)) {
        utils_nav.openUserHomePage(Number(item.userId));
      }
    }
    common_vendor.onLoad(async (options) => {
      if (!utils_authGuard.ensurePageAuth())
        return;
      type.value = normalizeType(options == null ? void 0 : options.type);
      title.value = titleMap[type.value];
      common_vendor.index.setNavigationBarTitle({ title: title.value });
      if (type.value === "answers")
        await loadAnswerEffectiveCount();
      if (type.value === "questions")
        await loadQuestionEffectiveCount();
      fetchPage(true);
    });
    common_vendor.onShow(async () => {
      if (!entered.value) {
        entered.value = true;
        return;
      }
      if (!utils_authGuard.ensurePageAuth())
        return;
      if (type.value === "answers")
        await loadAnswerEffectiveCount();
      if (type.value === "questions")
        await loadQuestionEffectiveCount();
      fetchPage(true);
    });
    common_vendor.onReachBottom(() => {
      fetchPage(false);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(headText.value),
        b: !items.value.length && !loading.value
      }, !items.value.length && !loading.value ? {} : {}, {
        c: isQuestionList.value
      }, isQuestionList.value ? {
        d: common_vendor.f(questionItems.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.n(questionStatusClass(item)),
            b: common_vendor.t(item.title),
            c: isInvalidQuestionRow(item)
          }, isInvalidQuestionRow(item) ? {} : {}, {
            d: isQuestionSelfOnly(item)
          }, isQuestionSelfOnly(item) ? {} : {}, {
            e: questionTags(item).length
          }, questionTags(item).length ? {
            f: common_vendor.f(questionTags(item), (tag, tagIndex, i1) => {
              return {
                a: common_vendor.t(tag),
                b: `${item.id}-${tag}-${tagIndex}`,
                c: common_vendor.n(tagIndex === 0 ? "chip-main" : "chip-sub")
              };
            })
          } : {}, {
            g: common_vendor.t(item.likeCount || 0),
            h: common_vendor.t(item.viewCount || 0),
            i: common_vendor.t(item.createdAt || ""),
            j: isInvalidQuestionRow(item)
          }, isInvalidQuestionRow(item) ? {} : {
            k: common_vendor.t(questionStatusText(item)),
            l: common_vendor.n(questionStatusClass(item))
          }, {
            m: item.id,
            n: isInvalidQuestionRow(item) ? 1 : "",
            o: common_vendor.o(($event) => openRow(item), item.id)
          });
        })
      } : isFollowList.value ? {
        f: common_vendor.f(followItems.value, (item, k0, i0) => {
          return common_vendor.e({
            a: resolveAvatar(item.avatar)
          }, resolveAvatar(item.avatar) ? {
            b: resolveAvatar(item.avatar)
          } : {
            c: common_vendor.t((item.nickname || "用户").slice(0, 1))
          }, {
            d: common_vendor.t(item.nickname || `用户 ${item.userId}`),
            e: common_vendor.t(formatDate(item.followedAt) || "-"),
            f: item.userId,
            g: common_vendor.o(($event) => openRow(item), item.userId)
          });
        })
      } : type.value === "favorites" ? {
        h: common_vendor.f(favoriteGroups.value, (group, k0, i0) => {
          return {
            a: common_vendor.t(group.title),
            b: common_vendor.t(group.items.length),
            c: common_vendor.f(group.items, (item, idx, i1) => {
              return {
                a: common_vendor.t(rowMainText(item)),
                b: common_vendor.t(rowSubText(item)),
                c: common_vendor.t(rowTimeText(item)),
                d: `${group.key}-${item.bizId || idx}`,
                e: common_vendor.o(($event) => openRow(item), `${group.key}-${item.bizId || idx}`)
              };
            }),
            d: group.key
          };
        })
      } : {
        i: common_vendor.f(items.value, (item, idx, i0) => {
          return common_vendor.e({
            a: common_vendor.t(rowMainText(item)),
            b: isInvalidAnswerRow(item)
          }, isInvalidAnswerRow(item) ? {} : {}, {
            c: common_vendor.t(rowSubText(item)),
            d: isInvalidAnswerRow(item) ? 1 : "",
            e: common_vendor.t(rowTimeText(item)),
            f: idx,
            g: isInvalidAnswerRow(item) ? 1 : "",
            h: common_vendor.o(($event) => openRow(item), idx)
          });
        })
      }, {
        e: isFollowList.value,
        g: type.value === "favorites",
        j: loading.value
      }, loading.value ? {} : finished.value && items.value.length ? {} : {}, {
        k: finished.value && items.value.length
      });
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-688829fb"]]);
wx.createPage(MiniProgramPage);
