import http from "./http";
import type { Result } from "../types/api";
import type { User } from "../types/user";
import type {
  AdminCreateStaffDTO,
  ExpertApply,
  ExpertApplyDetailVO,
  ExpertApplyQueryDTO,
  ExpertManageVO,
  ExpertReviewDTO,
  ExpertStatusUpdateDTO,
  ExpertUserQueryDTO,
  Permission,
  PermissionQueryDTO,
  PermissionSaveDTO,
  PageInfo,
  Role,
  RoleCreateDTO,
  RolePermUpdateDTO,
  RoleQueryDTO,
  RoleUpdateDTO,
  UserManageVO,
  UserQueryDTO,
  UserStatusDTO,
  UserUpdateDTO
} from "../types/adminUser";

export const fetchMe = () => {
  return http.get<Result<User>>("/me");
};

export const listUsers = (query: UserQueryDTO) => {
  return http.get<Result<PageInfo<UserManageVO>>>("/admin/user/list", { params: query });
};

export const createStaff = (payload: AdminCreateStaffDTO) => {
  return http.post<Result<User>>("/admin/staff", payload);
};

export const updateUser = (id: number, payload: UserUpdateDTO) => {
  return http.put<Result<null>>(`/admin/user/${id}`, payload);
};

export const updateUserStatus = (id: number, payload: UserStatusDTO) => {
  return http.put<Result<null>>(`/admin/user/${id}/status`, payload);
};

export const getUserDetail = (id: number) => {
  return http.get<Result<UserManageVO>>(`/admin/user/${id}`);
};

export const deleteUser = (id: number) => {
  return http.delete<Result<null>>(`/admin/user/${id}`);
};

export const listExpertApplies = (query: ExpertApplyQueryDTO) => {
  return http.get<Result<PageInfo<ExpertApply>>>("/admin/expert/apply/list", { params: query });
};

export const reviewExpertApply = (payload: ExpertReviewDTO) => {
  return http.post<Result<null>>("/admin/expert/apply/review", payload);
};

export const getExpertApplyDetail = (userId: number) => {
  return http.get<Result<ExpertApplyDetailVO>>(`/admin/expert/apply/user/${userId}`);
};

export const listExpertUsers = (query: ExpertUserQueryDTO) => {
  return http.get<Result<PageInfo<ExpertManageVO>>>("/admin/expert/user/list", { params: query });
};

export const updateExpertStatus = (id: number, payload: ExpertStatusUpdateDTO) => {
  return http.put<Result<null>>(`/admin/expert/user/${id}/status`, payload);
};

export const getExpertUserDetail = (id: number) => {
  return http.get<Result<ExpertApplyDetailVO>>(`/admin/expert/user/${id}/detail`);
};

export const listRoles = (query: RoleQueryDTO) => {
  return http.get<Result<PageInfo<Role>>>("/admin/role/list", { params: query });
};

export const createRole = (payload: RoleCreateDTO) => {
  return http.post<Result<Role>>("/admin/role", payload);
};

export const getRoleDetail = (id: number) => {
  return http.get<Result<Role>>(`/admin/role/${id}`);
};

export const updateRole = (id: number, payload: RoleUpdateDTO) => {
  return http.put<Result<Role>>(`/admin/role/${id}`, payload);
};

export const deleteRole = (id: number) => {
  return http.delete<Result<null>>(`/admin/role/${id}`);
};

export const listRolePermissions = (id: number, pageNum = 1, pageSize = 100) => {
  return http.get<Result<PageInfo<Permission>>>(`/admin/role/${id}/perm/list`, { params: { pageNum, pageSize } });
};

export const updateRolePermissions = (payload: RolePermUpdateDTO) => {
  return http.put<Result<null>>("/admin/role/perm", payload);
};

export const listPermissions = (query: PermissionQueryDTO) => {
  return http.get<Result<PageInfo<Permission>>>("/admin/perm/list", { params: query });
};

export const createPermission = (payload: PermissionSaveDTO) => {
  return http.post<Result<Permission>>("/admin/perm", payload);
};

export const getPermissionDetail = (id: number) => {
  return http.get<Result<Permission>>(`/admin/perm/${id}`);
};

export const updatePermission = (id: number, payload: PermissionSaveDTO) => {
  return http.put<Result<Permission>>(`/admin/perm/${id}`, payload);
};

export const deletePermission = (id: number) => {
  return http.delete<Result<null>>(`/admin/perm/${id}`);
};
