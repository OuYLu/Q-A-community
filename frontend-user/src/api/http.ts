import { BASE_URL } from "@/utils/constants";
import { useAuthStore } from "@/stores/auth";

type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

interface ResultWrap<T> {
  code?: number;
  msg?: string;
  message?: string;
  data: T;
}

interface RequestOptions {
  url: string;
  method?: HttpMethod;
  params?: Record<string, unknown>;
  data?: Record<string, unknown> | string;
  withAuth?: boolean;
}

function toQuery(params?: Record<string, unknown>) {
  if (!params) return "";
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join("&");
  return query ? `?${query}` : "";
}

export function request<T>(options: RequestOptions): Promise<T> {
  const authStore = useAuthStore();
  const header: Record<string, string> = {};
  if (options.withAuth !== false && authStore.token) {
    header.Authorization = `Bearer ${authStore.token}`;
  }

  const url = `${BASE_URL}${options.url}${toQuery(options.params)}`;
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: options.method || "GET",
      data: options.data,
      header,
      success: (res) => {
        const statusCode = res.statusCode || 500;
        if (statusCode < 200 || statusCode >= 300) {
          reject(new Error(`HTTP ${statusCode}`));
          return;
        }
        const body = (res.data || {}) as ResultWrap<T>;
        if (typeof body.code === "number" && body.code !== 200 && body.code !== 0) {
          reject(new Error(body.message || body.msg || "请求失败"));
          return;
        }
        if (body && typeof body === "object" && "data" in body) {
          resolve(body.data);
          return;
        }
        resolve(res.data as T);
      },
      fail: (err) => reject(err)
    });
  });
}
