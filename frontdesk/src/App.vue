<template>
  <div id="app">
    <!-- 导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <h1 @click="$router.push('/')">
            <el-icon><Food /></el-icon>
            Web订餐系统
          </h1>
        </div>

        <el-menu
          mode="horizontal"
          :default-active="$route.path"
          class="nav-menu"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/menu">菜单浏览</el-menu-item>
        </el-menu>

        <div class="user-actions">
          <!-- 购物车图标 -->
          <el-badge
            :value="cartStore.totalCount"
            :hidden="cartStore.totalCount === 0"
            class="cart-badge"
          >
            <el-button
              type="primary"
              circle
              @click="$router.push('/cart')"
            >
              <el-icon><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>

          <!-- 用户菜单 -->
          <template v-if="authStore.isLoggedIn">
            <el-dropdown @command="handleUserCommand">
              <span class="user-info">
                <el-avatar :size="32" :icon="UserFilled" />
                <span class="username">{{ authStore.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 页脚 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>&copy; 2024 Web订餐系统. All rights reserved.</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Food, ShoppingCart, UserFilled, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

// 用户菜单命令处理
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'logout':
      authStore.logout()
      router.push('/')
      break
  }
}

// 页面加载时初始化
onMounted(() => {
  // 初始化认证状态
  authStore.initialize()

  // 初始化购物车
  cartStore.initialize()
})
</script>

<style scoped>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
}

.logo {
  cursor: pointer;
}

.logo h1 {
  display: flex;
  align-items: center;
  font-size: 20px;
  color: #409eff;
  margin: 0;
}

.logo h1 .el-icon {
  margin-right: 8px;
  font-size: 24px;
}

.nav-menu {
  border-bottom: none;
  flex: 1;
  margin: 0 40px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cart-badge {
  margin-right: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
}

.footer {
  background: #f5f5f5;
  padding: 20px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: #666;
}
</style>
