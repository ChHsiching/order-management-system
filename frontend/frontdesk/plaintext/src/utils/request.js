//Axios封装
import axios from 'axios'
import { useUserStore } from '../store/userStore'

// 创建Axios实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    timeout: 5000
})

// 请求拦截器：添加Token
request.interceptors.request.use(
    (config) => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// 响应拦截器：统一错误处理
request.interceptors.response.use(
    (response) => response.data,
    (error) => {
        // 未登录或Token过期，跳转登录页
        if (error.response?.status === 401) {
            const userStore = useUserStore()
            userStore.logout()
            window.location.href = `/login-register?redirect=${window.location.pathname}`
        }
        return Promise.reject(error.response?.data || { message: '网络错误' })
    }
)

export default request