//订单相关接口
import request from '../utils/request.js'

// 获取购物车列表
export const getCart = () => {
    return request({
        url: '/cart/list',
        method: 'GET'
    })
}

// 添加商品到购物车
export const addCart = (data) => {
    return request({
        url: '/cart/add',
        method: 'POST',
        data
    })
}

// 更新购物车商品数量
export const updateCart = (dishId, quantity) => {
    return request({
        url: '/cart/update',
        method: 'POST',
        data: { dishId, quantity }
    })
}

// 删除购物车商品
export const deleteCart = (dishId) => {
    return request({
        url: `/cart/delete/${dishId}`,
        method: 'DELETE'
    })
}

// 提交订单
export const submitOrder = (data) => {
    return request({
        url: '/order/submit',
        method: 'POST',
        data
    })
}