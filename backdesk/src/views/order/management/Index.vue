<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="订单号">
            <el-input
              v-model="searchForm.orderId"
              placeholder="请输入订单号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="待处理" :value="0" />
              <el-option label="已受理" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="下单时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="orderList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderid" label="订单号" width="180" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="address" label="送货地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="totalprice" label="总金额" width="100">
          <template #default="scope">
            ¥{{ scope.row.totalprice }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createtime" label="下单时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              link
              size="small"
              @click="handleAccept(scope.row)"
            >
              <el-icon><Check /></el-icon>
              受理
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="success"
              link
              size="small"
              @click="handleComplete(scope.row)"
            >
              <el-icon><Check /></el-icon>
              完成
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              link
              size="small"
              @click="handleCancel(scope.row)"
            >
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看订单对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="订单详情"
      width="700px"
    >
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderid }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentOrder.username }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="送货地址">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ currentOrder.totalprice }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createtime }}</el-descriptions-item>
        </el-descriptions>

        <div class="order-items">
          <h4>订单明细</h4>
          <el-table :data="currentOrder.items" border>
            <el-table-column prop="productname" label="菜品名称" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="productnum" label="数量" width="80" />
            <el-table-column label="小计" width="100">
              <template #default="scope">
                ¥{{ (scope.row.price * scope.row.productnum).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Check, Close } from '@element-plus/icons-vue'
import { orderApi } from '@/utils/api'

// 数据状态
const loading = ref(false)
const orderList = ref([])
const viewDialogVisible = ref(false)
const currentOrder = ref<any>(null)

// 搜索表单
const searchForm = reactive({
  orderId: '',
  username: '',
  status: undefined,
  dateRange: []
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载订单列表
const loadOrderList = async () => {
  loading.value = true
  try {
    const params = {
      orderId: searchForm.orderId || undefined,
      username: searchForm.username || undefined,
      status: searchForm.status,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }

    const response = await orderApi.getList(params)

    // 现在拦截器直接返回数组数据
    let orders = Array.isArray(response) ? response : (response.data || [])

      // 如果有搜索条件，在前端进行过滤
      if (searchForm.orderId) {
        orders = orders.filter(order => order.orderid.includes(searchForm.orderId))
      }
      if (searchForm.username) {
        orders = orders.filter(order => order.username.includes(searchForm.username))
      }
      if (searchForm.status !== undefined) {
        orders = orders.filter(order => order.status === searchForm.status)
      }
      if (searchForm.dateRange?.[0] && searchForm.dateRange?.[1]) {
        const startDate = new Date(searchForm.dateRange[0])
        const endDate = new Date(searchForm.dateRange[1])
        orders = orders.filter(order => {
          const orderDate = new Date(order.createtime)
          return orderDate >= startDate && orderDate <= endDate
        })
      }

      // 分页处理
      const startIndex = (pagination.page - 1) * pagination.size
      const endIndex = startIndex + pagination.size
      orderList.value = orders.slice(startIndex, endIndex)
      pagination.total = orders.length

      // 为每个订单加载订单项
      for (const order of orderList.value) {
        try {
          const itemsResponse = await orderApi.getOrderItems(order.orderid)
          // 拦截器直接返回数组数据
          order.items = Array.isArray(itemsResponse) ? itemsResponse : (itemsResponse.data || [])
        } catch (error) {
          console.error('加载订单项失败:', error)
          order.items = []
        }
      }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取订单状态类型
const getStatusType = (status: number) => {
  const types = ['warning', 'success', 'success', 'info']
  return types[status] || 'info'
}

// 获取订单状态文本
const getStatusText = (status: number) => {
  const texts = ['待处理', '已受理', '已完成', '已取消']
  return texts[status] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadOrderList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    orderId: '',
    username: '',
    status: undefined,
    dateRange: []
  })
  handleSearch()
}

// 查看订单
const handleView = async (order: any) => {
  try {
    // 加载订单项详情
    const itemsResponse = await orderApi.getOrderItems(order.orderid)
    if (itemsResponse.code === 0 || itemsResponse.code === 200) {
      currentOrder.value = {
        ...order,
        items: itemsResponse.data || []
      }
    } else {
      currentOrder.value = {
        ...order,
        items: []
      }
    }
    viewDialogVisible.value = true
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  }
}

// 受理订单
const handleAccept = async (order: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要受理订单"${order.orderid}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await orderApi.acceptOrder(order.orderid)
    if (response.code === 0 || response.code === 200) {
      ElMessage.success('受理成功')
      loadOrderList()
    } else {
      ElMessage.error(response.message || '受理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('受理订单失败:', error)
      ElMessage.error('受理失败')
    }
  }
}

// 完成订单
const handleComplete = async (order: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要完成订单"${order.orderid}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await orderApi.completeOrder(order.orderid)
    if (response.code === 0 || response.code === 200) {
      ElMessage.success('订单已完成')
      loadOrderList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成订单失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 取消订单
const handleCancel = async (order: any) => {
  try {
    const reason = await ElMessageBox.prompt(
      '请输入取消原因',
      `取消订单"${order.orderid}"`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S+/,
        inputErrorMessage: '取消原因不能为空'
      }
    )

    const response = await orderApi.cancelOrder(order.orderid, reason.value)
    if (response.code === 0 || response.code === 200) {
      ElMessage.success('订单已取消')
      loadOrderList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 分页相关
const handleSizeChange = (val: number) => {
  pagination.size = val
  loadOrderList()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadOrderList()
}

// 页面加载时初始化
onMounted(() => {
  loadOrderList()
})
</script>

<style scoped>
.order-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.order-detail {
  padding: 20px 0;
}

.order-items {
  margin-top: 20px;
}

.order-items h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>