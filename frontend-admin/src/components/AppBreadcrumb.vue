<template>
  <div class="crumb-wrap">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.title">
        {{ item.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { useBreadcrumbs } from "../router/breadcrumbs";

const { breadcrumbs } = useBreadcrumbs();
</script>

<style scoped>
.crumb-wrap {
  padding: 0 0 2px;
}

.crumb-wrap :deep(.el-breadcrumb__inner) {
  color: var(--app-text-muted);
  font-weight: 500;
  font-size: 13px;
}

.crumb-wrap :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--app-text);
  font-weight: 600;
}
</style>
