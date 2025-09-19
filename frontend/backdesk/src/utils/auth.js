import api from './api.js'

export default {
  // 管理员登录
  async login(username, password) {
    const response = await api.post('/api/admin/login', null, {
      params: {
        username,
        password
      }
    })
    return response
  },

  // 获取管理员信息
  async getAdminInfo() {
    const response = await api.get('/api/admin/info')
    return response
  },

  // 退出登录
  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('adminInfo')
    window.location.href = '/login'
  },

  // 检查是否已登录
  isLoggedIn() {
    return !!localStorage.getItem('token')
  },

  // 获取token
  getToken() {
    return localStorage.getItem('token')
  },

  // 存储token
  setToken(token) {
    localStorage.setItem('token', token)
  }
}