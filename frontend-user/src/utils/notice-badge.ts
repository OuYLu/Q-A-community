import { AUTH_EXPIRES_KEY, BASE_URL, TOKEN_KEY } from "@/utils/constants";

const NOTICE_TAB_INDEX = 3;

type UnreadWrap = {
  code?: number;
  data?: {
    total?: number;
  };
};

function hasValidToken() {
  const token = uni.getStorageSync(TOKEN_KEY) as string;
  const expiresAt = Number(uni.getStorageSync(AUTH_EXPIRES_KEY) || 0);
  return Boolean(token) && expiresAt > Date.now();
}

function getToken() {
  return (uni.getStorageSync(TOKEN_KEY) as string) || "";
}

export function hideNoticeTabDot() {
  uni.hideTabBarRedDot({ index: NOTICE_TAB_INDEX });
  uni.removeTabBarBadge({ index: NOTICE_TAB_INDEX });
}

export function syncNoticeTabDot(total: number) {
  if (total > 0) {
    uni.showTabBarRedDot({ index: NOTICE_TAB_INDEX });
  } else {
    hideNoticeTabDot();
  }
}

export async function refreshNoticeTabDot() {
  if (!hasValidToken()) {
    hideNoticeTabDot();
    return;
  }
  const token = getToken();
  try {
    const resp = await new Promise<UnreadWrap>((resolve, reject) => {
      uni.request({
        url: `${BASE_URL}/api/customer/notifications/unread-count`,
        method: "GET",
        header: {
          Authorization: `Bearer ${token}`
        },
        success: (res) => resolve((res.data || {}) as UnreadWrap),
        fail: reject
      });
    });
    const total = Number(resp?.data?.total || 0);
    syncNoticeTabDot(total);
  } catch {
    hideNoticeTabDot();
  }
}
