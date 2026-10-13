<template>
  <div class="change-pwd-page">
    <el-card shadow="hover" style="max-width:460px;margin:40px auto 0">
      <template #header><span style="font-weight:600">修改密码</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" style="max-width:360px">
        <el-form-item label="旧密码" prop="oldPassword"><el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入旧密码" /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="loading" @click="handleSubmit">确认修改</el-button><el-button @click="router.back()">返回</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword } from '../api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ oldPassword:'', newPassword:'', confirmPassword:'' })
const rules = {
  oldPassword:[{required:true,message:'请输入旧密码',trigger:'blur'}],
  newPassword:[{required:true,message:'请输入新密码',trigger:'blur'},{min:6,message:'密码至少6位',trigger:'blur'}],
  confirmPassword:[{required:true,message:'请确认新密码',trigger:'blur'},{validator:(_rule:any,value:string,callback:Function)=>{if(value!==form.newPassword)callback(new Error('两次输入不一致'));else callback()},trigger:'blur'}]
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  loading.value=true
  try { await changePassword(form.oldPassword,form.newPassword); ElMessage.success('密码修改成功，请重新登录'); localStorage.removeItem('token'); localStorage.removeItem('role'); localStorage.removeItem('username'); router.push('/login') } catch {} finally { loading.value=false }
}
</script>

<style scoped>
</style>
