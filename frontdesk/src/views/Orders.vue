<template>
  <div class="orders-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>我的订单</h1>
      <p>查看和管理您的订单记录</p>
    </div>

    <!-- 订单筛选 -->
    <el-card class="filter-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="4">
          <span class="filter-label">订单状态：</span>
        </el-col>
        <el-col :span="20">
          <el-radio-group v-model="selectedStatus" @change="handleStatusChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待处理</el-radio-button>
            <el-radio-button label="PROCESSING">制作中</el-radio-button>
            <el-radio-button label="COMPLETED">已完成</el-radio-button>
            <el-radio-button label="CANCELLED">已取消</el-radio-button>
          </el-radio-group>
        </el-col>
      </el-row>
    </el-card>

    <!-- 订单列表 -->
    <div v-loading="loading" class="orders-container">
      <div v-if="orders.length === 0" class="empty-state">
        <el-empty description="暂无订单记录">
          <el-button type="primary" @click="$router.push('/')">
            去点餐
          </el-button>
        </el-empty>
      </div>

      <div v-else class="orders-list">
        <el-card
          v-for="order in orders"
          :key="order.id"
          class="order-card"
          :class="getStatusClass(order.status)"
        >
          <template #header>
            <div class="order-header">
              <div class="order-info">
                <span class="order-number">订单号：{{ order.orderNumber || order.id }}</span>
                <span class="order-time">{{ formatDate(order.createTime) }}</span>
              </div>
              <div class="order-status">
                <el-tag :type="getStatusType(order.status)">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>
            </div>
          </template>

          <!-- 订单商品列表 -->
          <div class="order-items">
            <div
              v-for="item in order.items || order.orderItems"
              :key="item.id"
              class="order-item"
            >
              <div class="item-info">
                <img
                  :src="item.imgPath || item.image || '/placeholder-food.jpg'"
                  :alt="item.productName || item.name"
                  class="item-image"
                />
                <div class="item-details">
                  <h4>{{ item.productName || item.name }}</h4>
                  <p class="item-price">¥{{ item.price }}</p>
                  <p class="item-quantity">数量：{{ item.quantity }}</p>
                </div>
              </div>
              <div class="item-subtotal">
                ¥{{ (item.price * item.quantity).toFixed(2) }}
              </div>
            </div>
          </div>

          <!-- 订单总计 -->
          <div class="order-summary">
            <div class="summary-row">
              <span>商品总计：</span>
              <span>¥{{ order.totalAmount || order.totalPrice }}</span>
            </div>
            <div class="summary-row">
              <span>配送费：</span>
              <span>¥{{ order.deliveryFee || 0 }}</span>
            </div>
            <div class="summary-row total">
              <span>实付金额：</span>
              <span class="total-amount">¥{{ order.finalAmount || order.finalPrice || order.totalAmount || order.totalPrice }}</span>
            </div>
          </div>

          <!-- 订单操作 -->
          <div class="order-actions">
            <el-button
              size="small"
              @click="viewOrderDetail(order)"
            >
              查看详情
            </el-button>

            <el-button
              v-if="order.status === 'PENDING'"
              type="danger"
              size="small"
              @click="cancelOrder(order)"
              :loading="cancellingOrderId === order.id"
            >
              取消订单
            </el-button>

            <el-button
              v-if="order.status === 'COMPLETED'"
              type="primary"
              size="small"
              @click="reorder(order)"
            >
              再来一单
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div v-if="orders.length > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="60%"
      class="order-detail-dialog"
    >
      <div v-if="selectedOrder" v-loading="detailLoading">
        <div class="detail-section">
          <h3>订单信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">
              {{ selectedOrder.orderNumber || selectedOrder.id }}
            </el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(selectedOrder.status)">
                {{ getStatusText(selectedOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">
              {{ formatDate(selectedOrder.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付方式">
              {{ selectedOrder.paymentMethod || '在线支付' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ selectedOrder.phone || '未提供' }}
            </el-descriptions-item>
            <el-descriptions-item label="配送地址">
              {{ selectedOrder.address || '到店就餐' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h3>商品清单</h3>
          <el-table :data="selectedOrder.items || selectedOrder.orderItems" stripe>
            <el-table-column label="商品" min-width="200">
              <template #default="{ row }">
                <div class="table-item-info">
                  <img
                    :src="row.imgPath || row.image || '/placeholder-food.jpg'"
                    :alt="row.productName || row.name"
                    class="table-item-image"
                  />
                  <div>
                    <div class="item-name">{{ row.productName || row.name }}</div>
                    <div class="item-price">¥{{ row.price }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="80" align="center">
              <template #default="{ row }">
                {{ row.quantity }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100" align="right">
              <template #default="{ row }">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section">
          <h3>费用明细</h3>
          <div class="cost-breakdown">
            <div class="cost-row">
              <span>商品总计：</span>
              <span>¥{{ selectedOrder.totalAmount || selectedOrder.totalPrice }}</span>
            </div>
            <div class="cost-row">
              <span>配送费：</span>
              <span>¥{{ selectedOrder.deliveryFee || 0 }}</span>
            </div>
            <div class="cost-row">
              <span>优惠：</span>
              <span class="discount">-¥{{ selectedOrder.discount || 0 }}</span>
            </div>
            <div class="cost-row total">
              <span>实付金额：</span>
              <span class="total-amount">¥{{ selectedOrder.finalAmount || selectedOrder.finalPrice || selectedOrder.totalAmount || selectedOrder.totalPrice }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="selectedOrder?.status === 'COMPLETED'"
            type="primary"
            @click="reorder(selectedOrder)"
          >
            再来一单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { orderApi } from '@/utils/api'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

// 状态管理
const loading = ref(false)
const detailLoading = ref(false)
const cancellingOrderId = ref<string | null>(null)
const detailDialogVisible = ref(false)

// 数据
const orders = ref<any[]>([])
const selectedOrder = ref<any>(null)

// 筛选条件
const selectedStatus = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取订单列表
const fetchOrders = async () => {
  console.log('[Orders] ===== 开始获取订单列表 =====')
  console.log('[Orders] 当前认证状态:', {
    isLoggedIn: authStore.isLoggedIn,
    username: authStore.username,
    hasToken: !!authStore.token,
    token: authStore.token ? authStore.token.substring(0, 20) + '...' : 'null'
  })

  if (!authStore.isLoggedIn) {
    console.log('[Orders] 用户未登录，跳转到登录页')
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }

    if (selectedStatus.value) {
      params.status = selectedStatus.value
    }

    console.log('[Orders] 准备发送请求，参数:', params)
    console.log('[Orders] API函数:', orderApi.getUserOrders)
    console.log('[Orders] 请求URL配置: http://localhost:8080/WebOrderSystem/api/order/user')

    // 添加请求拦截器日志
    const requestInterceptor = axios.interceptors.request.use(config => {
      console.log('[Orders] 请求拦截器 - 请求配置:', {
        url: config.url,
        method: config.method,
        headers: config.headers,
        params: config.params,
        data: config.data
      })
      return config
    })

    // 添加响应拦截器日志
    const responseInterceptor = axios.interceptors.response.use(response => {
      console.log('[Orders] 响应拦截器 - 响应信息:', {
        status: response.status,
        statusText: response.statusText,
        headers: response.headers,
        data: response.data
      })
      return response
    })

    const response: any = await orderApi.getUserOrders(params)

    // 清理拦截器
    axios.interceptors.request.eject(requestInterceptor)
    axios.interceptors.response.eject(responseInterceptor)

    console.log('[Orders] ===== API调用完成 =====')
    console.log('[Orders] 完整响应对象:', response)
    console.log('[Orders] 响应类型:', typeof response)
    console.log('[Orders] 响应状态码:', response?.status)
    console.log('[Orders] 响应状态文本:', response?.statusText)
    console.log('[Orders] 响应头部:', response?.headers)
    console.log('[Orders] 响应数据:', response?.data)

    // 检查响应结构
    if (response && typeof response === 'object') {
      console.log('[Orders] 响应对象键:', Object.keys(response))
      console.log('[Orders] 是否有data属性:', 'data' in response)
      console.log('[Orders] data属性类型:', typeof response.data)

      if (response.data) {
        console.log('[Orders] data对象键:', Object.keys(response.data))
        console.log('[Orders] data.code:', response.data.code)
        console.log('[Orders] data.success:', response.data.success)
        console.log('[Orders] data.message:', response.data.message)
        console.log('[Orders] data.data:', response.data.data)
      }
    }

    // 处理不同的响应格式 - 现在response是完整response对象
    const data = response.data || response
    console.log('[Orders] 处理后的数据:', data)

    if (response?.status === 200) {
      console.log('[Orders] HTTP状态码200，检查业务状态...')

      if (data.code === 200 || data.success === true || Array.isArray(data)) {
        console.log('[Orders] 业务状态成功，提取订单数据...')
        const orderData = data.data || data
        console.log('[Orders] 订单数据:', orderData)

        if (orderData) {
          console.log('[Orders] 订单数据对象键:', Object.keys(orderData))
          console.log('[Orders] records:', orderData.records)
          console.log('[Orders] content:', orderData.content)
          console.log('[Orders] total:', orderData.total)
          console.log('[Orders] totalElements:', orderData.totalElements)
        }

        orders.value = orderData?.records || orderData?.content || orderData || []
        total.value = orderData?.total || orderData?.totalElements || orders.value.length

        console.log('[Orders] ===== 订单列表设置完成 =====')
        console.log('[Orders] 订单数量:', orders.value.length)
        console.log('[Orders] 总数:', total.value)
        console.log('[Orders] 第一个订单:', orders.value[0])

        if (orders.value.length > 0) {
          console.log('[Orders] 订单数据样本:', orders.value.slice(0, 2))
        }
      } else {
        console.error('[Orders] 业务状态失败:', data)
        console.error('[Orders] 失败详情:', {
          code: data.code,
          success: data.success,
          message: data.message,
          expected: 'code === 200 || success === true || Array.isArray(data)'
        })
        ElMessage.error(data.message || response.message || '获取订单列表失败：业务状态错误')
        orders.value = []
      }
    } else {
      console.error('[Orders] HTTP请求失败:', response?.status)
      console.error('[Orders] 完整错误响应:', response)
      ElMessage.error(`获取订单列表失败：HTTP ${response?.status || '未知错误'}`)
      orders.value = []
    }
  } catch (error: any) {
    console.error('[Orders] ===== 获取订单列表异常 =====')
    console.error('[Orders] 错误对象:', error)
    console.error('[Orders] 错误类型:', typeof error)
    console.error('[Orders] 错误名称:', error.name)
    console.error('[Orders] 错误消息:', error.message)
    console.error('[Orders] 错误堆栈:', error.stack)

    if (error.response) {
      console.error('[Orders] HTTP错误响应:', {
        status: error.response.status,
        statusText: error.response.statusText,
        headers: error.response.headers,
        data: error.response.data
      })
    } else if (error.request) {
      console.error('[Orders] 请求发送失败:', error.request)
    } else {
      console.error('[Orders] 请求配置错误:', error.config)
    }

    if (error.response?.status === 401) {
      console.log('[Orders] 登录已过期，跳转到登录页')
      ElMessage.error('登录已过期，请重新登录')
      authStore.logout()
      router.push('/login')
    } else if (error.response?.status === 403) {
      console.error('[Orders] 权限不足错误')
      ElMessage.error('权限不足，无法访问订单信息')
    } else if (error.response?.status === 404) {
      console.error('[Orders] API端点不存在')
      ElMessage.error('订单服务暂不可用')
    } else if (error.response?.status >= 500) {
      console.error('[Orders] 服务器内部错误')
      ElMessage.error('服务器繁忙，请稍后重试')
    } else if (error.code === 'ECONNABORTED') {
      console.error('[Orders] 请求超时')
      ElMessage.error('请求超时，请检查网络连接')
    } else if (error.code === 'ERR_NETWORK') {
      console.error('[Orders] 网络连接错误')
      ElMessage.error('网络连接失败，请检查后端服务是否启动')
    } else {
      console.error('[Orders] 其他未知错误:', error)
      ElMessage.error('获取订单列表失败: ' + (error.message || '网络错误'))
    }
    orders.value = []
  } finally {
    loading.value = false
    console.log('[Orders] ===== 获取订单列表结束 =====')
  }
}

// 状态筛选处理
const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchOrders()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchOrders()
}

// 查看订单详情
const viewOrderDetail = async (order: any) => {
  selectedOrder.value = order
  detailDialogVisible.value = true

  // 如果需要获取更详细的订单信息，可以在这里调用API
  // try {
  //   detailLoading.value = true
  //   const response: any = await orderApi.getOrderDetail(order.id)
  //   if (response.code === 200) {
  //     selectedOrder.value = response.data
  //   }
  // } catch (error) {
  //   console.error('获取订单详情失败:', error)
  // } finally {
  //   detailLoading.value = false
  // }
}

// 取消订单
const cancelOrder = async (order: any) => {
  console.log('[Orders] ===== 开始取消订单 =====')
  console.log('[Orders] 订单信息:', order)

  try {
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    console.log('[Orders] 用户确认取消订单')
    cancellingOrderId.value = order.id

    console.log('[Orders] 发送取消订单请求，订单ID:', order.id)
    const response: any = await orderApi.cancelOrder(order.id)
    console.log('[Orders] 取消订单响应:', response)
    console.log('[Orders] 响应状态:', response?.status)
    console.log('[Orders] 响应数据:', response?.data)

    // 处理不同的响应格式 - 现在response是完整response对象
    const data = response.data || response
    console.log('[Orders] 处理后的数据:', data)

    if (data.code === 200 || data.success === true) {
      console.log('[Orders] 取消订单成功')
      ElMessage.success('订单已取消')
      // 重新获取订单列表
      fetchOrders()
    } else {
      console.error('[Orders] 取消订单失败:', data)
      ElMessage.error(data.message || response.message || '取消订单失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[Orders] 取消订单异常:', error)
      console.error('[Orders] 错误详情:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      })
      ElMessage.error('取消订单失败: ' + error.message)
    } else {
      console.log('[Orders] 用户取消操作')
    }
  } finally {
    cancellingOrderId.value = null
    console.log('[Orders] ===== 取消订单结束 =====')
  }
}

// 再来一单
const reorder = async (order: any) => {
  console.log('[Orders] ===== 开始再来一单 =====')
  console.log('[Orders] 订单信息:', order)

  if (!authStore.isLoggedIn) {
    console.log('[Orders] 用户未登录')
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    console.log('[Orders] 清空当前购物车')
    await cartStore.clearCart()

    // 将订单商品添加到购物车
    const items = order.items || order.orderItems
    console.log('[Orders] 订单商品:', items)

    for (const item of items) {
      console.log('[Orders] 添加商品到购物车:', item)
      await cartStore.addToCart(
        item.productId || item.id,
        item.quantity,
        {
          name: item.productName || item.name,
          price: item.price
        }
      )
    }

    console.log('[Orders] 所有商品已添加到购物车')
    ElMessage.success('已添加到购物车')
    // 跳转到购物车页面
    router.push('/cart')
  } catch (error: any) {
    console.error('[Orders] 再来一单失败:', error)
    console.error('[Orders] 错误详情:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    })
    ElMessage.error('操作失败: ' + error.message)
  }
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'PROCESSING': '制作中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 获取状态样式类
const getStatusClass = (status: number | string) => {
  const statusStr = typeof status === 'number' ? status.toString() : status
  return `status-${statusStr.toLowerCase()}`
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'

  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

// 页面加载时获取订单列表
onMounted(() => {
  console.log('[Orders] ===== Orders页面挂载 =====')
  console.log('[Orders] 组件挂载，准备获取订单列表')
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
  padding: 20px 0;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
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

.filter-card {
  margin-bottom: 20px;
}

.filter-label {
  font-weight: bold;
  color: #333;
}

.orders-container {
  min-height: 400px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-number {
  font-weight: bold;
  color: #333;
}

.order-time {
  font-size: 14px;
  color: #999;
}

.order-items {
  margin: 20px 0;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-image {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  object-fit: cover;
}

.item-details h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #333;
}

.item-price {
  color: #e63946;
  font-weight: bold;
  margin: 0;
}

.item-quantity {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.item-subtotal {
  font-weight: bold;
  color: #333;
}

.order-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #666;
}

.summary-row.total {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.total-amount {
  color: #e63946;
  font-size: 18px;
}

.order-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 30px;
  text-align: center;
}

/* 订单详情对话框 */
.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 18px;
}

.table-item-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-item-image {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.item-name {
  font-weight: bold;
  color: #333;
}

.cost-breakdown {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
}

.cost-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #666;
}

.cost-row.total {
  font-weight: bold;
  color: #333;
  font-size: 16px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #dee2e6;
}

.discount {
  color: #28a745;
}

.total-amount {
  color: #e63946;
  font-size: 18px;
}

/* 状态样式 */
.status-pending {
  border-left: 4px solid #e6a23c;
}

.status-processing {
  border-left: 4px solid #409eff;
}

.status-completed {
  border-left: 4px solid #67c23a;
}

.status-cancelled {
  border-left: 4px solid #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .orders-page {
    padding: 10px 0;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .page-header p {
    font-size: 14px;
  }

  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .order-actions {
    flex-direction: column;
  }

  .order-detail-dialog {
    width: 90% !important;
  }
}
</style>