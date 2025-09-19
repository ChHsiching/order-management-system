
<!--  前台主页面  -->

<template>
  <div class="home-container">
    <Navbar />
    <div class="main-content">
      <Sidebar
          :categories="categories"
          :activeCategoryId="activeCategoryId"
          @category-change="handleCategoryChange"
      />
      <div class="dish-content">
        <h2 class="content-title">{{ activeCategory.catename || '全部菜品' }}</h2>
        <div v-if="filteredDishes.length > 0" class="dish-list">
          <DishCard
              v-for="dish in filteredDishes"
              :key="dish.id"
              :dish="dish"
              @to-detail="handleToDetail"
          />
        </div>
        <div v-else class="empty-tip">
          暂无菜品
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../components/Navbar.vue'
import Sidebar from '../components/Sidebar.vue'
import DishCard from '../components/DishCard.vue'
import { getCategories, getDishes } from '../api/dishApi.js'

// 路由实例
const router = useRouter()

// 状态定义
const categories = ref([]) // 菜品分类列表
const dishes = ref([]) // 全部菜品列表
const activeCategoryId = ref('') // 当前选中分类ID
const activeCategory = computed(() => {
  return categories.value.find(cate => cate.id === activeCategoryId.value) || {}
})

// 筛选后的菜品（根据选中分类）
const filteredDishes = computed(() => {
  if (!activeCategoryId.value) return dishes.value
  return dishes.value.filter(dish => dish.cateid === activeCategoryId.value)
})

// 生命周期：初始化数据
onMounted(async () => {
  await Promise.all([
    fetchCategories(),
    fetchDishes()
  ])
})

// 获取菜品分类
const fetchCategories = async () => {
  const res = await getCategories()
  categories.value = res.data || []
  // 默认选中第一个分类
  if (categories.value.length > 0) {
    activeCategoryId.value = categories.value[0].id
  }
}

// 获取菜品列表
const fetchDishes = async () => {
  const res = await getDishes()
  dishes.value = res.data || []
}

// 切换分类
const handleCategoryChange = (categoryId) => {
  activeCategoryId.value = categoryId
}

// 跳转到菜品详情页
const handleToDetail = (dishId) => {
  router.push({ name: 'DishDetail', params: { id: dishId } })
}
</script>

<style scoped>
.home-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.main-content {
  display: flex;
  width: 1200px;
  margin: 20px auto;
  gap: 20px;
}

.dish-content {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.content-title {
  font-size: 18px;
  color: #333;
  border-bottom: 2px solid #ff6700;
  padding-bottom: 10px;
  margin-bottom: 20px;
}

.dish-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.empty-tip {
  text-align: center;
  padding: 50px 0;
  color: #999;
  font-size: 16px;
}
</style>