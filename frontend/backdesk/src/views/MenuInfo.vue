<template>
  <div class="menu-info-container">
    <div class="page-header">
      <h2>菜单信息管理</h2>
    </div>
    
    <!-- 添加菜单表单 -->
    <div class="card">
      <h3>添加菜单信息</h3>
      <div class="form-container">
        <div class="form-group">
          <label class="form-label">类别选择：</label>
          <select v-model="menuForm.categoryId" class="form-select">
            <option value="">请选择系</option>
            <option v-for="category in categoryList" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </div>
        
        <div class="form-group">
          <label class="form-label">菜单名称：</label>
          <input 
            type="text" 
            v-model="menuForm.name" 
            class="form-input"
            placeholder="请输入菜名"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">菜单介绍：</label>
          <div class="editor-container">
            <div class="editor-toolbar">
              <button type="button" class="btn-tool" title="粗体">B</button>
              <button type="button" class="btn-tool" title="斜体">I</button>
              <button type="button" class="btn-tool" title="无序列表">📋</button>
              <button type="button" class="btn-tool" title="有序列表">📃</button>
              <button type="button" class="btn-tool" title="插入图片">🖼️</button>
              <button type="button" class="btn-tool" title="插入链接">🔗</button>
              <button type="button" class="btn-tool" title="撤销">↶</button>
            </div>
            <textarea 
              v-model="menuForm.description" 
              class="form-textarea-large"
              placeholder="请输入菜品介绍"
            ></textarea>
          </div>
        </div>
        
        <div class="form-group">
          <label class="form-label">上传图片：</label>
          <div class="upload-container">
            <input 
              type="file" 
              ref="fileInput"
              @change="handleFileUpload"
              accept="image/*"
              style="display: none"
            />
            <button @click="$refs.fileInput.click()" class="btn-upload">上传</button>
            <span class="upload-text" v-if="menuForm.image">{{ menuForm.image }}</span>
          </div>
        </div>
        
        <div class="form-group">
          <label class="form-label">菜单价格：</label>
          <input 
            type="number" 
            v-model="menuForm.price" 
            class="form-input"
            placeholder="100"
            min="0"
            step="0.01"
          />
        </div>
        
        <div class="form-buttons">
          <button @click="handleAddMenu" class="btn-primary">提交</button>
          <button @click="handleResetForm" class="btn-secondary">重置</button>
        </div>
      </div>
    </div>
    
    <!-- 菜单列表 -->
    <div class="card">
      <h3>菜单列表</h3>
      <table class="table">
        <thead>
          <tr>
            <th>序号</th>
            <th>名称</th>
            <th>图片</th>
            <th>价格</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(menu, index) in menuList" :key="menu.id">
            <td>{{ index + 1 }}</td>
            <td>{{ menu.name }}</td>
            <td>图片</td>
            <td>{{ menu.price }}</td>
            <td>
              <button @click="handleDeleteMenu(menu.id)" class="btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 添加菜单信息链接 -->
      <div class="add-menu-link">
        <a href="#" @click.prevent="scrollToForm">添加菜单信息</a>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'MenuInfo',
  setup() {
    const menuForm = reactive({
      categoryId: '',
      name: '',
      description: '',
      image: '',
      price: 100
    })
    
    const menuList = ref([])
    const categoryList = ref([])
    
    // 模拟菜单数据
    const mockMenus = [
      { id: 1, name: '培根鸡蛋蛋', price: 22, image: 'egg.jpg', categoryId: 1 },
      { id: 2, name: '巧师傅较量三十神', price: 23, image: 'noodle.jpg', categoryId: 2 },
      { id: 3, name: '盖浇饭三文鱼', price: 100, image: 'rice.jpg', categoryId: 3 },
      { id: 4, name: '意大利茄汁面', price: 11, image: 'pasta.jpg', categoryId: 4 },
      { id: 5, name: '奶油蘑菇大利面', price: 33, image: 'mushroom.jpg', categoryId: 4 },
      { id: 6, name: '奶油蘑菇花菜汤', price: 23, image: 'soup.jpg', categoryId: 1 },
      { id: 7, name: '新奥尔良烤鸡肉串', price: 22, image: 'chicken.jpg', categoryId: 2 },
      { id: 8, name: '什锦吐司披萨', price: 22, image: 'pizza.jpg', categoryId: 3 }
    ]
    
    // 模拟分类数据
    const mockCategories = [
      { id: 1, name: '推荐套餐' },
      { id: 2, name: '巧师傅茶餐厅' },
      { id: 3, name: '盖浇饭三文鱼' },
      { id: 4, name: '意大利茄汁面' }
    ]
    
    // 获取菜单列表
    const fetchMenuList = async () => {
      try {
        // TODO: 调用后端接口获取菜单列表
        // const response = await axios.get('/api/admin/menus')
        // menuList.value = response.data
        
        // 模拟数据
        menuList.value = [...mockMenus]
      } catch (error) {
        ElMessage.error('获取菜单列表失败')
        console.error('获取菜单列表错误:', error)
      }
    }
    
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
    
    // 文件上传处理
    const handleFileUpload = (event) => {
      const file = event.target.files[0]
      if (file) {
        // TODO: 调用后端接口上传文件
        // const formData = new FormData()
        // formData.append('file', file)
        // const response = await axios.post('/api/admin/upload', formData)
        // menuForm.image = response.data.filename
        
        // 模拟上传成功
        menuForm.image = file.name
        ElMessage.success('文件上传成功')
      }
    }
    
    // 添加菜单
    const handleAddMenu = async () => {
      if (!menuForm.categoryId || !menuForm.name || !menuForm.price) {
        ElMessage.error('请填写完整的菜单信息')
        return
      }
      
      try {
        // TODO: 调用后端接口添加菜单
        // await axios.post('/api/admin/menus', menuForm)
        
        // 模拟添加成功
        const newMenu = {
          id: Date.now(),
          ...menuForm,
          price: Number(menuForm.price)
        }
        menuList.value.push(newMenu)
        
        ElMessage.success('添加菜单成功')
        handleResetForm()
      } catch (error) {
        ElMessage.error('添加菜单失败')
        console.error('添加菜单错误:', error)
      }
    }
    
    // 重置表单
    const handleResetForm = () => {
      Object.assign(menuForm, {
        categoryId: '',
        name: '',
        description: '',
        image: '',
        price: 100
      })
    }
    
    // 删除菜单
    const handleDeleteMenu = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除该菜单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // TODO: 调用后端接口删除菜单
        // await axios.delete(`/api/admin/menus/${id}`)
        
        // 模拟删除成功
        menuList.value = menuList.value.filter(item => item.id !== id)
        ElMessage.success('删除菜单成功')
      } catch (error) {
        // 用户取消删除或删除失败
        if (error !== 'cancel') {
          ElMessage.error('删除菜单失败')
          console.error('删除菜单错误:', error)
        }
      }
    }
    
    // 滚动到表单
    const scrollToForm = () => {
      document.querySelector('.form-container').scrollIntoView({ behavior: 'smooth' })
    }
    
    onMounted(() => {
      fetchMenuList()
      fetchCategoryList()
    })
    
    return {
      menuForm,
      menuList,
      categoryList,
      handleFileUpload,
      handleAddMenu,
      handleResetForm,
      handleDeleteMenu,
      scrollToForm
    }
  }
}
</script>

<style scoped>
.menu-info-container {
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
  max-width: 800px;
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

.form-input, .form-select {
  flex: 1;
  max-width: 300px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.form-select {
  cursor: pointer;
}

.editor-container {
  flex: 1;
  max-width: 500px;
}

.editor-toolbar {
  display: flex;
  gap: 5px;
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f5f5;
  border: 1px solid #dcdfe6;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.btn-tool {
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 2px;
  cursor: pointer;
  font-size: 12px;
}

.btn-tool:hover {
  background: #e6e6e6;
}

.form-textarea-large {
  width: 100%;
  min-height: 120px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-top: none;
  border-radius: 0 0 4px 4px;
  resize: vertical;
  font-family: inherit;
}

.upload-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-upload {
  background: #909399;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-upload:hover {
  background: #a6a9ad;
}

.upload-text {
  color: #666;
  font-size: 14px;
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
  text-align: center;
  border-bottom: 1px solid #ebeef5;
}

.table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.table tr:hover {
  background-color: #f5f7fa;
}

.add-menu-link {
  margin-top: 20px;
  text-align: left;
}

.add-menu-link a {
  color: #409eff;
  text-decoration: none;
}

.add-menu-link a:hover {
  text-decoration: underline;
}
</style> 