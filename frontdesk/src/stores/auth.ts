import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, authUtils } from '@/utils/api'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<any>(null)
  const token = ref<string | null>(null)
  const loading = ref<boolean>(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const username = computed(() => user.value?.username || '')
  const role = computed(() => user.value?.role || 0)
  const isAdmin = computed(() => role.value === 1)
  const isMember = computed(() => role.value === 0)

  // 初始化状态
  const initialize = () => {
    console.log('[Auth Store] ===== 开始初始化 =====')
    const savedToken = authUtils.getToken()
    const savedUser = authUtils.getUserInfo()

    console.log('[Auth Store] 初始化数据:', {
      savedToken: savedToken ? '有token' : '无token',
      savedUser: savedUser ? '有用户信息' : '无用户信息',
      tokenValueType: typeof token.value,
      tokenValue: token.value,
      userType: typeof user.value,
      userValue: user.value
    })

    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = savedUser
      console.log('[Auth Store] 初始化成功:', {
        token: token.value?.substring(0, 10) + '...',
        user: user.value
      })
    } else {
      console.log('[Auth Store] 没有找到保存的认证信息')
    }

    console.log('[Auth Store] 初始化后状态:', {
      isLoggedIn: isLoggedIn.value,
      username: username.value,
      token: token.value,
      user: user.value
    })
  }

  // 登录
  const login = async (username: string, password: string) => {
    loading.value = true
    try {
      const response: any = await authApi.login(username, password)
      console.log('Auth Store - 登录响应:', response)

      // 处理不同的响应格式 - 现在response是完整response对象
      const data = response.data || response
      if (data.code === 200 || data.success === true) {
        const { token: newToken, userInfo } = data.data || data

        // 保存到store
        token.value = newToken
        user.value = userInfo

        // 保存到localStorage
        authUtils.setToken(newToken)
        authUtils.setUserInfo(userInfo)

        ElMessage.success('登录成功')
        return true
      } else {
        ElMessage.error(data.message || response.message || '登录失败')
        return false
      }
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 注册
  const register = async (userData: any) => {
    loading.value = true
    try {
      const response: any = await authApi.register(userData)

      if (response.code === 200) {
        ElMessage.success('注册成功，请登录')
        return true
      } else {
        ElMessage.error(response.message || '注册失败')
        return false
      }
    } catch (error: any) {
      ElMessage.error(error.message || '注册失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    if (!token.value) return false

    loading.value = true
    try {
      const response: any = await authApi.getCurrentUser()
      console.log('Auth Store - 获取用户信息响应:', response)

      // 处理不同的响应格式 - 现在response是完整response对象
      const data = response.data || response
      if (data.code === 200 && data.data) {
        user.value = data.data
        authUtils.setUserInfo(data.data)
        return true
      } else {
        // token可能过期，清除登录状态
        logout()
        return false
      }
    } catch (error: any) {
      console.error('获取用户信息失败:', error)
      logout()
      return false
    } finally {
      loading.value = false
    }
  }

  // 修改密码
  const changePassword = async (oldPassword: string, newPassword: string) => {
    loading.value = true
    try {
      const response: any = await authApi.changePassword(oldPassword, newPassword)
      console.log('Auth Store - 修改密码响应:', response)

      // 处理不同的响应格式 - 现在response是完整response对象
      const data = response.data || response
      if (data.code === 200) {
        ElMessage.success('密码修改成功')
        return true
      } else {
        ElMessage.error(data.message || response.message || '密码修改失败')
        return false
      }
    } catch (error: any) {
      ElMessage.error(error.message || '密码修改失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    user.value = null
    authUtils.removeToken()
  }

  // 检查权限
  const hasRole = (requiredRole: string | number) => {
    if (!user.value) return false
    return user.value.role === requiredRole || user.value.role === parseInt(requiredRole as string)
  }

  // 自动初始化
  initialize()

  return {
    // 状态
    user,
    token,
    loading,

    // 计算属性
    isLoggedIn,
    username,
    role,
    isAdmin,
    isMember,

    // 方法
    initialize,
    login,
    register,
    fetchCurrentUser,
    changePassword,
    logout,
    hasRole
  }
})