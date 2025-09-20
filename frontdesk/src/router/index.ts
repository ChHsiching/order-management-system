import { createRouter, createWebHistory } from 'vue-router'
import { authGuard, afterEachHook } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue'),
      meta: {
        title: '首页 - Web订餐系统'
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: {
        title: '用户登录 - Web订餐系统',
        requiresGuest: true
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: {
        title: '用户注册 - Web订餐系统',
        requiresGuest: true
      }
    },
    {
      path: '/menu',
      name: 'menu',
      component: () => import('../views/Menu.vue'),
      meta: {
        title: '菜单浏览 - Web订餐系统'
      }
    },
    {
      path: '/menu/:id',
      name: 'menu-detail',
      component: () => import('../views/MenuDetail.vue'),
      meta: {
        title: '菜品详情 - Web订餐系统'
      }
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/Cart.vue'),
      meta: {
        title: '购物车 - Web订餐系统',
        requiresAuth: true
      }
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('../views/Checkout.vue'),
      meta: {
        title: '订单结算 - Web订餐系统',
        requiresAuth: true
      }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../views/Orders.vue'),
      meta: {
        title: '我的订单 - Web订餐系统',
        requiresAuth: true
      }
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('../views/OrderDetail.vue'),
      meta: {
        title: '订单详情 - Web订餐系统',
        requiresAuth: true
      }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/Profile.vue'),
      meta: {
        title: '个人中心 - Web订餐系统',
        requiresAuth: true
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFound.vue'),
      meta: {
        title: '页面未找到 - Web订餐系统'
      }
    }
  ],
})

// 注册全局前置守卫
router.beforeEach(authGuard)

// 注册全局后置钩子
router.afterEach(afterEachHook)

export default router
