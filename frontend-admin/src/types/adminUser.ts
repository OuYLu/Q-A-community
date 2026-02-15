export type PageInfo<T> = {
  total: number;
  list: T[];
  pageNum: number;
  pageSize: number;
  size?: number;
  pages?: number;
  hasPreviousPage?: boolean;
  hasNextPage?: boolean;
};

export type UserManageVO = {
  id: number;
  username: string;
  nickname?: string;
  phone?: string;
  email?: string;
  avatar?: string;
  status: number;
  roleCodes: string[];
  displayRole?: string;
  createdAt?: string;
};

export type UserQueryDTO = {
  username?: string;
  nickname?: string;
  status?: number | null;
  pageNum?: number;
  pageSize?: number;
  role?: string;
  roleCode?: string;
};

export type UserUpdateDTO = {
  username?: string;
  nickname?: string;
  phone?: string;
  email?: string;
  avatar?: string;
};

export type UserStatusDTO = {
  status: number;
};

export type AdminCreateStaffDTO = {
  username: string;
  password: string;
  nickname?: string;
  phone?: string;
  email?: string;
  avatar?: string;
};

export type ExpertApply = {
  id: number;
  userId: number;
  realName: string;
  organization?: string;
  title?: string;
  expertise?: string;
  proofUrls?: unknown;
  status: number;
  rejectReason?: string;
  reviewerId?: number;
  reviewAt?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type ProofFileItem = {
  url: string;
  mime?: string;
  name?: string;
  size?: number;
};

export type ProofUrlsMap = Record<string, ProofFileItem[]>;

export type ExpertApplyDetailVO = {
  realName?: string;
  organization?: string;
  title?: string;
  expertise?: string;
  proofUrls?: ProofUrlsMap;
  status?: number;
  createdAt?: string;
  updatedAt?: string;
};

export type UploadFileVO = {
  url: string;
  filename: string;
  originalFilename: string;
  contentType: string;
  size: number;
  bizType: string;
};

export type ExpertApplyQueryDTO = {
  status?: number | null;
  realName?: string;
  organization?: string;
  expertise?: string;
  pageNum?: number;
  pageSize?: number;
};

export type ExpertManageVO = {
  userId: number;
  realName?: string;
  organization?: string;
  title?: string;
  expertise?: string;
  expertStatus?: number;
  verifiedAt?: string;
};

export type ExpertUserQueryDTO = {
  realName?: string;
  organization?: string;
  expertise?: string;
  expertStatus?: number | null;
  pageNum?: number;
  pageSize?: number;
};

export type ExpertStatusUpdateDTO = {
  expertStatus: number;
};

export type ExpertReviewDTO = {
  applyId: number;
  status: number;
  rejectReason?: string;
};

export type Role = {
  id: number;
  code: string;
  name: string;
  description?: string;
  createdAt?: string;
};

export type RoleQueryDTO = {
  name?: string;
  pageNum?: number;
  pageSize?: number;
};

export type RoleCreateDTO = {
  code: string;
  name: string;
  description?: string;
};

export type RoleUpdateDTO = {
  name: string;
};

export type RolePermUpdateDTO = {
  roleId: number;
  permissionIds: number[];
};

export type Permission = {
  id: number;
  code: string;
  name: string;
  type: string;
  parentId?: number;
  pathOrApi?: string;
  method?: string;
  sort?: number;
  icon?: string;
  component?: string;
  visible?: number;
  createdAt?: string;
};

export type PermissionQueryDTO = {
  name?: string;
  pageNum?: number;
  pageSize?: number;
};

export type PermissionSaveDTO = {
  code: string;
  name: string;
  type: string;
  pathOrApi?: string;
  method?: string;
};
