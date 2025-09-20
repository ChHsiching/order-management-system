<template>
  <div class="checkout-page">
    <div class="page-header">
      <h1>订单结算</h1>
      <p>确认您的订单信息并完成支付</p>
    </div>

    <!-- 结算内容 -->
    <div v-loading="loading" class="checkout-content">
      <!-- 收货信息 -->
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <h3>收货信息</h3>
          </div>
        </template>

        <el-form
          ref="checkoutFormRef"
          :model="checkoutForm"
          :rules="rules"
          label-width="100px"
          class="checkout-form"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="收货人" prop="receiverName">
                <el-input
                  v-model="checkoutForm.receiverName"
                  placeholder="请输入收货人姓名"
                  maxlength="20"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input
                  v-model="checkoutForm.phone"
                  placeholder="请输入联系电话"
                  maxlength="11"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="收货地址" prop="address">
            <el-input
              v-model="checkoutForm.address"
              type="textarea"
              :rows="3"
              placeholder="请输入详细收货地址"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="备注信息">
            <el-input
              v-model="checkoutForm.remark"
              type="textarea"
              :rows="2"
              placeholder="选填：如有特殊要求请在此说明"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 商品清单 -->
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <h3>商品清单</h3>
            <el-button type="text" @click="$router.push('/cart')">
              返回购物车修改
            </el-button>
          </div>
        </template>

        <div class="order-items">
          <div
            v-for="item in cartStore.items"
            :key="item.menuId"
            class="order-item"
          >
            <div class="item-info">
              <div class="item-image">
                <img
                  :src="item.image || '/images/default-food.jpg'"
                  :alt="item.menuName"
                  @error="handleImageError"
                />
              </div>
              <div class="item-details">
                <h4>{{ item.menuName }}</h4>
                <p class="item-price">单价：¥{{ item.price.toFixed(2) }}</p>
                <p class="item-quantity">数量：{{ item.quantity }}</p>
              </div>
            </div>
            <div class="item-subtotal">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </div>
          </div>
        </div>
      </el-card>

      <!-- 费用明细 -->
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <h3>费用明细</h3>
          </div>
        </template>

        <div class="cost-breakdown">
          <div class="cost-row">
            <span>商品总计：</span>
            <span>¥{{ cartStore.totalPrice.toFixed(2) }}</span>
          </div>
          <div class="cost-row">
            <span>配送费：</span>
            <span>¥{{ deliveryFee.toFixed(2) }}</span>
          </div>
          <div class="cost-row" v-if="discount > 0">
            <span>优惠：</span>
            <span class="discount">-¥{{ discount.toFixed(2) }}</span>
          </div>
          <div class="cost-row total">
            <span>实付金额：</span>
            <span class="total-amount">¥{{ safeFinalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 支付方式 -->
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <h3>支付方式</h3>
          </div>
        </template>

        <el-radio-group v-model="checkoutForm.paymentMethod" class="payment-methods">
          <el-radio label="alipay">
            <i class="fab fa-alipay"></i> 支付宝
          </el-radio>
          <el-radio label="wechat">
            <i class="fab fa-weixin"></i> 微信支付
          </el-radio>
          <el-radio label="cash">
            <i class="fas fa-money-bill-wave"></i> 货到付款
          </el-radio>
        </el-radio-group>
      </el-card>

      <!-- 提交订单 -->
      <div class="submit-section">
        <div class="submit-info">
          <div class="total-info">
            <span>需支付：</span>
            <span class="final-amount">¥{{ safeFinalAmount.toFixed(2) }}</span>
          </div>
          <p class="submit-tips">点击提交订单，表示您同意我们的服务条款</p>
        </div>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmitOrder"
          class="submit-button"
        >
          提交订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { orderApi } from '@/utils/api'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

// 表单相关
const checkoutFormRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

// 表单数据
const checkoutForm = reactive({
  receiverName: '',
  phone: '',
  address: '',
  paymentMethod: 'alipay',
  remark: ''
})

// 费用计算
const deliveryFee = ref(5) // 配送费
const discount = ref(0) // 优惠金额

// 计算最终金额
const finalAmount = computed(() => {
  // 安全获取totalPrice，确保是数字类型
  const totalPriceValue = cartStore.totalPrice ?? 0
  const totalPrice = Number(totalPriceValue) || 0
  const delivery = Number(deliveryFee.value) || 0
  const disc = Number(discount.value) || 0
  const final = totalPrice + delivery - disc

  // 验证计算结果
  if (isNaN(final) || !isFinite(final)) {
    console.error('[Checkout] finalAmount计算结果无效:', {
      totalPrice,
      delivery,
      disc,
      final,
      cartStoreTotalPrice: cartStore.totalPrice,
      typeofTotalPrice: typeof cartStore.totalPrice,
      cartItems: cartStore.items
    })
    return 0 // 返回默认值而不是NaN
  }

  console.log('[Checkout] finalAmount计算:', {
    totalPriceValue,
    totalPrice,
    delivery,
    disc,
    final,
    cartStoreTotalPrice: cartStore.totalPrice,
    typeofTotalPrice: typeof cartStore.totalPrice
  })
  return final
})

// 安全的最终金额显示（确保总是返回有效数字）
const safeFinalAmount = computed(() => {
  return Number(finalAmount.value) || 0
})

// 表单验证规则
const rules: FormRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '收货人姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' },
    { min: 5, max: 100, message: '收货地址长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 处理图片加载失败
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-food.jpg'
}

// 加载用户信息
const loadUserInfo = () => {
  if (authStore.user) {
    checkoutForm.receiverName = authStore.user.name || authStore.user.username || ''
    checkoutForm.phone = authStore.user.phone || ''
    console.log('[Checkout] 已加载用户信息:', {
      name: checkoutForm.receiverName,
      phone: checkoutForm.phone
    })
  }
}

// 提交订单
const handleSubmitOrder = async () => {
  console.log('[Checkout] ===== 开始提交订单 =====')

  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (cartStore.isEmpty) {
    ElMessage.warning('购物车是空的')
    router.push('/cart')
    return
  }

  try {
    // 验证表单
    await checkoutFormRef.value?.validate()
    console.log('[Checkout] 表单验证通过')

    // 确认提交
    const finalAmountValue = Number(finalAmount.value) || 0
    await ElMessageBox.confirm(
      `确认提交订单？需支付金额：¥${finalAmountValue.toFixed(2)}`,
      '确认订单',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true
    console.log('[Checkout] 准备订单数据')

    // 准备订单数据
    const orderData = {
      username: authStore.username,
      items: cartStore.items.map(item => ({
        menuId: item.menuId,
        quantity: item.quantity,
        price: item.price
      })),
      address: checkoutForm.address,
      phone: checkoutForm.phone,
      paymentMethod: checkoutForm.paymentMethod,
      remark: checkoutForm.remark,
      deliveryFee: Number(deliveryFee.value) || 0,
      discount: Number(discount.value) || 0,
      totalAmount: finalAmountValue
    }

    console.log('[Checkout] 订单数据:', orderData)

    // 调用创建订单API
    console.log('[Checkout] 发送创建订单请求...')
    const response: any = await orderApi.createOrder(orderData)
    console.log('[Checkout] 创建订单响应:', response)

    // 处理响应
    const data = response.data || response
    console.log('[Checkout] 处理后的响应数据:', data)

    if (response?.status === 200) {
      // 检查是否是直接的订单对象（后端成功创建订单时返回）
      if (data.id && data.orderId) {
        console.log('[Checkout] 订单创建成功:', data)

        // 清空购物车
        await cartStore.clearCart()
        console.log('[Checkout] 购物车已清空')

        // 显示成功消息
        ElMessage.success('订单创建成功！')

        // 跳转到订单详情页面
        router.push(`/orders?highlight=${data.id || data.orderId}`)
      }
      // 检查是否是包装的响应格式 {code, success, data}
      else if (data.code === 200 || data.success === true) {
        const orderInfo = data.data || data
        console.log('[Checkout] 订单创建成功:', orderInfo)

        // 清空购物车
        await cartStore.clearCart()
        console.log('[Checkout] 购物车已清空')

        // 显示成功消息
        ElMessage.success('订单创建成功！')

        // 跳转到订单详情页面
        router.push(`/orders?highlight=${orderInfo.id || orderInfo.orderId}`)
      }
      // 处理错误情况
      else {
        console.error('[Checkout] 订单创建失败:', data)
        ElMessage.error(data.message || '订单创建失败')
      }
    } else {
      console.error('[Checkout] 订单创建失败:', data)
      ElMessage.error(data.message || '订单创建失败')
    }
  } catch (error: any) {
    console.error('[Checkout] 提交订单异常:', error)

    if (error !== 'cancel') {
      if (error.response) {
        console.error('[Checkout] HTTP错误:', {
          status: error.response.status,
          data: error.response.data
        })

        if (error.response.status === 401) {
          ElMessage.error('登录已过期，请重新登录')
          authStore.logout()
          router.push('/login')
        } else if (error.response.status === 400) {
          // 处理表单验证错误
          const errorData = error.response.data
          console.log('[Checkout] 表单验证错误详情:', errorData)

          // 处理Spring Boot Validation错误格式 (MethodArgumentNotValidException)
          if (typeof errorData === 'object') {
            // 检查是否是Spring Boot验证错误格式
            if (errorData.errors && Array.isArray(errorData.errors)) {
              // 处理标准Spring Boot验证错误格式
              const fieldErrors = errorData.errors
              console.log('[Checkout] Spring Boot验证错误:', fieldErrors)

              // 查找地址字段错误
              const addressError = fieldErrors.find((err: any) =>
                err.field === 'address' || err.defaultMessage?.includes('地址') || err.defaultMessage?.includes('address')
              )
              if (addressError) {
                ElMessage.error('请填写收货地址')
                return
              }

              // 查找电话字段错误
              const phoneError = fieldErrors.find((err: any) =>
                err.field === 'phone' || err.defaultMessage?.includes('电话') || err.defaultMessage?.includes('phone')
              )
              if (phoneError) {
                ElMessage.error('请填写正确的联系电话')
                return
              }

              // 查找商品项错误
              const itemsError = fieldErrors.find((err: any) =>
                err.field === 'items' || err.defaultMessage?.includes('商品') || err.defaultMessage?.includes('items')
              )
              if (itemsError) {
                ElMessage.error('购物车中没有商品或商品信息不完整')
                return
              }

              // 查找用户名错误
              const usernameError = fieldErrors.find((err: any) =>
                err.field === 'username' || err.defaultMessage?.includes('用户名') || err.defaultMessage?.includes('username')
              )
              if (usernameError) {
                ElMessage.error('用户信息验证失败')
                return
              }

              // 显示第一个错误消息
              if (fieldErrors.length > 0) {
                ElMessage.error(fieldErrors[0].defaultMessage || '订单数据验证失败')
                return
              }
            }

            // 检查简单的字段错误格式
            if (errorData.address) {
              ElMessage.error('请填写收货地址')
            } else if (errorData.phone) {
              ElMessage.error('请填写正确的联系电话')
            } else if (errorData.items) {
              ElMessage.error('购物车中没有商品或商品信息不完整')
            } else if (errorData.username) {
              ElMessage.error('用户信息验证失败')
            } else {
              // 检查是否有全局错误消息
              const errorMessage = errorData.message || errorData.error || '订单数据验证失败'
              ElMessage.error(errorMessage)
            }
          } else {
            // 处理字符串错误消息
            const errorMessage = errorData || '订单数据错误'
            if (errorMessage.includes('地址') || errorMessage.includes('address')) {
              ElMessage.error('请填写收货地址')
            } else if (errorMessage.includes('电话') || errorMessage.includes('phone')) {
              ElMessage.error('请填写正确的联系电话')
            } else if (errorMessage.includes('商品') || errorMessage.includes('items')) {
              ElMessage.error('购物车中没有商品或商品信息不完整')
            } else {
              ElMessage.error(errorMessage)
            }
          }
        } else {
          ElMessage.error(`订单创建失败: ${error.response.data?.message || error.message}`)
        }
      } else if (error.message) {
        // 处理网络错误或其他异常
        let errorMessage = '提交失败: ' + error.message

        // 提供更友好的错误提示
        if (error.message.includes('address') || error.message.includes('地址')) {
          errorMessage = '请填写收货地址'
        } else if (error.message.includes('phone') || error.message.includes('电话')) {
          errorMessage = '请填写正确的联系电话'
        } else if (error.message.includes('network') || error.message.includes('网络')) {
          errorMessage = '网络连接失败，请检查网络后重试'
        }

        ElMessage.error(errorMessage)
      } else {
        ElMessage.error('订单提交失败，请稍后重试')
      }
    }
  } finally {
    submitting.value = false
    console.log('[Checkout] ===== 提交订单结束 =====')
  }
}

// 页面加载时检查
onMounted(() => {
  console.log('[Checkout] ===== 结算页面加载 =====')

  // 检查登录状态
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 检查购物车
  if (cartStore.isEmpty) {
    ElMessage.warning('购物车是空的')
    router.push('/cart')
    return
  }

  // 加载用户信息
  loadUserInfo()
  console.log('[Checkout] 结算页面初始化完成')
})
</script>

<style scoped>
.checkout-page {
  padding: 20px 0;
  max-width: 1000px;
  margin: 0 auto;
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

.checkout-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  transition: box-shadow 0.3s ease;
}

.section-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 18px;
  font-weight: 600;
}

.checkout-form {
  padding: 10px 0;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid var(--color-border-light);
  border-radius: 8px;
  background: var(--color-bg-soft);
}

.item-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.item-image {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.item-details {
  flex: 1;
}

.item-details h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: var(--color-text);
}

.item-price {
  margin: 0 0 3px 0;
  color: var(--color-text-light);
  font-size: 14px;
}

.item-quantity {
  margin: 0;
  color: var(--color-text-light);
  font-size: 14px;
}

.item-subtotal {
  font-weight: bold;
  color: var(--color-danger);
  font-size: 16px;
}

.cost-breakdown {
  background: var(--color-bg-soft);
  padding: 20px;
  border-radius: 8px;
}

.cost-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: var(--color-text);
  font-size: 14px;
}

.cost-row:last-child {
  margin-bottom: 0;
}

.cost-row.total {
  font-weight: bold;
  font-size: 16px;
  padding-top: 10px;
  border-top: 2px solid var(--color-border);
  margin-top: 10px;
}

.total-amount {
  color: var(--color-danger);
  font-size: 20px;
}

.discount {
  color: var(--color-success);
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 10px 0;
}

.payment-methods :deep(.el-radio) {
  margin-right: 0;
  padding: 15px;
  border: 1px solid var(--color-border-light);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.payment-methods :deep(.el-radio.is-checked) {
  border-color: var(--color-primary);
  background: var(--color-primary-light-9);
}

.submit-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: var(--color-bg-soft);
  border-radius: 8px;
  border: 2px solid var(--color-border-light);
}

.submit-info {
  flex: 1;
}

.total-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.total-info span:first-child {
  color: var(--color-text);
  font-size: 16px;
}

.final-amount {
  color: var(--color-danger);
  font-size: 24px;
  font-weight: bold;
}

.submit-tips {
  margin: 0;
  color: var(--color-text-light);
  font-size: 14px;
}

.submit-button {
  min-width: 150px;
  height: 45px;
  font-size: 16px;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkout-page {
    padding: 10px 0;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .page-header p {
    font-size: 14px;
  }

  .submit-section {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .payment-methods {
    gap: 10px;
  }

  .order-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .item-info {
    width: 100%;
  }
}
</style>