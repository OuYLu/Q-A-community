"use strict";
const api_http = require("./http.js");
const utils_constants = require("../utils/constants.js");
function toAbsoluteUrl(url) {
  if (!url)
    return "";
  if (url.startsWith("http://") || url.startsWith("https://"))
    return url;
  if (url.startsWith("/"))
    return `${utils_constants.BASE_URL}${url}`;
  return `${utils_constants.BASE_URL}/${url}`;
}
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
  },
  kbInteraction(id) {
    return api_http.request({
      url: `/api/customer/kb/${id}/interaction`
    });
  },
  toggleKbLike(id) {
    return api_http.request({
      url: `/api/customer/kb/${id}/like`,
      method: "POST"
    });
  },
  toggleKbFavorite(id) {
    return api_http.request({
      url: `/api/customer/kb/${id}/favorite`,
      method: "POST"
    });
  },
  kbComments(id) {
    return api_http.request({
      url: `/api/customer/kb/${id}/comments`
    }).then(
      (list) => (list || []).map((item) => ({
        ...item,
        authorAvatar: toAbsoluteUrl(item == null ? void 0 : item.authorAvatar)
      }))
    );
  },
  createKbComment(id, content, parentId) {
    return api_http.request({
      url: `/api/customer/kb/${id}/comments`,
      method: "POST",
      data: { content, parentId }
    });
  }
};
exports.expertApi = expertApi;
