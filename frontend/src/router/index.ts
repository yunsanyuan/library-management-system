import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { title: '注册' } },
  {
    path: '/', component: () => import('../components/MainLayout.vue'), redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'books', name: 'Books', component: () => import('../views/BookList.vue'), meta: { title: '图书浏览' } },
      { path: 'books/:id', name: 'BookDetail', component: () => import('../views/BookDetail.vue'), meta: { title: '图书详情' } },
      { path: 'borrow/my', name: 'MyBorrow', component: () => import('../views/MyBorrow.vue'), meta: { title: '我的借阅', requireLogin: true } },
      { path: 'change-password', name: 'ChangePassword', component: () => import('../views/ChangePassword.vue'), meta: { title: '修改密码', requireLogin: true } },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心', requireLogin: true } },
      { path: 'user/:id', name: 'UserProfile', component: () => import('../views/UserProfile.vue'), meta: { title: '用户主页' } },
      { path: 'admin/books', name: 'AdminBooks', component: () => import('../views/AdminBooks.vue'), meta: { title: '图书管理', requireManage: true } },
      { path: 'admin/borrows', name: 'AdminBorrows', component: () => import('../views/AdminBorrows.vue'), meta: { title: '借阅管理', requireManage: true } },
      { path: 'admin/users', name: 'AdminUsers', component: () => import('../views/AdminUsers.vue'), meta: { title: '用户管理', requireManage: true } }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (token && (to.path === '/login' || to.path === '/register')) {
    next('/dashboard'); return
  }
  if (to.meta.requireManage && (!token || (role !== 'admin' && role !== 'super_admin'))) {
    next('/dashboard'); return
  }
  if (to.meta.requireLogin && !token) {
    next('/login'); return
  }
  next()
})

export default router
