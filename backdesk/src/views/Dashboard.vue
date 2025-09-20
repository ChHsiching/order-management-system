<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>控制台</h1>
      <p>欢迎来到 Web订餐系统后台管理</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon users">
                <el-icon size="32"><User /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.userCount }}</h3>
                <p>用户总数</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon orders">
                <el-icon size="32"><ShoppingCart /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.orderCount }}</h3>
                <p>今日订单</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon revenue">
                <el-icon size="32"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <h3>¥{{ stats.revenue.toFixed(2) }}</h3>
                <p>今日营收</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon menu">
                <el-icon size="32"><Food /></el-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.menuCount }}</h3>
                <p>菜品数量</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h2>快捷操作</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="action-card" @click="$router.push('/admin/orders')">
            <div class="action-content">
              <el-icon size="40"><ShoppingCart /></el-icon>
              <div class="action-info">
                <h3>订单管理</h3>
                <p>查看和处理订单</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="action-card" @click="$router.push('/admin/menu')">
            <div class="action-content">
              <el-icon size="40"><Food /></el-icon>
              <div class="action-info">
                <h3>菜品管理</h3>
                <p>管理菜品和分类</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="action-card" @click="$router.push('/admin/users')">
            <div class="action-content">
              <el-icon size="40"><User /></el-icon>
              <div class="action-info">
                <h3>用户管理</h3>
                <p>管理系统用户</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { User, ShoppingCart, Money, Food } from '@element-plus/icons-vue'

// 统计数据
const stats = reactive({
  userCount: 0,
  orderCount: 0,
  revenue: 0,
  menuCount: 0
})

// 获取统计数据
const getStats = async () => {
  try {
    // TODO: 从API获取实际统计数据
    stats.userCount = 156
    stats.orderCount = 45
    stats.revenue = 3580.50
    stats.menuCount = 28
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 页面加载时获取数据
onMounted(() => {
  getStats()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h1 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 28px;
}

.dashboard-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.stats-grid {
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.users {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.orders {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.menu {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: #333;
}

.stat-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.quick-actions {
  margin-bottom: 30px;
}

.quick-actions h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 120px;
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.action-content {
  display: flex;
  align-items: center;
  gap: 15px;
  height: 100%;
}

.action-content .el-icon {
  color: #409eff;
}

.action-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.action-info p {
  margin: 0;
  color: #666;
  font-size: 12px;
}
</style>