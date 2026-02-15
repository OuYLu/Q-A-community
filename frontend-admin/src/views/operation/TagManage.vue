<template>
  <el-card>
    <h2>标签管理</h2>
    <p>这里是标签管理占位页面。</p>
    <el-table :data="rows" style="margin-top: 16px">
      <el-table-column prop="name" label="标签" />
      <el-table-column prop="hot" label="热度" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
const rows = [
  { name: "慢病管理", hot: "高" },
  { name: "健康咨询", hot: "中" }
];
</script>
