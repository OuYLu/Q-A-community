"use strict";
const api_http = require("./http.js");
const meApi = {
  overview() {
    return api_http.request({
      url: "/api/customer/me/overview"
    });
  },
  updateProfile(data) {
    return api_http.request({
      url: "/api/customer/me/profile",
      method: "PUT",
      data
    });
  },
  setFirstPassword(data) {
    return api_http.request({
      url: "/api/customer/me/password/set-first",
      method: "POST",
      data
    });
  },
  changePassword(data) {
    return api_http.request({
      url: "/api/customer/me/password/change",
      method: "POST",
      data
    });
  },
  favorites(params) {
    return api_http.request({
      url: "/api/customer/me/favorites",
      params
    });
  },
  history(params) {
    return api_http.request({
      url: "/api/customer/me/history",
      params
    });
  },
  questions(params) {
    return api_http.request({
      url: "/api/customer/me/questions",
      params
    });
  },
  answers(params) {
    return api_http.request({
      url: "/api/customer/me/answers",
      params
    });
  },
  following(params) {
    return api_http.request({
      url: "/api/customer/me/following",
      params
    });
  },
  followers(params) {
    return api_http.request({
      url: "/api/customer/me/followers",
      params
    });
  },
  followedTopics(params) {
    return api_http.request({
      url: "/api/customer/me/topics/following",
      params
    });
  },
  privacy() {
    return api_http.request({
      url: "/api/customer/me/privacy"
    });
  },
  updatePrivacy(data) {
    return api_http.request({
      url: "/api/customer/me/privacy",
      method: "PUT",
      data
    });
  },
  exportData() {
    return api_http.request({
      url: "/api/customer/me/export"
    });
  },
  submitCancelRequest(data) {
    return api_http.request({
      url: "/api/customer/me/cancel-request",
      method: "POST",
      data
    });
  },
  latestCancelRequest() {
    return api_http.request({
      url: "/api/customer/me/cancel-request/latest"
    });
  },
  doc(type) {
    return api_http.request({
      url: `/api/customer/me/docs/${type}`
    });
  }
};
exports.meApi = meApi;
