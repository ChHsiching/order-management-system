import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/utils/api'
import { ElMessage } from 'element-plus'
import { useAuthStore } from './auth'

export interface CartItem {
  id?: number
  menuId: number
  menuName: string
  price: number
  quantity: number
  image?: string
}

export const useCartStore = defineStore('cart', () => {
  // 状态
  const items = ref<CartItem[]>([])
  const loading = ref<boolean>(false)

  // 自动初始化购物车
  const authStore = useAuthStore()
  console.log('[Cart Store] Auth store引用:', {
    authStoreExists: !!authStore,
    authStoreState: {
      isLoggedIn: authStore.isLoggedIn,
      username: authStore.username,
      token: authStore.token
    }
  })

  // 监听登录状态变化，自动获取购物车
  authStore.$onAction(({ name, after }) => {
    if (name === 'login' || name === 'initialize') {
      after(() => {
        console.log('[Cart Store] 检测到认证状态变化:', { name, isLoggedIn: authStore.isLoggedIn })
        if (authStore.isLoggedIn) {
          fetchCart()
        }
      })
    }
  })

  // 计算属性
  const totalCount = computed(() => {
    if (!items.value || items.value.length === 0) return 0
    return items.value.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0)
  })

  const totalPrice = computed(() => {
    console.log('[Cart] totalPrice计算开始:', {
      items: items.value,
      itemsLength: items.value?.length || 0
    })

    if (!items.value || items.value.length === 0) {
      console.log('[Cart] 购物车为空，返回0')
      return 0
    }

    const total = items.value.reduce((sum, item) => {
      const price = Number(item.price) || 0
      const quantity = Number(item.quantity) || 0
      return sum + (price * quantity)
    }, 0)

    console.log('[Cart] totalPrice计算完成:', {
      total,
      items: items.value,
      itemsWithInvalidPrice: items.value.filter(item => !item.price || isNaN(Number(item.price)))
    })

    return total
  })
  const isEmpty = computed(() => items.value.length === 0)

  // 添加到购物车
  const addToCart = async (menuId: number, quantity: number = 1, menuInfo?: any) => {
    const authStore = useAuthStore()

    if (!authStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      return false
    }

    loading.value = true
    try {
      console.log('添加到购物车 - 用户状态:', {
        isLoggedIn: authStore.isLoggedIn,
        username: authStore.username,
        menuId,
        quantity,
        menuInfo
      })

      // 已登录，同步到后端
      const response: any = await cartApi.addToCart(authStore.username, menuId, quantity)
      console.log('添加到购物车 - API响应:', response)

      // 处理不同的响应格式 - 现在response是完整response对象
      const data = response.data || response
      if (data.code === 200 || data.code === 0 || data.success === true) {
        await fetchCart() // 重新获取购物车数据
        ElMessage.success('已添加到购物车')
        return true
      } else {
        console.error('添加到购物车失败:', response)
        ElMessage.error(data.message || data.error || '添加失败')
        return false
      }
    } catch (error: any) {
      console.error('添加到购物车异常:', error)
      ElMessage.error(error.message || '添加失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 更新购物车商品数量
  const updateQuantity = async (itemId: number, quantity: number) => {
    if (quantity <= 0) {
      await removeFromCart(itemId)
      return
    }

    const authStore = useAuthStore()

    loading.value = true
    try {
      if (!authStore.isLoggedIn) {
        const item = items.value.find(item => item.id === itemId)
        if (item) {
          item.quantity = quantity
        }
        return
      }

      const response: any = await cartApi.updateCartItem(authStore.username, itemId, quantity)
      const data = response.data || response

      if (data.code === 200 || data.code === 0) {
        await fetchCart()
        return true
      } else {
        ElMessage.error(data.message || '更新失败')
        return false
      }
    } catch (error: any) {
      ElMessage.error(error.message || '更新失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 从购物车移除
  const removeFromCart = async (itemId: number) => {
    const authStore = useAuthStore()

    loading.value = true
    try {
      if (!authStore.isLoggedIn) {
        items.value = items.value.filter(item => item.id !== itemId)
        ElMessage.success('已移除')
        return
      }

      const response: any = await cartApi.removeCartItem(authStore.username, itemId)
      const data = response.data || response

      if (data.code === 200 || data.code === 0) {
        await fetchCart()
        ElMessage.success('已移除')
        return true
      } else {
        ElMessage.error(data.message || '移除失败')
        return false
      }
    } catch (error: any) {
      ElMessage.error(error.message || '移除失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 清空购物车
  const clearCart = async () => {
    const authStore = useAuthStore()

    loading.value = true
    try {
      if (!authStore.isLoggedIn) {
        items.value = []
        ElMessage.success('购物车已清空')
        return
      }

      const response: any = await cartApi.clearCart(authStore.username)
      const data = response.data || response

      if (data.code === 200 || data.code === 0) {
        items.value = []
        ElMessage.success('购物车已清空')
        return true
      } else {
        ElMessage.error(data.message || '清空失败')
        return false
      }
    } catch (error: any) {
      ElMessage.error(error.message || '清空失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 获取购物车数据
  const fetchCart = async () => {
    const authStore = useAuthStore()

    if (!authStore.isLoggedIn) {
      console.log('获取购物车 - 用户未登录，跳过')
      return // 未登录时不获取
    }

    loading.value = true
    try {
      console.log('获取购物车 - 开始获取用户购物车数据:', authStore.username)
      const response: any = await cartApi.getCart(authStore.username)
      console.log('获取购物车 - API响应:', response)

      // 处理不同的响应格式 - 现在response是完整response对象
      const data = response.data || response
      if (data.code === 200 || data.code === 0) {
        items.value = (data.data || []).map((item: any) => ({
          id: item.id,
          menuId: item.productId || item.product_id,
          menuName: item.productName || item.product_name,
          price: item.price,
          quantity: item.quantity,
          image: '' // ShoppingCart doesn't have image field, will be populated from menu data
        }))
        console.log('获取购物车 - 数据处理完成，项目数:', items.value.length)
      } else if (Array.isArray(data)) {
        // 直接返回数组的情况
        items.value = data.map((item: any) => ({
          id: item.id,
          menuId: item.productId || item.product_id,
          menuName: item.productName || item.product_name,
          price: item.price,
          quantity: item.quantity,
          image: ''
        }))
        console.log('获取购物车 - 数组数据处理完成，项目数:', items.value.length)
      } else {
        console.warn('获取购物车 - 响应格式异常:', data)
        items.value = []
      }
    } catch (error: any) {
      console.error('获取购物车失败:', error)
      ElMessage.error('获取购物车失败: ' + error.message)
      items.value = []
    } finally {
      loading.value = false
    }
  }

  // 初始化购物车
  const initialize = () => {
    const authStore = useAuthStore()
    if (authStore.isLoggedIn) {
      fetchCart()
    }
  }

  // 初始化时如果已登录则获取购物车
  if (authStore.isLoggedIn) {
    console.log('[Cart Store] 初始化时用户已登录，获取购物车')
    fetchCart()
  } else {
    console.log('[Cart Store] 初始化时用户未登录')
  }

  return {
    // 状态
    items,
    loading,

    // 计算属性
    totalCount,
    totalPrice,
    isEmpty,

    // 方法
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    fetchCart,
    initialize
  }
})