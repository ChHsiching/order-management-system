
<!--  顶部导航栏  -->

<template>
  <nav class="navbar">
    <div class="navbar-container">
      <!-- 网站标题 -->
      <div class="navbar-logo" @click="handleGoHome">
        <h1>基于Web的订餐管理系统</h1>
      </div>

      <!-- 导航菜单 -->
      <div class="navbar-menu">
        <a href="/" class="menu-item">首页</a>
        <a href="/#" class="menu-item">点餐活动</a>
        <a href="/#" class="menu-item">留言板</a>
      </div>

      <!-- 用户操作 -->
      <div class="navbar-user">
        <div v-if="userStore.token">
          <span class="user-name">欢迎, {{ userStore.userInfo.name }}</span>
          <button class="btn-logout" @click="handleLogout">退出登录</button>
          <a href="/cart" class="btn-cart">
            <i class="icon-cart">🛒</i>
            <span class="cart-count" v-if="cartStore.cartList.length > 0">
              {{ cartStore.cartList.length }}
            </span>
          </a>
        </div>
        <div v-else>
          <a href="/login-register" class="btn-login">登录/注册</a>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/userStore.js'
import { useCartStore } from '../store/cartStore.js'

const router = useRouter()
//const userStore = useUserStore()
const cartStore = useCartStore()

// 跳转到首页
const handleGoHome = () => {
  router.push('/')
}

// 退出登录
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    //userStore.logout()
    router.push('/')
  }
}
</script>

<style scoped>
.navbar {
  background-color: #ff6700;
  color: #fff;
  height: 60px;
  line-height: 60px;
}

.navbar-container {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-logo h1 {
  font-size: 20px;
  font-weight: 500;
  cursor: pointer;
}

.navbar-menu {
  display: flex;
  gap: 30px;
}

.menu-item {
  color: #fff;
  text-decoration: none;
  font-size: 14px;
}

.menu-item:hover {
  text-decoration: underline;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-name {
  font-size: 14px;
}

.btn-logout {
  background-color: transparent;
  color: #fff;
  border: 1px solid #fff;
  border-radius: 4px;
  padding: 3px 10px;
  font-size: 12px;
  cursor: pointer;
}

.btn-cart {
  position: relative;
  color: #fff;
  text-decoration: none;
  font-size: 20px;
}

.cart-count {
  position: absolute;
  top: -10px;
  right: -10px;
  background-color: #ff4d4f;
  color: #fff;
  font-size: 12px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  line-height: 18px;
  text-align: center;
}

.btn-login {
  color: #fff;
  text-decoration: none;
  font-size: 14px;
  border: 1px solid #fff;
  border-radius: 4px;
  padding: 3px 15px;
}
</style>