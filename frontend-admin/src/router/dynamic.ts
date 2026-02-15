import type { MenuItem } from "../types/menu";
import { type RouteRecordRaw, RouterView } from "vue-router";
import { h } from "vue";
import Dashboard from "../views/dashboard/Dashboard.vue";
import UserManage from "../views/rbac/UserManage.vue";
import RoleManage from "../views/rbac/RoleManage.vue";
import PermManage from "../views/rbac/PermManage.vue";
import ReportHandle from "../views/content/ReportHandle.vue";
import AuditHandle from "../views/content/AuditHandle.vue";
import CategoryManage from "../views/operation/CategoryManage.vue";
import TagManage from "../views/operation/TagManage.vue";
import TopicManage from "../views/operation/TopicManage.vue";
import KbManage from "../views/operation/KbManage.vue";

const RouteView = {
  render: () => h(RouterView)
};

const routeByCode: Record<string, RouteRecordRaw> = {
  "menu:dashboard": {
    path: "/dashboard",
    name: "Dashboard",
    component: Dashboard,
    meta: { title: "看板" }
  },
  "menu:rbac": {
    path: "/rbac",
    name: "RbacRoot",
    component: RouteView,
    redirect: "/rbac/user",
    meta: { title: "权限管理" }
  },
  "menu:rbac:user": {
    path: "user",
    name: "UserManage",
    component: UserManage,
    meta: { title: "用户管理" }
  },
  "menu:rbac:role": {
    path: "role",
    name: "RoleManage",
    component: RoleManage,
    meta: { title: "角色管理" }
  },
  "menu:rbac:perm": {
    path: "perm",
    name: "PermManage",
    component: PermManage,
    meta: { title: "权限管理" }
  },
  "menu:content": {
    path: "/content",
    name: "ContentRoot",
    component: RouteView,
    redirect: "/content/report",
    meta: { title: "内容治理" }
  },
  "menu:content:report": {
    path: "report",
    name: "ReportHandle",
    component: ReportHandle,
    meta: { title: "举报处理" }
  },
  "menu:content:audit": {
    path: "audit",
    name: "AuditHandle",
    component: AuditHandle,
    meta: { title: "审核队列" }
  },
  "menu:operation": {
    path: "/operation",
    name: "OperationRoot",
    component: RouteView,
    redirect: "/operation/category",
    meta: { title: "运营配置" }
  },
  "menu:operation:category": {
    path: "category",
    name: "CategoryManage",
    component: CategoryManage,
    meta: { title: "分类管理" }
  },
  "menu:operation:tag": {
    path: "tag",
    name: "TagManage",
    component: TagManage,
    meta: { title: "标签管理" }
  },
  "menu:operation:topic": {
    path: "topic",
    name: "TopicManage",
    component: TopicManage,
    meta: { title: "专题管理" }
  },
  "menu:operation:kb": {
    path: "kb",
    name: "KbManage",
    component: KbManage,
    meta: { title: "知识库管理" }
  }
};

const parentByCode: Record<string, string> = {
  "menu:rbac:user": "menu:rbac",
  "menu:rbac:role": "menu:rbac",
  "menu:rbac:perm": "menu:rbac",
  "menu:content:report": "menu:content",
  "menu:content:audit": "menu:content",
  "menu:operation:category": "menu:operation",
  "menu:operation:tag": "menu:operation",
  "menu:operation:topic": "menu:operation",
  "menu:operation:kb": "menu:operation"
};

const cloneRoute = (route: RouteRecordRaw): RouteRecordRaw => {
  return {
    ...route,
    meta: route.meta ? { ...route.meta } : undefined,
    children: route.children ? [...route.children] : undefined
  };
};

const withTitle = (route: RouteRecordRaw, item: MenuItem): RouteRecordRaw => {
  if (!route.meta) {
    route.meta = { title: item.name };
  } else if (!route.meta.title) {
    route.meta.title = item.name;
  }
  return route;
};

export const buildRoutesFromMenu = (menuTree: MenuItem[]): RouteRecordRaw[] => {
  const walk = (items: MenuItem[]): RouteRecordRaw[] => {
    const routes: RouteRecordRaw[] = [];
    items.forEach((item) => {
      const mapped = routeByCode[item.code];
      if (!mapped) {
        return;
      }
      const route = withTitle(cloneRoute(mapped), item);
      if (item.children && item.children.length > 0) {
        route.children = walk(item.children);
      }
      routes.push(route);
    });
    return routes;
  };

  return walk(menuTree);
};

const joinPath = (parent: string, child: string): string => {
  if (!parent) {
    return child;
  }
  if (child.startsWith("/")) {
    return child;
  }
  return `${parent.replace(/\/$/, "")}/${child}`;
};

export const resolveMenuPath = (code: string): string => {
  const route = routeByCode[code];
  if (!route || !route.path) {
    return "";
  }
  const path = route.path.toString();
  if (path.startsWith("/")) {
    return path;
  }
  const parentCode = parentByCode[code];
  if (!parentCode) {
    return path;
  }
  const parentRoute = routeByCode[parentCode];
  const parentPath = parentRoute?.path?.toString() ?? "";
  return joinPath(parentPath, path);
};
