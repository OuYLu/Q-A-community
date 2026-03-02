"use strict";
const api_http = require("./http.js");
const utils_constants = require("../utils/constants.js");
function toAbsoluteUrl(url) {
  if (!url)
    return "";
  const text = url.trim();
  if (!text)
    return "";
  if (text.startsWith("http://") || text.startsWith("https://"))
    return text;
  if (text.startsWith("/"))
    return `${utils_constants.BASE_URL}${text}`;
  return `${utils_constants.BASE_URL}/${text}`;
}
function normalizeCategory(item) {
  return {
    ...item,
    icon: toAbsoluteUrl(item.icon)
  };
}
function normalizeCategoryTree(node) {
  var _a;
  return {
    ...node,
    icon: toAbsoluteUrl(node.icon),
    children: (_a = node.children) == null ? void 0 : _a.map((x) => normalizeCategoryTree(x))
  };
}
function normalizeTopic(item) {
  return {
    ...item,
    coverImg: toAbsoluteUrl(item.coverImg)
  };
}
function normalizeHotQuestion(item) {
  return {
    ...item,
    authorAvatar: toAbsoluteUrl(item.authorAvatar)
  };
}
function normalizeExpert(item) {
  return {
    ...item,
    avatar: toAbsoluteUrl(item.avatar)
  };
}
const discoverApi = {
  getHome() {
    return api_http.request({
      url: "/api/customer/discover/home"
    }).then((data) => ({
      ...data,
      categories: ((data == null ? void 0 : data.categories) || []).map((x) => normalizeCategory(x)),
      hotTopics: ((data == null ? void 0 : data.hotTopics) || []).map((x) => normalizeTopic(x)),
      hotQuestions: ((data == null ? void 0 : data.hotQuestions) || []).map((x) => normalizeHotQuestion(x)),
      experts: ((data == null ? void 0 : data.experts) || []).map((x) => normalizeExpert(x))
    }));
  },
  getCategories() {
    return api_http.request({
      url: "/api/customer/discover/categories"
    }).then((data) => (data || []).map((x) => normalizeCategory(x)));
  },
  getCategoryTree(parentId) {
    return api_http.request({
      url: "/api/customer/discover/categories/tree",
      params: { parentId }
    }).then((data) => (data || []).map((x) => normalizeCategoryTree(x)));
  },
  getHotTopics(limit = 6) {
    return api_http.request({
      url: "/api/customer/discover/topics/hot",
      params: { limit }
    }).then((data) => (data || []).map((x) => normalizeTopic(x)));
  },
  getHotQuestions(limit = 10) {
    return api_http.request({
      url: "/api/customer/discover/rank/hot",
      params: { limit }
    }).then((data) => (data || []).map((x) => normalizeHotQuestion(x)));
  },
  getExperts(limit = 6) {
    return api_http.request({
      url: "/api/customer/discover/experts",
      params: { limit }
    }).then((data) => (data || []).map((x) => normalizeExpert(x)));
  },
  getQuestionPage(params) {
    return api_http.request({
      url: "/api/customer/discover/questions",
      params
    });
  }
};
exports.discoverApi = discoverApi;
