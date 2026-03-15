<template>
  <div class="sidebar-container" :class="{ collapsed }">
    <button class="corner-toggle" type="button" :title="collapsed ? '展开菜单' : '收起菜单'" @click="emit('toggle')">
      {{ collapsed ? "»" : "«" }}
    </button>

    <div class="brand">
      <div class="brand-mark">SC</div>
      <div v-if="!collapsed" class="brand-text">
        <div class="brand-title">Smart Community</div>
        <div class="brand-subtitle">后台运营中心</div>
      </div>
    </div>

    <el-menu
      :default-active="activePath"
      class="menu"
      background-color="var(--app-sidebar-bg)"
      text-color="var(--app-sidebar-text)"
      active-text-color="#ffffff"
      :collapse="collapsed"
      router
    >
      <template v-for="item in menuTree" :key="item.code">
        <el-sub-menu v-if="item.children && item.children.length" :index="resolvePath(item)">
          <template #title>
            <span>{{ item.name }}</span>
          </template>
          <el-menu-item v-for="child in item.children" :key="child.code" :index="resolvePath(child)">
            {{ child.name }}
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="resolvePath(item)">
          {{ item.name }}
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useMenuStore } from "../store/menu";
import { resolveMenuPath } from "../router/dynamic";
import type { MenuItem } from "../types/menu";

defineProps<{
  collapsed: boolean;
}>();

const emit = defineEmits<{
  (e: "toggle"): void;
}>();

const route = useRoute();
const menuStore = useMenuStore();

const menuTree = computed(() => menuStore.menuTree);
const activePath = computed(() => route.path);

const resolvePath = (item: MenuItem) => {
  return item.path ?? item.pathOrApi ?? resolveMenuPath(item.code) ?? "/";
};
</script>

<style scoped>
.sidebar-container {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 10px 0 8px;
}

.corner-toggle {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 8px;
  border: 1px solid rgba(29, 111, 255, 0.2);
  background: rgba(29, 111, 255, 0.08);
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
  z-index: 3;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 58px;
  margin: 2px 12px 10px;
  padding: 10px 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(29, 111, 255, 0.12) 0%, rgba(29, 111, 255, 0.04) 100%);
  border: 1px solid rgba(29, 111, 255, 0.16);
}

.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  color: #ffffff;
  background: linear-gradient(140deg, #1d6fff 0%, #26b491 100%);
}

.brand-text {
  min-width: 0;
}

.brand-title {
  color: var(--app-sidebar-text);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.brand-subtitle {
  margin-top: 2px;
  color: var(--app-text-muted);
  font-size: 12px;
}

.menu {
  border-right: none;
  flex: 1;
}

.collapsed .brand {
  margin-top: 28px;
  justify-content: center;
  padding: 10px 0;
}
</style>
