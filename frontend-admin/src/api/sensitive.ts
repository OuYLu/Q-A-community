import http from "./http";
import type { Result } from "../types/api";
import type { PageInfo } from "../types/adminUser";
import type { CmsSensitiveWord, CmsSensitiveWordQueryDTO, CmsSensitiveWordSaveDTO } from "../types/sensitive";

export const listSensitiveWords = (query: CmsSensitiveWordQueryDTO) => {
  return http.get<Result<PageInfo<CmsSensitiveWord>>>("/admin/cms/sensitive-word/list", { params: query });
};

export const createSensitiveWord = (payload: CmsSensitiveWordSaveDTO) => {
  return http.post<Result<CmsSensitiveWord>>("/admin/cms/sensitive-word", payload);
};

export const getSensitiveWordDetail = (id: number) => {
  return http.get<Result<CmsSensitiveWord>>(`/admin/cms/sensitive-word/${id}`);
};

export const updateSensitiveWord = (id: number, payload: CmsSensitiveWordSaveDTO) => {
  return http.put<Result<CmsSensitiveWord>>(`/admin/cms/sensitive-word/${id}`, payload);
};

export const batchEnableSensitiveWords = (ids: number[]) => {
  return http.put<Result<null>>("/admin/cms/sensitive-word/enable/batch", { ids });
};

export const batchDisableSensitiveWords = (ids: number[]) => {
  return http.put<Result<null>>("/admin/cms/sensitive-word/disable/batch", { ids });
};

