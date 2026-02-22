import http from "./http";
import type { Result } from "../types/api";
import type { PageInfo } from "../types/adminUser";
import type { CmsReportDetailVO, CmsReportHandleDTO, CmsReportPageItemVO, CmsReportPageQueryDTO } from "../types/report";

export const pageCmsReport = (query: CmsReportPageQueryDTO) => {
  return http.get<Result<PageInfo<CmsReportPageItemVO>>>("/admin/cms/report/page", { params: query });
};

export const getCmsReportDetail = (id: number) => {
  return http.get<Result<CmsReportDetailVO>>(`/admin/cms/report/${id}`);
};

export const handleCmsReport = (id: number, payload: CmsReportHandleDTO) => {
  return http.post<Result<null>>(`/admin/cms/report/${id}/handle`, payload);
};

export const transferReportToAudit = (id: number) => {
  return http.post<Result<null>>(`/admin/cms/report/${id}/to-audit`);
};
