import http from "./http";
import type { Result } from "../types/api";
import type { PageInfo } from "../types/adminUser";
import type { QaTag, QaTagQueryDTO, QaTagSaveDTO, TagDetailExtra, TagUsageTrendPoint } from "../types/tag";

export const listTags = (query: QaTagQueryDTO) => {
  return http.get<Result<PageInfo<QaTag>>>("/admin/tag/list", { params: query });
};

export const createTag = (payload: QaTagSaveDTO) => {
  return http.post<Result<QaTag>>("/admin/tag", payload);
};

export const getTagDetail = (id: number) => {
  return http.get<Result<QaTag>>(`/admin/tag/${id}`);
};

export const updateTag = (id: number, payload: QaTagSaveDTO) => {
  return http.put<Result<QaTag>>(`/admin/tag/${id}`, payload);
};

export const deleteTag = (id: number) => {
  return http.delete<Result<null>>(`/admin/tag/${id}`);
};

export const batchEnableTags = (ids: number[]) => {
  return http.put<Result<null>>("/admin/tag/enable/batch", { ids });
};

export const batchDisableTags = (ids: number[]) => {
  return http.put<Result<null>>("/admin/tag/disable/batch", { ids });
};

export const getTagDetailExtra = (id: number) => {
  return http.get<Result<TagDetailExtra>>(`/admin/tag/${id}/detail-extra`);
};

export const getTagUsageTrend = (id: number, days = 7) => {
  return http.get<Result<TagUsageTrendPoint[]>>(`/admin/tag/${id}/usage-trend`, { params: { days } });
};
