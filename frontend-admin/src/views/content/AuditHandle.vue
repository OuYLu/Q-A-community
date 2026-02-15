<template>
  <el-card>
    <h2>审核处理</h2>
    <p>这里是审核处理占位页面。</p>
    <el-table :data="rows" style="margin-top: 16px">
      <el-table-column prop="item" label="审核项" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
const rows = [
  { item: "文章审核", status: "待审核" },
  { item: "问答审核", status: "已审核" }
];
</script>
