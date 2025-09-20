import axios from 'axios'
import { ElMessage } from 'element-plus'

// API基础配置
const API_BASE_URL = 'http://localhost:8080/WebOrderSystem/api'
const TOKEN_KEY = 'web_order_token'
const USER_INFO_KEY = 'web_order_user_info'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem(TOKEN_KEY)
          localStorage.removeItem(USER_INFO_KEY)
          window.location.href = '/login'
          ElMessage.error('登录已过期，请重新登录')
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data.message || '网络错误')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败')
    } else {
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

// 认证相关API
export const authApi = {
  // 用户登录
  login: (username: string, password: string) => {
    return api.post('/user/login', { username, password })
  },

  // 用户注册
  register: (userData: any) => {
    return api.post('/user/register', userData)
  },

  // 获取当前用户信息
  getCurrentUser: () => {
    return api.get('/user/me')
  },

  // 修改密码
  changePassword: (oldPassword: string, newPassword: string) => {
    return api.post('/user/change-password', { oldPassword, newPassword })
  },

  // 更新用户信息
  updateProfile: (userData: any) => {
    return api.put('/user/update', userData)
  }
}

// 菜品相关API
export const menuApi = {
  // 获取所有菜品分类
  getCategories: () => {
    return api.get('/categories')
  },

  // 获取菜品列表
  getMenus: (params?: any) => {
    return api.get('/menu', { params })
  },

  // 根据分类获取菜品
  getMenusByCategory: (categoryId: number) => {
    return api.get(`/menu/category/${categoryId}`)
  },

  // 获取推荐菜品
  getRecommendedMenus: () => {
    return api.get('/menu/recommended')
  },

  // 获取菜品详情
  getMenuDetail: (id: number) => {
    return api.get(`/menu/${id}`)
  }
}

// 订单相关API
export const orderApi = {
  // 创建订单
  createOrder: (orderData: any) => {
    return api.post('/orders', orderData)
  },

  // 获取用户订单列表
  getUserOrders: (params?: any) => {
    return api.get('/orders', { params })
  },

  // 获取订单详情
  getOrderDetail: (orderId: string) => {
    return api.get(`/orders/${orderId}`)
  },

  // 取消订单
  cancelOrder: (orderId: string) => {
    return api.post(`/orders/${orderId}/cancel`)
  }
}

// 购物车相关API
export const cartApi = {
  // 获取购物车
  getCart: (username: string) => {
    return api.get('/cart', { params: { username } })
  },

  // 添加到购物车
  addToCart: (username: string, menuId: number, quantity: number) => {
    return api.post('/cart/add', null, {
      params: { username, productId: menuId, quantity }
    })
  },

  // 更新购物车商品数量
  updateCartItem: (username: string, productId: number, quantity: number) => {
    return api.put('/cart/update', null, {
      params: { username, productId, quantity }
    })
  },

  // 删除购物车商品
  removeCartItem: (username: string, productId: number) => {
    return api.delete('/cart/remove', {
      params: { username, productId }
    })
  },

  // 清空购物车
  clearCart: (username: string) => {
    return api.delete('/cart/clear', {
      params: { username }
    })
  }
}

// 工具函数
export const authUtils = {
  // 保存token
  setToken: (token: string) => {
    localStorage.setItem(TOKEN_KEY, token)
  },

  // 获取token
  getToken: () => {
    return localStorage.getItem(TOKEN_KEY)
  },

  // 移除token
  removeToken: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  },

  // 保存用户信息
  setUserInfo: (userInfo: any) => {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
  },

  // 获取用户信息
  getUserInfo: () => {
    const userInfo = localStorage.getItem(USER_INFO_KEY)
    return userInfo ? JSON.parse(userInfo) : null
  },

  // 检查是否已登录
  isLoggedIn: () => {
    return !!localStorage.getItem(TOKEN_KEY)
  },

  // 检查用户角色
  hasRole: (role: string | number) => {
    const userInfo = authUtils.getUserInfo()
    if (!userInfo) return false
    return userInfo.role === role || userInfo.role === parseInt(role as string)
  }
}

export default api