import { defineStore } from "pinia";
import type { MenuItem } from "../types/menu";
import type { RouteRecordRaw } from "vue-router";

export const useMenuStore = defineStore("menu", {
  state: () => ({
    menuTree: [] as MenuItem[],
    flatRoutes: [] as RouteRecordRaw[],
    isRoutesAdded: false,
    loading: false
  }),
  actions: {
    setMenu(tree: MenuItem[], routes: RouteRecordRaw[]) {
      this.menuTree = tree;
      this.flatRoutes = routes;
    },
    setRoutesAdded(value: boolean) {
      this.isRoutesAdded = value;
    },
    setLoading(value: boolean) {
      this.loading = value;
    },
    reset() {
      this.menuTree = [];
      this.flatRoutes = [];
      this.isRoutesAdded = false;
      this.loading = false;
    }
  }
});
