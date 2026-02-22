export type CmsReportPageQueryDTO = {
  bizType?: number | null;
  status?: number | null;
  reasonType?: number | null;
  keyword?: string;
  startTime?: string;
  endTime?: string;
  page: number;
  pageSize: number;
};

export type CmsReportPageItemVO = {
  id: number;
  bizType?: number;
  bizId?: number;
  reasonType?: number;
  reasonCode?: string;
  reasonDetail?: string;
  reporterId?: number;
  createdAt?: string;
  status?: number;
  contentTitle?: string;
  contentSnippet?: string;
  contentStatus?: number;
  authorId?: number;
  authorName?: string;
  handlerId?: number;
  handledAt?: string;
  handleAction?: number;
  handleResult?: string;
};

export type CmsReportVO = {
  id?: number;
  bizType?: number;
  bizId?: number;
  reasonType?: number;
  reporterId?: number;
  reasonCode?: string;
  reasonDetail?: string;
  status?: number;
  handlerId?: number;
  handleAction?: number;
  handleResult?: string;
  handledAt?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type CmsReportContentVO = {
  bizType?: number;
  bizId?: number;
  title?: string;
  content?: string;
  status?: number;
  rejectReason?: string;
  createdAt?: string;
};

export type CmsReportAuthorVO = {
  id?: number;
  username?: string;
  nickname?: string;
  status?: number;
};

export type CmsReportDetailVO = {
  report: CmsReportVO;
  content: CmsReportContentVO;
  author: CmsReportAuthorVO;
};

export type CmsReportHandleDTO = {
  handleAction: 1 | 2 | 3 | 4;
  handleResult?: string;
};
