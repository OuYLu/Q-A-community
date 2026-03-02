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
function normalizeHome(data) {
  return {
    userId: Number((data == null ? void 0 : data.userId) || 0),
    nickname: (data == null ? void 0 : data.nickname) || "用户",
    avatar: toAbsoluteUrl(data == null ? void 0 : data.avatar),
    slogan: (data == null ? void 0 : data.slogan) || "",
    expertStatus: data == null ? void 0 : data.expertStatus,
    questionCount: Number((data == null ? void 0 : data.questionCount) || 0),
    answerCount: Number((data == null ? void 0 : data.answerCount) || 0),
    expertPostCount: Number((data == null ? void 0 : data.expertPostCount) || 0),
    followerCount: Number((data == null ? void 0 : data.followerCount) || 0),
    followingCount: Number((data == null ? void 0 : data.followingCount) || 0),
    followed: !!(data == null ? void 0 : data.followed),
    self: !!(data == null ? void 0 : data.self)
  };
}
const userApi = {
  home(userId) {
    return api_http.request({
      url: `/api/customer/users/${userId}/home`
    }).then((data) => normalizeHome(data));
  },
  follow(userId) {
    return api_http.request({
      url: `/api/customer/users/${userId}/follow`,
      method: "POST"
    });
  },
  unfollow(userId) {
    return api_http.request({
      url: `/api/customer/users/${userId}/follow`,
      method: "DELETE"
    });
  },
  answers(userId, params) {
    return api_http.request({
      url: `/api/customer/users/${userId}/answers`,
      params
    });
  },
  expertPosts(userId, params) {
    return api_http.request({
      url: `/api/customer/users/${userId}/expert-posts`,
      params
    });
  }
};
exports.userApi = userApi;
