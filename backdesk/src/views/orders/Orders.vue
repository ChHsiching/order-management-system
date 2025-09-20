<template>
  <div class="orders-page">
    <div class="page-header">
      <h1>订单管理</h1>
      <p>查看和处理客户订单</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true">
        <el-form-item label="订单状态">
          <el-select v-model="filterStatus" placeholder="选择状态" clearable>
            <el-option label="待接单" value="pending" />
            <el-option label="已接单" value="accepted" />
            <el-option label="制作中" value="preparing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="searchKeyword"
            placeholder="订单号/用户名/手机号"
            clearable
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单统计 -->
    <div class="order-stats">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6">
          <div class="stat-item pending">
            <div class="stat-number">{{ orderStats.pending }}</div>
            <div class="stat-label">待接单</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item accepted">
            <div class="stat-number">{{ orderStats.accepted }}</div>
            <div class="stat-label">已接单</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item preparing">
            <div class="stat-number">{{ orderStats.preparing }}</div>
            <div class="stat-label">制作中</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item completed">
            <div class="stat-number">{{ orderStats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 订单列表 -->
    <div class="orders-table">
      <el-table :data="filteredOrders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单号" width="100" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="$router.push(`/admin/orders/${row.id}`)"
            >
              详情
            </el-button>
            <el-button
              size="small"
              :type="getActionType(row.status)"
              @click="handleOrderAction(row)"
              :disabled="!canHandleAction(row.status)"
            >
              {{ getActionText(row.status) }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 筛选条件
const filterStatus = ref('')
const dateRange = ref<[string, string] | null>(null)
const searchKeyword = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 订单统计
const orderStats = reactive({
  pending: 12,
  accepted: 8,
  preparing: 15,
  completed: 156
})

// 订单列表
const orders = ref([
  {
    id: '2024010101',
    username: '张三',
    phone: '13800138000',
    totalAmount: 68.00,
    status: 'pending',
    createdAt: '2024-01-01 12:00:00'
  },
  {
    id: '2024010102',
    username: '李四',
    phone: '13800138001',
    totalAmount: 35.50,
    status: 'accepted',
    createdAt: '2024-01-01 12:05:00'
  },
  {
    id: '2024010103',
    username: '王五',
    phone: '13800138002',
    totalAmount: 128.00,
    status: 'preparing',
    createdAt: '2024-01-01 12:10:00'
  },
  {
    id: '2024010104',
    username: '赵六',
    phone: '13800138003',
    totalAmount: 45.00,
    status: 'completed',
    createdAt: '2024-01-01 11:30:00'
  }
])

// 过滤后的订单列表
const filteredOrders = computed(() => {
  let result = orders.value

  // 状态筛选
  if (filterStatus.value) {
    result = result.filter(order => order.status === filterStatus.value)
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(order =>
      order.id.toLowerCase().includes(keyword) ||
      order.username.toLowerCase().includes(keyword) ||
      order.phone.includes(keyword)
    )
  }

  return result
})

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap = {
    pending: '待接单',
    accepted: '已接单',
    preparing: '制作中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status as keyof typeof statusMap] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap = {
    pending: 'warning',
    accepted: 'primary',
    preparing: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status as keyof typeof typeMap] || 'info'
}

// 获取操作类型
const getActionType = (status: string) => {
  const actionMap = {
    pending: 'success',
    accepted: 'warning',
    preparing: 'danger',
    completed: 'info',
    cancelled: 'info'
  }
  return actionMap[status as keyof typeof actionMap] || 'info'
}

// 获取操作文本
const getActionText = (status: string) => {
  const actionMap = {
    pending: '接单',
    accepted: '开始制作',
    preparing: '完成',
    completed: '已完成',
    cancelled: '已取消'
  }
  return actionMap[status as keyof typeof actionMap] || '查看'
}

// 是否可以处理操作
const canHandleAction = (status: string) => {
  return ['pending', 'accepted', 'preparing'].includes(status)
}

// 处理订单操作
const handleOrderAction = async (order: any) => {
  try {
    const actionText = getActionText(order.status)

    await ElMessageBox.confirm(
      `确定要${actionText}订单 ${order.id} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 更新订单状态
    const statusFlow = {
      pending: 'accepted',
      accepted: 'preparing',
      preparing: 'completed'
    }

    const newStatus = statusFlow[order.status as keyof typeof statusFlow]
    if (newStatus) {
      order.status = newStatus

      // 更新统计数据
      orderStats[order.status as keyof typeof orderStats]--
      orderStats[newStatus as keyof typeof orderStats]++

      ElMessage.success(`订单 ${order.id} 已${actionText}`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('处理订单失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 重置筛选条件
const resetFilter = () => {
  filterStatus.value = ''
  dateRange.value = null
  searchKeyword.value = ''
  currentPage.value = 1
  loadOrders()
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadOrders()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadOrders()
}

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    // TODO: 从API获取实际订单列表
    await new Promise(resolve => setTimeout(resolve, 500))

    // 模拟总数
    total.value = filteredOrders.value.length
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 28px;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.order-stats {
  margin-bottom: 20px;
}

.stat-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stat-item.pending {
  border-left: 4px solid #e6a23c;
}

.stat-item.accepted {
  border-left: 4px solid #409eff;
}

.stat-item.preparing {
  border-left: 4px solid #909399;
}

.stat-item.completed {
  border-left: 4px solid #67c23a;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.orders-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.amount {
  color: #e63946;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>