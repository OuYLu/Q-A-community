<template>
  <div class="login">
    <div class="ambient"></div>
    <div class="login-panel">
      <section class="intro">
        <span class="intro-tag">Smart Community</span>
        <h1>智慧医养健康问答社区</h1>
        <p>统一处理问答治理、内容运营、权限配置和业务数据看板。</p>
        <ul>
          <li>内容全链路运营管理</li>
          <li>权限与角色精细化控制</li>
          <li>数据看板实时追踪趋势</li>
        </ul>
      </section>

      <el-card class="card">
        <div class="card-head">
          <h2>后台人员登录</h2>
          <p>请使用后台账号进入系统</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button class="submit" type="primary" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form>
      </el-card>
    </div>
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
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  overflow: hidden;
}

.ambient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 22%, rgba(29, 111, 255, 0.2) 0%, rgba(29, 111, 255, 0) 45%),
    radial-gradient(circle at 82% 78%, rgba(18, 170, 145, 0.2) 0%, rgba(18, 170, 145, 0) 46%);
}

.login-panel {
  position: relative;
  z-index: 1;
  width: min(960px, 100%);
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 24px;
  box-shadow: 0 30px 56px rgba(20, 48, 94, 0.22);
  backdrop-filter: blur(14px);
  overflow: hidden;
  animation: rise-in 0.55s ease-out;
}

.intro {
  padding: 44px 40px;
  color: #153059;
  background: linear-gradient(145deg, rgba(228, 238, 255, 0.82) 0%, rgba(233, 250, 247, 0.78) 100%);
}

.intro-tag {
  display: inline-flex;
  height: 28px;
  align-items: center;
  border-radius: 999px;
  padding: 0 14px;
  background: rgba(23, 98, 220, 0.12);
  color: #165bd7;
  font-size: 12px;
  font-weight: 600;
}

.intro h1 {
  margin: 16px 0 10px;
  font-size: 30px;
  line-height: 1.25;
}

.intro p {
  margin: 0;
  color: #476083;
  line-height: 1.7;
}

.intro ul {
  margin: 24px 0 0;
  padding-left: 18px;
  color: #2a4467;
  line-height: 2;
  font-weight: 500;
}

.card {
  margin: 22px;
  border: 0;
  box-shadow: none;
  background: rgba(255, 255, 255, 0.9);
}

.card-head h2 {
  margin: 0;
  font-size: 24px;
  color: var(--app-text);
}

.card-head p {
  margin: 8px 0 18px;
  color: var(--app-text-muted);
}

.login-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--app-text);
}

.submit {
  width: 100%;
  margin-top: 6px;
  height: 40px;
}

@keyframes rise-in {
  0% {
    opacity: 0;
    transform: translateY(16px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 920px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .intro {
    padding: 34px 28px 22px;
  }

  .intro h1 {
    font-size: 25px;
  }

  .intro ul {
    margin-top: 14px;
    line-height: 1.9;
  }

  .card {
    margin: 0 20px 20px;
  }
}

@media (max-width: 560px) {
  .login {
    padding: 12px;
  }

  .login-panel {
    border-radius: 18px;
  }

  .intro {
    padding: 24px 20px 14px;
  }

  .intro h1 {
    font-size: 22px;
  }

  .card {
    margin: 0 12px 12px;
  }
}
</style>
