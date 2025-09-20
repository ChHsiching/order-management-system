import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminAuthUtils } from '@/utils/api'

// 白名单路径（不需要登录即可访问）
const whiteList = ['/admin/login', '/admin']

// 路由守卫
export const authGuard = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) => {
  const isLoggedIn = adminAuthUtils.isLoggedIn()
  const requiresAuth = to.meta.requiresAuth
  const requiresGuest = to.meta.requiresGuest

  // 检查是否为后台管理路径
  if (to.path.startsWith('/admin')) {
    // 如果需要登录权限但管理员未登录
    if (requiresAuth && !isLoggedIn) {
      ElMessage.warning('请先登录')
      next('/admin/login')
      return
    }

    // 如果是游客页面但管理员已登录
    if (requiresGuest && isLoggedIn) {
      next('/admin/dashboard')
      return
    }

    // 检查管理员权限
    if (isLoggedIn && to.meta.roles) {
      const adminInfo = adminAuthUtils.getAdminInfo()
      if (!adminInfo || !(to.meta.roles as number[]).includes(adminInfo.role)) {
        ElMessage.error('权限不足')
        next('/admin/dashboard')
        return
      }
    }
  } else {
    // 非后台路径，拒绝访问
    ElMessage.error('非法访问')
    next('/admin')
    return
  }

  next()
}

// 全局后置钩子（用于设置页面标题）
export const afterEachHook = (to: RouteLocationNormalized) => {
  // 设置页面标题
  document.title = to.meta.title as string || '后台管理系统 - Web订餐系统'
}