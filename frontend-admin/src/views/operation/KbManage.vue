<template>
  <el-card>
    <h2>知识库管理</h2>
    <p>这里是知识库管理占位页面。</p>
    <el-table :data="rows" style="margin-top: 16px">
      <el-table-column prop="name" label="知识条目" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
const rows = [
  { name: "常见问答", status: "已发布" },
  { name: "健康评估", status: "草稿" }
];
</script>
