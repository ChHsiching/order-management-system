<template>
  <div class="menu-category-container">
    <div class="page-header">
      <h2>菜单类别管理</h2>
    </div>
    
    <!-- 添加分类表单 -->
    <div class="card">
      <h3>添加菜单分类</h3>
      <div class="form-container">
        <div class="form-group">
          <label class="form-label">分类名称：</label>
          <input 
            type="text" 
            v-model="categoryForm.name" 
            class="form-input"
            placeholder="请输入分类名称"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">排序号：</label>
          <input 
            type="number" 
            v-model="categoryForm.sort" 
            class="form-input"
            placeholder="请输入排序号"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">描述：</label>
          <textarea 
            v-model="categoryForm.description" 
            class="form-textarea"
            placeholder="请输入分类描述"
          ></textarea>
        </div>
        
        <div class="form-buttons">
          <button @click="handleAddCategory" class="btn-primary">添加</button>
          <button @click="handleResetForm" class="btn-secondary">重置</button>
        </div>
      </div>
    </div>
    
    <!-- 分类列表 -->
    <div class="card">
      <h3>分类列表</h3>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>分类名称</th>
            <th>排序号</th>
            <th>描述</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="category in categoryList" :key="category.id">
            <td>{{ category.id }}</td>
            <td>{{ category.name }}</td>
            <td>{{ category.sort }}</td>
            <td>{{ category.description }}</td>
            <td>{{ category.createTime }}</td>
            <td>
              <button @click="handleEditCategory(category)" class="btn-edit">编辑</button>
              <button @click="handleDeleteCategory(category.id)" class="btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 编辑分类对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑分类" 
      width="500px"
    >
      <div class="form-container">
        <div class="form-group">
          <label class="form-label">分类名称：</label>
          <input 
            type="text" 
            v-model="editForm.name" 
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">排序号：</label>
          <input 
            type="number" 
            v-model="editForm.sort" 
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">描述：</label>
          <textarea 
            v-model="editForm.description" 
            class="form-textarea"
          ></textarea>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <button @click="editDialogVisible = false" class="btn-secondary">取消</button>
          <button @click="handleUpdateCategory" class="btn-primary">确定</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'MenuCategory',
  setup() {
    const categoryForm = reactive({
      name: '',
      sort: 1,
      description: ''
    })
    
    const editForm = reactive({
      id: null,
      name: '',
      sort: 1,
      description: ''
    })
    
    const categoryList = ref([])
    const editDialogVisible = ref(false)
    
    // 模拟数据
    const mockCategories = [
      { id: 1, name: '推荐套餐', sort: 1, description: '精选推荐套餐', createTime: '2023-09-15 10:00:00' },
      { id: 2, name: '巧师傅茶餐厅', sort: 2, description: '茶餐厅美食', createTime: '2023-09-15 10:30:00' },
      { id: 3, name: '盖浇饭三文鱼', sort: 3, description: '盖浇饭系列', createTime: '2023-09-15 11:00:00' },
      { id: 4, name: '意大利茄汁面', sort: 4, description: '意式面食', createTime: '2023-09-15 11:30:00' }
    ]
    
    // 获取分类列表
    const fetchCategoryList = async () => {
      try {
        // TODO: 调用后端接口获取分类列表
        // const response = await axios.get('/api/admin/categories')
        // categoryList.value = response.data
        
        // 模拟数据
        categoryList.value = [...mockCategories]
      } catch (error) {
        ElMessage.error('获取分类列表失败')
        console.error('获取分类列表错误:', error)
      }
    }
    
    // 添加分类
    const handleAddCategory = async () => {
      if (!categoryForm.name) {
        ElMessage.error('请输入分类名称')
        return
      }
      
      try {
        // TODO: 调用后端接口添加分类
        // await axios.post('/api/admin/categories', categoryForm)
        
        // 模拟添加成功
        const newCategory = {
          id: Date.now(),
          ...categoryForm,
          createTime: new Date().toLocaleString()
        }
        categoryList.value.push(newCategory)
        
        ElMessage.success('添加分类成功')
        handleResetForm()
      } catch (error) {
        ElMessage.error('添加分类失败')
        console.error('添加分类错误:', error)
      }
    }
    
    // 重置表单
    const handleResetForm = () => {
      Object.assign(categoryForm, {
        name: '',
        sort: 1,
        description: ''
      })
    }
    
    // 编辑分类
    const handleEditCategory = (category) => {
      Object.assign(editForm, category)
      editDialogVisible.value = true
    }
    
    // 更新分类
    const handleUpdateCategory = async () => {
      try {
        // TODO: 调用后端接口更新分类
        // await axios.put(`/api/admin/categories/${editForm.id}`, editForm)
        
        // 模拟更新成功
        const index = categoryList.value.findIndex(item => item.id === editForm.id)
        if (index !== -1) {
          categoryList.value[index] = { ...editForm }
        }
        
        ElMessage.success('更新分类成功')
        editDialogVisible.value = false
      } catch (error) {
        ElMessage.error('更新分类失败')
        console.error('更新分类错误:', error)
      }
    }
    
    // 删除分类
    const handleDeleteCategory = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // TODO: 调用后端接口删除分类
        // await axios.delete(`/api/admin/categories/${id}`)
        
        // 模拟删除成功
        categoryList.value = categoryList.value.filter(item => item.id !== id)
        ElMessage.success('删除分类成功')
      } catch (error) {
        // 用户取消删除或删除失败
        if (error !== 'cancel') {
          ElMessage.error('删除分类失败')
          console.error('删除分类错误:', error)
        }
      }
    }
    
    onMounted(() => {
      fetchCategoryList()
    })
    
    return {
      categoryForm,
      editForm,
      categoryList,
      editDialogVisible,
      handleAddCategory,
      handleResetForm,
      handleEditCategory,
      handleUpdateCategory,
      handleDeleteCategory
    }
  }
}
</script>

<style scoped>
.menu-category-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  color: #333;
  font-size: 24px;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.card h3 {
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.form-container {
  max-width: 600px;
}

.form-group {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
}

.form-label {
  display: inline-block;
  width: 100px;
  text-align: right;
  margin-right: 10px;
  font-weight: bold;
  line-height: 32px;
}

.form-input {
  flex: 1;
  max-width: 300px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.form-textarea {
  flex: 1;
  max-width: 300px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: vertical;
  min-height: 80px;
}

.form-buttons {
  margin-top: 20px;
  text-align: left;
  padding-left: 110px;
}

.form-buttons button {
  margin-right: 10px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.table th,
.table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

.table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.table tr:hover {
  background-color: #f5f7fa;
}

.btn-edit {
  background-color: #e6a23c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 8px;
  font-size: 12px;
}

.btn-edit:hover {
  background-color: #ebb563;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer button {
  margin-left: 10px;
}
</style> 