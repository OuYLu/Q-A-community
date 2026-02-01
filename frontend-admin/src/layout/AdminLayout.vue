<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">后台管理</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/roles">角色管理</el-menu-item>
        <el-menu-item index="/permissions">权限管理</el-menu-item>
        <el-menu-item index="/users">用户管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">Smart Admin</div>
        <div class="header-right">
          <el-button size="small" type="primary" @click="handleLogout">
            退出
          </el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeMenu = computed(() => {
  const segments = route.path.split('/');
  return segments.length > 1 ? `/${segments[1]}` : '/roles';
});

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  padding-top: 8px;
}

.logo {
  padding: 16px;
  font-weight: 600;
  font-size: 16px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
}

.main {
  background: #f7f8fb;
  padding: 16px;
}
</style>
