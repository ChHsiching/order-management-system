
<!--  用户注册/登录页  -->

<template>
  <div class="auth-container">
    <div class="auth-card">
      <!-- 切换标签 -->
      <div class="tab-header">
        <div
            class="tab-item"
            :class="{ active: isLogin }"
            @click="isLogin = true"
        >
          会员登录
        </div>
        <div
            class="tab-item"
            :class="{ active: !isLogin }"
            @click="isLogin = false"
        >
          会员注册
        </div>
      </div>

      <!-- 登录表单 -->
      <form class="auth-form" v-if="isLogin" @submit.prevent="handleLogin">
        <div class="form-group">
          <label>账号</label>
          <input
              type="text"
              v-model.trim="loginForm.username"
              placeholder="请输入账号"
              @blur="validateField('username', 'login')"
          >
          <div class="error-tip" v-if="loginErrors.username">{{ loginErrors.username }}</div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
              type="password"
              v-model.trim="loginForm.password"
              placeholder="请输入密码"
              @blur="validateField('password', 'login')"
          >
          <div class="error-tip" v-if="loginErrors.password">{{ loginErrors.password }}</div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-submit" :disabled="isSubmitting">
            {{ isSubmitting ? "登录中..." : "登录" }}
          </button>
          <button type="button" class="btn-reset" @click="resetForm('login')">重置</button>
        </div>
      </form>

      <!-- 注册表单 -->
      <form class="auth-form" v-else @submit.prevent="handleRegister">
        <div class="form-group">
          <label>账号</label>
          <input
              type="text"
              v-model.trim="registerForm.username"
              placeholder="请设置账号"
              @blur="validateField('username', 'register')"
          >
          <div class="error-tip" v-if="registerErrors.username">{{ registerErrors.username }}</div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
              type="password"
              v-model.trim="registerForm.password"
              placeholder="请设置密码"
              @blur="validateField('password', 'register')"
          >
          <div class="error-tip" v-if="registerErrors.password">{{ registerErrors.password }}</div>
        </div>

        <div class="form-group">
          <label>姓名</label>
          <input
              type="text"
              v-model.trim="registerForm.name"
              placeholder="请输入姓名"
              @blur="validateField('name', 'register')"
          >
          <div class="error-tip" v-if="registerErrors.name">{{ registerErrors.name }}</div>
        </div>

        <div class="form-group">
          <label>性别</label>
          <div class="radio-group">
            <label>
              <input type="radio" value="男" v-model="registerForm.gender"> 男
            </label>
            <label>
              <input type="radio" value="女" v-model="registerForm.gender"> 女
            </label>
          </div>
          <div class="error-tip" v-if="registerErrors.gender">{{ registerErrors.gender }}</div>
        </div>

        <div class="form-group">
          <label>年龄</label>
          <input
              type="number"
              v-model.number="registerForm.age"
              placeholder="请输入年龄"
              @blur="validateField('age', 'register')"
          >
          <div class="error-tip" v-if="registerErrors.age">{{ registerErrors.age }}</div>
        </div>

        <div class="form-group">
          <label>住址</label>
          <input
              type="text"
              v-model.trim="registerForm.address"
              placeholder="请输入住址"
              @blur="validateField('address', 'register')"
          >
          <div class="error-tip" v-if="registerErrors.address">{{ registerErrors.address }}</div>
        </div>

        <div class="form-group">
          <label>电话</label>
          <input
              type="text"
              v-model.trim="registerForm.phone"
              placeholder="请输入手机号"
              @blur="validateField('phone', 'register')"
          >
          <div class="error-tip" v-if="registerErrors.phone">{{ registerErrors.phone }}</div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-submit" :disabled="isSubmitting">
            {{ isSubmitting ? "注册中..." : "注册" }}
          </button>
          <button type="button" class="btn-reset" @click="resetForm('register')">重置</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/userStore.js'
import { login, register } from '../api/userApi.js'
import { validateUsername, validatePassword, validatePhone, validateRequired } from '../utils/validator.js'

// 路由实例
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 状态定义
const isLogin = ref(true) // true:登录表单, false:注册表单
const isSubmitting = ref(false) // 提交状态

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 登录表单错误信息
const loginErrors = reactive({
  username: '',
  password: ''
})

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  name: '',
  gender: '男',
  age: '',
  address: '',
  phone: ''
})

// 注册表单错误信息
const registerErrors = reactive({
  username: '',
  password: '',
  name: '',
  gender: '',
  age: '',
  address: '',
  phone: ''
})

// 表单字段校验
const validateField = (field, formType) => {
  const form = formType === 'login' ? loginForm : registerForm
  const errors = formType === 'login' ? loginErrors : registerErrors

  switch (field) {
    case 'username':
      errors.username = validateUsername(form.username)
      break
    case 'password':
      errors.password = validatePassword(form.password)
      break
    case 'name':
      errors.name = validateRequired(form.name, '姓名')
      break
    case 'gender':
      errors.gender = validateRequired(form.gender, '性别')
      break
    case 'age':
      if (!form.age) {
        errors.age = '年龄不能为空'
      } else if (form.age < 1 || form.age > 120) {
        errors.age = '年龄必须在1-120之间'
      } else {
        errors.age = ''
      }
      break
    case 'address':
      errors.address = validateRequired(form.address, '住址')
      break
    case 'phone':
      errors.phone = validatePhone(form.phone)
      break
    default:
      break
  }
}

// 整体表单校验
const validateForm = (formType) => {
  const fields = formType === 'login'
      ? ['username', 'password']
      : ['username', 'password', 'name', 'gender', 'age', 'address', 'phone']

  let isValid = true
  fields.forEach(field => {
    validateField(field, formType)
    const errors = formType === 'login' ? loginErrors : registerErrors
    if (errors[field]) isValid = false
  })
  return isValid
}

// 重置表单
const resetForm = (formType) => {
  if (formType === 'login') {
    loginForm.username = ''
    loginForm.password = ''
    Object.keys(loginErrors).forEach(key => loginErrors[key] = '')
  } else {
    registerForm.username = ''
    registerForm.password = ''
    registerForm.name = ''
    registerForm.gender = '男'
    registerForm.age = ''
    registerForm.address = ''
    registerForm.phone = ''
    Object.keys(registerErrors).forEach(key => registerErrors[key] = '')
  }
}

// 处理登录
const handleLogin = async () => {
  if (!validateForm('login')) return

  try {
    isSubmitting.value = true
    const res = await login(loginForm)
    // 存储用户信息和Token
    userStore.login({
      token: res.token,
      userInfo: res.user
    })
    // 跳转回之前页面或首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    alert(error.message || '登录失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  if (!validateForm('register')) return

  try {
    isSubmitting.value = true
    await register(registerForm)
    alert('注册成功，请登录')
    isLogin.value = true // 切换到登录表单
    // 自动填充账号
    loginForm.username = registerForm.username
  } catch (error) {
    alert(error.message || '注册失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.auth-card {
  width: 400px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.1);
  overflow: hidden;
}

.tab-header {
  display: flex;
  border-bottom: 1px solid #eee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 15px 0;
  font-size: 16px;
  color: #666;
  cursor: pointer;
}

.tab-item.active {
  color: #ff6700;
  border-bottom: 2px solid #ff6700;
  font-weight: 500;
}

.auth-form {
  padding: 25px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group input:focus {
  outline: none;
  border-color: #ff6700;
}

.radio-group {
  display: flex;
  gap: 20px;
  padding: 5px 0;
}

.error-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #ff4d4f;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.btn-submit {
  flex: 1;
  padding: 12px 0;
  background-color: #ff6700;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-submit:disabled {
  background-color: #ffb88a;
  cursor: not-allowed;
}

.btn-reset {
  flex: 1;
  padding: 12px 0;
  background-color: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}
</style>