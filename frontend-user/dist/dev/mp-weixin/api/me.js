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
  doc(type) {
    return api_http.request({
      url: `/api/customer/me/docs/${type}`
    });
  }
};
exports.meApi = meApi;
