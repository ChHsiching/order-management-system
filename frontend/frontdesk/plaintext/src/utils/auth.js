import api from './api.js'

export default {
  // 用户登录
  async login(username, password) {
    const response = await api.post('/api/user/login', null, {
      params: {
        username,
        password
      }
    })
    return response
  },

  // 用户注册
  async register(userData) {
    const response = await api.post('/api/user/register', null, {
      params: userData
    })
    return response
  },

  // 获取用户信息
  async getUserInfo() {
    const response = await api.get('/api/user/info')
    return response
  },

  // 退出登录
  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
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