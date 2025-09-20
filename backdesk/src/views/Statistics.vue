<template>
  <div class="statistics">
    <div class="statistics-header">
      <h1>统计报表</h1>
      <p>查看系统运营数据</p>
    </div>

    <!-- 时间范围选择 -->
    <div class="date-filter">
      <el-form :inline="true">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStatistics">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 核心指标 -->
    <div class="core-metrics">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon revenue">
                <el-icon><Money /></el-icon>
              </div>
              <div class="metric-info">
                <h3>¥{{ statistics.totalRevenue.toFixed(2) }}</h3>
                <p>总营收</p>
                <span class="trend" :class="statistics.revenueTrend">
                  {{ statistics.revenueTrend === 'up' ? '↑' : '↓' }} {{ statistics.revenueGrowth }}%
                </span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon orders">
                <el-icon><ShoppingCart /></el-icon>
              </div>
              <div class="metric-info">
                <h3>{{ statistics.totalOrders }}</h3>
                <p>总订单数</p>
                <span class="trend" :class="statistics.ordersTrend">
                  {{ statistics.ordersTrend === 'up' ? '↑' : '↓' }} {{ statistics.ordersGrowth }}%
                </span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon users">
                <el-icon><User /></el-icon>
              </div>
              <div class="metric-info">
                <h3>{{ statistics.newUsers }}</h3>
                <p>新增用户</p>
                <span class="trend" :class="statistics.usersTrend">
                  {{ statistics.usersTrend === 'up' ? '↑' : '↓' }} {{ statistics.usersGrowth }}%
                </span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon avg-order">
                <el-icon><DataLine /></el-icon>
              </div>
              <div class="metric-info">
                <h3>¥{{ statistics.avgOrderValue.toFixed(2) }}</h3>
                <p>平均订单价值</p>
                <span class="trend" :class="statistics.avgOrderTrend">
                  {{ statistics.avgOrderTrend === 'up' ? '↑' : '↓' }} {{ statistics.avgOrderGrowth }}%
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>营收趋势</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon><DataLine /></el-icon>
                <p>营收趋势图表</p>
                <p class="chart-desc">显示选定时间范围内的营收变化趋势</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>订单统计</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon><PieChart /></el-icon>
                <p>订单状态分布</p>
                <p class="chart-desc">显示不同状态订单的数量分布</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>热销菜品</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="hot-dishes">
                <div
                  v-for="(dish, index) in statistics.hotDishes"
                  :key="dish.id"
                  class="dish-item"
                >
                  <div class="dish-rank">{{ index + 1 }}</div>
                  <div class="dish-info">
                    <div class="dish-name">{{ dish.name }}</div>
                    <div class="dish-sales">销量: {{ dish.sales }}</div>
                  </div>
                  <div class="dish-revenue">¥{{ dish.revenue.toFixed(2) }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>用户活跃度</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon><UserFilled /></el-icon>
                <p>用户活跃度趋势</p>
                <p class="chart-desc">显示用户活跃度的变化趋势</p>
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
import { Money, ShoppingCart, User, DataLine, PieChart, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 时间范围
const dateRange = ref<[string, string]>(['2024-01-01', '2024-12-31'])

// 统计数据
const statistics = reactive({
  totalRevenue: 125680.50,
  revenueTrend: 'up',
  revenueGrowth: 15.2,

  totalOrders: 3580,
  ordersTrend: 'up',
  ordersGrowth: 8.5,

  newUsers: 156,
  usersTrend: 'up',
  usersGrowth: 12.3,

  avgOrderValue: 35.10,
  avgOrderTrend: 'up',
  avgOrderGrowth: 6.8,

  hotDishes: [
    { id: 1, name: '巨无霸汉堡', sales: 456, revenue: 4560.00 },
    { id: 2, name: '薯条', sales: 389, revenue: 1945.00 },
    { id: 3, name: '可乐', sales: 367, revenue: 1835.00 },
    { id: 4, name: '麦乐鸡', sales: 234, revenue: 1404.00 },
    { id: 5, name: '苹果派', sales: 189, revenue: 945.00 }
  ]
})

// 处理日期变化
const handleDateChange = (value: [string, string]) => {
  console.log('日期范围变化:', value)
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    // TODO: 从API获取实际统计数据
    await new Promise(resolve => setTimeout(resolve, 500))

    ElMessage.success('统计数据已更新')
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载失败，请重试')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.statistics-header {
  margin-bottom: 30px;
}

.statistics-header h1 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 28px;
}

.statistics-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.date-filter {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.core-metrics {
  margin-bottom: 30px;
}

.metric-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.metric-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.metric-icon.revenue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.metric-icon.orders {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.metric-icon.users {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.metric-icon.avg-order {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.metric-info h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: #333;
}

.metric-info p {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 14px;
}

.trend {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.trend.up {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.trend.down {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.charts-section {
  margin-bottom: 30px;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #999;
}

.chart-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
  color: #ddd;
}

.chart-placeholder p {
  margin: 5px 0;
  font-size: 14px;
}

.chart-placeholder .chart-desc {
  font-size: 12px;
  color: #bbb;
}

.hot-dishes {
  padding: 10px 0;
}

.dish-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.dish-item:last-child {
  border-bottom: none;
}

.dish-rank {
  width: 24px;
  height: 24px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  margin-right: 15px;
}

.dish-item:nth-child(1) .dish-rank {
  background: #f56c6c;
}

.dish-item:nth-child(2) .dish-rank {
  background: #e6a23c;
}

.dish-item:nth-child(3) .dish-rank {
  background: #67c23a;
}

.dish-info {
  flex: 1;
}

.dish-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.dish-sales {
  font-size: 12px;
  color: #666;
}

.dish-revenue {
  font-weight: 500;
  color: #e63946;
}
</style>