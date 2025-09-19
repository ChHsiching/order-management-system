//购物车状态
import { defineStore } from 'pinia'
import { addCart, getCart, updateCart, deleteCart } from '../api/orderApi.js'

export const useCartStore = defineStore('cart', {
    state: () => ({
        cartList: [], // 购物车列表
        totalPrice: 0 // 购物车总金额
    }),
    actions: {
        // 初始化购物车（从后端获取）
        async initCart() {
            const res = await getCart()
            this.cartList = res.data || []
            this.calcTotalPrice()
        },
        // 计算总金额
        calcTotalPrice() {
            this.totalPrice = this.cartList.reduce(
                (sum, item) => sum + item.price * item.quantity,
                0
            )
        },
        // 添加商品到购物车
        async addToCart(dish, quantity = 1) {
            // 检查商品是否已在购物车
            const existingItem = this.cartList.find(item => item.dishId === dish.id)
            if (existingItem) {
                // 已存在则更新数量
                existingItem.quantity += quantity
                await updateCart(existingItem.dishId, existingItem.quantity)
            } else {
                // 新增商品
                const newItem = {
                    dishId: dish.id,
                    name: dish.name,
                    price: dish.price2, // 热销价
                    image: dish.imgpath,
                    quantity
                }
                await addCart(newItem)
                this.cartList.push(newItem)
            }
            this.calcTotalPrice()
        },
        // 更新商品数量
        async updateQuantity(dishId, quantity) {
            if (quantity < 1) return
            const item = this.cartList.find(item => item.dishId === dishId)
            if (item) {
                item.quantity = quantity
                await updateCart(dishId, quantity)
                this.calcTotalPrice()
            }
        },
        // 删除购物车商品
        async removeFromCart(dishId) {
            await deleteCart(dishId)
            this.cartList = this.cartList.filter(item => item.dishId !== dishId)
            this.calcTotalPrice()
        },
        // 清空购物车
        async clearCart() {
            for (const item of this.cartList) {
                await deleteCart(item.dishId)
            }
            this.cartList = []
            this.totalPrice = 0
        }
    }
})