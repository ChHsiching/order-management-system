import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'
import App from './App.vue'
import router from './router/index.js'

// 导入全局样式（若有，可根据项目实际需求添加）
//import './assets/css/global.css'

// 1. 创建核心实例
const app = createApp(App)
const pinia = createPinia()

// 2. 配置全局依赖
// 2.1 安装Pinia状态管理（用于购物车、用户信息等全局状态）
app.use(pinia)

// 2.2 安装路由（用于页面跳转和权限控制）
app.use(router)

// 2.3 配置Axios全局挂载（方便组件内直接通过this.$axios调用）
app.config.globalProperties.$axios = axios

// 2.4 配置Axios基础路径（与后端接口地址匹配，对应文档中Tomcat服务器配置）
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
// 配置请求超时时间（避免因网络问题导致的无限等待）
axios.defaults.timeout = 5000

// 2.5 配置Axios请求拦截器（添加Token，实现文档2.4节的权限区分逻辑）
axios.interceptors.request.use(
    (config) => {
        // 从Pinia的用户Store中获取Token（未登录时Token为空）
        const userStore = useUserStore()
        if (userStore.token) {
            // 为请求头添加Token，供后端验证用户身份和权限
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    (error) => {
        // 请求发送失败时的全局错误处理
        console.error('请求发送失败:', error)
        return Promise.reject(error)
    }
)

// 2.6 配置Axios响应拦截器（统一处理后端返回结果，包括权限失效等场景）
axios.interceptors.response.use(
    (response) => {
        // 直接返回响应体中的data字段，简化组件内数据获取逻辑
        return response.data
    },
    (error) => {
        // 处理401权限错误（Token过期或未登录，对应文档2.4节权限控制要求）
        if (error.response?.status === 401) {
            const userStore = useUserStore()
            // 清除本地失效的用户信息
            userStore.logout()
            // 跳转到登录页，并携带当前页面路径（登录成功后返回原页面）
            router.push({
                path: '/login-register',
                query: { redirect: router.currentRoute.value.fullPath }
            })
            // 提示用户重新登录
            alert('登录已失效，请重新登录')
        }

        // 其他错误统一提示（避免组件内重复写错误提示逻辑）
        const errorMsg = error.response?.data?.message || '操作失败，请稍后重试'
        alert(errorMsg)
        return Promise.reject(error)
    }
)

// 3. 全局注册公共组件（方便所有页面直接使用，无需重复导入）
import Navbar from './components/Navbar.vue'
import Sidebar from './components/Sidebar.vue'
import DishCard from './components/DishCard.vue'

app.component('Navbar', Navbar)    // 顶部导航栏（文档6.3.1节主页面核心组件）
app.component('Sidebar', Sidebar)  // 左侧分类栏（文档6.3.1节菜单分类展示组件）
app.component('DishCard', DishCard)// 菜品卡片（文档6.3.1节菜品列表展示组件）

// 4. 挂载应用（渲染到页面挂载点，对应public/index.html中的<div id="app"></div>）
app.mount('#app')