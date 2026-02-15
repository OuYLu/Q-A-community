<template>
  <el-card>
    <h2>分类管理</h2>
    <p>这里是分类管理占位页面。</p>
    <el-table :data="rows" style="margin-top: 16px">
      <el-table-column prop="name" label="分类" />
      <el-table-column prop="count" label="数量" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
const rows = [
  { name: "养生", count: 12 },
  { name: "护理", count: 8 }
];
</script>
