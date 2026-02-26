"use strict";
const api_http = require("./http.js");
const discoverApi = {
  getHome() {
    return api_http.request({
      url: "/api/customer/discover/home"
    });
  },
  getCategories() {
    return api_http.request({
      url: "/api/customer/discover/categories"
    });
  },
  getCategoryTree(parentId) {
    return api_http.request({
      url: "/api/customer/discover/categories/tree",
      params: { parentId }
    });
  },
  getHotTopics(limit = 6) {
    return api_http.request({
      url: "/api/customer/discover/topics/hot",
      params: { limit }
    });
  },
  getHotQuestions(limit = 10) {
    return api_http.request({
      url: "/api/customer/discover/rank/hot",
      params: { limit }
    });
  },
  getExperts(limit = 6) {
    return api_http.request({
      url: "/api/customer/discover/experts",
      params: { limit }
    });
  }
};
exports.discoverApi = discoverApi;
