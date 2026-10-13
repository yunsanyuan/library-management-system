<template>
  <div class="user-profile-page" v-loading="loading">
    <el-card shadow="hover" style="max-width:600px;margin:0 auto" v-if="profile">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:600">用户主页</span>
          <el-button text type="primary" size="small" @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
        </div>
      </template>
      <div class="profile-header">
        <el-avatar :size="72" :src="profile.avatarUrl || undefined" class="profile-avatar">{{ !profile.avatarUrl ? profile.username?.charAt(0).toUpperCase() : '' }}</el-avatar>
        <div class="profile-meta">
          <h3>{{ profile.username }}</h3>
          <el-tag :type="profile.role==='super_admin'?'danger':profile.role==='admin'?'warning':'info'" size="small">{{ profile.role==='super_admin'?'超管':profile.role==='admin'?'管理员':'普通用户' }}</el-tag>
          <span class="meta-text" v-if="profile.createTime">注册于 {{ profile.createTime?.replace('T',' ').substring(0,10) }}</span>
        </div>
      </div>
      <div class="bio-section" v-if="profile.bio">
        <el-divider />
        <p class="bio-text">{{ profile.bio }}</p>
      </div>
      <div v-else>
        <el-divider />
        <p class="bio-text bio-empty">该用户暂无简介</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicProfile, type UserVO } from '../api/user'
import { useDisposed } from '../composables/useDisposed'

const route = useRoute()
const router = useRouter()
const disposed = useDisposed()
const profile = ref<UserVO | null>(null)
const loading = ref(false)

async function loadProfile() {
  loading.value = true
  try {
    const res = await getPublicProfile(Number(route.params.id))
    if (disposed.value) return
    profile.value = res.data
  } catch {} finally { if (!disposed.value) loading.value = false }
}

onMounted(() => loadProfile())
</script>

<style scoped>
.profile-header { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; }
.profile-avatar { background: linear-gradient(135deg, #409eff, #66b1ff); color: #fff; font-size: 28px; font-weight: 700; }
.profile-meta h3 { color: #303133; font-size: 17px; font-weight: 600; margin-bottom: 4px; }
.meta-text { color: #909399; font-size: 12px; margin-left: 8px; }
.bio-section { margin-bottom: 8px; }
.bio-text { color: #606266; font-size: 13px; line-height: 1.6; }
.bio-empty { color: #c0c4cc; font-style: italic; }

[data-theme="dark"] .profile-meta h3 { color: #e0e0e0; }
[data-theme="dark"] .meta-text { color: #a0a0a0; }
[data-theme="dark"] .bio-text { color: #c0c0c0; }
[data-theme="dark"] .bio-empty { color: #6a6a6a; }
</style>
