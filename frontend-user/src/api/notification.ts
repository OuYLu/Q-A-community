import { request } from "@/api/http";

export interface PageInfo<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface AppNotificationItemVO {
  id: number;
  type: number;
  bizType: number;
  bizId: number;
  title: string;
  content: string;
  isRead: number;
  createdAt: string;
}

export interface AppNotificationTypeCountVO {
  type: number;
  cnt: number;
}

export interface AppNotificationUnreadCountVO {
  total: number;
  byType: AppNotificationTypeCountVO[];
}

export interface AppReportFeedbackDetailVO {
  reportId: number;
  bizType: number;
  bizId: number;
  contentTitle: string;
  contentText: string;
  reasonCode: string;
  reasonDetail: string;
  status: number;
  handleAction: number;
  handleResult: string;
  reporterId: number;
  reporterName: string;
  authorId: number;
  authorName: string;
  handlerId: number;
  handlerName: string;
  createdAt: string;
  handledAt: string;
}

export const notificationApi = {
  list(params: { type?: number; types?: string; isRead?: number; page?: number; pageSize?: number }) {
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
  },
  reportFeedbackDetail(reportId: number) {
    return request<AppReportFeedbackDetailVO>({
      url: `/api/customer/notifications/report-feedback/${reportId}`
    });
  }
};
