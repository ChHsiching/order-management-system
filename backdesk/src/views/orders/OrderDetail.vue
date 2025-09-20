<template>
  <div class="order-detail">
    <div class="page-header">
      <el-button @click="$router.go(-1)" :icon="ArrowLeft">返回</el-button>
      <h1>订单详情</h1>
      <p>订单号: {{ orderId }}</p>
    </div>

    <div v-loading="loading" class="order-content">
      <!-- 订单基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
            <el-tag :type="getStatusTagType(order.status)">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <div class="info-item">
              <span class="label">订单号:</span>
              <span class="value">{{ order.id }}</span>
            </div>
            <div class="info-item">
              <span class="label">下单时间:</span>
              <span class="value">{{ order.createdAt }}</span>
            </div>
            <div class="info-item">
              <span class="label">用户名:</span>
              <span class="value">{{ order.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机号:</span>
              <span class="value">{{ order.phone }}</span>
            </div>
          </el-col>
          <el-col :xs="24" :md="12">
            <div class="info-item">
              <span class="label">订单金额:</span>
              <span class="value amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付方式:</span>
              <span class="value">{{ order.paymentMethod || '在线支付' }}</span>
            </div>
            <div class="info-item">
              <span class="label">配送地址:</span>
              <span class="value">{{ order.address || '自取' }}</span>
            </div>
            <div class="info-item">
              <span class="label">备注:</span>
              <span class="value">{{ order.remark || '无' }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 订单状态流转 -->
      <el-card class="timeline-card">
        <template #header>
          <span>订单状态</span>
        </template>

        <el-timeline>
          <el-timeline-item
            v-for="(activity, index) in order.activities"
            :key="index"
            :type="activity.type"
            :color="activity.color"
            :size="activity.size"
            :timestamp="activity.timestamp"
          >
            {{ activity.content }}
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 订单商品 -->
      <el-card class="items-card">
        <template #header>
          <span>订单商品</span>
        </template>

        <el-table :data="order.items" stripe>
          <el-table-column prop="name" label="商品名称" min-width="150" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">
              ¥{{ row.price.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              ¥{{ (row.price * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>

        <div class="order-summary">
          <div class="summary-item">
            <span>商品总额:</span>
            <span>¥{{ order.subtotal?.toFixed(2) }}</span>
          </div>
          <div class="summary-item">
            <span>配送费:</span>
            <span>¥{{ order.deliveryFee?.toFixed(2) || '0.00' }}</span>
          </div>
          <div class="summary-item total">
            <span>实付金额:</span>
            <span>¥{{ order.totalAmount?.toFixed(2) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="order-actions">
        <el-button
          v-if="order.status === 'pending'"
          type="success"
          @click="acceptOrder"
        >
          接受订单
        </el-button>
        <el-button
          v-if="order.status === 'pending'"
          type="danger"
          @click="rejectOrder"
        >
          拒绝订单
        </el-button>
        <el-button
          v-if="order.status === 'accepted'"
          type="primary"
          @click="startPreparing"
        >
          开始制作
        </el-button>
        <el-button
          v-if="order.status === 'preparing'"
          type="success"
          @click="completeOrder"
        >
          完成订单
        </el-button>
        <el-button
          v-if="order.status === 'completed'"
          type="primary"
          @click="printOrder"
        >
          打印订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const orderId = route.params.id as string

// 加载状态
const loading = ref(false)

// 订单信息
const order = reactive({
  id: orderId,
  username: '张三',
  phone: '13800138000',
  address: '北京市朝阳区某某街道123号',
  totalAmount: 68.00,
  subtotal: 65.00,
  deliveryFee: 3.00,
  paymentMethod: '在线支付',
  status: 'pending',
  remark: '不要辣，多加香菜',
  createdAt: '2024-01-01 12:00:00',
  activities: [
    {
      content: '订单已提交',
      timestamp: '2024-01-01 12:00:00',
      type: 'primary',
      color: '#409eff',
      size: 'large'
    },
    {
      content: '等待商家接单',
      timestamp: '2024-01-01 12:00:05',
      type: 'warning',
      color: '#e6a23c',
      size: 'normal'
    }
  ],
  items: [
    {
      name: '巨无霸汉堡',
      price: 25.00,
      quantity: 2
    },
    {
      name: '薯条',
      price: 10.00,
      quantity: 1
    },
    {
      name: '可乐',
      price: 5.00,
      quantity: 1
    }
  ]
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

// 添加活动记录
const addActivity = (content: string, type: 'primary' | 'success' | 'warning' | 'danger' | 'info') => {
  const typeColors = {
    primary: '#409eff',
    success: '#67c23a',
    warning: '#e6a23c',
    danger: '#f56c6c',
    info: '#909399'
  }

  order.activities.unshift({
    content,
    timestamp: new Date().toISOString().slice(0, 19).replace('T', ' '),
    type,
    color: typeColors[type],
    size: 'normal'
  })
}

// 接受订单
const acceptOrder = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要接受这个订单吗？',
      '确认接单',
      {
        confirmButtonText: '接受',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true

    // TODO: 调用API接受订单
    await new Promise(resolve => setTimeout(resolve, 1000))

    order.status = 'accepted'
    addActivity('商家已接单', 'success')

    ElMessage.success('订单已接受')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接受订单失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 拒绝订单
const rejectOrder = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '拒绝订单',
      {
        confirmButtonText: '拒绝',
        cancelButtonText: '取消',
        type: 'warning',
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (value: string) => {
          if (!value.trim()) {
            return '请输入拒绝原因'
          }
          return true
        }
      }
    )

    loading.value = true

    // TODO: 调用API拒绝订单
    await new Promise(resolve => setTimeout(resolve, 1000))

    order.status = 'cancelled'
    addActivity(`订单已拒绝: ${reason}`, 'danger')

    ElMessage.success('订单已拒绝')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝订单失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 开始制作
const startPreparing = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要开始制作这个订单吗？',
      '开始制作',
      {
        confirmButtonText: '开始制作',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    loading.value = true

    // TODO: 调用API更新订单状态
    await new Promise(resolve => setTimeout(resolve, 1000))

    order.status = 'preparing'
    addActivity('开始制作', 'primary')

    ElMessage.success('已开始制作')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始制作失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 完成订单
const completeOrder = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要完成这个订单吗？',
      '完成订单',
      {
        confirmButtonText: '完成',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    loading.value = true

    // TODO: 调用API完成订单
    await new Promise(resolve => setTimeout(resolve, 1000))

    order.status = 'completed'
    addActivity('订单已完成', 'success')

    ElMessage.success('订单已完成')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成订单失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 打印订单
const printOrder = () => {
  ElMessage.success('正在打印订单...')
  // TODO: 实现打印功能
}

// 获取订单详情
const getOrderDetail = async () => {
  loading.value = true
  try {
    // TODO: 从API获取实际订单详情
    await new Promise(resolve => setTimeout(resolve, 1000))
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  getOrderDetail()
})
</script>

<style scoped>
.order-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.info-card,
.timeline-card,
.items-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  min-width: 80px;
  color: #666;
  font-size: 14px;
}

.info-item .value {
  color: #333;
  font-size: 14px;
}

.info-item .amount {
  color: #e63946;
  font-weight: 500;
}

.order-summary {
  margin-top: 20px;
  text-align: right;
}

.summary-item {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.summary-item span:first-child {
  color: #666;
  margin-right: 10px;
}

.summary-item span:last-child {
  color: #333;
  font-weight: 500;
}

.summary-item.total {
  font-size: 16px;
  font-weight: bold;
  color: #e63946;
}

.order-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
</style>