//用户相关接口
import request from '../utils/request.js'

// 会员登录
export const login = (data) => {
    return request({
        url: '/user/login',
        method: 'POST',
        data
    })
}

// 会员注册
export const register = (data) => {
    return request({
        url: '/user/register',
        method: 'POST',
        data
    })
}

// 获取当前用户信息
export const getUserInfo = () => {
    return request({
        url: '/user/info',
        method: 'GET'
    })
}