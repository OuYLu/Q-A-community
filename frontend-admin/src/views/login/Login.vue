<template>
  <div class="login">
    <el-card class="card">
      <h2>管理员登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import type { FormInstance, FormRules } from "element-plus";
import { login } from "../../api/auth";
import { useAuthStore } from "../../store/auth";

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({ username: "", password: "" });

const rules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

const handleLogin = async () => {
  await formRef.value?.validate();
  loading.value = true;
  try {
    const res = await login(form);
    authStore.setTokenOnly(res.data.token);
    router.replace("/");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 20% 20%, #e6ecff 0%, #f7f8fc 35%, #ffffff 100%);
  position: relative;
  overflow: hidden;
}

.card {
  width: 360px;
  border-radius: 16px;
  box-shadow: var(--app-shadow);
}

.login::before {
  content: "";
  position: absolute;
  width: 560px;
  height: 560px;
  left: -200px;
  top: -120px;
  background: radial-gradient(circle at 30% 30%, rgba(66, 115, 255, 0.16), rgba(66, 115, 255, 0));
  border-radius: 50%;
}

.login::after {
  content: "";
  position: absolute;
  width: 480px;
  height: 480px;
  right: -220px;
  bottom: -160px;
  background: radial-gradient(circle at 70% 70%, rgba(99, 102, 241, 0.14), rgba(99, 102, 241, 0));
  border-radius: 50%;
}
</style>
