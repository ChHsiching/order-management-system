<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="statistics-row">
      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="card-content">
            <div class="card-icon sales">
              <el-icon><Money /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">今日销售额</div>
              <div class="card-value">¥{{ statistics.todaySales }}</div>
              <div class="card-compare">
                较昨日
                <span :class="statistics.salesGrowth >= 0 ? 'up' : 'down'">
                  {{ Math.abs(statistics.salesGrowth) }}%
                  <el-icon><CaretTop v-if="statistics.salesGrowth >= 0" /><CaretBottom v-else /></el-icon>
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="card-content">
            <div class="card-icon orders">
              <el-icon><Document /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">今日订单</div>
              <div class="card-value">{{ statistics.todayOrders }}</div>
              <div class="card-compare">
                较昨日
                <span :class="statistics.ordersGrowth >= 0 ? 'up' : 'down'">
                  {{ Math.abs(statistics.ordersGrowth) }}%
                  <el-icon><CaretTop v-if="statistics.ordersGrowth >= 0" /><CaretBottom v-else /></el-icon>
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="card-content">
            <div class="card-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">注册用户</div>
              <div class="card-value">{{ statistics.totalUsers }}</div>
              <div class="card-compare">
                较昨日
                <span :class="statistics.usersGrowth >= 0 ? 'up' : 'down'">
                  {{ Math.abs(statistics.usersGrowth) }}%
                  <el-icon><CaretTop v-if="statistics.usersGrowth >= 0" /><CaretBottom v-else /></el-icon>
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="statistics-card">
          <div class="card-content">
            <div class="card-icon menu">
              <el-icon><Menu /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">菜品数量</div>
              <div class="card-value">{{ statistics.totalMenus }}</div>
              <div class="card-compare">
                较昨日
                <span :class="statistics.menusGrowth >= 0 ? 'up' : 'down'">
                  {{ Math.abs(statistics.menusGrowth) }}%
                  <el-icon><CaretTop v-if="statistics.menusGrowth >= 0" /><CaretBottom v-else /></el-icon>
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
              <el-radio-group v-model="salesRange" size="small">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里可以集成 ECharts 等图表库 -->
            <div class="chart-placeholder">
              <el-icon><TrendCharts /></el-icon>
              <p>销售趋势图表</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门菜品</span>
            </div>
          </template>
          <div class="chart-container">
            <!-- 这里可以集成 ECharts 等图表库 -->
            <div class="chart-placeholder">
              <el-icon><PieChart /></el-icon>
              <p>热门菜品统计</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新订单 -->
    <el-row :gutter="20" class="orders-row">
      <el-col :span="24">
        <el-card class="orders-card">
          <template #header>
            <div class="card-header">
              <span>最新订单</span>
              <el-button type="primary" link @click="$router.push('/order/management')">
                查看更多
              </el-button>
            </div>
          </template>

          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderId" label="订单号" width="180" />
            <el-table-column prop="username" label="用户" width="120" />
            <el-table-column prop="totalPrice" label="金额" width="100">
              <template #default="scope">
                ¥{{ scope.row.totalPrice }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" link size="small" @click="viewOrder(scope.row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Money,
  Document,
  User,
  Menu,
  CaretTop,
  CaretBottom,
  TrendCharts,
  PieChart
} from '@element-plus/icons-vue'
import { statisticsApi } from '../../utils/api'

const router = useRouter()

// 统计数据
const statistics = reactive({
  todaySales: 0,
  salesGrowth: 0,
  todayOrders: 0,
  ordersGrowth: 0,
  totalUsers: 0,
  usersGrowth: 0,
  totalMenus: 0,
  menusGrowth: 0
})

// 销售趋势时间范围
const salesRange = ref('week')

// 最新订单数据
const recentOrders = ref([])

// 获取订单状态类型
const getStatusType = (status: number) => {
  const types = ['', 'warning', 'success', 'info']
  return types[status] || 'info'
}

// 获取订单状态文本
const getStatusText = (status: number) => {
  const texts = ['', '待处理', '已受理', '已完成']
  return texts[status] || '未知'
}

// 查看订单详情
const viewOrder = (order: any) => {
  router.push(`/order/management?id=${order.orderId}`)
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await statisticsApi.getDashboard()

    // 开发环境调试信息
    if (process.env.NODE_ENV === 'development') {
      console.log('📊 控制台统计响应格式:', typeof response)
    }

    // 拦截器处理后可能返回直接数据或标准格式
    if (response && typeof response === 'object' && !response.code) {
      // 拦截器返回的直接数据对象
      Object.assign(statistics, {
        todaySales: response.todaySales || 0,
        salesGrowth: response.salesGrowth || 0,
        todayOrders: response.todayOrders || 0,
        ordersGrowth: response.ordersGrowth || 0,
        totalUsers: response.totalUsers || 0,
        usersGrowth: response.usersGrowth || 0,
        totalMenus: response.totalMenus || 0,
        menusGrowth: response.menusGrowth || 0
      })
    } else if (response.code === 0 || response.code === 200) {
      // 标准格式响应
      const data = response.data
      Object.assign(statistics, {
        todaySales: data.todaySales || 0,
        salesGrowth: data.salesGrowth || 0,
        todayOrders: data.todayOrders || 0,
        ordersGrowth: data.ordersGrowth || 0,
        totalUsers: data.totalUsers || 0,
        usersGrowth: data.usersGrowth || 0,
        totalMenus: data.totalMenus || 0,
        menusGrowth: data.menusGrowth || 0
      })
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败，请检查网络连接')
  }
}

// 加载最新订单
const loadRecentOrders = async () => {
  try {
    const response = await statisticsApi.getRecentOrders(5)

    // 开发环境调试信息
    if (process.env.NODE_ENV === 'development') {
      console.log('📊 控制台订单响应格式:', Array.isArray(response) ? '数组' : '对象')
    }

    // 拦截器处理后可能返回直接数组或标准格式
    if (Array.isArray(response)) {
      // 拦截器返回的直接数组
      recentOrders.value = response
    } else if (response && typeof response === 'object' && !response.code) {
      // 拦截器返回的直接数据对象（可能包含data属性）
      recentOrders.value = response.data || response || []
    } else if (response.code === 0 || response.code === 200) {
      // 标准格式响应
      recentOrders.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取最新订单失败')
    }
  } catch (error) {
    console.error('获取最新订单失败:', error)
    ElMessage.error('获取最新订单失败，请检查网络连接')
  }
}

// 页面加载时的初始化
onMounted(async () => {
  await Promise.all([
    loadStatistics(),
    loadRecentOrders()
  ])
  console.log('Dashboard loaded with real data')
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.statistics-row {
  margin-bottom: 20px;
}

.statistics-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.statistics-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.card-icon .el-icon {
  font-size: 30px;
  color: #fff;
}

.card-icon.sales {
  background-color: #667eea;
}

.card-icon.orders {
  background-color: #f093fb;
}

.card-icon.users {
  background-color: #4facfe;
}

.card-icon.menu {
  background-color: #43e97b;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.card-compare {
  font-size: 12px;
  color: #909399;
}

.card-compare .up {
  color: #67c23a;
}

.card-compare .down {
  color: #f56c6c;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card,
.orders-card {
  border-radius: 8px;
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
  color: #909399;
}

.chart-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.chart-placeholder p {
  margin: 0;
  font-size: 14px;
}
</style>