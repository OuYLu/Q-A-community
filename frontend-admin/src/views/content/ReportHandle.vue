<template>
  <el-card>
    <h2>举报处理</h2>
    <p>这里是举报处理占位页面。</p>
    <el-table :data="rows" style="margin-top: 16px">
      <el-table-column prop="report" label="举报内容" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
const rows = [
  { report: "内容不当", status: "待处理" },
  { report: "重复问题", status: "已处理" }
];
</script>
