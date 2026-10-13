<template>
  <div class="dashboard-page">
    <div class="welcome-bar">
      <div class="welcome-text">
        <h2>{{ userStore.isLoggedIn() ? greeting + '，' + userStore.username : '欢迎来到图书馆管理系统' }}</h2>
        <p>{{ userStore.isAdmin() ? '您拥有系统管理权限' : '开始浏览图书或查看借阅记录' }}</p>
      </div>
    </div>
    <p>Dashboard 测试页面</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
</script>

<style scoped>
.dashboard-page { padding: 20px; }
.welcome-bar { margin-bottom: 20px; padding: 18px 24px; border-radius: 4px; background: linear-gradient(135deg, #409eff, #66b1ff); }
.welcome-text h2 { color: #fff; font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.welcome-text p { color: rgba(255,255,255,0.8); font-size: 13px; }
</style>
