import { request } from "@/api/http";

export interface PageInfo<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface AppNotificationItemVO {
  id: number;
  type: string;
  bizType: string;
  bizId: number;
  title: string;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export interface AppNotificationTypeCountVO {
  type: string;
  cnt: number;
}

export interface AppNotificationUnreadCountVO {
  total: number;
  byType: AppNotificationTypeCountVO[];
}

export const notificationApi = {
  list(params: { type?: string; isRead?: boolean; page?: number; pageSize?: number }) {
    return request<PageInfo<AppNotificationItemVO>>({
      url: "/api/customer/notifications",
      params
    });
  },
  unreadCount() {
    return request<AppNotificationUnreadCountVO>({
      url: "/api/customer/notifications/unread-count"
    });
  },
  read(id: number) {
    return request<void>({
      url: `/api/customer/notifications/${id}/read`,
      method: "POST"
    });
  },
  readAll() {
    return request<void>({
      url: "/api/customer/notifications/read-all",
      method: "POST"
    });
  }
};
