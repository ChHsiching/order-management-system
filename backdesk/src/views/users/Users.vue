<template>
  <div class="users-page">
    <div class="page-header">
      <h1>用户管理</h1>
      <p>管理系统用户和权限</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-row :gutter="20" align="middle">
        <el-col :span="12">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户..."
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="12" style="text-align: right;">
          <el-button type="primary" @click="$router.push('/admin/users/create')">
            <el-icon><Plus /></el-icon>
            创建用户
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 用户列表 -->
    <div class="users-table">
      <el-table :data="filteredUsers" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="100" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="$router.push(`/admin/users/${row.username}/edit`)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleUserStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteUser(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 用户列表
const users = ref([
  {
    username: 'admin',
    email: 'admin@example.com',
    phone: '13800138000',
    realName: '系统管理员',
    role: 1,
    status: 1,
    createdAt: '2023-01-01 10:00:00'
  },
  {
    username: 'staff1',
    email: 'staff1@example.com',
    phone: '13800138001',
    realName: '接单员1',
    role: 2,
    status: 1,
    createdAt: '2023-01-02 10:00:00'
  },
  {
    username: 'member1',
    email: 'member1@example.com',
    phone: '13800138002',
    realName: '会员1',
    role: 3,
    status: 1,
    createdAt: '2023-01-03 10:00:00'
  }
])

// 过滤后的用户列表
const filteredUsers = computed(() => {
  if (!searchKeyword.value) return users.value

  const keyword = searchKeyword.value.toLowerCase()
  return users.value.filter(user =>
    user.username.toLowerCase().includes(keyword) ||
    user.email.toLowerCase().includes(keyword) ||
    user.phone.toLowerCase().includes(keyword) ||
    user.realName.toLowerCase().includes(keyword)
  )
})

// 获取角色文本
const getRoleText = (role: number) => {
  const roleMap = {
    1: '管理员',
    2: '接单员',
    3: '会员'
  }
  return roleMap[role as keyof typeof roleMap] || '未知'
}

// 获取角色标签类型
const getRoleTagType = (role: number) => {
  const typeMap = {
    1: 'danger',
    2: 'warning',
    3: 'success'
  }
  return typeMap[role as keyof typeof typeMap] || 'info'
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑由计算属性自动处理
}

// 切换用户状态
const toggleUserStatus = async (user: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要${user.status === 1 ? '禁用' : '启用'}用户 ${user.username} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API切换用户状态
    user.status = user.status === 1 ? 0 : 1

    ElMessage.success(`用户 ${user.username} 已${user.status === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换用户状态失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 删除用户
const deleteUser = async (user: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.username} 吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error',
        inputPattern: new RegExp(`^${user.username}$`),
        inputPlaceholder: `请输入用户名 ${user.username} 确认删除`,
        inputValidator: (value: string) => {
          if (value !== user.username) {
            return '输入的用户名不正确'
          }
          return true
        },
        showInput: true
      }
    )

    // TODO: 调用API删除用户
    const index = users.value.findIndex(u => u.username === user.username)
    if (index > -1) {
      users.value.splice(index, 1)
    }

    ElMessage.success(`用户 ${user.username} 已删除`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 获取用户列表
const getUsers = async () => {
  loading.value = true
  try {
    // TODO: 从API获取实际用户列表
    await new Promise(resolve => setTimeout(resolve, 500))
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  getUsers()
})
</script>

<style scoped>
.users-page {
  padding: 20px;
}

.page-header {
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

.action-bar {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.users-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}
</style>