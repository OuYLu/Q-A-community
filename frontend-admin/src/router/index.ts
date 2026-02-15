import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import AdminLayout from "../layouts/AdminLayout.vue";
import BlankLayout from "../layouts/BlankLayout.vue";
import Login from "../views/login/Login.vue";
import Exception403 from "../views/exception/403.vue";
import Exception500 from "../views/exception/500.vue";

const staticRoutes: RouteRecordRaw[] = [
  {
    path: "/login",
    component: BlankLayout,
    children: [{ path: "", name: "Login", component: Login, meta: { title: "登录" } }]
  },
  {
    path: "/403",
    component: BlankLayout,
    children: [{ path: "", name: "403", component: Exception403, meta: { title: "403" } }]
  },
  {
    path: "/500",
    component: BlankLayout,
    children: [{ path: "", name: "500", component: Exception500, meta: { title: "500" } }]
  },
  {
    path: "/",
    name: "AdminRoot",
    component: AdminLayout,
    redirect: "/dashboard",
    children: []
  },
  
];

export const notFoundRoute: RouteRecordRaw = {
  path: "/:pathMatch(.*)*",
  component: BlankLayout,
  children: [
    {
      path: "",
      name: "404",
      component: () => import("../views/exception/404.vue"),
      meta: { title: "404" }
    }
  ]
};

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes
});

export default router;
