<template>
  <div class="menu-category">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜品分类管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="分类名称">
            <el-input
              v-model="searchForm.cateName"
              placeholder="请输入分类名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.cateLock" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="启用" :value="0" />
              <el-option label="禁用" :value="1" />
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
        :data="categoryList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="catename" label="分类名称" min-width="120" />
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="productname" label="关联菜品" min-width="120" />
        <el-table-column prop="catelock" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.catelock === 0 ? 'success' : 'danger'">
              {{ scope.row.catelock === 0 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createtime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
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
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
            <el-button
              :type="scope.row.catelock === 0 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(scope.row)"
            >
              <el-icon><Switch /></el-icon>
              {{ scope.row.catelock === 0 ? '禁用' : '启用' }}
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
      :title="dialogType === 'add' ? '新增分类' : '编辑分类'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="catename">
          <el-input v-model="form.catename" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="关联菜品" prop="productname">
          <el-input v-model="form.productname" placeholder="请输入关联菜品名称" />
        </el-form-item>
        <el-form-item label="状态" prop="catelock">
          <el-radio-group v-model="form.catelock">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">禁用</el-radio>
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
import { menuCategoryApi } from '@/utils/api'

// 数据状态
const loading = ref(false)
const categoryList = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const submitLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  cateName: '',
  cateLock: undefined
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
  catename: '',
  address: '',
  productname: '',
  catelock: 0
})

// 表单引用
const formRef = ref<FormInstance>()

// 表单验证规则
const rules: FormRules = {
  catename: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度应为 2-50 个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' }
  ],
  catelock: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载分类列表
const loadCategoryList = async () => {
  loading.value = true
  try {
    const response = await menuCategoryApi.getList()

    // 调试信息：检查响应格式
    if (process.env.NODE_ENV === 'development') {
      console.log('🍽️ 菜单分类响应格式:', Array.isArray(response) ? '数组' : '对象')
    }

    // 拦截器处理后可能返回直接数组或标准格式
    if (Array.isArray(response)) {
      categoryList.value = response
      pagination.total = response.length
    } else if (response.code === 0 || response.code === 200) {
      categoryList.value = response.data || []
      pagination.total = response.data?.length || 0
    } else {
      ElMessage.error(response.message || '获取分类列表失败')
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadCategoryList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    cateName: '',
    cateLock: undefined
  })
  handleSearch()
}

// 新增分类
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(form, {
    id: undefined,
    catename: '',
    address: '',
    productname: '',
    catelock: 0
  })
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除分类
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.catename}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await menuCategoryApi.delete(row.id)
    if (response.code === 0 || response.code === 200) {
      ElMessage.success('删除成功')
      loadCategoryList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  try {
    const newStatus = row.catelock === 0 ? 1 : 0
    const statusText = newStatus === 0 ? '启用' : '禁用'

    const response = await menuCategoryApi.update(row.id, {
      ...row,
      catelock: newStatus
    })

    if (response.code === 0 || response.code === 200) {
      ElMessage.success(`${statusText}成功`)
      loadCategoryList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitLoading.value = true

    if (dialogType.value === 'add') {
      const response = await menuCategoryApi.create(form)
      if (response.code === 0 || response.code === 200) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        loadCategoryList()
      } else {
        ElMessage.error(response.message || '新增失败')
      }
    } else {
      const response = await menuCategoryApi.update(form.id, form)
      if (response.code === 0 || response.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadCategoryList()
      } else {
        ElMessage.error(response.message || '更新失败')
      }
    }
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
  loadCategoryList()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadCategoryList()
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return dateStr
}

// 页面加载时初始化
onMounted(() => {
  loadCategoryList()
})
</script>

<style scoped>
.menu-category {
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