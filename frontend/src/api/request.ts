import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/**
 * Axios实例封装
 * 配置基础路径、请求拦截器（自动添加Token）和响应拦截器（统一错误处理）
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 5000
})

/**
 * 请求拦截器：自动在请求头添加JWT Token
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器：统一处理错误响应
 * - 401: Token无效或过期，跳转登录页
 * - 业务错误码非200: 弹出后端返回的错误信息
 * - HTTP错误: 弹出具体错误信息
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      // 业务错误，弹出提示
      const msg = res.message || '请求失败'
      ElMessage.error(msg)
      if (res.code === 401 || res.code === 4002) {
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        localStorage.removeItem('username')
        router.push('/login')
      }
      return Promise.reject(new Error(msg))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      localStorage.removeItem('avatarUrl')
      router.push('/login')
    } else if (error.response?.status === 403) {
      ElMessage.error('没有操作权限')
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message && error.message !== 'Network Error') {
      ElMessage.error(error.message)
    }
    return Promise.reject(error)
  }
)

export default request
