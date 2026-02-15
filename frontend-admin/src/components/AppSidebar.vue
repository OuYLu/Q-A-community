<template>
  <div class="sidebar-container">
    <div class="logo">Community Admin</div>
    <el-menu
      :default-active="activePath"
      class="menu"
      background-color="var(--app-sidebar-bg)"
      text-color="var(--app-sidebar-text)"
      active-text-color="#ffffff"
      router
      :collapse="collapsed"
    >
      <template v-for="item in menuTree" :key="item.code">
        <el-sub-menu v-if="item.children && item.children.length" :index="resolvePath(item)">
          <template #title>
            <span>{{ item.name }}</span>
          </template>
          <el-menu-item
            v-for="child in item.children"
            :key="child.code"
            :index="resolvePath(child)"
          >
            {{ child.name }}
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="resolvePath(item)">
          {{ item.name }}
        </el-menu-item>
      </template>
    </el-menu>
    <div class="collapse">
      <el-button size="small" text @click="collapsed = !collapsed">
        {{ collapsed ? "展开" : "收起" }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { useMenuStore } from "../store/menu";
import { resolveMenuPath } from "../router/dynamic";
import type { MenuItem } from "../types/menu";

const route = useRoute();
const menuStore = useMenuStore();
const collapsed = ref(false);

const menuTree = computed(() => menuStore.menuTree);
const activePath = computed(() => route.path);

const resolvePath = (item: MenuItem) => {
  return item.path ?? item.pathOrApi ?? resolveMenuPath(item.code) ?? "/";
};
</script>

<style scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  color: var(--app-sidebar-text);
  font-weight: 600;
  letter-spacing: 0.4px;
}

.menu {
  border-right: none;
  flex: 1;
}

.collapse {
  padding: 12px 16px;
}
</style>
