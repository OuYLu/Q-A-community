import http from "./http";
import type { Result } from "../types/api";
import type { PageInfo } from "../types/adminUser";
import type {
  KbCategorySaveDTO,
  KbCategoryStatusDTO,
  KbCategoryTreeVO,
  KbEntryDetailVO,
  KbEntryPageItemVO,
  KbEntryPageQueryDTO,
  KbEntrySaveDTO,
  KbEntryStatusDTO
} from "../types/kb";

export const getKbCategoryTree = () => {
  return http.get<Result<KbCategoryTreeVO[]>>("/admin/kb/category/tree");
};

export const createKbCategory = (payload: KbCategorySaveDTO) => {
  return http.post<Result<Record<string, number>>>("/admin/kb/category", payload);
};

export const updateKbCategory = (id: number, payload: KbCategorySaveDTO) => {
  return http.put<Result<null>>(`/admin/kb/category/${id}`, payload);
};

export const updateKbCategoryStatus = (id: number, payload: KbCategoryStatusDTO) => {
  return http.put<Result<null>>(`/admin/kb/category/${id}/status`, payload);
};

export const pageKbEntries = (query: KbEntryPageQueryDTO) => {
  return http.get<Result<PageInfo<KbEntryPageItemVO>>>("/admin/kb/entry/page", { params: query });
};

export const getKbEntryDetail = (id: number) => {
  return http.get<Result<KbEntryDetailVO>>(`/admin/kb/entry/${id}`);
};

export const createKbEntry = (payload: KbEntrySaveDTO) => {
  return http.post<Result<Record<string, number>>>("/admin/kb/entry", payload);
};

export const updateKbEntry = (id: number, payload: KbEntrySaveDTO) => {
  return http.put<Result<null>>(`/admin/kb/entry/${id}`, payload);
};

export const updateKbEntryStatus = (id: number, payload: KbEntryStatusDTO) => {
  return http.put<Result<null>>(`/admin/kb/entry/${id}/status`, payload);
};
