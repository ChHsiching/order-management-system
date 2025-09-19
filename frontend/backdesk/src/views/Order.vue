<template>
  <div class="order-container">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>
    
    <!-- 订单列表 -->
    <div class="card">
      <h3>订单列表</h3>
      <table class="table">
        <thead>
          <tr>
            <th>序号</th>
            <th>订单编号</th>
            <th>下单日期</th>
            <th>状态</th>
            <th>点餐桌位</th>
            <th>订单价格</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(order, index) in orderList" :key="order.id">
            <td>{{ index + 1 }}</td>
            <td>{{ order.orderNumber }}</td>
            <td>{{ order.orderDate }}</td>
            <td>
              <span :class="getStatusClass(order.status)">{{ order.status }}</span>
            </td>
            <td>{{ order.tablePosition }}</td>
            <td>{{ order.totalPrice }}</td>
            <td>
              <button 
                v-if="order.status === '待受理'" 
                @click="handleAcceptOrder(order.id)" 
                class="btn-primary btn-small"
              >
                订单明细
              </button>
              <button 
                v-if="order.status === '待受理'" 
                @click="handleProcessOrder(order.id)" 
                class="btn-success btn-small"
              >
                受理订单
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 订单明细对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="订单明细" 
      width="600px"
    >
      <div v-if="selectedOrder">
        <div class="order-info">
          <h4>订单信息</h4>
          <p><strong>订单编号：</strong>{{ selectedOrder.orderNumber }}</p>
          <p><strong>下单时间：</strong>{{ selectedOrder.orderDate }}</p>
          <p><strong>用户：</strong>{{ selectedOrder.customerName }}</p>
          <p><strong>桌位：</strong>{{ selectedOrder.tablePosition }}</p>
          <p><strong>状态：</strong>{{ selectedOrder.status }}</p>
        </div>
        
        <div class="order-items">
          <h4>订单商品</h4>
          <table class="detail-table">
            <thead>
              <tr>
                <th>商品名称</th>
                <th>单价</th>
                <th>数量</th>
                <th>小计</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in selectedOrder.items" :key="item.id">
                <td>{{ item.menuName }}</td>
                <td>￥{{ item.price }}</td>
                <td>{{ item.quantity }}</td>
                <td>￥{{ (item.price * item.quantity).toFixed(2) }}</td>
              </tr>
            </tbody>
          </table>
          <div class="total-price">
            <strong>总计：￥{{ selectedOrder.totalPrice }}</strong>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <button @click="detailDialogVisible = false" class="btn-secondary">关闭</button>
          <button 
            v-if="selectedOrder && selectedOrder.status === '待受理'" 
            @click="handleProcessOrder(selectedOrder.id)" 
            class="btn-success"
          >
            受理订单
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Order',
  setup() {
    const orderList = ref([])
    const detailDialogVisible = ref(false)
    const selectedOrder = ref(null)
    
    // 模拟订单数据
    const mockOrders = [
      {
        id: 1,
        orderNumber: '20170314074202',
        orderDate: '2017-04-25 14:38:29',
        status: '待受理',
        tablePosition: '第二桌',
        totalPrice: 77,
        customerName: '张三',
        items: [
          { id: 1, menuName: '培根鸡蛋蛋', price: 22, quantity: 1 },
          { id: 2, menuName: '巧师傅较量三十神', price: 23, quantity: 1 },
          { id: 3, menuName: '盖浇饭三文鱼', price: 32, quantity: 1 }
        ]
      },
      {
        id: 2,
        orderNumber: '20170314074338',
        orderDate: '2017-04-25 14:38:29',
        status: '待受理',
        tablePosition: '第二桌',
        totalPrice: 77,
        customerName: '李四',
        items: [
          { id: 1, menuName: '意大利茄汁面', price: 11, quantity: 2 },
          { id: 2, menuName: '奶油蘑菇大利面', price: 33, quantity: 1 },
          { id: 3, menuName: '什锦吐司披萨', price: 22, quantity: 1 }
        ]
      },
      {
        id: 3,
        orderNumber: '20170425042114',
        orderDate: '2017-04-25 14:38:29',
        status: '待受理',
        tablePosition: '第一桌',
        totalPrice: 33,
        customerName: '王五',
        items: [
          { id: 1, menuName: '奶油蘑菇大利面', price: 33, quantity: 1 }
        ]
      },
      {
        id: 4,
        orderNumber: '20170450242037',
        orderDate: '2017-04-25 02:40:37',
        status: '待受理',
        tablePosition: '第一桌',
        totalPrice: 33,
        customerName: '赵六',
        items: [
          { id: 1, menuName: '新奥尔良烤鸡肉串', price: 22, quantity: 1 },
          { id: 2, menuName: '培根鸡蛋蛋', price: 11, quantity: 1 }
        ]
      },
      {
        id: 5,
        orderNumber: '20170429033151',
        orderDate: '2017-04-29 03:31:51',
        status: '待受理',
        tablePosition: '第一桌',
        totalPrice: 22,
        customerName: '孙七',
        items: [
          { id: 1, menuName: '培根鸡蛋蛋', price: 22, quantity: 1 }
        ]
      }
    ]
    
    // 获取订单列表
    const fetchOrderList = async () => {
      try {
        // TODO: 调用后端接口获取订单列表
        // const response = await axios.get('/api/admin/orders')
        // orderList.value = response.data
        
        // 模拟数据
        orderList.value = [...mockOrders]
      } catch (error) {
        ElMessage.error('获取订单列表失败')
        console.error('获取订单列表错误:', error)
      }
    }
    
    // 查看订单明细
    const handleAcceptOrder = async (id) => {
      try {
        // TODO: 调用后端接口获取订单明细
        // const response = await axios.get(`/api/admin/orders/${id}`)
        // selectedOrder.value = response.data
        
        // 模拟数据
        selectedOrder.value = orderList.value.find(order => order.id === id)
        detailDialogVisible.value = true
      } catch (error) {
        ElMessage.error('获取订单明细失败')
        console.error('获取订单明细错误:', error)
      }
    }
    
    // 受理订单
    const handleProcessOrder = async (id) => {
      try {
        await ElMessageBox.confirm('确定要受理该订单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        
        // TODO: 调用后端接口受理订单
        // await axios.put(`/api/admin/orders/${id}/process`)
        
        // 模拟受理成功
        const orderIndex = orderList.value.findIndex(order => order.id === id)
        if (orderIndex !== -1) {
          orderList.value[orderIndex].status = '已受理'
        }
        
        ElMessage.success('订单受理成功')
        detailDialogVisible.value = false
      } catch (error) {
        // 用户取消受理或受理失败
        if (error !== 'cancel') {
          ElMessage.error('受理订单失败')
          console.error('受理订单错误:', error)
        }
      }
    }
    
    // 获取状态样式类
    const getStatusClass = (status) => {
      switch (status) {
        case '待受理':
          return 'status-pending'
        case '已受理':
          return 'status-accepted'
        case '已完成':
          return 'status-completed'
        default:
          return ''
      }
    }
    
    onMounted(() => {
      fetchOrderList()
    })
    
    return {
      orderList,
      detailDialogVisible,
      selectedOrder,
      handleAcceptOrder,
      handleProcessOrder,
      getStatusClass
    }
  }
}
</script>

<style scoped>
.order-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  color: #333;
  font-size: 24px;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.card h3 {
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.table th,
.table td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
}

.table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.table tr:hover {
  background-color: #f5f7fa;
}

.btn-small {
  padding: 4px 8px;
  font-size: 12px;
  margin-right: 5px;
}

.btn-success {
  background-color: #67c23a;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-success:hover {
  background-color: #85ce61;
}

.status-pending {
  color: #e6a23c;
  font-weight: bold;
}

.status-accepted {
  color: #67c23a;
  font-weight: bold;
}

.status-completed {
  color: #909399;
  font-weight: bold;
}

.order-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.order-info h4 {
  margin-bottom: 10px;
  color: #333;
}

.order-info p {
  margin: 5px 0;
  color: #666;
}

.order-items h4 {
  margin-bottom: 15px;
  color: #333;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}

.detail-table th,
.detail-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

.detail-table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.total-price {
  text-align: right;
  padding: 10px 0;
  border-top: 2px solid #409eff;
  color: #333;
  font-size: 16px;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer button {
  margin-left: 10px;
}
</style> 