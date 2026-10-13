<template>
  <el-container class="main-layout" :class="{ 'dark-theme': isDark }">
    <el-aside :width="isCollapse ? '64px' : '210px'" class="sidebar">
      <div class="logo-area">
        <div class="logo-icon"><el-icon :size="20" color="#fff"><Reading /></el-icon></div>
        <span class="logo-text" v-show="!isCollapse">图书馆管理系统</span>
      </div>
      <el-menu :default-active="currentRoute" class="sidebar-menu" :background-color="isDark ? '#1a1a2e' : '#304156'" text-color="#bfcbd9" active-text-color="#409eff" :collapse="isCollapse" @select="handleMenuSelect">
        <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon><template #title>首页</template></el-menu-item>
        <el-menu-item index="/books"><el-icon><Reading /></el-icon><template #title>图书浏览</template></el-menu-item>
        <el-menu-item v-if="userStore.canBorrow()" index="/borrow/my"><el-icon><Tickets /></el-icon><template #title>我的借阅</template></el-menu-item>
        <el-sub-menu v-if="userStore.canManage()" index="admin">
          <template #title><el-icon><Management /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/admin/books"><el-icon><Notebook /></el-icon><template #title>图书管理</template></el-menu-item>
          <el-menu-item index="/admin/borrows"><el-icon><List /></el-icon><template #title>借阅管理</template></el-menu-item>
          <el-menu-item index="/admin/users"><el-icon><UserFilled /></el-icon><template #title>用户管理</template></el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header" height="52px">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="18">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title && route.path !== '/dashboard'">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-switch v-model="isDark" :active-action="Moon" :inactive-action="Sunny" inline-prompt class="theme-switch" />
          <el-tag v-if="userStore.isLoggedIn()" :type="userStore.roleTagType" effect="dark" size="small" round>{{ userStore.roleLabel }}</el-tag>
          <el-button v-if="!userStore.isLoggedIn()" type="primary" size="small" @click="router.push('/login')">登录</el-button>
          <el-button v-if="!userStore.isLoggedIn()" size="small" @click="router.push('/register')">注册</el-button>
          <el-dropdown v-if="userStore.isLoggedIn()" trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="28" :src="userStore.avatarUrl || undefined" class="user-avatar">{{ !userStore.avatarUrl ? userStore.username.charAt(0).toUpperCase() : '' }}</el-avatar>
              <span class="user-name">{{ userStore.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon> 个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content" ref="mainRef">
        <router-view v-slot="{ Component, route: routeInfo }">
          <component :is="Component" :key="routeInfo.fullPath" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'
import { Sunny, Moon } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const isDark = ref(localStorage.getItem('theme') === 'dark')
const mainRef = ref()

const currentRoute = ref(route.path)

watch(() => route.path, (v) => {
  currentRoute.value = v
  if (mainRef.value?.$el) {
    mainRef.value.$el.scrollTop = 0
  }
})
watch(isDark, (v) => {
  localStorage.setItem('theme', v ? 'dark' : 'light')
  document.documentElement.setAttribute('data-theme', v ? 'dark' : 'light')
}, { immediate: true })

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
      .then(() => { userStore.clearLoginInfo(); router.push('/login') }).catch(() => {})
  } else if (cmd === 'profile') { router.push('/profile') }
}

function handleMenuSelect(index: string) {
  if (index.startsWith('/')) {
    router.push(index)
  }
}
</script>

<style scoped>
.main-layout { height: 100vh; transition: background 0.3s ease; }
.sidebar {
  background: #304156; display: flex; flex-direction: column; overflow: hidden;
  transition: width 0.28s, background 0.3s ease; box-shadow: 2px 0 6px rgba(0,21,41,0.08);
}
.dark-theme .sidebar { background: #1a1a2e !important; }
.logo-area { height: 52px; display: flex; align-items: center; padding: 0 16px; gap: 10px; background: #263445; flex-shrink: 0; }
.dark-theme .logo-area { background: #12122a; }
.logo-icon { width: 32px; height: 32px; border-radius: 6px; flex-shrink: 0; background: linear-gradient(135deg, #409eff, #66b1ff); display: flex; align-items: center; justify-content: center; }
.logo-text { font-size: 15px; font-weight: 700; color: #fff; white-space: nowrap; letter-spacing: 1px; }
.sidebar-menu { border-right: none; flex: 1; overflow-y: auto; }
:deep(.el-menu-item) { font-size: 13px; height: 44px; line-height: 44px; }
:deep(.el-menu-item.is-active) { background-color: #263445 !important; border-right: 3px solid #409eff; }
:deep(.el-menu-item:hover) { background-color: #263445 !important; }
:deep(.el-sub-menu__title:hover) { background-color: #263445 !important; }
.dark-theme :deep(.el-menu-item.is-active) { background-color: #12122a !important; }
.dark-theme :deep(.el-menu-item:hover) { background-color: #12122a !important; }
.dark-theme :deep(.el-sub-menu__title:hover) { background-color: #12122a !important; }

.header {
  background: #fff; display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08); padding: 0 20px; z-index: 10;
  transition: background 0.3s ease, color 0.3s ease;
}
.dark-theme .header { background: #1e2a3a !important; }
.header-left { display: flex; align-items: center; gap: 12px; }
.collapse-btn { cursor: pointer; color: #606266; transition: color 0.2s; }
.dark-theme .collapse-btn { color: #b0b3c5; }
.collapse-btn:hover { color: #409eff; }
.breadcrumb { font-size: 13px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-avatar { background: linear-gradient(135deg, #409eff, #66b1ff); color: #fff; font-size: 12px; font-weight: 700; }
.user-name { font-size: 13px; color: #606266; font-weight: 500; }
.dark-theme .user-name { color: #e0e0e0; }
.header-right { display: flex; align-items: center; gap: 12px; }
.theme-switch { margin-right: 4px; }
.main-content { background: #f0f2f5; min-height: 0; padding: 20px; overflow-y: auto; transition: background 0.3s ease; }
.dark-theme .main-content { background: #161d29 !important; }
</style>
