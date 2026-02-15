import router, { notFoundRoute } from "./index";
import { startProgress, stopProgress } from "../utils/nprogress";
import { useAuthStore } from "../store/auth";
import { useMenuStore } from "../store/menu";
import { fetchMe } from "../api/user";
import { fetchMenu } from "../api/menu";
import { buildRoutesFromMenu } from "./dynamic";

const WHITE_LIST = ["/login", "/403", "/404", "/500"];

router.beforeEach(async (to, _from, next) => {
  startProgress();
  const authStore = useAuthStore();
  const menuStore = useMenuStore();

  if (WHITE_LIST.includes(to.path)) {
    stopProgress();
    return next();
  }

  if (!authStore.token) {
    stopProgress();
    return next("/login");
  }

  if (!authStore.user) {
    try {
      const meResp = await fetchMe();
      authStore.setUser(meResp.data);
    } catch {
      authStore.clear();
      stopProgress();
      return next("/login");
    }
  }

  if (!menuStore.isRoutesAdded) {
    try {
      menuStore.setLoading(true);
      const menuResp = await fetchMenu();
      const dynamicRoutes = buildRoutesFromMenu(menuResp.data);
      menuStore.setMenu(menuResp.data, dynamicRoutes);
      dynamicRoutes.forEach((route) => {
        router.addRoute("AdminRoot", route);
      });
      router.addRoute(notFoundRoute);
      menuStore.setRoutesAdded(true);
      stopProgress();
      return next({ ...to, replace: true });
    } catch {
      menuStore.setLoading(false);
      stopProgress();
      return next("/500");
    } finally {
      menuStore.setLoading(false);
    }
  }

  stopProgress();
  return next();
});

router.afterEach(() => {
  stopProgress();
});
