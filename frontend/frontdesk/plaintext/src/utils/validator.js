//表单校验工具
/**
 * 验证必填项
 * @param {string} value - 输入值
 * @param {string} fieldName - 字段名称
 * @returns {string} 错误信息（空表示验证通过）
 */
export const validateRequired = (value, fieldName) => {
    if (!value || value.trim() === '') {
        return `${fieldName}不能为空`
    }
    return ''
}

/**
 * 验证用户名（4-20位字母/数字）
 * @param {string} username - 用户名
 * @returns {string} 错误信息
 */
export const validateUsername = (username) => {
    if (!username) {
        return '账号不能为空'
    }
    const reg = /^[a-zA-Z0-9]{4,20}$/
    if (!reg.test(username)) {
        return '账号必须为4-20位字母或数字'
    }
    return ''
}

/**
 * 验证密码（6-20位，包含字母和数字）
 * @param {string} password - 密码
 * @returns {string} 错误信息
 */
export const validatePassword = (password) => {
    if (!password) {
        return '密码不能为空'
    }
    if (password.length < 6 || password.length > 20) {
        return '密码必须为6-20位'
    }
    const hasLetter = /[a-zA-Z]/.test(password)
    const hasNumber = /\d/.test(password)
    if (!hasLetter || !hasNumber) {
        return '密码必须包含字母和数字'
    }
    return ''
}

/**
 * 验证手机号
 * @param {string} phone - 手机号
 * @returns {string} 错误信息
 */
export const validatePhone = (phone) => {
    if (!phone) {
        return '手机号不能为空'
    }
    const reg = /^1[3-9]\d{9}$/
    if (!reg.test(phone)) {
        return '请输入正确的手机号'
    }
    return ''
}