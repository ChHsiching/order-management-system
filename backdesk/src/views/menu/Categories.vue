<template>
  <div class="categories-page">
    <div class="page-header">
      <h1>菜品分类</h1>
      <p>管理菜品分类信息</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-row :gutter="20" align="middle">
        <el-col :span="12">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索分类..."
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="12" style="text-align: right;">
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加分类
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 分类列表 -->
    <div class="categories-table">
      <el-table :data="filteredCategories" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="catename" label="分类名称" min-width="150" />
        <el-table-column prop="productname" label="描述" min-width="200" />
        <el-table-column label="菜品数量" width="100">
          <template #default="{ row }">
            <el-tag type="info">{{ row.menuCount || 0 }}</el-tag>
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
              @click="editCategory(row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleCategoryStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteCategory(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="catename">
          <el-input v-model="categoryForm.catename" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="productname">
          <el-input
            v-model="categoryForm.productname"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="categoryForm.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCategory">
          {{ editingCategory ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)
const saving = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 对话框显示
const showAddDialog = ref(false)

// 编辑中的分类
const editingCategory = ref<any>(null)

// 表单引用
const categoryFormRef = ref<FormInstance>()

// 分类列表
const categories = ref([
  {
    id: 1,
    catename: '汉堡',
    productname: '各种美味汉堡',
    menuCount: 8,
    status: 1,
    createdAt: '2023-01-01 10:00:00'
  },
  {
    id: 2,
    catename: '小食',
    productname: '薯条、鸡块等小食',
    menuCount: 12,
    status: 1,
    createdAt: '2023-01-01 10:00:00'
  },
  {
    id: 3,
    catename: '饮品',
    productname: '各种饮料',
    menuCount: 6,
    status: 1,
    createdAt: '2023-01-01 10:00:00'
  }
])

// 分类表单
const categoryForm = reactive({
  catename: '',
  productname: '',
  status: 1
})

// 表单验证规则
const categoryRules: FormRules = {
  catename: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '分类名称长度在2-20个字符', trigger: 'blur' }
  ],
  productname: [
    { required: true, message: '请输入分类描述', trigger: 'blur' }
  ]
}

// 过滤后的分类列表
const filteredCategories = computed(() => {
  if (!searchKeyword.value) return categories.value

  const keyword = searchKeyword.value.toLowerCase()
  return categories.value.filter(category =>
    category.catename.toLowerCase().includes(keyword) ||
    category.productname.toLowerCase().includes(keyword)
  )
})

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑由计算属性自动处理
}

// 编辑分类
const editCategory = (category: any) => {
  editingCategory.value = category
  Object.assign(categoryForm, category)
  showAddDialog.value = true
}

// 切换分类状态
const toggleCategoryStatus = async (category: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要${category.status === 1 ? '禁用' : '启用'}分类 "${category.catename}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API切换分类状态
    category.status = category.status === 1 ? 0 : 1

    ElMessage.success(`分类 "${category.catename}" 已${category.status === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换分类状态失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 删除分类
const deleteCategory = async (category: any) => {
  try {
    if (category.menuCount > 0) {
      await ElMessageBox.confirm(
        `分类 "${category.catename}" 下还有 ${category.menuCount} 个菜品，删除后这些菜品将变为无分类状态。`,
        '确认删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'error'
        }
      )
    } else {
      await ElMessageBox.confirm(
        `确定要删除分类 "${category.catename}" 吗？`,
        '确认删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'error'
        }
      )
    }

    // TODO: 调用API删除分类
    const index = categories.value.findIndex(c => c.id === category.id)
    if (index > -1) {
      categories.value.splice(index, 1)
    }

    ElMessage.success(`分类 "${category.catename}" 已删除`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 保存分类
const saveCategory = async () => {
  if (!categoryFormRef.value) return

  try {
    await categoryFormRef.value.validate()
    saving.value = true

    if (editingCategory.value) {
      // 编辑模式
      // TODO: 调用API更新分类
      const index = categories.value.findIndex(c => c.id === editingCategory.value.id)
      if (index > -1) {
        Object.assign(categories.value[index], categoryForm)
      }
      ElMessage.success('分类更新成功')
    } else {
      // 添加模式
      // TODO: 调用API添加分类
      const newCategory = {
        id: Date.now(),
        ...categoryForm,
        menuCount: 0,
        createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      categories.value.push(newCategory)
      ElMessage.success('分类添加成功')
    }

    showAddDialog.value = false
    resetForm()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  editingCategory.value = null
  Object.assign(categoryForm, {
    catename: '',
    productname: '',
    status: 1
  })
  categoryFormRef.value?.resetFields()
}

// 获取分类列表
const getCategories = async () => {
  loading.value = true
  try {
    // TODO: 从API获取实际分类列表
    await new Promise(resolve => setTimeout(resolve, 500))
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 监听对话框关闭事件
const handleDialogClose = () => {
  resetForm()
}

// 页面加载时获取数据
onMounted(() => {
  getCategories()
})
</script>

<style scoped>
.categories-page {
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

.categories-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}
</style>