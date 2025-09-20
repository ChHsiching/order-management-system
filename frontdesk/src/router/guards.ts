import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// 白名单路径（不需要登录即可访问）
const whiteList = ['/login', '/register', '/']

// 路由守卫
export const authGuard = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) => {
  const authStore = useAuthStore()
  const isLoggedIn = authStore.isLoggedIn
  const requiresAuth = to.meta.requiresAuth
  const requiresGuest = to.meta.requiresGuest

  // 如果需要登录权限但用户未登录
  if (requiresAuth && !isLoggedIn) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  // 如果是游客页面但用户已登录
  if (requiresGuest && isLoggedIn) {
    next('/')  // 改为跳转到首页，而不是/home
    return
  }

  // 如果用户已登录但尝试访问后台管理页面
  if (isLoggedIn && to.path.startsWith('/admin')) {
    ElMessage.error('您没有访问后台的权限')
    next('/home')
    return
  }

  // 检查特定页面权限
  if (to.meta.roles) {
    if (!authStore.user || !(to.meta.roles as any[]).includes(authStore.user.role)) {
      ElMessage.error('权限不足')
      next('/home')
      return
    }
  }

  next()
}

// 全局后置钩子（用于设置页面标题）
export const afterEachHook = (to: RouteLocationNormalized) => {
  // 设置页面标题
  document.title = to.meta.title as string || 'Web订餐系统'
}