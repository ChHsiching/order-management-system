import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import MenuCategory from '../views/MenuCategory.vue'
import MenuInfo from '../views/MenuInfo.vue'
import Member from '../views/Member.vue'
import Order from '../views/Order.vue'
import PasswordChange from '../views/PasswordChange.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/admin',
    name: 'Layout',
    component: Layout,
    children: [
      {
        path: 'menu-category',
        name: 'MenuCategory',
        component: MenuCategory
      },
      {
        path: 'menu-info',
        name: 'MenuInfo',
        component: MenuInfo
      },
      {
        path: 'member',
        name: 'Member',
        component: Member
      },
      {
        path: 'order',
        name: 'Order',
        component: Order
      },
      {
        path: 'password-change',
        name: 'PasswordChange',
        component: PasswordChange
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 