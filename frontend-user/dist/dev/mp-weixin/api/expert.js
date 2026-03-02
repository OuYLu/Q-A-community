"use strict";
const api_http = require("./http.js");
const expertApi = {
  apply(data) {
    return api_http.request({
      url: "/api/expert/apply",
      method: "POST",
      data
    });
  },
  createPost(data) {
    return api_http.request({
      url: "/api/expert/posts",
      method: "POST",
      data
    });
  },
  categories() {
    return api_http.request({
      url: "/api/expert/posts/categories"
    });
  },
  page(params) {
    return api_http.request({
      url: "/api/expert/posts",
      params
    });
  },
  myPosts(params) {
    return api_http.request({
      url: "/api/expert/posts/my",
      params
    });
  },
  detail(id) {
    return api_http.request({
      url: `/api/expert/posts/${id}`
    });
  }
};
exports.expertApi = expertApi;
