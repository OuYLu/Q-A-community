<template>
  <div class="login-page">
    <div class="login-card">
      <h2>后台登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="onSubmit">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const formRef = ref<FormInstance>();
const router = useRouter();
const authStore = useAuthStore();
const submitting = ref(false);

const form = reactive({
  username: '',
  password: '',
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const onSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return;
    submitting.value = true;
    try {
      // TODO: replace with real login API
      const fakeToken = `mock-token-${Date.now()}`;
      const expiresAt = new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString();
      authStore.login(fakeToken, expiresAt);
      ElMessage.success('登录成功');
      router.push('/roles');
    } finally {
      submitting.value = false;
    }
  });
};
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4b7bec 0%, #a55eea 100%);
}

.login-card {
  width: 360px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
}
</style>
