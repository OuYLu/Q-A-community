<template>
  <el-card>
    <h2>仪表盘</h2>
    <p>欢迎进入智慧医养健康问答社区管理后台。</p>
    <el-table :data="rows" style="margin-top: 16px">
      <el-table-column prop="name" label="模块" />
      <el-table-column prop="status" label="状态" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
const rows = [
  { name: "内容审核", status: "运行中" },
  { name: "运营管理", status: "运行中" }
];
</script>
