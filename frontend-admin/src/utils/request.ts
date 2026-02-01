import axios, { AxiosError } from 'axios';
import type { AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useAuthStore } from '@/store/auth';

export interface Result<T = unknown> {
  code: number;
  message: string;
  data: T;
}

const instance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
});

instance.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  const token = authStore.token || localStorage.getItem('auth_token');
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

instance.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const result = response.data;
    if (result.code === 40101) {
      const authStore = useAuthStore();
      authStore.logout();
      router.push('/login');
      return Promise.reject(new Error('Unauthorized'));
    }
    if (result.code === 40301) {
      ElMessage.error('无权限访问');
      return Promise.reject(new Error('Forbidden'));
    }
    return response;
  },
  (error: AxiosError) => {
    ElMessage.error(error.message || '请求失败');
    return Promise.reject(error);
  }
);

export default instance;
