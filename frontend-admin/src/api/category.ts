import http from "./http";
import type { Result } from "../types/api";
import type { PageInfo } from "../types/adminUser";
import type { CategoryTreeVO, QaCategory, QaCategoryQueryDTO, QaCategorySaveDTO } from "../types/category";

export const listCategories = (query: QaCategoryQueryDTO) => {
  return http.get<Result<PageInfo<QaCategory>>>("/admin/category/list", { params: query });
};

export const createCategory = (payload: QaCategorySaveDTO) => {
  return http.post<Result<QaCategory>>("/admin/category", payload);
};

export const getCategoryDetail = (id: number) => {
  return http.get<Result<QaCategory>>(`/admin/category/${id}`);
};

export const updateCategory = (id: number, payload: QaCategorySaveDTO) => {
  return http.put<Result<QaCategory>>(`/admin/category/${id}`, payload);
};

export const deleteCategory = (id: number) => {
  return http.delete<Result<null>>(`/admin/category/${id}`);
};

export const listCategoryTree = (parentId?: number) => {
  return http.get<Result<CategoryTreeVO[]>>("/admin/category/tree", { params: { parentId } });
};
