<template>
  <div class="menu-management">
    <div class="page-header">
      <h1>菜品管理</h1>
      <p>管理菜品信息和库存</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-row :gutter="20" align="middle">
        <el-col :span="12">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索菜品..."
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedCategory" placeholder="选择分类" clearable @change="handleCategoryChange">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.catename"
              :value="category.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6" style="text-align: right;">
          <el-button type="primary" @click="$router.push('/admin/menu/create')">
            <el-icon><Plus /></el-icon>
            添加菜品
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 菜品列表 -->
    <div class="menu-table">
      <el-table :data="filteredMenus" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image
              :src="row.imgpath || '/images/default-food.jpg'"
              :preview-src-list="[row.imgpath]"
              fit="cover"
              style="width: 50px; height: 50px; border-radius: 4px;"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜品名称" min-width="120" />
        <el-table-column prop="catename" label="分类" width="100" />
        <el-table-column prop="price1" label="原价" width="80">
          <template #default="{ row }">
            ¥{{ row.price1?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="price2" label="售价" width="80">
          <template #default="{ row }">
            <span class="sale-price">¥{{ row.price2?.toFixed(2) || row.price1?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="xiaoliang" label="销量" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '在售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.newstuijian" type="danger">推荐</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="$router.push(`/admin/menu/${row.id}/edit`)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleMenuStatus(row)"
            >
              {{ row.status === 1 ? '停售' : '在售' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteMenu(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Picture } from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 选中的分类
const selectedCategory = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 分类列表
const categories = ref([
  { id: 1, catename: '汉堡' },
  { id: 2, catename: '小食' },
  { id: 3, catename: '饮品' }
])

// 菜品列表
const menus = ref([
  {
    id: 1,
    name: '巨无霸汉堡',
    catename: '汉堡',
    price1: 30.00,
    price2: 25.00,
    xiaoliang: 456,
    status: 1,
    newstuijian: true,
    imgpath: '/images/burger.jpg'
  },
  {
    id: 2,
    name: '薯条',
    catename: '小食',
    price1: 12.00,
    price2: null,
    xiaoliang: 389,
    status: 1,
    newstuijian: false,
    imgpath: '/images/fries.jpg'
  },
  {
    id: 3,
    name: '可乐',
    catename: '饮品',
    price1: 8.00,
    price2: null,
    xiaoliang: 367,
    status: 1,
    newstuijian: false,
    imgpath: '/images/cola.jpg'
  }
])

// 过滤后的菜品列表
const filteredMenus = computed(() => {
  let result = menus.value

  // 分类筛选
  if (selectedCategory.value) {
    result = result.filter(menu => (menu as any).cateid === selectedCategory.value)
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(menu =>
      menu.name.toLowerCase().includes(keyword) ||
      menu.catename.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadMenus()
}

// 分类变化处理
const handleCategoryChange = () => {
  currentPage.value = 1
  loadMenus()
}

// 切换菜品状态
const toggleMenuStatus = async (menu: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要${menu.status === 1 ? '停售' : '在售'}菜品 "${menu.name}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API切换菜品状态
    menu.status = menu.status === 1 ? 0 : 1

    ElMessage.success(`菜品 "${menu.name}" 已${menu.status === 1 ? '在售' : '停售'}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换菜品状态失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 删除菜品
const deleteMenu = async (menu: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜品 "${menu.name}" 吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    // TODO: 调用API删除菜品
    const index = menus.value.findIndex(m => m.id === menu.id)
    if (index > -1) {
      menus.value.splice(index, 1)
    }

    ElMessage.success(`菜品 "${menu.name}" 已删除`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除菜品失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadMenus()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadMenus()
}

// 获取菜品列表
const getMenus = async () => {
  loading.value = true
  try {
    // TODO: 从API获取实际菜品列表
    await new Promise(resolve => setTimeout(resolve, 500))

    // 模拟总数
    total.value = filteredMenus.value.length
  } catch (error) {
    console.error('获取菜品列表失败:', error)
    ElMessage.error('获取菜品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const getCategories = async () => {
  try {
    // TODO: 从API获取实际分类列表
    await new Promise(resolve => setTimeout(resolve, 300))
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 加载数据
const loadMenus = async () => {
  await getMenus()
}

// 页面加载时获取数据
onMounted(async () => {
  await getCategories()
  await getMenus()
})
</script>

<style scoped>
.menu-management {
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

.menu-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.image-placeholder {
  width: 50px;
  height: 50px;
  background: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.sale-price {
  color: #e63946;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>