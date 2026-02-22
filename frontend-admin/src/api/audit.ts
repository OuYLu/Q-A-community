import http from "./http";
import type { Result } from "../types/api";
import type { PageInfo } from "../types/adminUser";
import type {
  CmsAuditBatchReviewDTO,
  CmsAuditDetailVO,
  CmsAuditPageItemVO,
  CmsAuditPageQueryDTO,
  CmsAuditReviewDTO
} from "../types/audit";

export const pageCmsAuditQueue = (query: CmsAuditPageQueryDTO) => {
  return http.get<Result<PageInfo<CmsAuditPageItemVO>>>("/admin/cms/audit/page", { params: query });
};

export const getCmsAuditDetail = (id: number) => {
  return http.get<Result<CmsAuditDetailVO>>(`/admin/cms/audit/${id}`);
};

export const reviewCmsAudit = (id: number, payload: CmsAuditReviewDTO) => {
  return http.post<Result<null>>(`/admin/cms/audit/${id}/review`, payload);
};

export const batchReviewCmsAudit = (payload: CmsAuditBatchReviewDTO) => {
  return http.post<Result<null>>("/admin/cms/audit/batch-review", payload);
};

export const reopenCmsAudit = (id: number) => {
  return http.post<Result<null>>(`/admin/cms/audit/${id}/reopen`);
};
