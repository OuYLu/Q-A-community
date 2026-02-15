<template>
  <el-card>
    <h2>话题管理</h2>
    <p>这里是话题管理占位页面。</p>
    <el-table :data="rows" style="margin-top: 16px">
      <el-table-column prop="name" label="话题" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
const rows = [
  { name: "慢病护理", status: "上架" },
  { name: "康复训练", status: "下架" }
];
</script>
