import { createRouter, createWebHistory } from 'vue-router'
import { authGuard, afterEachHook } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/admin',
      name: 'admin',
      redirect: '/admin/dashboard',
      meta: {
        title: '后台管理 - Web订餐系统',
        requiresAuth: true
      }
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../views/Login.vue'),
      meta: {
        title: '管理员登录 - Web订餐系统',
        requiresGuest: true
      }
    },
    {
      path: '/admin/dashboard',
      name: 'dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: {
        title: '控制台 - Web订餐系统',
        requiresAuth: true,
        roles: [1, 2] // 管理员和接单员
      }
    },
    {
      path: '/admin/users',
      name: 'users',
      component: () => import('../views/users/Users.vue'),
      meta: {
        title: '用户管理 - Web订餐系统',
        requiresAuth: true,
        roles: [1] // 仅管理员
      }
    },
    {
      path: '/admin/users/create',
      name: 'create-user',
      component: () => import('../views/users/CreateUser.vue'),
      meta: {
        title: '创建用户 - Web订餐系统',
        requiresAuth: true,
        roles: [1] // 仅管理员
      }
    },
    {
      path: '/admin/users/:username/edit',
      name: 'edit-user',
      component: () => import('../views/users/EditUser.vue'),
      meta: {
        title: '编辑用户 - Web订餐系统',
        requiresAuth: true,
        roles: [1] // 仅管理员
      }
    },
    {
      path: '/admin/categories',
      name: 'categories',
      component: () => import('../views/menu/Categories.vue'),
      meta: {
        title: '菜品分类 - Web订餐系统',
        requiresAuth: true,
        roles: [1] // 仅管理员
      }
    },
    {
      path: '/admin/menu',
      name: 'menu-management',
      component: () => import('../views/menu/MenuManagement.vue'),
      meta: {
        title: '菜品管理 - Web订餐系统',
        requiresAuth: true,
        roles: [1] // 仅管理员
      }
    },
    {
      path: '/admin/menu/create',
      name: 'create-menu',
      component: () => import('../views/menu/CreateMenu.vue'),
      meta: {
        title: '添加菜品 - Web订餐系统',
        requiresAuth: true,
        roles: [1] // 仅管理员
      }
    },
    {
      path: '/admin/menu/:id/edit',
      name: 'edit-menu',
      component: () => import('../views/menu/EditMenu.vue'),
      meta: {
        title: '编辑菜品 - Web订餐系统',
        requiresAuth: true,
        roles: [1] // 仅管理员
      }
    },
    {
      path: '/admin/orders',
      name: 'orders',
      component: () => import('../views/orders/Orders.vue'),
      meta: {
        title: '订单管理 - Web订餐系统',
        requiresAuth: true,
        roles: [1, 2] // 管理员和接单员
      }
    },
    {
      path: '/admin/orders/:id',
      name: 'order-detail',
      component: () => import('../views/orders/OrderDetail.vue'),
      meta: {
        title: '订单详情 - Web订餐系统',
        requiresAuth: true,
        roles: [1, 2] // 管理员和接单员
      }
    },
    {
      path: '/admin/statistics',
      name: 'statistics',
      component: () => import('../views/Statistics.vue'),
      meta: {
        title: '统计报表 - Web订餐系统',
        requiresAuth: true,
        roles: [1] // 仅管理员
      }
    },
    {
      path: '/admin/profile',
      name: 'admin-profile',
      component: () => import('../views/Profile.vue'),
      meta: {
        title: '个人中心 - Web订餐系统',
        requiresAuth: true,
        roles: [1, 2] // 管理员和接单员
      }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/admin'
    }
  ],
})

// 注册全局前置守卫
router.beforeEach(authGuard)

// 注册全局后置钩子
router.afterEach(afterEachHook)

export default router
