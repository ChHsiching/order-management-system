<template>
  <div class="menu-page">
    <div class="page-header">
      <h1>菜单浏览</h1>
      <p>选择您喜欢的美食</p>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-section">
      <el-row :gutter="20" align="middle">
        <el-col :span="4">
          <span class="filter-label">分类：</span>
        </el-col>
        <el-col :span="20">
          <el-radio-group v-model="selectedCategory" @change="handleCategoryChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button
              v-for="category in categories"
              :key="category.id"
              :label="category.id"
            >
              {{ category.catename || category.cateName }}
            </el-radio-button>
          </el-radio-group>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索框 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索菜品..."
        :prefix-icon="Search"
        clearable
        @input="handleSearch"
      />
    </div>

    <!-- 菜品列表 -->
    <div v-loading="loading" class="menu-grid">
      <el-row :gutter="20">
        <el-col
          v-for="menu in filteredMenus"
          :key="menu.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card
            class="menu-card"
            :body-style="{ padding: '0px' }"
            @click="$router.push(`/menu/${menu.id}`)"
          >
            <div class="menu-image">
              <img :src="menu.imgpath || menu.imgPath || '/images/default-food.jpg'" :alt="menu.name" />
              <el-tag v-if="menu.newstuijian || menu.isRecommend" type="danger" size="small" class="recommend-tag">
                推荐
              </el-tag>
            </div>
            <div class="menu-info">
              <h3>{{ menu.name }}</h3>
              <p class="description">{{ menu.info5 || menu.info }}</p>
              <div class="price-row">
                <span class="price">¥{{ (menu.price2 || menu.hotPrice || menu.price1)?.toFixed(2) }}</span>
                <span v-if="menu.price2 || menu.hotPrice" class="original-price">¥{{ (menu.price1 || menu.originalPrice)?.toFixed(2) }}</span>
              </div>
              <div class="action-row">
                <span class="sales">销量: {{ menu.xiaoliang || menu.sales || 0 }}</span>
                <el-button
                  type="primary"
                  size="small"
                  @click.stop="addToCart(menu)"
                >
                  加入购物车
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && filteredMenus.length === 0"
        description="暂无相关菜品"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { menuApi } from '@/utils/api'
import { useCartStore } from '@/stores/cart'

// 购物车store
const cartStore = useCartStore()
const route = useRoute()

// 加载状态
const loading = ref(false)

// 数据
const categories = ref<any[]>([])
const menus = ref<any[]>([])

// 分类筛选
const selectedCategory = ref<number | string>('')
const searchKeyword = ref('')

// 过滤后的菜品列表
const filteredMenus = computed(() => {
  let result = menus.value

  // 分类筛选 - 如果选择了分类，则menus已经是分类后的数据，无需再次筛选
  // 但如果没有选择分类（显示全部），则可以进行搜索筛选

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(menu =>
      menu.name.toLowerCase().includes(keyword) ||
      (menu.info && menu.info.toLowerCase().includes(keyword))
    )
  }

  return result
})

// 获取菜品分类
const getCategories = async () => {
  try {
    const response: any = await menuApi.getCategories()
    console.log('Menu页面分类响应:', response)

    // 兼容多种后端响应格式
    if (response.code === 200 || response.code === 0) {
      categories.value = response.data || []
    } else if (Array.isArray(response)) {
      categories.value = response
    } else if (response && typeof response === 'object') {
      categories.value = response.data || response.list || []
    } else {
      categories.value = []
    }

    console.log('Menu页面处理后的分类:', categories.value)
  } catch (error) {
    console.error('获取分类失败:', error)
    categories.value = []
  }
}

// 获取菜品列表
const getMenus = async () => {
  loading.value = true
  try {
    const response: any = await menuApi.getMenus()
    console.log('Menu页面菜品响应:', response)

    // 兼容多种后端响应格式 - 现在response是完整response对象
    const data = response.data || response
    if (data.code === 200 || data.code === 0) {
      menus.value = data.data || []
    } else if (Array.isArray(data)) {
      menus.value = data
    } else if (data && typeof data === 'object') {
      menus.value = data.data || data.list || []
    } else {
      menus.value = []
    }

    console.log('Menu页面处理后的菜品:', menus.value)
  } catch (error) {
    console.error('获取菜品失败:', error)
    menus.value = []
  } finally {
    loading.value = false
  }
}

// 分类变化处理
const handleCategoryChange = async () => {
  loading.value = true
  try {
    if (selectedCategory.value) {
      // 根据分类ID获取菜品 - 确保转换为数字类型
      const categoryId = Number(selectedCategory.value)
      console.log('分类筛选 - 选择的分类ID:', selectedCategory.value, '转换后:', categoryId)

      const response: any = await menuApi.getMenusByCategory(categoryId)
      console.log('分类筛选响应:', response)

      // 处理不同的响应格式 - 现在response是完整response对象
      const data = response.data || response
      if (data.code === 200 || data.code === 0) {
        menus.value = data.data || []
      } else if (Array.isArray(data)) {
        menus.value = data
      } else if (data && typeof data === 'object') {
        menus.value = data.data || data.list || []
      } else {
        menus.value = []
      }
    } else {
      // 如果没有选择分类，获取所有菜品
      await getMenus()
    }

    console.log('分类筛选后的菜品:', menus.value)
  } catch (error) {
    console.error('分类筛选失败:', error)
    ElMessage.error('获取分类菜品失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索时重新筛选 - 通过computed属性自动处理
}

// 添加到购物车
const addToCart = async (menu: any) => {
  const success = await cartStore.addToCart(menu.id, 1, menu)
  if (success) {
    ElMessage.success(`已添加 ${menu.name} 到购物车`)
  }
}

// 页面加载时获取数据
onMounted(() => {
  getCategories()

  // 检查URL参数中是否有分类ID
  const categoryId = route.query.category
  if (categoryId) {
    console.log('URL参数中发现分类ID:', categoryId)
    selectedCategory.value = Number(categoryId)
    // 延迟一下确保分类数据已加载
    setTimeout(() => {
      handleCategoryChange()
    }, 100)
  } else {
    getMenus()
  }
})
</script>

<style scoped>
.menu-page {
  padding: 20px 0;
}

.page-header {
  text-align: center;
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

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-label {
  font-weight: bold;
  color: #333;
}

.search-section {
  margin-bottom: 20px;
  max-width: 400px;
}

.menu-grid {
  min-height: 200px;
}

.menu-card {
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.menu-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.menu-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.menu-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.menu-card:hover .menu-image img {
  transform: scale(1.05);
}

.recommend-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.menu-info {
  padding: 15px;
}

.menu-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.description {
  color: #666;
  font-size: 14px;
  margin: 0 0 10px 0;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.price {
  color: #e63946;
  font-size: 18px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
}

.action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sales {
  color: #666;
  font-size: 12px;
}
</style>