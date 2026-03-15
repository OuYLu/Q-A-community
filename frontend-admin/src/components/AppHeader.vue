<template>
  <div class="header-container">
    <div class="title-block">
      <div class="title">智慧医养健康问答社区</div>
      <div class="subtitle">后台运营管理台</div>
    </div>
    <div class="actions">
      <el-dropdown @command="setTheme">
        <el-button size="small" class="theme-btn">主题：{{ themeLabel }}</el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="light">浅色</el-dropdown-item>
            <el-dropdown-item command="dark">深色</el-dropdown-item>
            <el-dropdown-item command="highlight">高亮</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <span class="user">{{ userLabel }}</span>
      <el-button type="primary" size="small" @click="handleLogout">退出</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../store/auth";
import { useMenuStore } from "../store/menu";

const router = useRouter();
const authStore = useAuthStore();
const menuStore = useMenuStore();
const theme = ref<"light" | "dark" | "highlight">(
  (localStorage.getItem("admin_theme") as "light" | "dark" | "highlight") || "light"
);

const userLabel = computed(() => authStore.user?.nickname || authStore.user?.username || "-");
const themeLabel = computed(() => {
  if (theme.value === "dark") return "深色";
  if (theme.value === "highlight") return "高亮";
  return "浅色";
});

const setTheme = (value: "light" | "dark" | "highlight") => {
  theme.value = value;
  document.documentElement.setAttribute("data-theme", value);
  localStorage.setItem("admin_theme", value);
};

setTheme(theme.value);

const handleLogout = () => {
  authStore.clear();
  menuStore.reset();
  router.replace("/login");
};
</script>

<style scoped>
.header-container {
  min-height: 60px;
  padding: 0 6px 0 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.title-block {
  min-width: 0;
}

.title {
  font-weight: 700;
  font-size: 16px;
  color: var(--app-text);
  letter-spacing: 0.2px;
}

.subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: var(--app-text-muted);
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.theme-btn {
  min-width: 88px;
}

.user {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: var(--app-primary-soft);
  color: var(--app-text);
  font-size: 13px;
}

@media (max-width: 760px) {
  .title {
    font-size: 14px;
  }

  .subtitle,
  .user {
    display: none;
  }
}
</style>
