export type CmsAuditAction = "pass" | "reject";

export type CmsAuditPageQueryDTO = {
  page: number;
  pageSize: number;
  bizType?: number | null;
  auditStatus?: number | null;
  triggerSource?: number | null;
  keyword?: string;
  startTime?: string;
  endTime?: string;
  sortBy?: "createdAt" | "modelScore" | "";
  sortOrder?: "asc" | "desc" | "";
};

export type CmsAuditPageItemVO = {
  id: number;
  bizType?: number;
  bizId?: number;
  auditStatus?: number;
  triggerSource?: number;
  title?: string;
  contentTitle?: string;
  contentSummary?: string;
  contentSnippet?: string;
  content?: string;
  submitterId?: number;
  submitterName?: string;
  submitUserId?: number;
  submitUserName?: string;
  modelScore?: number;
  riskLevel?: string;
  riskLabel?: string;
  modelLabel?: string;
  rejectReason?: string;
  action?: CmsAuditAction;
  reviewerId?: number;
  reviewerName?: string;
  auditorId?: number;
  createdAt?: string;
  reviewedAt?: string;
  auditedAt?: string;
  [key: string]: unknown;
};

export type CmsAuditDetailVO = {
  audit: Record<string, unknown>;
  content: {
    title?: string;
    content?: string;
    status?: number;
    rejectReason?: string;
    createdAt?: string;
    [key: string]: unknown;
  };
  author: {
    id?: number;
    username?: string;
    nickname?: string;
    phone?: string;
    email?: string;
    avatar?: string;
    [key: string]: unknown;
  };
};

export type CmsAuditReviewDTO = {
  action: CmsAuditAction;
  rejectReason?: string;
};

export type CmsAuditBatchReviewDTO = {
  ids: number[];
  action: CmsAuditAction;
  rejectReason?: string;
};
