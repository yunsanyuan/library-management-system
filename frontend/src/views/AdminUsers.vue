<template>
  <div class="admin-users-page">
    <el-card shadow="hover">
      <template #header><span style="font-weight:600">用户管理</span></template>
      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索用户名" clearable @clear="loadUsers" @keyup.enter="loadUsers" style="width:220px" size="default">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadUsers">搜索</el-button>
      </div>
      <el-table :data="users" stripe border highlight-current-row v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }"><el-tag :type="row.role==='super_admin'?'danger':row.role==='admin'?'warning':'info'" size="small">{{ row.role==='super_admin'?'超管':row.role==='admin'?'管理员':'普通用户' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170">
          <template #default="{ row }">{{ row.createTime ? row.createTime.replace('T',' ').substring(0,16) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openRoleDialog(row)" v-if="userStore.isSuperAdmin()">改角色</el-button>
            <el-button size="small" link type="warning" @click="openResetPwdDialog(row)" :disabled="row.role==='super_admin'&&!userStore.isSuperAdmin()">重置密码</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row)" :disabled="row.role==='admin'||row.role==='super_admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-area">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="loadUsers" />
      </div>
    </el-card>

    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="360px" destroy-on-close>
      <p style="margin-bottom:12px">用户：<b>{{ editUser?.username }}</b></p>
      <el-select v-model="newRole" style="width:100%">
        <el-option label="普通用户" value="user" />
        <el-option label="管理员" value="admin" v-if="userStore.isSuperAdmin()" />
        <el-option label="超级管理员" value="super_admin" v-if="userStore.isSuperAdmin()" />
      </el-select>
      <template #footer><el-button @click="roleDialogVisible=false">取消</el-button><el-button type="primary" @click="handleRoleChange">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="pwdDialogVisible" title="重置用户密码" width="360px" destroy-on-close>
      <p style="margin-bottom:12px">用户：<b>{{ editUser?.username }}</b></p>
      <el-input v-model="newPassword" placeholder="输入新密码" type="password" show-password />
      <template #footer><el-button @click="pwdDialogVisible=false">取消</el-button><el-button type="primary" @click="handleResetPwd">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUsers, updateUserRole, deleteUser, resetPassword, type UserVO } from '../api/user'
import { useUserStore } from '../stores/user'
import { useDisposed } from '../composables/useDisposed'

const userStore = useUserStore()
const disposed = useDisposed()

const users = ref<UserVO[]>([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const roleDialogVisible = ref(false)
const pwdDialogVisible = ref(false)
const editUser = ref<UserVO | null>(null)
const newRole = ref('user')
const newPassword = ref('')

async function loadUsers() { loading.value=true; try { const res=await getUsers(page.value,size.value,keyword.value||undefined); if(disposed.value)return; users.value=res.data.records; total.value=res.data.total } catch {} finally { if(!disposed.value) loading.value=false } }
function openRoleDialog(user:UserVO){editUser.value=user;newRole.value=user.role;roleDialogVisible.value=true}
async function handleRoleChange(){if(!editUser.value)return;try{await updateUserRole(editUser.value.id,newRole.value);ElMessage.success('角色修改成功');roleDialogVisible.value=false;loadUsers()}catch{}}
function openResetPwdDialog(user:UserVO){editUser.value=user;newPassword.value='';pwdDialogVisible.value=true}
async function handleResetPwd(){if(!editUser.value||!newPassword.value)return;try{await resetPassword(editUser.value.id,newPassword.value);ElMessage.success('密码重置成功');pwdDialogVisible.value=false}catch{}}
async function handleDelete(user:UserVO){try{await ElMessageBox.confirm(`确定删除用户「${user.username}」吗？`,'删除确认',{confirmButtonText:'确定',cancelButtonText:'取消',type:'warning'});await deleteUser(user.id);ElMessage.success('删除成功');loadUsers()}catch{}}

onMounted(()=>loadUsers())
</script>

<style scoped>
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; }
.pagination-area { margin-top: 16px; display: flex; justify-content: center; }
</style>
