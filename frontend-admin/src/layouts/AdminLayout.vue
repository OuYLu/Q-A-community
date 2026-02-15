<template>
  <div class="layout">
    <aside class="sidebar">
      <AppSidebar />
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
</template>

<script setup lang="ts">
import AppHeader from "../components/AppHeader.vue";
import AppSidebar from "../components/AppSidebar.vue";
import AppBreadcrumb from "../components/AppBreadcrumb.vue";
import AppMain from "../components/AppMain.vue";
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  height: 100%;
  background: var(--app-bg);
}

.sidebar {
  background: var(--app-sidebar-bg);
  color: var(--app-sidebar-text);
  box-shadow: 2px 0 12px rgba(14, 24, 52, 0.12);
}

.main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  height: 60px;
  background: var(--app-surface);
  border-bottom: 1px solid var(--app-border);
}

.breadcrumb {
  padding: 12px 24px 0 24px;
}

.content {
  padding: 16px 24px 24px 24px;
  overflow: auto;
  background: var(--app-bg);
}

@media (max-width: 960px) {
  .layout {
    grid-template-columns: 72px 1fr;
  }
}
</style>
