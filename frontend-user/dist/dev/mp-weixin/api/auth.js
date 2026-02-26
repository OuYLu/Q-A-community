"use strict";
const api_http = require("./http.js");
const authApi = {
  login(data) {
    return api_http.request({
      url: "/api/auth/login",
      method: "POST",
      data,
      withAuth: false
    });
  },
  register(data) {
    return api_http.request({
      url: "/api/customer/register",
      method: "POST",
      data,
      withAuth: false
    });
  },
  wechatLogin(data) {
    return api_http.request({
      url: "/api/auth/wechat/login",
      method: "POST",
      data,
      withAuth: false
    });
  },
  wechatBindPhone(data) {
    return api_http.request({
      url: "/api/auth/wechat/bind-phone",
      method: "POST",
      data,
      withAuth: false
    });
  }
};
exports.authApi = authApi;
