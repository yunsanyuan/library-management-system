<template>
  <div class="login-page">
    <div class="login-left">
      <div class="deco-content">
        <div class="icon-ring">
          <el-icon :size="40" color="#67c23a"><UserFilled /></el-icon>
        </div>
        <h1>加入我们</h1>
        <p>注册账号，开启借阅之旅</p>
        <div class="feature-list">
          <div class="feature-item"><el-icon color="#409eff"><CircleCheck /></el-icon> 免费注册，即刻使用</div>
          <div class="feature-item"><el-icon color="#e6a23c"><CircleCheck /></el-icon> 海量图书，一键借阅</div>
          <div class="feature-item"><el-icon color="#67c23a"><CircleCheck /></el-icon> 到期提醒，按时归还</div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2>创建账号</h2>
        <p class="sub-text">填写信息完成注册</p>
        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleRegister" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名（3-50字符）" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleRegister" class="login-btn">注 册</el-button>
          </el-form-item>
        </el-form>
        <div class="card-footer">
          已有账号？<router-link to="/login">去登录</router-link>
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
import { register } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form.username, form.password)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {} finally { loading.value = false }
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
  background: radial-gradient(ellipse at 60% 50%, rgba(103,194,58,0.12) 0%, transparent 55%);
}
.deco-content { position: relative; z-index: 1; text-align: center; padding: 40px; }
.icon-ring {
  width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 20px;
  background: rgba(255,255,255,0.1); border: 2px solid rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
}
.deco-content h1 { color: #fff; font-size: 28px; margin: 0 0 8px; font-weight: 700; }
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
