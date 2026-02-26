"use strict";
const api_http = require("./http.js");
const searchApi = {
  search(params) {
    return api_http.request({
      url: "/api/customer/search",
      params
    });
  },
  hot(limit = 10) {
    return api_http.request({
      url: "/api/customer/search/hot",
      params: { limit }
    });
  },
  history(limit = 10) {
    return api_http.request({
      url: "/api/customer/search/history",
      params: { limit }
    });
  },
  clearHistory() {
    return api_http.request({
      url: "/api/customer/search/history",
      method: "DELETE"
    });
  },
  logSearch(data) {
    return api_http.request({
      url: "/api/customer/search/log",
      method: "POST",
      data
    });
  }
};
exports.searchApi = searchApi;
