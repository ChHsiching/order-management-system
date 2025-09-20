<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input
              v-model="searchForm.phone"
              placeholder="请输入手机号"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input
              v-model="searchForm.email"
              placeholder="请输入邮箱"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 120px">
              <el-option label="会员" :value="0" />
              <el-option label="管理员" :value="1" />
              <el-option label="接单员" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
      >
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="qq" label="QQ" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(scope.row)"
              :disabled="scope.row.role === 1"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
            <el-button
              :type="scope.row.role === 0 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleChangeRole(scope.row)"
              :disabled="scope.row.role === 1"
            >
              <el-icon><Switch /></el-icon>
              {{ scope.row.role === 0 ? '设为管理员' : '设为会员' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="QQ" prop="qq">
          <el-input v-model="form.qq" placeholder="请输入QQ" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio :label="0">会员</el-radio>
            <el-radio :label="1">管理员</el-radio>
            <el-radio :label="2">接单员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Switch } from '@element-plus/icons-vue'
import { userApi } from '@/utils/api'

// 数据状态
const loading = ref(false)
const userList = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const submitLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  phone: '',
  role: undefined,
  email: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const form = reactive({
  id: undefined,
  username: '',
  password: '',
  phone: '',
  email: '',
  qq: '',
  role: 0
})

// 表单引用
const formRef = ref<FormInstance>()

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: dialogType.value === 'add', message: '请输入密码', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const params = {
      username: searchForm.username || undefined,
      phone: searchForm.phone || undefined,
      role: searchForm.role,
      email: searchForm.email || undefined
    }

    const response = await userApi.getList(params)

    // 现在拦截器直接返回数组数据
    let users = Array.isArray(response) ? response : (response.data || [])

    // 应用搜索过滤
    if (searchForm.username) {
      users = users.filter(user => user.username.toLowerCase().includes(searchForm.username.toLowerCase()))
    }
    if (searchForm.phone) {
      users = users.filter(user => user.phone && user.phone.includes(searchForm.phone))
    }
    if (searchForm.role !== undefined) {
      users = users.filter(user => user.role === searchForm.role)
    }

    // 分页处理
    const start = (pagination.page - 1) * pagination.size
    const end = start + pagination.size
    userList.value = users.slice(start, end)
    pagination.total = users.length
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
    userList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    phone: '',
    role: undefined,
    email: ''
  })
  handleSearch()
}

// 新增用户
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '',
    phone: '',
    email: '',
    qq: '',
    role: 0
  })
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  form.password = '' // 编辑时不显示密码
  dialogVisible.value = true
}

// 删除用户
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.username}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await userApi.delete(row.username)
    if (response.code === 0 || response.code === 200) {
      ElMessage.success('删除成功')
      loadUserList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 切换角色
const handleChangeRole = async (row: any) => {
  try {
    const newRole = row.role === 0 ? 1 : 0
    const roleText = newRole === 1 ? '管理员' : '会员'

    const response = await userApi.updateRole(row.username, newRole)
    if (response.code === 0 || response.code === 200) {
      ElMessage.success(`设置为${roleText}成功`)
      loadUserList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('切换角色失败:', error)
    ElMessage.error('操作失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitLoading.value = true

    let response
    if (dialogType.value === 'add') {
      response = await userApi.create(form)
      if (response.code === 0 || response.code === 200) {
        ElMessage.success('新增成功')
      } else {
        ElMessage.error(response.message || '新增失败')
        return
      }
    } else {
      response = await userApi.update(form.username, form)
      if (response.code === 0 || response.code === 200) {
        ElMessage.success('更新成功')
      } else {
        ElMessage.error(response.message || '更新失败')
        return
      }
    }

    dialogVisible.value = false
    loadUserList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 分页相关
const handleSizeChange = (val: number) => {
  pagination.size = val
  loadUserList()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadUserList()
}

// 角色显示相关函数
const getRoleType = (role: number) => {
  switch (role) {
    case 0: return 'info'
    case 1: return 'danger'
    case 2: return 'warning'
    default: return 'info'
  }
}

const getRoleText = (role: number) => {
  switch (role) {
    case 0: return '会员'
    case 1: return '管理员'
    case 2: return '接单员'
    default: return '未知'
  }
}

// 日期格式化
const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 页面加载时初始化
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>