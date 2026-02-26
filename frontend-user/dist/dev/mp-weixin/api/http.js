"use strict";
const common_vendor = require("../common/vendor.js");
const utils_constants = require("../utils/constants.js");
const stores_auth = require("../stores/auth.js");
function toQuery(params) {
  if (!params)
    return "";
  const query = Object.entries(params).filter(([, value]) => value !== void 0 && value !== null && value !== "").map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`).join("&");
  return query ? `?${query}` : "";
}
function request(options) {
  const authStore = stores_auth.useAuthStore();
  const header = {};
  if (options.withAuth !== false && authStore.token) {
    header.Authorization = `Bearer ${authStore.token}`;
  }
  const url = `${utils_constants.BASE_URL}${options.url}${toQuery(options.params)}`;
  return new Promise((resolve, reject) => {
    common_vendor.index.request({
      url,
      method: options.method || "GET",
      data: options.data,
      header,
      success: (res) => {
        const statusCode = res.statusCode || 500;
        if (statusCode < 200 || statusCode >= 300) {
          reject(new Error(`HTTP ${statusCode}`));
          return;
        }
        const body = res.data || {};
        if (typeof body.code === "number" && body.code !== 200 && body.code !== 0) {
          reject(new Error(body.message || body.msg || "请求失败"));
          return;
        }
        if (body && typeof body === "object" && "data" in body) {
          resolve(body.data);
          return;
        }
        resolve(res.data);
      },
      fail: (err) => reject(err)
    });
  });
}
exports.request = request;
