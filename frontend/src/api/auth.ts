import request from './request'
import type { ApiResult } from '../types'

export function register(username: string, password: string) {
  return request.post<any, ApiResult<any>>('/auth/register', { username, password })
}

export function login(username: string, password: string) {
  return request.post<any, ApiResult<{ token: string; role: string; username: string; avatarUrl: string }>>('/auth/login', { username, password })
}
