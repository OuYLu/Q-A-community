import { defineStore } from 'pinia';

const TOKEN_KEY = 'auth_token';
const EXPIRES_AT_KEY = 'auth_expires_at';

interface AuthState {
  token: string;
  expiresAt: string;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    expiresAt: localStorage.getItem(EXPIRES_AT_KEY) || '',
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    login(token: string, expiresAt: string) {
      this.token = token;
      this.expiresAt = expiresAt;
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(EXPIRES_AT_KEY, expiresAt);
    },
    logout() {
      this.token = '';
      this.expiresAt = '';
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(EXPIRES_AT_KEY);
    },
  },
});
