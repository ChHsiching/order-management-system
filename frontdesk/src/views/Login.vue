<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>用户登录</h2>
          <p>欢迎来到Web订餐系统</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        size="large"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="authStore.loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-footer">
        <p>
          还没有账号？
          <el-link type="primary" @click="$router.push('/register')">
            立即注册
          </el-link>
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  console.log('[Login] ===== 开始登录流程 =====')
  console.log('[Login] 表单数据:', loginForm)
  console.log('[Login] authStore状态:', {
    loading: authStore.loading,
    isLoggedIn: authStore.isLoggedIn
  })

  if (!loginFormRef.value) {
    console.log('[Login] loginFormRef为空，返回')
    return
  }

  try {
    // 验证表单
    console.log('[Login] 验证表单...')
    await loginFormRef.value.validate()
    console.log('[Login] 表单验证通过')

    // 调用登录接口
    console.log('[Login] 调用authStore.login...')
    const success = await authStore.login(loginForm.username, loginForm.password)
    console.log('[Login] 登录结果:', success)

    if (success) {
      console.log('[Login] 登录成功，准备跳转')
      ElMessage.success('登录成功')
      // 跳转到首页或之前要访问的页面
      const redirect = router.currentRoute.value.query.redirect as string
      console.log('[Login] 跳转到:', redirect || '/')
      router.push(redirect || '/')
    } else {
      console.log('[Login] 登录失败，success=false')
    }
  } catch (error: any) {
    console.error('[Login] 登录异常:', error)
    console.error('[Login] 异常详情:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    })
  }
}
</script>

<style scoped>
.login-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #f5f5f5;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
}

.card-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
}

.form-footer p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

:deep(.el-card__header) {
  padding: 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

:deep(.el-card__body) {
  padding: 24px;
}
</style>