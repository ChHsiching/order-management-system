<template>
  <div class="layout-container">
    <!-- 头部 -->
    <header class="header">
      <div class="header-left">
        <h1>基于web的订餐管理系统</h1>
      </div>
      <div class="header-right">
        <span>欢迎：系统管理员</span>
        <button @click="handleLogout" class="btn-logout">退出登录</button>
      </div>
    </header>
    
    <div class="main-container">
      <!-- 左侧菜单 -->
      <aside class="sidebar">
        <nav class="nav-menu">
          <div class="menu-title">系统菜单</div>
          <ul class="menu-list">
            <li class="menu-item">
              <router-link to="/admin/menu-category" class="menu-link">
                📁 菜单类别管理
              </router-link>
            </li>
            <li class="menu-item">
              <router-link to="/admin/menu-info" class="menu-link">
                🍽️ 菜单信息管理
              </router-link>
            </li>
            <li class="menu-item">
              <router-link to="/admin/member" class="menu-link">
                👥 会员管理
              </router-link>
            </li>
            <li class="menu-item">
              <router-link to="/admin/order" class="menu-link">
                📋 订单管理
              </router-link>
            </li>
            <li class="menu-item">
              <router-link to="/admin/password-change" class="menu-link">
                🔐 密码修改
              </router-link>
            </li>
          </ul>
        </nav>
      </aside>
      
      <!-- 主内容区域 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Layout',
  setup() {
    const router = useRouter()
    
    // 退出登录
    const handleLogout = async () => {
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // TODO: 调用后端退出登录接口
        // await axios.post('/api/admin/logout')
        
        // 清除本地存储的token
        localStorage.removeItem('token')
        ElMessage.success('退出登录成功')
        router.push('/login')
      } catch (error) {
        // 用户取消退出
      }
    }
    
    return {
      handleLogout
    }
  },
  created() {
    // 检查是否已登录
    const token = localStorage.getItem('token')
    if (!token) {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: linear-gradient(90deg, #a8e6cf 0%, #dcedc8 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-left h1 {
  color: #2c3e50;
  font-size: 20px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-logout {
  background: #f56c6c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-logout:hover {
  background: #f78989;
}

.main-container {
  flex: 1;
  display: flex;
}

.sidebar {
  width: 200px;
  background: #a8e6cf;
  border-right: 1px solid #ddd;
}

.nav-menu {
  padding: 20px 0;
}

.menu-title {
  padding: 10px 20px;
  font-weight: bold;
  color: #2c3e50;
  border-bottom: 1px solid rgba(255,255,255,0.3);
  margin-bottom: 10px;
}

.menu-list {
  list-style: none;
}

.menu-item {
  margin-bottom: 2px;
}

.menu-link {
  display: block;
  padding: 12px 20px;
  color: #2c3e50;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.menu-link:hover {
  background: rgba(255,255,255,0.2);
  border-left-color: #409eff;
}

.menu-link.router-link-active {
  background: rgba(255,255,255,0.3);
  border-left-color: #409eff;
  font-weight: bold;
}

.content {
  flex: 1;
  padding: 20px;
  background: #f5f5f5;
  overflow-y: auto;
}
</style> 