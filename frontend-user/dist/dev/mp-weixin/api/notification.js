"use strict";
const api_http = require("./http.js");
const notificationApi = {
  list(params) {
    return api_http.request({
      url: "/api/customer/notifications",
      params
    });
  },
  unreadCount() {
    return api_http.request({
      url: "/api/customer/notifications/unread-count"
    });
  },
  read(id) {
    return api_http.request({
      url: `/api/customer/notifications/${id}/read`,
      method: "POST"
    });
  },
  readAll() {
    return api_http.request({
      url: "/api/customer/notifications/read-all",
      method: "POST"
    });
  }
};
exports.notificationApi = notificationApi;
