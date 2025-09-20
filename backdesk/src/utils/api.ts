import axios from 'axios'
import { ElMessage } from 'element-plus'

// API基础配置
const API_BASE_URL = 'http://localhost:8080/WebOrderSystem/api'
const ADMIN_TOKEN_KEY = 'web_order_admin_token'
const ADMIN_INFO_KEY = 'web_order_admin_info'

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
    // 从localStorage获取admin token
    const token = localStorage.getItem(ADMIN_TOKEN_KEY)
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
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem(ADMIN_TOKEN_KEY)
          localStorage.removeItem(ADMIN_INFO_KEY)
          window.location.href = '/admin/login'
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

// 管理员认证API
export const adminAuthApi = {
  // 管理员登录
  login: (username: string, password: string) => {
    return api.post('/admin/login', { username, password })
  },

  // 获取当前管理员信息
  getCurrentAdmin: () => {
    return api.get('/admin/info')
  },

  // 管理员登出
  logout: () => {
    return api.post('/admin/logout')
  }
}

// 用户管理API
export const userManagementApi = {
  // 获取用户列表
  getUsers: (params?: any) => {
    return api.get('/admin/users', { params })
  },

  // 添加用户
  addUser: (userData: any) => {
    return api.post('/admin/users', userData)
  },

  // 更新用户信息
  updateUser: (username: string, userData: any) => {
    return api.put(`/admin/users/${username}`, userData)
  },

  // 删除用户
  deleteUser: (username: string) => {
    return api.delete(`/admin/users/${username}`)
  },

  // 修改用户密码
  changeUserPassword: (username: string, newPassword: string) => {
    return api.post(`/admin/users/${username}/password`, { newPassword })
  }
}

// 菜品分类管理API
export const categoryApi = {
  // 获取所有分类
  getCategories: () => {
    return api.get('/admin/menuCategory/list')
  },

  // 添加分类
  addCategory: (categoryData: any) => {
    return api.post('/admin/menuCategory', categoryData)
  },

  // 更新分类
  updateCategory: (id: number, categoryData: any) => {
    return api.put(`/admin/menuCategory/${id}`, categoryData)
  },

  // 删除分类
  deleteCategory: (id: number) => {
    return api.delete(`/admin/menuCategory/${id}`)
  }
}

// 菜品管理API
export const menuManagementApi = {
  // 获取菜品列表
  getMenus: (params?: any) => {
    return api.get('/admin/menu', { params })
  },

  // 添加菜品
  addMenu: (menuData: any) => {
    return api.post('/admin/menu', menuData)
  },

  // 更新菜品
  updateMenu: (id: number, menuData: any) => {
    return api.put(`/admin/menu/${id}`, menuData)
  },

  // 删除菜品
  deleteMenu: (id: number) => {
    return api.delete(`/admin/menu/${id}`)
  },

  // 设置菜品推荐状态
  setRecommended: (id: number, recommended: boolean) => {
    return api.put(`/admin/menu/${id}/recommended`, { recommended })
  },

  // 设置菜品上架状态
  setAvailable: (id: number, available: boolean) => {
    return api.put(`/admin/menu/${id}/available`, { available })
  }
}

// 订单管理API
export const orderManagementApi = {
  // 获取订单列表
  getOrders: (params?: any) => {
    return api.get('/admin/orders', { params })
  },

  // 获取订单详情
  getOrderDetail: (orderId: string) => {
    return api.get(`/admin/orders/${orderId}`)
  },

  // 更新订单状态
  updateOrderStatus: (orderId: string, status: number) => {
    return api.put(`/admin/orders/${orderId}/status`, { status })
  },

  // 受理订单
  acceptOrder: (orderId: string) => {
    return api.post(`/admin/orders/${orderId}/accept`)
  },

  // 拒绝订单
  rejectOrder: (orderId: string, reason: string) => {
    return api.post(`/admin/orders/${orderId}/reject`, { reason })
  }
}

// 统计API
export const statisticsApi = {
  // 获取基础统计
  getBasicStats: () => {
    return api.get('/admin/statistics/basic')
  },

  // 获取销售统计
  getSalesStats: (params?: any) => {
    return api.get('/admin/statistics/sales', { params })
  },

  // 获取用户统计
  getUserStats: (params?: any) => {
    return api.get('/admin/statistics/users', { params })
  }
}

// 工具函数
export const adminAuthUtils = {
  // 保存admin token
  setToken: (token: string) => {
    localStorage.setItem(ADMIN_TOKEN_KEY, token)
  },

  // 获取admin token
  getToken: () => {
    return localStorage.getItem(ADMIN_TOKEN_KEY)
  },

  // 移除admin token
  removeToken: () => {
    localStorage.removeItem(ADMIN_TOKEN_KEY)
    localStorage.removeItem(ADMIN_INFO_KEY)
  },

  // 保存管理员信息
  setAdminInfo: (adminInfo: any) => {
    localStorage.setItem(ADMIN_INFO_KEY, JSON.stringify(adminInfo))
  },

  // 获取管理员信息
  getAdminInfo: () => {
    const adminInfo = localStorage.getItem(ADMIN_INFO_KEY)
    return adminInfo ? JSON.parse(adminInfo) : null
  },

  // 检查是否已登录
  isLoggedIn: () => {
    return !!localStorage.getItem(ADMIN_TOKEN_KEY)
  },

  // 检查是否为管理员
  isAdmin: () => {
    const adminInfo = adminAuthUtils.getAdminInfo()
    return adminInfo && adminInfo.role === 1
  }
}

export default api