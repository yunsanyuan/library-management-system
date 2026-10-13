<template>
  <div class="not-found-page">
    <div class="content">
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.not-found-page { height: 100vh; display: flex; align-items: center; justify-content: center; background: #f0f2f5; }
.content { text-align: center; }
.content h1 { font-size: 80px; font-weight: 700; color: #dcdfe6; line-height: 1; margin-bottom: 16px; }
.content p { color: #909399; font-size: 15px; margin-bottom: 24px; }

[data-theme="dark"] .not-found-page { background: #141414; }
[data-theme="dark"] .content h1 { color: #4a4a4a; }
[data-theme="dark"] .content p { color: #a0a0a0; }
</style>
