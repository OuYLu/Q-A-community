"use strict";
const api_http = require("./http.js");
const utils_constants = require("../utils/constants.js");
function toAbsoluteUrl(url) {
  if (!url)
    return "";
  if (url.startsWith("http://") || url.startsWith("https://"))
    return url;
  if (url.startsWith("/"))
    return `${utils_constants.BASE_URL}${url}`;
  return `${utils_constants.BASE_URL}/${url}`;
}
function normalizeImageUrls(urls) {
  if (!urls || !urls.length)
    return [];
  return urls.map((u) => toAbsoluteUrl(u)).filter(Boolean);
}
function normalizeAvatar(url) {
  return toAbsoluteUrl(url);
}
function normalizeAnswerItem(item) {
  return {
    ...item || {},
    authorAvatar: normalizeAvatar(item == null ? void 0 : item.authorAvatar),
    imageUrls: normalizeImageUrls(item == null ? void 0 : item.imageUrls)
  };
}
function normalizeAnswerDetail(data) {
  return {
    ...data || {},
    answer: normalizeAnswerItem(data == null ? void 0 : data.answer),
    comments: ((data == null ? void 0 : data.comments) || []).map((item) => ({
      ...item,
      authorAvatar: normalizeAvatar(item == null ? void 0 : item.authorAvatar)
    }))
  };
}
const questionApi = {
  createQuestion(data) {
    return api_http.request({
      url: "/api/customer/questions",
      method: "POST",
      data
    });
  },
  detail(id) {
    return api_http.request({
      url: `/api/customer/questions/${id}`
    }).then((data) => ({
      ...data,
      imageUrls: normalizeImageUrls(data == null ? void 0 : data.imageUrls),
      authorAvatar: normalizeAvatar(data == null ? void 0 : data.authorAvatar),
      answers: ((data == null ? void 0 : data.answers) || []).map((item) => normalizeAnswerItem(item))
    }));
  },
  page(params) {
    return api_http.request({
      url: "/api/customer/questions",
      params
    }).then((data) => ({
      ...data,
      list: ((data == null ? void 0 : data.list) || []).map((item) => ({
        ...item,
        authorAvatar: normalizeAvatar(item == null ? void 0 : item.authorAvatar),
        imageUrls: normalizeImageUrls(item == null ? void 0 : item.imageUrls)
      }))
    }));
  },
  myQuestions(params) {
    return api_http.request({
      url: "/api/customer/questions/my",
      params
    }).then((data) => ({
      ...data,
      list: ((data == null ? void 0 : data.list) || []).map((item) => ({
        ...item,
        imageUrls: normalizeImageUrls(item == null ? void 0 : item.imageUrls)
      }))
    }));
  },
  createAnswer(questionId, data) {
    return api_http.request({
      url: `/api/customer/questions/${questionId}/answers`,
      method: "POST",
      data
    });
  },
  toggleLike(questionId) {
    return api_http.request({
      url: `/api/customer/questions/${questionId}/like`,
      method: "POST"
    });
  },
  toggleFavorite(questionId) {
    return api_http.request({
      url: `/api/customer/questions/${questionId}/favorite`,
      method: "POST"
    });
  },
  answerDetail(answerId) {
    return api_http.request({
      url: `/api/customer/answers/${answerId}`
    }).then((data) => normalizeAnswerDetail(data));
  },
  toggleAnswerLike(answerId) {
    return api_http.request({
      url: `/api/customer/answers/${answerId}/like`,
      method: "POST"
    }).then((data) => normalizeAnswerDetail(data));
  },
  toggleAnswerFavorite(answerId) {
    return api_http.request({
      url: `/api/customer/answers/${answerId}/favorite`,
      method: "POST"
    }).then((data) => normalizeAnswerDetail(data));
  },
  recommendBest(questionId, answerId) {
    return api_http.request({
      url: `/api/customer/questions/${questionId}/answers/${answerId}/recommend`,
      method: "POST"
    });
  },
  answerComments(answerId) {
    return api_http.request({
      url: `/api/customer/answers/${answerId}/comments`
    });
  },
  createAnswerComment(answerId, content, parentId) {
    return api_http.request({
      url: `/api/customer/answers/${answerId}/comments`,
      method: "POST",
      data: { content, parentId }
    });
  }
};
exports.questionApi = questionApi;
