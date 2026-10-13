import request from './request'
import type { ApiResult, PageResult } from '../types'

export interface UserVO {
  id: number
  username: string
  role: string
  avatarUrl: string
  bio: string
  createTime: string
}

export function getUsers(page: number, size: number, keyword?: string) {
  return request.get<any, ApiResult<PageResult<UserVO>>>('/borrow/users', {
    params: { page, size, keyword }
  })
}

export function updateUserRole(id: number, role: string) {
  return request.put<any, ApiResult<UserVO>>(`/borrow/users/${id}/role`, { role })
}

export function deleteUser(id: number) {
  return request.delete<any, ApiResult<void>>(`/borrow/users/${id}`)
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request.put<any, ApiResult<void>>('/auth/password', { oldPassword, newPassword })
}

export function resetPassword(id: number, newPassword: string) {
  return request.put<any, ApiResult<void>>(`/borrow/users/${id}/password`, { newPassword })
}

export function getCurrentUser() {
  return request.get<any, ApiResult<UserVO>>('/auth/me')
}

export function changeUsername(username: string) {
  return request.put<any, ApiResult<UserVO>>('/auth/username', { username })
}

export function updateAvatar(avatarUrl: string) {
  return request.put<any, ApiResult<UserVO>>('/auth/avatar', { avatarUrl })
}

export function updateBio(bio: string) {
  return request.put<any, ApiResult<UserVO>>('/auth/bio', { bio })
}

export function getPublicProfile(id: number) {
  return request.get<any, ApiResult<UserVO>>(`/borrow/users/${id}/profile`)
}
