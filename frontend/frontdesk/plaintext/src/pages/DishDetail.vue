
<!--  菜品详情页  -->

<template>
  <div class="dish-detail-container">
    <Navbar />

    <div class="detail-content">
      <!-- 加载中 -->
      <div class="loading" v-if="isLoading">加载中...</div>

      <!-- 菜品详情 -->
      <div class="detail-card" v-if="dish && !isLoading">
        <div class="detail-header">
          <h1 class="dish-name">{{ dish.name }}</h1>
          <div class="dish-price">
            <span class="price-original">原价: ¥{{ dish.price1 }}</span>
            <span class="price-hot">热销价: ¥{{ dish.price2 }}</span>
          </div>
        </div>

        <div class="detail-body">
          <div class="dish-image">
            <img :src="dish.imgpath" :alt="dish.name" />
          </div>

          <div class="dish-info">
            <div class="info-item">
              <span class="info-label">菜品分类:</span>
              <span class="info-value">{{ dish.categoryName || '未分类' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">销量:</span>
              <span class="info-value">{{ dish.xiaoliang }} 份</span>
            </div>
            <div class="info-item">
              <span class="info-label">推荐状态:</span>
              <span class="info-value">{{ dish.newstuijian === 1 ? '推荐' : '不推荐' }}</span>
            </div>
            <div class="info-item info-desc">
              <span class="info-label">菜品介绍:</span>
              <div class="info-value" v-html="dish.info5"></div>
            </div>
          </div>
        </div>

        <div class="detail-footer">
          <div class="quantity-control">
            <button
                class="btn-quantity"
                @click="handleQuantityChange(-1)"
                :disabled="quantity <= 1"
            >
              -
            </button>
            <input
                type="number"
                v-model.number="quantity"
                @change="handleQuantityInput"
                min="1"
            >
            <button
                class="btn-quantity"
                @click="handleQuantityChange(1)"
            >
              +
            </button>
          </div>

          <button
              class="btn-order"
              @click="handleAddToCart"
          >
            加入点餐单 (¥{{ dish.price2 * quantity }})
          </button>
        </div>
      </div>

      <!-- 无数据提示 -->
      <div class="empty-tip" v-if="!dish && !isLoading">
        未找到该菜品信息
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/userStore.js'
import { useCartStore } from '../store/cartStore.js'
import { getDishById } from '../api/dishApi.js'
import Navbar from '../components/Navbar.vue'

// 路由实例
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 状态定义
const dishId = ref(route.params.id) // 菜品ID（从路由获取）
const dish = ref(null) // 菜品详情数据
const isLoading = ref(true) // 加载状态
const quantity = ref(1) // 点餐数量

// 生命周期：加载菜品详情
onMounted(async () => {
  await fetchDishDetail()
})

// 获取菜品详情
const fetchDishDetail = async () => {
  try {
    isLoading.value = true
    const res = await getDishById(dishId.value)
    dish.value = res.data
  } catch (error) {
    alert(error.message || '获取菜品信息失败')
  } finally {
    isLoading.value = false
  }
}

// 修改点餐数量（+/-按钮）
const handleQuantityChange = (step) => {
  const newQuantity = quantity.value + step
  if (newQuantity >= 1) {
    quantity.value = newQuantity
  }
}

// 输入框修改数量
const handleQuantityInput = () => {
  if (quantity.value < 1) {
    quantity.value = 1
  }
}

// 加入购物车
const handleAddToCart = async () => {
  // 检查是否登录
  if (!userStore.token) {
    if (confirm('请先登录会员才能点餐，是否前往登录？')) {
      router.push({
        name: 'LoginRegister',
        query: { redirect: `/dish/${dishId.value}` }
      })
    }
    return
  }

  try {
    await cartStore.addToCart(dish.value, quantity.value)
    alert('已成功加入点餐单！')
    // 可选择留在详情页或跳转到购物车
    if (confirm('是否前往点餐单查看？')) {
      router.push('/cart')
    }
  } catch (error) {
    alert(error.message || '加入点餐单失败，请重试')
  }
}
</script>

<style scoped>
.dish-detail-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.detail-content {
  width: 1200px;
  margin: 20px auto;
}

.loading {
  text-align: center;
  padding: 100px 0;
  font-size: 16px;
  color: #666;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 30px;
}

.detail-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.dish-name {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.dish-price {
  display: flex;
  gap: 20px;
  align-items: center;
}

.price-original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.price-hot {
  font-size: 18px;
  color: #ff4d4f;
  font-weight: bold;
}

.detail-body {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.dish-image {
  width: 400px;
  height: 300px;
  overflow: hidden;
  border-radius: 8px;
}

.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dish-info {
  flex: 1;
}

.info-item {
  margin-bottom: 15px;
  font-size: 14px;
}

.info-label {
  display: inline-block;
  width: 80px;
  color: #666;
}

.info-value {
  color: #333;
}

.info-desc .info-value {
  display: inline-block;
  width: calc(100% - 80px);
  line-height: 1.6;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.quantity-control {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.btn-quantity {
  width: 36px;
  height: 36px;
  border: none;
  background-color: #f5f5f5;
  color: #333;
  cursor: pointer;
  font-size: 18px;
}

.btn-quantity:disabled {
  background-color: #eee;
  cursor: not-allowed;
  color: #999;
}

.quantity-control input {
  width: 60px;
  height: 36px;
  border: none;
  border-left: 1px solid #ddd;
  border-right: 1px solid #ddd;
  text-align: center;
  font-size: 14px;
}

.quantity-control input:focus {
  outline: none;
}

.btn-order {
  padding: 10px 30px;
  background-color: #ff6700;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.btn-order:hover {
  background-color: #e05a00;
}

.empty-tip {
  text-align: center;
  padding: 100px 0;
  color: #999;
  font-size: 16px;
}
</style>