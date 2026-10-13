<template>
  <div class="profile-page">
    <el-card shadow="hover" style="max-width:600px;margin:0 auto">
      <template #header><span style="font-weight:600">个人中心</span></template>
      <div class="profile-header">
        <div class="avatar-wrap">
          <el-avatar :size="72" :src="userInfo.avatarUrl || undefined" class="profile-avatar">{{ !userInfo.avatarUrl ? userInfo.username?.charAt(0).toUpperCase() : '' }}</el-avatar>
          <label class="avatar-upload-btn">
            <el-icon :size="14"><Camera /></el-icon>
            <input type="file" accept="image/jpeg,image/png" @change="handleAvatarChange" style="display:none" />
          </label>
        </div>
        <div class="profile-meta">
          <h3>{{ userInfo.username }}</h3>
          <el-tag :type="userInfo.role==='super_admin'?'danger':userInfo.role==='admin'?'warning':'info'" size="small">{{ userInfo.role==='super_admin'?'超管':userInfo.role==='admin'?'管理员':'普通用户' }}</el-tag>
          <span class="meta-text" v-if="userInfo.createTime">注册于 {{ userInfo.createTime?.replace('T',' ').substring(0,10) }}</span>
        </div>
      </div>
      <p class="avatar-tip">支持 JPG、PNG 格式，大小不超过 2MB</p>

      <div class="bio-section" v-if="!editingBio">
        <p class="bio-text" v-if="userInfo.bio">{{ userInfo.bio }}</p>
        <p class="bio-text bio-empty" v-else>暂无简介</p>
        <el-button type="primary" link size="small" @click="startEditBio">{{ userInfo.bio ? '编辑' : '添加简介' }}</el-button>
      </div>
      <div class="bio-section" v-else>
        <el-input v-model="bioInput" type="textarea" :rows="3" placeholder="写一段个人简介..." maxlength="200" show-word-limit />
        <div style="margin-top:8px;display:flex;gap:8px">
          <el-button type="primary" size="small" @click="handleSaveBio" :loading="bioLoading">保存</el-button>
          <el-button size="small" @click="editingBio=false">取消</el-button>
        </div>
      </div>

      <el-divider />
      <h4 class="section-title">修改账号名</h4>
      <p class="section-desc">修改后需使用新账号名登录</p>
      <el-form :model="usernameForm" :rules="usernameRules" ref="usernameFormRef" style="max-width:340px" @submit.prevent>
        <el-form-item label="新账号名" prop="newUsername" label-width="80px">
          <el-input v-model="usernameForm.newUsername" placeholder="输入新账号名（3-50字符）" />
        </el-form-item>
        <el-form-item label-width="80px">
          <el-button type="primary" @click="handleChangeUsername" :loading="usernameLoading">保存修改</el-button>
        </el-form-item>
      </el-form>
      <el-divider />
      <h4 class="section-title">修改密码</h4>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" style="max-width:340px" @submit.prevent>
        <el-form-item label="旧密码" prop="oldPassword" label-width="80px">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword" label-width="80px">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" label-width="80px">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
        <el-form-item label-width="80px">
          <el-button type="primary" @click="handleChangePassword" :loading="pwdLoading">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getCurrentUser, changeUsername, changePassword, updateAvatar, updateBio, type UserVO } from '../api/user'
import { useDisposed } from '../composables/useDisposed'

const userStore = useUserStore()
const disposed = useDisposed()
const userInfo = ref<UserVO>({ id: 0, username: '', role: '', avatarUrl: '', bio: '', createTime: '' })
const usernameFormRef = ref()
const usernameLoading = ref(false)
const usernameForm = reactive({ newUsername: '' })
const usernameRules = { newUsername: [{ required: true, message: '请输入新账号名', trigger: 'blur' }, { min: 3, max: 50, message: '账号名长度3-50字符', trigger: 'blur' }] }
const pwdFormRef = ref()
const pwdLoading = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: (_rule:any,value:string,callback:Function)=>{if(value!==pwdForm.newPassword)callback(new Error('两次输入不一致'));else callback()}, trigger:'blur' }]
}

const editingBio = ref(false)
const bioInput = ref('')
const bioLoading = ref(false)

async function loadUserInfo() { try { const res=await getCurrentUser(); if(disposed.value)return; userInfo.value=res.data; usernameForm.newUsername=res.data.username } catch(e){console.error('获取用户信息失败',e)} }

const AVATAR_MAX_SIZE = 2 * 1024 * 1024
const AVATAR_MAX_BASE64 = 60000

function compressImage(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      let w = img.width, h = img.height
      const maxDim = 200
      if (w > maxDim || h > maxDim) {
        if (w > h) { h = Math.round(h * maxDim / w); w = maxDim }
        else { w = Math.round(w * maxDim / h); h = maxDim }
      }
      canvas.width = w; canvas.height = h
      const ctx = canvas.getContext('2d')!
      ctx.drawImage(img, 0, 0, w, h)
      let quality = 0.8
      let base64 = canvas.toDataURL('image/jpeg', quality)
      while (base64.length > AVATAR_MAX_BASE64 && quality > 0.1) {
        quality -= 0.1
        base64 = canvas.toDataURL('image/jpeg', quality)
      }
      if (base64.length > AVATAR_MAX_BASE64) {
        reject(new Error('compress_fail'))
      } else {
        resolve(base64)
      }
    }
    img.onerror = () => reject(new Error('invalid_image'))
    img.src = URL.createObjectURL(file)
  })
}

function handleAvatarChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG 格式的图片')
    return
  }
  if (file.size > AVATAR_MAX_SIZE) {
    ElMessage.warning('图片过大，请选择 2MB 以内的图片（支持 JPG、PNG）')
    return
  }
  compressImage(file).then(base64 => {
    updateAvatar(base64).then(res => {
      userInfo.value = res.data
      ElMessage.success('头像更新成功')
    }).catch(() => ElMessage.error('头像更新失败'))
  }).catch(err => {
    if (err.message === 'compress_fail') {
      ElMessage.warning('图片压缩后仍然过大，请选择更小或更简单的图片')
    } else {
      ElMessage.error('图片处理失败，请重试')
    }
  })
  ;(e.target as HTMLInputElement).value = ''
}

function startEditBio() { bioInput.value = userInfo.value.bio || ''; editingBio.value = true }

async function handleSaveBio() {
  bioLoading.value = true
  try {
    const res = await updateBio(bioInput.value)
    userInfo.value = res.data
    editingBio.value = false
    ElMessage.success('简介更新成功')
  } catch { ElMessage.error('简介更新失败') }
  finally { bioLoading.value = false }
}

async function handleChangeUsername() {
  try { await usernameFormRef.value?.validate() } catch { return }
  if(usernameForm.newUsername===userInfo.value.username){ElMessage.info('账号名未变更');return}
  usernameLoading.value=true
  try { const res=await changeUsername(usernameForm.newUsername); userInfo.value=res.data; userStore.setLoginInfo(userStore.token,userStore.role,usernameForm.newUsername); ElMessage.success('账号名修改成功！新账号名为：'+usernameForm.newUsername) }
  catch(e:any){const msg=e?.response?.data?.message||e?.message;if(msg)ElMessage.error('修改失败：'+msg);else ElMessage.error('修改账号名失败，请稍后重试')}
  finally{usernameLoading.value=false}
}

async function handleChangePassword() {
  try { await pwdFormRef.value?.validate() } catch { return }
  pwdLoading.value=true
  try { await changePassword(pwdForm.oldPassword,pwdForm.newPassword); ElMessage.success('密码修改成功！请使用新密码重新登录'); userStore.clearLoginInfo(); window.location.href='/login' }
  catch(e:any){const msg=e?.response?.data?.message||e?.message;if(msg)ElMessage.error('修改失败：'+msg);else ElMessage.error('修改密码失败，请稍后重试')}
  finally{pwdLoading.value=false}
}

onMounted(()=>loadUserInfo())
</script>

<style scoped>
.profile-header { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; }
.avatar-wrap { position: relative; }
.profile-avatar { background: linear-gradient(135deg, #409eff, #66b1ff); color: #fff; font-size: 28px; font-weight: 700; }
.avatar-upload-btn {
  position: absolute; bottom: 0; right: 0; width: 24px; height: 24px; border-radius: 50%;
  background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: transform 0.2s; border: 2px solid #fff;
}
.avatar-upload-btn:hover { transform: scale(1.1); }
.avatar-tip { color: #909399; font-size: 11px; margin-top: 4px; margin-bottom: 8px; }
.profile-meta h3 { color: #303133; font-size: 17px; font-weight: 600; margin-bottom: 4px; }
.meta-text { color: #909399; font-size: 12px; margin-left: 8px; }
.bio-section { margin-bottom: 8px; }
.bio-text { color: #606266; font-size: 13px; line-height: 1.6; margin-bottom: 4px; }
.bio-empty { color: #c0c4cc; font-style: italic; }
.section-title { color: #303133; font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.section-desc { color: #909399; font-size: 12px; margin-bottom: 12px; }

[data-theme="dark"] .avatar-upload-btn { border-color: #1d1e1f; }
[data-theme="dark"] .avatar-tip { color: #6a6a6a; }
[data-theme="dark"] .profile-meta h3 { color: #e0e0e0; }
[data-theme="dark"] .meta-text { color: #a0a0a0; }
[data-theme="dark"] .bio-text { color: #c0c0c0; }
[data-theme="dark"] .bio-empty { color: #6a6a6a; }
[data-theme="dark"] .section-title { color: #e0e0e0; }
[data-theme="dark"] .section-desc { color: #a0a0a0; }
</style>
