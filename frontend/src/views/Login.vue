<template>
  <div class="login-page">
    <div class="login-left">
      <div class="deco-content">
        <div class="icon-ring">
          <el-icon :size="40" color="#409eff"><Reading /></el-icon>
        </div>
        <h1>图书馆管理系统</h1>
        <p>高效管理图书资源，轻松借阅归还</p>
        <div class="feature-list">
          <div class="feature-item"><el-icon color="#67c23a"><CircleCheck /></el-icon> 图书浏览与智能搜索</div>
          <div class="feature-item"><el-icon color="#409eff"><CircleCheck /></el-icon> 在线借阅与到期提醒</div>
          <div class="feature-item"><el-icon color="#e6a23c"><CircleCheck /></el-icon> 管理员库存与借阅管理</div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2>欢迎回来</h2>
        <p class="sub-text">请登录您的账号</p>
        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="card-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form.username, form.password)
    userStore.setLoginInfo(res.data.token, res.data.role, res.data.username, res.data.avatarUrl)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message
    if (msg) ElMessage.error(msg)
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page { height: 100vh; display: flex; background: #f0f2f5; }
.login-left {
  flex: 1; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #2b4c7e 0%, #304156 50%, #263445 100%);
  position: relative; overflow: hidden;
}
.login-left::after {
  content: ''; position: absolute; inset: 0;
  background: radial-gradient(ellipse at 30% 40%, rgba(64,158,255,0.15) 0%, transparent 60%);
}
.deco-content { position: relative; z-index: 1; text-align: center; padding: 40px; }
.icon-ring {
  width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 20px;
  background: rgba(255,255,255,0.1); border: 2px solid rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
}
.deco-content h1 { color: #fff; font-size: 28px; margin: 0 0 8px; font-weight: 700; letter-spacing: 3px; }
.deco-content > p { color: rgba(255,255,255,0.65); font-size: 14px; margin-bottom: 30px; }
.feature-list { display: flex; flex-direction: column; gap: 10px; align-items: center; }
.feature-item { color: rgba(255,255,255,0.8); font-size: 13px; display: flex; align-items: center; gap: 6px; }
.login-right {
  width: 460px; display: flex; align-items: center; justify-content: center;
  background: #fff;
}
.login-card { width: 340px; }
.login-card h2 { color: #303133; font-size: 22px; font-weight: 600; }
.sub-text { color: #909399; font-size: 13px; margin: 6px 0 28px; }
.login-btn { width: 100%; font-size: 15px; letter-spacing: 4px; }
.card-footer { text-align: center; color: #909399; font-size: 13px; margin-top: 16px; }
.card-footer a { color: #409eff; text-decoration: none; font-weight: 500; }

[data-theme="dark"] .login-page { background: #141414; }
[data-theme="dark"] .login-right { background: #1d1e1f; }
[data-theme="dark"] .login-card h2 { color: #e0e0e0; }
[data-theme="dark"] .sub-text { color: #a0a0a0; }
[data-theme="dark"] .card-footer { color: #a0a0a0; }
</style>
