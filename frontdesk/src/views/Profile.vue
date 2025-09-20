<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理您的个人信息和账户设置</p>
    </div>

    <!-- 个人信息卡片 -->
    <el-card class="profile-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-button type="primary" size="small" @click="startEdit" v-if="!isEditing">
            编辑
          </el-button>
        </div>
      </template>

      <!-- 查看模式 -->
      <div v-if="!isEditing" class="profile-view">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">
            {{ userInfo.username }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userInfo.email || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ userInfo.phone || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="userInfo.role === 1 ? 'success' : 'primary'">
              {{ userInfo.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDate(userInfo.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 编辑模式 -->
      <div v-else class="profile-edit">
        <el-form
          ref="formRef"
          :model="editForm"
          :rules="rules"
          label-width="80px"
          size="large"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username" disabled />
            <div class="help-text">用户名不可修改</div>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="editForm.email"
              placeholder="请输入邮箱地址"
              type="email"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="editForm.phone"
              placeholder="请输入手机号码"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item>
            <el-space>
              <el-button type="primary" @click="saveProfile" :loading="saving">
                保存
              </el-button>
              <el-button @click="cancelEdit">
                取消
              </el-button>
            </el-space>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 修改密码卡片 -->
    <el-card class="password-card" v-loading="passwordLoading">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="changePassword"
            :loading="changingPassword"
          >
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 账户操作 -->
    <el-card class="actions-card">
      <template #header>
        <span>账户操作</span>
      </template>

      <div class="actions-section">
        <el-button
          type="danger"
          plain
          @click="handleLogout"
          :loading="logoutLoading"
        >
          退出登录
        </el-button>

        <div class="help-text">
          退出登录将清除您的登录状态，需要重新登录才能访问
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/utils/api'

const router = useRouter()
const authStore = useAuthStore()

// 状态管理
const loading = ref(false)
const saving = ref(false)
const passwordLoading = ref(false)
const changingPassword = ref(false)
const logoutLoading = ref(false)
const isEditing = ref(false)

// 表单引用
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 用户信息
const userInfo = ref<any>({})

// 编辑表单
const editForm = reactive({
  username: '',
  email: '',
  phone: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules: FormRules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20位之间', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    console.log('[Profile] ===== 开始获取用户信息 =====')

    // 等待一下确保store已经初始化
    await new Promise(resolve => setTimeout(resolve, 100))

    console.log('[Profile] Auth store 完整状态:', {
      authStore: authStore,
      isLoggedIn: authStore.isLoggedIn,
      username: authStore.username,
      user: authStore.user,
      token: authStore.token,
      hasToken: !!authStore.token,
      tokenType: typeof authStore.token,
      userType: typeof authStore.user
    })

    if (!authStore.isLoggedIn || !authStore.token) {
      console.log('[Profile] 用户未登录，跳转到登录页')
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    const response: any = await authApi.getCurrentUser()
    console.log('[Profile] 获取用户信息响应:', response)
    console.log('[Profile] 响应状态:', response?.status)
    console.log('[Profile] 响应数据:', response?.data)

    // 处理不同的响应格式 - 现在response是完整response对象
    const data = response.data || response
    console.log('[Profile] 处理后的数据:', data)

    if (response?.status === 200) {
      if (data.code === 200 || data.success === true || typeof data.username === 'string') {
        userInfo.value = data.data || data
        console.log('[Profile] 用户信息设置成功:', userInfo.value)

        // 初始化编辑表单
        editForm.username = userInfo.value.username
        editForm.email = userInfo.value.email || ''
        editForm.phone = userInfo.value.phone || ''
        console.log('[Profile] 编辑表单初始化完成')
      } else {
        console.error('[Profile] 响应数据格式异常:', data)
        ElMessage.error(data.message || response.message || '获取用户信息失败：数据格式错误')
      }
    } else {
      console.error('[Profile] HTTP请求失败:', response?.status)
      ElMessage.error(`获取用户信息失败：HTTP ${response?.status}`)
    }
  } catch (error: any) {
    console.error('[Profile] 获取用户信息异常:', error)
    console.error('[Profile] 错误详情:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      config: error.config
    })

    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      authStore.logout()
      router.push('/login')
    } else if (error.response?.status === 403) {
      ElMessage.error('权限不足')
    } else {
      ElMessage.error('获取用户信息失败: ' + (error.message || '网络错误'))
    }
  } finally {
    loading.value = false
  }
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
  // 重置表单数据
  editForm.username = userInfo.value.username
  editForm.email = userInfo.value.email || ''
  editForm.phone = userInfo.value.phone || ''
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  formRef.value?.resetFields()
}

// 保存个人信息
const saveProfile = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      console.log('[Profile] 开始保存个人信息...')
      console.log('[Profile] 请求数据:', editForm)

      const response: any = await authApi.updateProfile(editForm)
      console.log('[Profile] 更新个人信息响应:', response)
      console.log('[Profile] 响应状态:', response?.status)
      console.log('[Profile] 响应数据:', response?.data)

      // 处理不同的响应格式 - 现在response是完整response对象
      const data = response.data || response
      console.log('[Profile] 处理后的数据:', data)

      if (response?.status === 200) {
        console.log('[Profile] HTTP状态码200，检查业务状态...')
        if (data.code === 200 || data.success === true || typeof data.username === 'string') {
          console.log('[Profile] 业务状态成功，更新本地用户信息...')
          // 更新本地用户信息
          userInfo.value = { ...userInfo.value, ...editForm }
          // 更新store中的用户信息
          authStore.user = { ...authStore.user, ...editForm }

          console.log('[Profile] 用户信息更新完成:', userInfo.value)
          ElMessage.success('个人信息更新成功')
          isEditing.value = false
        } else {
          console.error('[Profile] 业务状态失败:', data)
          ElMessage.error(data.message || response.message || '更新失败：业务状态错误')
        }
      } else {
        console.error('[Profile] HTTP请求失败:', response?.status)
        ElMessage.error(`更新失败：HTTP ${response?.status}`)
      }
    } catch (error: any) {
      console.error('[Profile] 更新个人信息异常:', error)
      console.error('[Profile] 错误详情:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        config: error.config
      })

      if (error.response?.status === 400) {
        ElMessage.error('请求参数错误: ' + (error.response?.data?.message || error.message))
      } else if (error.response?.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        authStore.logout()
        router.push('/login')
      } else if (error.response?.status === 403) {
        ElMessage.error('权限不足')
      } else {
        ElMessage.error('更新失败: ' + error.message)
      }
    } finally {
      saving.value = false
    }
  })
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    changingPassword.value = true
    try {
      console.log('[Profile] 开始修改密码...')
      console.log('[Profile] 请求数据:', {
        oldPassword: '***',
        newPassword: '***'
      })

      const response: any = await authApi.changePassword(
        passwordForm.oldPassword,
        passwordForm.newPassword
      )
      console.log('[Profile] 修改密码响应:', response)
      console.log('[Profile] 响应状态:', response?.status)
      console.log('[Profile] 响应数据:', response?.data)

      // 处理不同的响应格式 - 现在response是完整response对象
      const data = response.data || response
      console.log('[Profile] 处理后的数据:', data)

      if (response?.status === 200) {
        console.log('[Profile] HTTP状态码200，检查业务状态...')
        if (data.code === 200 || data.success === true || data.message === '密码修改成功') {
          console.log('[Profile] 密码修改成功')
          ElMessage.success('密码修改成功，请重新登录')
          // 清空表单
          passwordFormRef.value?.resetFields()
          // 退出登录
          await handleLogout()
        } else {
          console.error('[Profile] 业务状态失败:', data)
          ElMessage.error(data.message || response.message || '密码修改失败：业务状态错误')
        }
      } else if (response?.status === 403) {
        console.error('[Profile] 原密码错误，403状态')
        ElMessage.error('原密码错误，请重新输入')
      } else {
        console.error('[Profile] HTTP请求失败:', response?.status)
        ElMessage.error(`修改失败：HTTP ${response?.status}`)
      }
    } catch (error: any) {
      console.error('[Profile] 修改密码异常:', error)
      console.error('[Profile] 错误详情:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        config: error.config
      })

      if (error.response?.status === 400) {
        ElMessage.error('请求参数错误: ' + (error.response?.data?.message || error.message))
      } else if (error.response?.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        authStore.logout()
        router.push('/login')
      } else if (error.response?.status === 403) {
        ElMessage.error('原密码错误，请重新输入')
      } else {
        ElMessage.error('修改失败: ' + error.message)
      }
    } finally {
      changingPassword.value = false
    }
  })
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    logoutLoading.value = true
    // 清除本地认证信息
    authStore.logout()

    ElMessage.success('已退出登录')
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  } finally {
    logoutLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'

  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch (error) {
    return dateString
  }
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 20px 0;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 28px;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.profile-card,
.password-card,
.actions-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.help-text {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.profile-view {
  padding: 10px 0;
}

.profile-edit {
  padding: 10px 0;
}

.actions-section {
  text-align: center;
}

.actions-section .help-text {
  margin-top: 12px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-page {
    padding: 10px 0;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .page-header p {
    font-size: 14px;
  }
}
</style>