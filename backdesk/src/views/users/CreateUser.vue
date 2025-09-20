<template>
  <div class="create-user">
    <div class="page-header">
      <el-button @click="$router.go(-1)" :icon="ArrowLeft">返回</el-button>
      <h1>创建用户</h1>
      <p>添加新的系统用户</p>
    </div>

    <div class="form-container">
      <el-card>
        <el-form
          ref="userFormRef"
          :model="userForm"
          :rules="userRules"
          label-width="100px"
          @submit.prevent="createUser"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="userForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="userForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="userForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              :prefix-icon="Lock"
              show-password
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

          <el-form-item>
            <el-button type="primary" :loading="creating" @click="createUser">
              创建用户
            </el-button>
            <el-button @click="$router.go(-1)">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, User, Lock, Message, Phone, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()

// 表单引用
const userFormRef = ref<FormInstance>()

// 加载状态
const creating = ref(false)

// 用户表单
const userForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: '',
  email: '',
  phone: '',
  realName: ''
})

// 表单验证规则
const userRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
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

// 创建用户
const createUser = async () => {
  if (!userFormRef.value) return

  try {
    await userFormRef.value.validate()
    creating.value = true

    // TODO: 调用API创建用户
    await new Promise(resolve => setTimeout(resolve, 1500))

    ElMessage.success('用户创建成功')
    router.push('/admin/users')
  } catch (error) {
    console.error('创建用户失败:', error)
    ElMessage.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.create-user {
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