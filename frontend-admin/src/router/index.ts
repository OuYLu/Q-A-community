import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import AdminLayout from '@/layout/AdminLayout.vue';
import { useAuthStore } from '@/store/auth';

const whiteList = ['/login'];

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/roles',
    children: [
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/Roles.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'permissions',
        name: 'Permissions',
        component: () => import('@/views/Permissions.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { requiresAuth: true },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore();
  if (whiteList.includes(to.path)) {
    if (to.path === '/login' && authStore.token) {
      return next('/roles');
    }
    return next();
  }
  if (!authStore.token) {
    return next('/login');
  }
  return next();
});

export default router;
