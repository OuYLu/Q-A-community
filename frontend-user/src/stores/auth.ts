import { defineStore } from "pinia";
import { AUTH_EXPIRES_KEY, TOKEN_KEY, USER_KEY } from "@/utils/constants";
import type { LoginVO } from "@/api/auth";

interface UserProfile {
  userId?: number;
  username?: string;
  nickname: string;
  avatar?: string;
  slogan?: string;
  email?: string;
}

function readStorage<T>(key: string, fallback: T): T {
  const val = uni.getStorageSync(key);
  if (!val) return fallback;
  return val as T;
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: readStorage<string>(TOKEN_KEY, ""),
    expiresAt: readStorage<number>(AUTH_EXPIRES_KEY, 0),
    user: readStorage<UserProfile | null>(USER_KEY, null)
  }),
  getters: {
    isLogin: (state) => Boolean(state.token) && state.expiresAt > Date.now()
  },
  actions: {
    applyLogin(login: LoginVO, nicknameHint?: string, usernameHint?: string) {
      this.token = login.token;
      this.expiresAt = login.expiresAt;
      this.user = {
        userId: login.userId,
        username: usernameHint || this.user?.username,
        nickname: nicknameHint || this.user?.nickname || "用户",
        avatar: this.user?.avatar,
        slogan: this.user?.slogan,
        email: this.user?.email
      };
      uni.setStorageSync(TOKEN_KEY, this.token);
      uni.setStorageSync(AUTH_EXPIRES_KEY, this.expiresAt);
      uni.setStorageSync(USER_KEY, this.user);
    },
    mockLogin(nickname: string) {
      this.token = `mock-token-${Date.now()}`;
      this.expiresAt = Date.now() + 7 * 24 * 3600 * 1000;
      this.user = {
        nickname
      };
      uni.setStorageSync(TOKEN_KEY, this.token);
      uni.setStorageSync(AUTH_EXPIRES_KEY, this.expiresAt);
      uni.setStorageSync(USER_KEY, this.user);
    },
    logout() {
      this.token = "";
      this.expiresAt = 0;
      this.user = null;
      uni.removeStorageSync(TOKEN_KEY);
      uni.removeStorageSync(AUTH_EXPIRES_KEY);
      uni.removeStorageSync(USER_KEY);
    }
  }
});
