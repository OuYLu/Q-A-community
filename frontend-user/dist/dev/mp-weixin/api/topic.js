"use strict";
const api_http = require("./http.js");
const topicApi = {
  list(params) {
    return api_http.request({
      url: "/api/customer/topics",
      params
    });
  },
  detail(id) {
    return api_http.request({
      url: `/api/customer/topics/${id}`
    });
  },
  questions(id, params) {
    return api_http.request({
      url: `/api/customer/topics/${id}/questions`,
      params
    });
  },
  follow(id) {
    return api_http.request({
      url: `/api/customer/topics/${id}/follow`,
      method: "POST"
    });
  },
  unfollow(id) {
    return api_http.request({
      url: `/api/customer/topics/${id}/follow`,
      method: "DELETE"
    });
  }
};
exports.topicApi = topicApi;
