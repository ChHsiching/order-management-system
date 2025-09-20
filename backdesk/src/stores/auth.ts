import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAuthApi } from '@/utils/api'

export const useAuthStore = defineStore('auth', () => {
  // 用户信息
  const user = ref<any>(null)
  const token = ref<string>('')

  // 是否已登录
  const isLoggedIn = () => {
    return !!token.value
  }

  // 获取用户信息
  const getUserInfo = () => {
    return user.value
  }

  // 登录
  const login = async (username: string, password: string) => {
    try {
      const response: any = await adminAuthApi.login(username, password)

      if (response.code === 200) {
        token.value = response.data.token
        user.value = response.data.user

        // 保存到本地存储
        localStorage.setItem('admin_token', token.value)
        localStorage.setItem('admin_user', JSON.stringify(user.value))

        return true
      } else {
        ElMessage.error(response.message || '登录失败')
        return false
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败，请检查网络连接')
      return false
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    user.value = null

    // 清除本地存储
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')

    ElMessage.success('已退出登录')
  }

  // 从本地存储恢复登录状态
  const restoreAuth = () => {
    const savedToken = localStorage.getItem('admin_token')
    const savedUser = localStorage.getItem('admin_user')

    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
    }
  }

  // 检查权限
  const hasPermission = (requiredRoles: number[]) => {
    if (!user.value) return false
    return requiredRoles.includes(user.value.role)
  }

  return {
    user,
    token,
    isLoggedIn,
    getUserInfo,
    login,
    logout,
    restoreAuth,
    hasPermission
  }
})