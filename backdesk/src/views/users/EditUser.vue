<template>
  <div class="edit-user">
    <div class="page-header">
      <el-button @click="$router.go(-1)" :icon="ArrowLeft">返回</el-button>
      <h1>编辑用户</h1>
      <p>修改用户信息</p>
    </div>

    <div class="form-container">
      <el-card>
        <el-form
          ref="userFormRef"
          :model="userForm"
          :rules="userRules"
          label-width="100px"
          @submit.prevent="updateUser"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="userForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              disabled
            />
          </el-form-item>

          <el-form-item label="角色" prop="role">
            <el-select v-model="userForm.role" placeholder="请选择角色">
              <el-option label="管理员" :value="1" />
              <el-option label="接单员" :value="2" />
              <el-option label="会员" :value="3" />
            </el-select>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="userForm.email"
              placeholder="请输入邮箱"
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="userForm.phone"
              placeholder="请输入手机号"
              :prefix-icon="Phone"
            />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="userForm.realName"
              placeholder="请输入真实姓名"
              :prefix-icon="UserFilled"
            />
          </el-form-item>

          <el-form-item label="状态" prop="status">
            <el-switch
              v-model="userForm.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="updating" @click="updateUser">
              更新用户
            </el-button>
            <el-button @click="$router.go(-1)">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, User, Message, Phone, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const username = route.params.username as string

// 表单引用
const userFormRef = ref<FormInstance>()

// 加载状态
const updating = ref(false)

// 用户表单
const userForm = reactive({
  username: '',
  role: '',
  email: '',
  phone: '',
  realName: '',
  status: 1
})

// 表单验证规则
const userRules: FormRules = {
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    // TODO: 从API获取实际用户信息
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 模拟用户数据
    Object.assign(userForm, {
      username: username,
      role: 1,
      email: 'admin@example.com',
      phone: '13800138000',
      realName: '系统管理员',
      status: 1
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 更新用户
const updateUser = async () => {
  if (!userFormRef.value) return

  try {
    await userFormRef.value.validate()
    updating.value = true

    // TODO: 调用API更新用户
    await new Promise(resolve => setTimeout(resolve, 1500))

    ElMessage.success('用户信息更新成功')
    router.push('/admin/users')
  } catch (error) {
    console.error('更新用户失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    updating.value = false
  }
}

// 页面加载时获取用户信息
onMounted(() => {
  getUserInfo()
})
</script>

<style scoped>
.edit-user {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.form-container {
  max-width: 600px;
  margin: 0 auto;
}
</style>