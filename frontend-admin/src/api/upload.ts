import http from "./http";
import type { Result } from "../types/api";
import type { UploadFileVO } from "../types/adminUser";

export const uploadAvatar = (file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  return http.post<Result<string>>("/common/avatar/upload", formData);
};

export const uploadCommonFile = (file: File, bizType: "avatar" | "expert-proof") => {
  const formData = new FormData();
  formData.append("file", file);
  return http.post<Result<UploadFileVO>>("/common/upload", formData, {
    params: { bizType }
  });
};
