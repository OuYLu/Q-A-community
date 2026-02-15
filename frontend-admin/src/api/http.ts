import axios, { type AxiosError } from "axios";
import { ElMessage } from "element-plus";
import router from "../router";
import { getToken, removeToken } from "../utils/token";
import type { Result } from "../types/api";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data as Result<unknown> & { message?: string };
    if (typeof payload?.code === "number") {
      if (payload.code === 0) {
        return payload;
      }
      const msg = payload.msg || payload.message;
      if (msg) {
        ElMessage.error(msg);
      }
      if (payload.code === 401) {
        removeToken();
        router.replace("/login");
        return Promise.reject(payload);
      }
      if (payload.code === 403) {
        router.replace("/403");
        return Promise.reject(payload);
      }
      return Promise.reject(payload);
    }
    return response.data;
  },
  (error: AxiosError) => {
    const status = error.response?.status;
    const data = error.response?.data as (Result<unknown> & { message?: string }) | undefined;
    const message = data?.msg || data?.message || error.message || "网络异常";
    if (status === 401) {
      removeToken();
      router.replace("/login");
    } else if (status === 403) {
      router.replace("/403");
    } else {
      ElMessage.error(message);
    }
    return Promise.reject(error);
  }
);

export default http;
