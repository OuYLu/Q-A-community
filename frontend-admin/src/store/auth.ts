import { defineStore } from "pinia";
import type { User } from "../types/user";
import { getToken, removeToken, setToken } from "../utils/token";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: getToken(),
    user: null as User | null,
    roles: [] as string[],
    perms: [] as string[]
  }),
  actions: {
    setTokenOnly(token: string) {
      this.token = token;
      setToken(token);
    },
    setAuth(token: string, user: User) {
      this.token = token;
      this.user = user;
      this.roles = user.roles;
      this.perms = user.perms ?? [];
      setToken(token);
    },
    setUser(user: User) {
      this.user = user;
      this.roles = user.roles;
      this.perms = user.perms ?? [];
    },
    clear() {
      this.token = null;
      this.user = null;
      this.roles = [];
      this.perms = [];
      removeToken();
    }
  }
});
