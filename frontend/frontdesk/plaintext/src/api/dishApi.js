//菜品相关接口
import request from '../utils/request.js'

// 获取菜品分类列表
export const getCategories = () => {
    return request({
        url: '/dish/categories',
        method: 'GET'
    })
}

// 获取菜品列表（支持分类筛选）
export const getDishes = (categoryId = '') => {
    return request({
        url: '/dish/list',
        method: 'GET',
        params: { categoryId }
    })
}

// 根据ID获取菜品详情
export const getDishById = (dishId) => {
    return request({
        url: `/dish/${dishId}`,
        method: 'GET'
    })
}