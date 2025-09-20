<template>
  <div class="profile">
    <div class="profile-header">
      <h1>个人中心</h1>
      <p>管理您的个人信息</p>
    </div>

    <div class="profile-content">
      <el-row :gutter="20">
        <el-col :xs="24" :md="8">
          <el-card class="profile-card">
            <div class="avatar-section">
              <el-avatar :size="80" :src="userInfo.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <h3>{{ userInfo.username }}</h3>
              <p>{{ getRoleText(userInfo.role) }}</p>
            </div>
            <div class="profile-stats">
              <div class="stat-item">
                <span class="label">注册时间</span>
                <span class="value">{{ userInfo.createdAt }}</span>
              </div>
              <div class="stat-item">
                <span class="label">最后登录</span>
                <span class="value">{{ userInfo.lastLogin }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="16">
          <el-card class="form-card">
            <template #header>
              <div class="card-header">
                <span>个人信息</span>
              </div>
            </template>

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="80px"
              @submit.prevent="handleUpdateProfile"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" />
              </el-form-item>

              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="profileForm.realName" />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="updating"
                  @click="handleUpdateProfile"
                >
                  更新信息
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-card class="password-card">
            <template #header>
              <div class="card-header">
                <span>修改密码</span>
              </div>
            </template>

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="80px"
              @submit.prevent="handleChangePassword"
            >
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="changingPassword"
                  @click="handleChangePassword"
                >
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User } from '@element-plus/icons-vue'

// 用户信息
const userInfo = reactive({
  username: 'admin',
  email: 'admin@example.com',
  phone: '13800138000',
  realName: '系统管理员',
  role: 1,
  avatar: '',
  createdAt: '2023-01-01',
  lastLogin: '2024-01-01 12:00:00'
})

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 加载状态
const updating = ref(false)
const changingPassword = ref(false)

// 个人信息表单
const profileForm = reactive({
  username: userInfo.username,
  email: userInfo.email,
  phone: userInfo.phone,
  realName: userInfo.realName
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const profileRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
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

// 获取角色文本
const getRoleText = (role: number) => {
  const roleMap = {
    1: '管理员',
    2: '接单员',
    3: '会员'
  }
  return roleMap[role as keyof typeof roleMap] || '未知角色'
}

// 更新个人信息
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    updating.value = true

    // TODO: 调用API更新个人信息
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('个人信息更新成功')
    Object.assign(userInfo, profileForm)
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    updating.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    // TODO: 调用API修改密码
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('密码修改成功')
    passwordFormRef.value?.resetFields()
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改失败，请重试')
  } finally {
    changingPassword.value = false
  }
}

// 页面加载时初始化数据
onMounted(() => {
  profileForm.username = userInfo.username
  profileForm.email = userInfo.email
  profileForm.phone = userInfo.phone
  profileForm.realName = userInfo.realName
})
</script>

<style scoped>
.profile {
  padding: 20px;
}

.profile-header {
  margin-bottom: 30px;
}

.profile-header h1 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 28px;
}

.profile-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.profile-card {
  margin-bottom: 20px;
}

.avatar-section {
  text-align: center;
  margin-bottom: 20px;
}

.avatar-section h3 {
  margin: 10px 0 5px 0;
  color: #333;
  font-size: 18px;
}

.avatar-section p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.profile-stats {
  padding: 0 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-item .label {
  color: #666;
  font-size: 14px;
}

.stat-item .value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.form-card,
.password-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>