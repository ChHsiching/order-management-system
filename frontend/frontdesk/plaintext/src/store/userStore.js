import { defineStore } from 'pinia'

// 会员状态管理（对应文档6.3.2节会员注册/登录功能）
export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '', // 登录Token（持久化到本地）
        userInfo: JSON.parse(localStorage.getItem('userInfo')) || {} // 会员信息（账号、姓名、电话等）
    }),
    actions: {
        // 登录：存储Token和会员信息
        login({ token, userInfo }) {
            this.token = token
            this.userInfo = userInfo
            // 持久化到localStorage，避免页面刷新后丢失
            localStorage.setItem('token', token)
            localStorage.setItem('userInfo', JSON.stringify(userInfo))
        },
        // 退出登录：清除Token和会员信息
        logout() {
            this.token = ''
            this.userInfo = {}
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
        },
        // 更新会员信息（如修改个人资料，文档未明确但预留扩展）
        updateUserInfo(newInfo) {
            this.userInfo = { ...this.userInfo, ...newInfo }
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        }
    }
})