<template>
  <div class="home">
    <!-- 轮播图 -->
    <el-carousel :interval="4000" type="card" height="400px" class="banner">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <div class="banner-content" :style="{ backgroundImage: `url(${item.image})` }">
          <div class="banner-text">
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 推荐菜品 -->
    <div class="section">
      <div class="section-header">
        <h2>推荐菜品</h2>
        <el-button text @click="$router.push('/menu')">查看更多</el-button>
      </div>

      <div v-loading="loading" class="menu-grid">
        <el-row :gutter="20">
          <el-col
            v-for="menu in recommendedMenus"
            :key="menu.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-card
              class="menu-card"
              :body-style="{ padding: '0px' }"
              @click="$router.push('/menu')"
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
      </div>
    </div>

    <!-- 菜品分类 -->
    <div class="section">
      <div class="section-header">
        <h2>菜品分类</h2>
      </div>

      <div v-loading="loadingCategories" class="category-grid">
        <el-row :gutter="20">
          <el-col
            v-for="category in categories"
            :key="category.id"
            :xs="12"
            :sm="8"
            :md="6"
            :lg="4"
          >
            <el-card
              class="category-card"
              @click="$router.push(`/menu?category=${category.id}`)"
            >
              <div class="category-content">
                <el-icon><Food /></el-icon>
                <h3>{{ category.catename || category.cateName }}</h3>
                <p>{{ category.productname || category.productName }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 系统特色 -->
    <div class="section features">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <div class="feature-item">
            <el-icon size="48" color="#409eff"><Timer /></el-icon>
            <h3>快速配送</h3>
            <p>30分钟内送达，新鲜美味</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="feature-item">
            <el-icon size="48" color="#67c23a"><Check /></el-icon>
            <h3>品质保证</h3>
            <p>严格品控，食材新鲜安全</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="feature-item">
            <el-icon size="48" color="#e6a23c"><Star /></el-icon>
            <h3>优质服务</h3>
            <p>24小时客服，贴心服务</p>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Food, Timer, Check, Star } from '@element-plus/icons-vue'
import { menuApi } from '@/utils/api'
import { useCartStore } from '@/stores/cart'

// 购物车store
const cartStore = useCartStore()

// 加载状态
const loading = ref(false)
const loadingCategories = ref(false)

// 轮播图数据
const banners = ref([
  {
    id: 1,
    title: '美味汉堡',
    description: '经典汉堡，限时优惠',
    image: '/images/burger-banner.jpg'
  },
  {
    id: 2,
    title: '健康沙拉',
    description: '新鲜蔬菜，健康搭配',
    image: '/images/salad-banner.jpg'
  },
  {
    id: 3,
    title: '特饮系列',
    description: '清爽饮品，夏日必备',
    image: '/images/drink-banner.jpg'
  }
])

// 推荐菜品
const recommendedMenus = ref<any[]>([])

// 菜品分类
const categories = ref<any[]>([])

// 获取推荐菜品
const getRecommendedMenus = async () => {
  loading.value = true
  try {
    const response: any = await menuApi.getRecommendedMenus()
    console.log('推荐菜品响应:', response)

    // 兼容多种后端响应格式
    if (response.code === 200 || response.code === 0) {
      recommendedMenus.value = response.data || []
    } else if (Array.isArray(response)) {
      // 后端直接返回数组
      recommendedMenus.value = response
    } else if (response && typeof response === 'object') {
      // 其他格式，尝试提取数据
      recommendedMenus.value = response.data || response.list || []
    } else {
      recommendedMenus.value = []
    }

    console.log('处理后的推荐菜品:', recommendedMenus.value)
  } catch (error) {
    console.error('获取推荐菜品失败:', error)
    recommendedMenus.value = []
  } finally {
    loading.value = false
  }
}

// 获取菜品分类
const getCategories = async () => {
  loadingCategories.value = true
  try {
    const response: any = await menuApi.getCategories()
    console.log('分类响应:', response)

    // 兼容多种后端响应格式
    if (response.code === 200 || response.code === 0) {
      categories.value = response.data || []
    } else if (Array.isArray(response)) {
      // 后端直接返回数组
      categories.value = response
    } else if (response && typeof response === 'object') {
      // 其他格式，尝试提取数据
      categories.value = response.data || response.list || []
    } else {
      categories.value = []
    }

    console.log('处理后的分类:', categories.value)
  } catch (error) {
    console.error('获取分类失败:', error)
    categories.value = []
  } finally {
    loadingCategories.value = false
  }
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
  getRecommendedMenus()
  getCategories()
})
</script>

<style scoped>
.home {
  padding: 20px 0;
}

.banner {
  margin-bottom: 40px;
}

.banner-content {
  height: 100%;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  display: flex;
  align-items: flex-end;
  position: relative;
}

.banner-content::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
}

.banner-text {
  position: relative;
  z-index: 1;
  color: white;
  padding: 30px;
}

.banner-text h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
}

.banner-text p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
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

.category-grid {
  min-height: 150px;
}

.category-card {
  cursor: pointer;
  text-align: center;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.category-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.category-content {
  padding: 20px;
}

.category-content .el-icon {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 10px;
}

.category-content h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.category-content p {
  margin: 0;
  color: #666;
  font-size: 12px;
}

.features {
  background: #f8f9fa;
  padding: 40px 20px;
  border-radius: 8px;
}

.feature-item {
  text-align: center;
  padding: 20px;
}

.feature-item h3 {
  margin: 15px 0 10px 0;
  color: #333;
}

.feature-item p {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>