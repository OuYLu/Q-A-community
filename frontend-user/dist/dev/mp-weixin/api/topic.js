"use strict";
const api_http = require("./http.js");
const utils_constants = require("../utils/constants.js");
function toAbsoluteUrl(url) {
  if (!url)
    return "";
  const text = url.trim();
  if (!text)
    return "";
  if (text.startsWith("http://") || text.startsWith("https://"))
    return text;
  if (text.startsWith("/"))
    return `${utils_constants.BASE_URL}${text}`;
  return `${utils_constants.BASE_URL}/${text}`;
}
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
    }).then((data) => ({
      ...data,
      coverImg: toAbsoluteUrl(data == null ? void 0 : data.coverImg)
    }));
  },
  questions(id, params) {
    return api_http.request({
      url: `/api/customer/topics/${id}/questions`,
      params
    });
  },
  createTopicQuestion(id, data) {
    return api_http.request({
      url: `/api/customer/topics/${id}/questions`,
      method: "POST",
      data
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
