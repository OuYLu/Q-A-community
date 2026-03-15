<template>
  <div class="layout-shell">
    <div class="layout-glow"></div>
    <div class="layout" :class="{ collapsed: sidebarCollapsed }">
      <aside class="sidebar">
        <AppSidebar :collapsed="sidebarCollapsed" @toggle="toggleSidebar" />
      </aside>
      <section class="main">
        <header class="header">
          <AppHeader />
        </header>
        <div class="breadcrumb">
          <AppBreadcrumb />
        </div>
        <main class="content">
          <AppMain />
        </main>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import AppHeader from "../components/AppHeader.vue";
import AppSidebar from "../components/AppSidebar.vue";
import AppBreadcrumb from "../components/AppBreadcrumb.vue";
import AppMain from "../components/AppMain.vue";

const sidebarCollapsed = ref(false);

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};
</script>

<style scoped>
.layout-shell {
  position: relative;
  height: 100%;
  padding: 8px 10px 10px;
}

.layout-glow {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(500px 320px at 88% 8%, rgba(29, 111, 255, 0.1) 0%, rgba(29, 111, 255, 0) 70%),
    radial-gradient(420px 320px at 8% 92%, rgba(38, 180, 145, 0.1) 0%, rgba(38, 180, 145, 0) 72%);
}

.layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 224px minmax(0, 1fr);
  gap: 10px;
  height: 100%;
  min-width: 0;
  transition: grid-template-columns 0.24s ease;
}

.layout.collapsed {
  grid-template-columns: 76px minmax(0, 1fr);
}

.sidebar {
  background: var(--app-sidebar-bg);
  color: var(--app-sidebar-text);
  border: 1px solid var(--app-sidebar-border);
  border-radius: 16px;
  box-shadow: var(--app-shadow);
  overflow: hidden;
}

.main {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

.header {
  padding: 0 2px;
}

.breadcrumb {
  padding: 0 6px;
}

.content {
  flex: 1;
  min-height: 0;
  min-width: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0 2px 12px;
  scrollbar-gutter: stable;
}

@media (max-width: 980px) {
  .layout-shell {
    padding: 8px;
  }

  .layout {
    grid-template-columns: 76px minmax(0, 1fr);
  }
}
</style>
