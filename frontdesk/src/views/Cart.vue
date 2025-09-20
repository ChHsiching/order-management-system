<template>
  <div class="cart-page">
    <div class="page-header">
      <h1>购物车</h1>
      <p>管理您的购物车商品</p>
    </div>

    <!-- 购物车内容 -->
    <div v-loading="cartStore.loading" class="cart-content">
      <!-- 购物车为空 -->
      <el-empty
        v-if="cartStore.isEmpty"
        description="购物车是空的"
      >
        <template #default>
          <el-button type="primary" @click="$router.push('/')">
            去点餐
          </el-button>
        </template>
      </el-empty>

      <!-- 购物车有商品 -->
      <div v-else class="cart-items">
        <!-- 购物车表格 -->
        <el-card>
          <el-table :data="cartStore.items" style="width: 100%">
            <!-- 商品信息 -->
            <el-table-column label="商品" min-width="300">
              <template #default="{ row }">
                <div class="product-info">
                  <div class="product-image">
                    <img
                      :src="row.image || '/images/default-food.jpg'"
                      :alt="row.menuName"
                      @error="handleImageError"
                    />
                  </div>
                  <div class="product-details">
                    <h3>{{ row.menuName }}</h3>
                    <p class="product-price">单价: ¥{{ row.price.toFixed(2) }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>

            <!-- 单价 -->
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <span class="price">¥{{ row.price.toFixed(2) }}</span>
              </template>
            </el-table-column>

            <!-- 数量 -->
            <el-table-column label="数量" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="99"
                  size="small"
                  @change="(value: number) => handleQuantityChange(row, value)"
                />
              </template>
            </el-table-column>

            <!-- 小计 -->
            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <!-- 操作 -->
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button
                  type="danger"
                  size="small"
                  @click="handleRemoveItem(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 购物车底部 -->
          <div class="cart-footer">
            <div class="cart-summary">
              <div class="summary-item">
                <span>商品总数:</span>
                <span>{{ cartStore.totalCount }} 件</span>
              </div>
              <div class="summary-item total">
                <span>总计:</span>
                <span class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
              </div>
            </div>
            <div class="cart-actions">
              <el-button @click="handleClearCart" size="large">
                清空购物车
              </el-button>
              <el-button
                type="primary"
                size="large"
                @click="handleCheckout"
                :disabled="cartStore.isEmpty"
              >
                去结算
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

// 处理图片加载失败
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-food.jpg'
}

// 处理数量变化
const handleQuantityChange = async (item: any, quantity: number) => {
  if (quantity <= 0) {
    await handleRemoveItem(item)
    return
  }

  const success = await cartStore.updateQuantity(item.menuId, quantity)
  if (!success) {
    ElMessage.error('更新数量失败')
  }
}

// 处理删除商品
const handleRemoveItem = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要从购物车中删除 "${item.menuName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const success = await cartStore.removeFromCart(item.menuId)
    if (success) {
      ElMessage.success('商品已删除')
    } else {
      ElMessage.error('删除失败')
    }
  } catch {
    // 用户取消删除
  }
}

// 处理清空购物车
const handleClearCart = async () => {
  if (cartStore.isEmpty) return

  try {
    await ElMessageBox.confirm(
      '确定要清空购物车吗？此操作不可恢复。',
      '确认清空',
      {
        confirmButtonText: '清空',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const success = await cartStore.clearCart()
    if (success) {
      ElMessage.success('购物车已清空')
    } else {
      ElMessage.error('清空失败')
    }
  } catch {
    // 用户取消清空
  }
}

// 处理去结算
const handleCheckout = () => {
  console.log('[Cart] ===== 开始结算流程 =====')

  if (!authStore.isLoggedIn) {
    console.log('[Cart] 用户未登录，跳转到登录页')
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (cartStore.isEmpty) {
    console.log('[Cart] 购物车为空')
    ElMessage.warning('购物车是空的')
    return
  }

  console.log('[Cart] 跳转到结算页面')
  // 跳转到结算页面
  router.push('/checkout')
}

// 页面加载时获取购物车数据
onMounted(() => {
  cartStore.fetchCart()
})
</script>

<style scoped>
.cart-page {
  padding: 20px 0;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  color: var(--color-text);
  font-size: 28px;
}

.page-header p {
  margin: 0;
  color: var(--color-text-light);
  font-size: 16px;
}

.cart-content {
  max-width: 1000px;
  margin: 0 auto;
}

.cart-items {
  margin-bottom: 30px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.product-details {
  flex: 1;
}

.product-details h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: var(--color-text);
}

.product-price {
  margin: 0;
  color: var(--color-text-light);
  font-size: 14px;
}

.price {
  color: var(--color-danger);
  font-weight: bold;
  font-size: 16px;
}

.subtotal {
  color: var(--color-danger);
  font-weight: bold;
  font-size: 16px;
}

.cart-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-summary {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.summary-item span:first-child {
  color: var(--color-text-light);
}

.summary-item.total {
  font-size: 18px;
  font-weight: bold;
}

.total-price {
  color: var(--color-danger);
  font-size: 24px;
}

.cart-actions {
  display: flex;
  gap: 15px;
}

@media (max-width: 768px) {
  .cart-footer {
    flex-direction: column;
    gap: 20px;
  }

  .cart-summary {
    width: 100%;
  }

  .cart-actions {
    width: 100%;
    justify-content: space-between;
  }

  .product-info {
    flex-direction: column;
    text-align: center;
  }

  .cart-footer {
    padding: 15px;
  }
}
</style>