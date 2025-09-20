import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

// 创建axios实例
const service = axios.create({
  baseURL: '/WebOrderSystem/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['Authorization'] = `Bearer ${authStore.token}`
    }

    // 简化的请求调试信息
    if (process.env.NODE_ENV === 'development') {
      const fullUrl = config.baseURL ? config.baseURL + config.url : config.url
      console.log(`🚀 API请求 [${config.method?.toUpperCase()}] ${config.url}`)
      console.log('📍 完整URL:', fullUrl)
    }

    return config
  },
  (error) => {
    console.error('❌ 请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 简化的响应调试信息
    if (process.env.NODE_ENV === 'development') {
      console.log(`✅ API响应 [${response.config.method?.toUpperCase()}] ${response.config.url} - 状态: ${response.status}`)
      console.log('🔍 响应格式:', Array.isArray(res) ? '数组' : (typeof res.code !== 'undefined' ? '标准格式' : '其他'))
    }

    // 如果返回的是文件流，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 情况1: 标准格式响应 {code: 0, data: [...]}
    if (res.code === 0 || res.code === 200) {
      return res.data || res
    }

    // 情况2: 直接返回数组数据 [...]
    if (Array.isArray(res)) {
      return res
    }

    // 情况3: 没有code字段但不是数组
    if (typeof res.code === 'undefined') {
      return res
    }

    // token过期或无效
    if (res.code === 401) {
      console.warn('⚠️ Token过期或无效，即将跳转到登录页')
      const authStore = useAuthStore()
      authStore.logout()
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
      return Promise.reject(new Error('登录已过期'))
    }

    // 其他错误
    console.error('❌ API业务错误:', res)
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // 简化的错误调试信息
    if (process.env.NODE_ENV === 'development') {
      console.group(`❌ API错误 [${error.config?.method?.toUpperCase()}] ${error.config?.url}`)
      console.error('💥 错误信息:', error.message)

      if (error.response) {
        console.error('🔥 HTTP状态:', error.response.status)
        console.error('🔥 响应数据:', error.response.data)
      } else if (error.request) {
        console.error('🌐 网络错误 - 后端服务可能未启动')
      }
      console.groupEnd()
    }

    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// 管理员认证API
export const adminAuthApi = {
  login: (username: string, password: string) => {
    const params = new URLSearchParams()
    params.append('username', username)
    params.append('password', password)
    return service.post('/admin/auth/login', params.toString(), {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
  },

  logout: () => {
    return service.post('/admin/auth/logout')
  },

  getInfo: () => {
    return service.get('/admin/info')
  }
}

// 菜单分类API
export const menuCategoryApi = {
  getList: (params?: any) => {
    return service.get('/admin/menuCategory/getList', { params })
  },

  getActive: () => {
    return service.get('/admin/menuCategory/active')
  },

  create: (data: any) => {
    return service.post('/admin/menuCategory/add', data)
  },

  update: (id: number, data: any) => {
    return service.put(`/admin/menuCategory/${id}`, data)
  },

  delete: (id: number) => {
    return service.delete(`/admin/menuCategory/${id}`)
  },

  deleteByName: (categoryName: string) => {
    return service.delete('/admin/menuCategory/delete', {
      params: { categoryName }
    })
  },

  restore: (id: number) => {
    return service.post(`/admin/menuCategory/${id}/restore`)
  },

  getStatistics: () => {
    return service.get('/admin/menuCategory/statistics')
  }
}

// 菜品管理API
export const menuApi = {
  // 菜单分类管理
  getCategories: () => {
    return service.get('/admin/menu/categories')
  },

  createCategory: (data: any) => {
    return service.post('/admin/menu/categories', data)
  },

  updateCategory: (id: number, data: any) => {
    return service.put(`/admin/menu/categories/${id}`, data)
  },

  deleteCategory: (id: number) => {
    return service.delete(`/admin/menu/categories/${id}`)
  },

  // 菜品管理
  getList: (params?: any) => {
    return service.get('/admin/menu/items', { params })
  },

  create: (data: any) => {
    return service.post('/admin/menu/items', data)
  },

  update: (id: number, data: any) => {
    return service.put(`/admin/menu/items/${id}`, data)
  },

  delete: (id: number) => {
    return service.delete(`/admin/menu/items/${id}`)
  },

  getById: (id: number) => {
    return service.get(`/admin/menu/items/${id}`)
  },

  getByCategory: (categoryId: number) => {
    return service.get(`/admin/menu/category/${categoryId}`)
  },

  getRecommended: () => {
    return service.get('/admin/menu/recommended')
  }
}

// 订单管理API
export const orderApi = {
  // 获取订单列表（管理员用）
  getList: (params?: any) => {
    return service.get('/orders/admin/all', { params })
  },

  // 获取订单详情
  getDetail: (id: string) => {
    return service.get(`/orders/${id}`)
  },

  // 获取订单项
  getOrderItems: (orderId: string) => {
    return service.get(`/orders/${orderId}/items`)
  },

  // 更新订单状态
  updateStatus: (id: string, status: number) => {
    return service.put(`/orders/admin/${id}/status`, null, {
      params: { status }
    })
  },

  // 受理订单
  acceptOrder: (id: string) => {
    return service.put(`/orders/admin/${id}/status`, null, {
      params: { status: 1 }
    })
  },

  // 完成订单
  completeOrder: (id: string) => {
    return service.put(`/orders/admin/${id}/status`, null, {
      params: { status: 2 }
    })
  },

  // 取消订单
  cancelOrder: (id: string, reason: string) => {
    return service.post(`/orders/${id}/cancel`, null, {
      params: { reason }
    })
  },

  // 删除订单
  deleteOrder: (id: string) => {
    return service.delete(`/orders/${id}`)
  }
}

// 用户管理API
export const userApi = {
  // 获取用户列表（管理员用）
  getList: (params?: any) => {
    return service.get('/user/admin/all', { params })
  },

  // 创建用户
  create: (data: any) => {
    return service.post('/user/admin/create', data)
  },

  // 更新用户
  update: (username: string, data: any) => {
    return service.put(`/user/admin/${username}`, data)
  },

  // 更新用户角色
  updateRole: (username: string, role: number) => {
    return service.put(`/user/admin/${username}/role`, null, {
      params: { role }
    })
  },

  // 删除用户
  delete: (username: string) => {
    return service.delete(`/user/admin/${username}`)
  }
}

// 管理员管理API
export const adminApi = {
  getList: (params?: any) => {
    return service.get('/admin/admins', { params })
  },

  create: (data: any) => {
    return service.post('/admin/admins', data)
  },

  update: (id: number, data: any) => {
    return service.put(`/admin/admins/${id}`, data)
  },

  delete: (id: number) => {
    return service.delete(`/admin/admins/${id}`)
  }
}

// 统计数据API
export const statisticsApi = {
  // 获取首页统计数据
  getDashboard: () => {
    return service.get('/admin/statistics/dashboard')
  },

  // 获取销售趋势
  getSalesTrend: (period?: string) => {
    return service.get('/admin/statistics/sales-trend', {
      params: { period }
    })
  },

  // 获取热门菜品
  getPopularMenus: () => {
    return service.get('/admin/statistics/popular-menus')
  },

  // 获取最新订单
  getRecentOrders: (limit?: number) => {
    return service.get('/admin/statistics/recent-orders', {
      params: { limit }
    })
  }
}

export default service