"use strict";
const common_vendor = require("../common/vendor.js");
const utils_constants = require("./constants.js");
const NOTICE_TAB_INDEX = 3;
function hasValidToken() {
  const token = common_vendor.index.getStorageSync(utils_constants.TOKEN_KEY);
  const expiresAt = Number(common_vendor.index.getStorageSync(utils_constants.AUTH_EXPIRES_KEY) || 0);
  return Boolean(token) && expiresAt > Date.now();
}
function getToken() {
  return common_vendor.index.getStorageSync(utils_constants.TOKEN_KEY) || "";
}
function hideNoticeTabDot() {
  common_vendor.index.hideTabBarRedDot({ index: NOTICE_TAB_INDEX });
  common_vendor.index.removeTabBarBadge({ index: NOTICE_TAB_INDEX });
}
function syncNoticeTabDot(total) {
  if (total > 0) {
    common_vendor.index.showTabBarRedDot({ index: NOTICE_TAB_INDEX });
  } else {
    hideNoticeTabDot();
  }
}
async function refreshNoticeTabDot() {
  var _a;
  if (!hasValidToken()) {
    hideNoticeTabDot();
    return;
  }
  const token = getToken();
  try {
    const resp = await new Promise((resolve, reject) => {
      common_vendor.index.request({
        url: `${utils_constants.BASE_URL}/api/customer/notifications/unread-count`,
        method: "GET",
        header: {
          Authorization: `Bearer ${token}`
        },
        success: (res) => resolve(res.data || {}),
        fail: reject
      });
    });
    const total = Number(((_a = resp == null ? void 0 : resp.data) == null ? void 0 : _a.total) || 0);
    syncNoticeTabDot(total);
  } catch {
    hideNoticeTabDot();
  }
}
exports.refreshNoticeTabDot = refreshNoticeTabDot;
exports.syncNoticeTabDot = syncNoticeTabDot;
