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
  background: var(--app-surface);
  border-radius: var(--app-radius);
  padding: 12px 16px;
  box-shadow: var(--app-shadow);
}
</style>
