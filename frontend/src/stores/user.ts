import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const role = ref(localStorage.getItem('role') || '')
  const username = ref(localStorage.getItem('username') || '')
  const avatarUrl = ref(localStorage.getItem('avatarUrl') || '')

  function setLoginInfo(newToken: string, newRole: string, newUsername: string, newAvatarUrl?: string) {
    token.value = newToken
    role.value = newRole
    username.value = newUsername
    avatarUrl.value = newAvatarUrl || ''
    localStorage.setItem('token', newToken)
    localStorage.setItem('role', newRole)
    localStorage.setItem('username', newUsername)
    localStorage.setItem('avatarUrl', newAvatarUrl || '')
  }

  function clearLoginInfo() {
    token.value = ''
    role.value = ''
    username.value = ''
    avatarUrl.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('username')
    localStorage.removeItem('avatarUrl')
  }

  const isLoggedIn = () => !!token.value
  const isSuperAdmin = () => role.value === 'super_admin'
  const isAdmin = () => role.value === 'admin' || role.value === 'super_admin'
  const isUser = () => role.value === 'user' || role.value === 'admin' || role.value === 'super_admin'
  const canManage = () => role.value === 'admin' || role.value === 'super_admin'
  const canBorrow = () => role.value === 'user' || role.value === 'admin' || role.value === 'super_admin'

  const roleLabel = computed(() => {
    if (role.value === 'super_admin') return '超管'
    if (role.value === 'admin') return '管理员'
    if (role.value === 'user') return '用户'
    return '游客'
  })

  const roleTagType = computed(() => {
    if (role.value === 'super_admin') return 'danger'
    if (role.value === 'admin') return 'warning'
    if (role.value === 'user') return 'info'
    return 'info'
  })

  return { token, role, username, avatarUrl, setLoginInfo, clearLoginInfo, isLoggedIn, isSuperAdmin, isAdmin, isUser, canManage, canBorrow, roleLabel, roleTagType }
})
