//路由配置
import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import LoginRegister from '../pages/LoginRegister.vue'
import DishDetail from '../pages/DishDetail.vue'
import Cart from '../pages/Cart.vue'
import { useUserStore } from '../store/userStore.js'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', name: 'Home', component: Home },
        { path: '/login-register', name: 'LoginRegister', component: LoginRegister },
        { path: '/dish/:id', name: 'DishDetail', component: DishDetail, props: true },
        {
            path: '/cart',
            name: 'Cart',
            component: Cart,
            // 路由守卫：未登录用户无法进入购物车
            beforeEnter: (to, from, next) => {
                const userStore = useUserStore()
                if (userStore.token) {
                    next()
                } else {
                    next('/login-register?redirect=/cart')
                }
            }
        }
    ]
})

export default router