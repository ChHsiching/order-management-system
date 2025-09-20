<template>
  <div class="menu-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜品管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增菜品
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="菜品名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入菜品名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.cateid" placeholder="请选择分类" clearable style="width: 150px">
              <el-option label="主食类" :value="1" />
              <el-option label="小吃类" :value="2" />
              <el-option label="饮品类" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.productlock" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="上架" :value="0" />
              <el-option label="下架" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="推荐">
            <el-select v-model="searchForm.newstuijian" placeholder="是否推荐" clearable style="width: 120px">
              <el-option label="推荐" :value="1" />
              <el-option label="不推荐" :value="0" />
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
        :data="menuList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.imgpath"
              :src="scope.row.imgpath"
              :preview-src-list="[scope.row.imgpath]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
            />
            <div v-else class="no-image">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜品名称" min-width="120" />
        <el-table-column prop="cateid" label="分类" width="100">
          <template #default="scope">
            <el-tag>{{ getCategoryName(scope.row.cateid) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price1" label="原价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price1 }}
          </template>
        </el-table-column>
        <el-table-column prop="price2" label="热销价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price2 }}
          </template>
        </el-table-column>
        <el-table-column prop="xiaoliang" label="销量" width="80" />
        <el-table-column prop="newstuijian" label="推荐" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.newstuijian === 1 ? 'success' : 'info'">
              {{ scope.row.newstuijian === 1 ? '推荐' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productlock" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.productlock === 0 ? 'success' : 'danger'">
              {{ scope.row.productlock === 0 ? '上架' : '下架' }}
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
              :type="scope.row.productlock === 0 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(scope.row)"
            >
              <el-icon><Switch /></el-icon>
              {{ scope.row.productlock === 0 ? '下架' : '上架' }}
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
      :title="dialogType === 'add' ? '新增菜品' : '编辑菜品'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="cateid">
          <el-select v-model="form.cateid" placeholder="请选择分类" style="width: 100%">
            <el-option label="主食类" :value="1" />
            <el-option label="小吃类" :value="2" />
            <el-option label="饮品类" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="原价" prop="price1">
          <el-input-number v-model="form.price1" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="热销价" prop="price2">
          <el-input-number v-model="form.price2" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图片" prop="imgpath">
          <el-upload
            class="avatar-uploader"
            action="/WebOrderSystem/api/upload"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
          >
            <img v-if="form.imgpath" :src="form.imgpath" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="简介" prop="info5">
          <el-input
            v-model="form.info5"
            type="textarea"
            :rows="3"
            placeholder="请输入菜品简介"
          />
        </el-form-item>
        <el-form-item label="推荐" prop="newstuijian">
          <el-radio-group v-model="form.newstuijian">
            <el-radio :label="1">推荐</el-radio>
            <el-radio :label="0">不推荐</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="productlock">
          <el-radio-group v-model="form.productlock">
            <el-radio :label="0">上架</el-radio>
            <el-radio :label="1">下架</el-radio>
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
import { Plus, Search, Refresh, Edit, Delete, Switch, Picture } from '@element-plus/icons-vue'
import { menuApi } from '@/utils/api'

// 数据状态
const loading = ref(false)
const menuList = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const submitLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  cateid: undefined,
  productlock: undefined,
  newstuijian: undefined
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
  name: '',
  cateid: undefined,
  price1: 0,
  price2: 0,
  imgpath: '',
  info5: '',
  newstuijian: 0,
  productlock: 0
})

// 表单引用
const formRef = ref<FormInstance>()

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '菜品名称长度应为 2-50 个字符', trigger: 'blur' }
  ],
  cateid: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  price1: [
    { required: true, message: '请输入原价', trigger: 'blur' }
  ],
  price2: [
    { required: true, message: '请输入热销价', trigger: 'blur' }
  ]
}

// 分类数据
const categories = ref([])

// 加载分类数据
const loadCategories = async () => {
  try {
    const response = await menuApi.getCategories()

    // 调试信息：检查响应格式
    if (process.env.NODE_ENV === 'development') {
      console.log('🍽️ 菜品分类加载格式:', Array.isArray(response) ? '数组' : '对象')
    }

    // 拦截器处理后可能返回直接数组或标准格式
    if (Array.isArray(response)) {
      categories.value = response
    } else if (response.code === 0 || response.code === 200) {
      categories.value = response.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载菜品列表
const loadMenuList = async () => {
  loading.value = true
  try {
    const response = await menuApi.getList()

    // 调试信息：检查响应格式
    if (process.env.NODE_ENV === 'development') {
      console.log('🍽️ 菜品管理响应格式:', Array.isArray(response) ? '数组' : '对象')
    }

    // 拦截器处理后可能返回直接数组或标准格式
    if (Array.isArray(response)) {
      menuList.value = response
      pagination.total = response.length
    } else if (response.code === 0 || response.code === 200) {
      menuList.value = response.data || []
      pagination.total = response.data?.length || 0
    } else {
      ElMessage.error(response.message || '获取菜品列表失败')
    }
  } catch (error) {
    console.error('加载菜品列表失败:', error)
    ElMessage.error('加载菜品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类名称
const getCategoryName = (cateid: number) => {
  const category = categories.value.find((c: any) => c.id === cateid)
  return category?.cateName || category?.catename || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadMenuList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    cateid: undefined,
    productlock: undefined,
    newstuijian: undefined
  })
  handleSearch()
}

// 新增菜品
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(form, {
    id: undefined,
    name: '',
    cateid: undefined,
    price1: 0,
    price2: 0,
    imgpath: '',
    info5: '',
    newstuijian: 0,
    productlock: 0
  })
  dialogVisible.value = true
}

// 编辑菜品
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除菜品
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜品"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await menuApi.delete(row.id)
    if (response.code === 0 || response.code === 200) {
      ElMessage.success('删除成功')
      loadMenuList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除菜品失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  try {
    const newStatus = row.productlock === 0 ? 1 : 0
    const statusText = newStatus === 0 ? '上架' : '下架'

    // 需要根据后端API格式调整
    const response = await menuApi.update(row.id, {
      ...row,
      productLock: newStatus
    })

    if (response.code === 0 || response.code === 200) {
      ElMessage.success(`${statusText}成功`)
      loadMenuList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 图片上传成功
const handleImageSuccess = (response: any) => {
  form.imgpath = response.data.url
}

// 图片上传前校验
const beforeImageUpload = (file: any) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitLoading.value = true

    // 构造提交数据，注意字段名映射
    const submitData = {
      name: form.name,
      categoryId: form.cateid,
      originalPrice: form.price1,
      hotPrice: form.price2,
      imgPath: form.imgpath,
      info: form.info5,
      isRecommend: form.newstuijian,
      productLock: form.productlock
    }

    if (dialogType.value === 'add') {
      const response = await menuApi.create(submitData)
      if (response.code === 0 || response.code === 200) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        loadMenuList()
      } else {
        ElMessage.error(response.message || '新增失败')
      }
    } else {
      const response = await menuApi.update(form.id, submitData)
      if (response.code === 0 || response.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadMenuList()
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
  loadMenuList()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadMenuList()
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return dateStr
}

// 页面加载时初始化
onMounted(() => {
  loadCategories()
  loadMenuList()
})
</script>

<style scoped>
.menu-management {
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

.no-image {
  width: 60px;
  height: 60px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c8c8c;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  object-fit: cover;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>