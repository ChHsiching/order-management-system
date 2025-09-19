
<!--  点餐单管理页  -->

<template>
  <div class="cart-container">
    <Navbar />

    <div class="cart-content">
      <h2 class="cart-title">我的点餐单</h2>

      <!-- 购物车为空 -->
      <div class="empty-cart" v-if="cartList.length === 0">
        <img src="@/assets/empty-cart.png" alt="空购物车" class="empty-img">
        <p class="empty-text">您的点餐单还是空的~</p>
        <button class="btn-go-shopping" @click="handleGoShopping">去点餐</button>
      </div>

      <!-- 购物车列表 -->
      <div class="cart-list" v-else>
        <table class="cart-table">
          <thead>
          <tr>
            <th>菜品名称</th>
            <th>图片</th>
            <th>单价(元)</th>
            <th>数量</th>
            <th>金额(元)</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in cartList" :key="item.dishId">
            <td class="cart-dish-name">{{ item.name }}</td>
            <td class="cart-dish-img">
              <img :src="item.image" :alt="item.name" />
            </td>
            <td class="cart-dish-price">{{ item.price }}</td>
            <td class="cart-dish-quantity">
              <div class="quantity-control">
                <button
                    class="btn-quantity"
                    @click="handleQuantityChange(item.dishId, -1)"
                    :disabled="item.quantity <= 1"
                >
                  -
                </button>
                <input
                    type="number"
                    v-model.number="item.quantity"
                    @change="handleQuantityInput(item.dishId)"
                    min="1"
                >
                <button
                    class="btn-quantity"
                    @click="handleQuantityChange(item.dishId, 1)"
                >
                  +
                </button>
              </div>
            </td>
            <td class="cart-dish-amount">{{ (item.price * item.quantity).toFixed(2) }}</td>
            <td class="cart-dish-action">
              <button class="btn-delete" @click="handleDelete(item.dishId)">删除</button>
            </td>
          </tr>
          </tbody>
        </table>

        <!-- 购物车底部 -->
        <div class="cart-footer">
          <div class="cart-total">
            <span class="total-label">总金额:</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>

          <div class="cart-actions">
            <button class="btn-clear" @click="handleClearCart">清空点餐单</button>
            <button class="btn-continue" @click="handleGoShopping">继续点餐</button>
            <button class="btn-submit" @click="handleSubmitOrder">去收银台</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../store/cartStore.js'
import { submitOrder } from '../api/orderApi.js'
import Navbar from '../components/Navbar.vue'

// 路由实例
const router = useRouter()
const cartStore = useCartStore()

// 状态定义（从Pinia获取）
const cartList = ref(cartStore.cartList)
const totalPrice = ref(cartStore.totalPrice)

// 生命周期：初始化购物车数据
onMounted(() => {
  // 监听Pinia状态变化，同步本地数据
  cartStore.$subscribe((mutation, state) => {
    cartList.value = state.cartList
    totalPrice.value = state.totalPrice
  })

  // 初始化购物车（从后端获取最新数据）
  cartStore.initCart()
})

// 修改数量（+/-按钮）
const handleQuantityChange = (dishId, step) => {
  cartStore.updateQuantity(dishId, cartStore.cartList.find(item => item.dishId === dishId).quantity + step)
}

// 输入框修改数量
const handleQuantityInput = (dishId) => {
  const item = cartStore.cartList.find(item => item.dishId === dishId)
  if (item.quantity < 1) {
    item.quantity = 1
  }
  cartStore.updateQuantity(dishId, item.quantity)
}

// 删除菜品
const handleDelete = async (dishId) => {
  if (confirm('确定要删除该菜品吗？')) {
    await cartStore.removeFromCart(dishId)
  }
}

// 清空购物车
const handleClearCart = async () => {
  if (confirm('确定要清空点餐单吗？')) {
    await cartStore.clearCart()
  }
}

// 继续点餐（返回首页）
const handleGoShopping = () => {
  router.push('/')
}

// 提交订单（去收银台）
const handleSubmitOrder = async () => {
  try {
    // 构造订单数据
    const orderData = {
      dishes: cartStore.cartList.map(item => ({
        dishId: item.dishId,
        quantity: item.quantity,
        price: item.price
      })),
      totalPrice: cartStore.totalPrice
    }

    // 调用提交订单接口
    const res = await submitOrder(orderData)
    alert('订单提交成功！订单号：' + res.orderId)

    // 清空购物车
    await cartStore.clearCart()

    // 跳转到订单详情页（假设存在）
    router.push(`/order/${res.orderId}`)
  } catch (error) {
    alert(error.message || '订单提交失败，请重试')
  }
}
</script>

<style scoped>
.cart-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.cart-content {
  width: 1200px;
  margin: 20px auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 30px;
}

.cart-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  border-bottom: 2px solid #ff6700;
  padding-bottom: 10px;
}

.empty-cart {
  text-align: center;
  padding: 80px 0;
}

.empty-img {
  width: 120px;
  height: 120px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  color: #999;
  margin-bottom: 30px;
}

.btn-go-shopping {
  padding: 10px 20px;
  background-color: #ff6700;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.cart-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.cart-table th,
.cart-table td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.cart-table th {
  background-color: #f9f9f9;
  font-weight: 500;
  color: #333;
}

.cart-dish-img img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.quantity-control {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 120px;
  margin: 0 auto;
}

.btn-quantity {
  width: 30px;
  height: 30px;
  border: none;
  background-color: #f5f5f5;
  color: #333;
  cursor: pointer;
  font-size: 16px;
}

.btn-quantity:disabled {
  background-color: #eee;
  cursor: not-allowed;
  color: #999;
}

.quantity-control input {
  width: 60px;
  height: 30px;
  border: none;
  border-left: 1px solid #ddd;
  border-right: 1px solid #ddd;
  text-align: center;
  font-size: 14px;
}

.btn-delete {
  padding: 5px 10px;
  background-color: #fff;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.btn-delete:hover {
  background-color: #fff2f0;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.cart-total {
  font-size: 18px;
}

.total-label {
  color: #666;
  margin-right: 10px;
}

.total-price {
  color: #ff4d4f;
  font-weight: bold;
}

.cart-actions {
  display: flex;
  gap: 10px;
}

.btn-clear {
  padding: 8px 15px;
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-continue {
  padding: 8px 15px;
  background-color: #fff;
  color: #ff6700;
  border: 1px solid #ff6700;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-submit {
  padding: 8px 20px;
  background-color: #ff6700;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-submit:hover {
  background-color: #e05a00;
}
</style>