
<!--  左侧分类栏  -->

<template>
  <div class="sidebar">
    <!-- 菜单分类 -->
    <div class="sidebar-section">
      <h3 class="section-title">菜单类别</h3>
      <ul class="category-list">
        <li
            class="category-item"
            :class="{ active: !activeCategoryId }"
            @click="$emit('category-change', '')"
        >
          全部菜品
        </li>
        <li
            class="category-item"
            v-for="category in categories"
            :key="category.id"
            :class="{ active: category.id === activeCategoryId }"
            @click="$emit('category-change', category.id)"
        >
          {{ category.catename }}
        </li>
      </ul>
    </div>

    <!-- 联系我们 -->
    <div class="sidebar-section contact-section">
      <h3 class="section-title">联系我们</h3>
      <div class="contact-info">
        <p>地址: 北京市大厦1256</p>
        <p>电话: 13512345678</p>
      </div>
    </div>

    <!-- 订餐活动 -->
    <div class="sidebar-section activity-section">
      <h3 class="section-title">网上订餐活动</h3>
      <div class="activity-info">
        <p>本站于2017年04月25日正式上线</p>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  },
  activeCategoryId: {
    type: [String, Number],
    default: ''
  }
})

defineEmits(['category-change'])
</script>

<style scoped>
.sidebar {
  width: 220px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
}

.sidebar-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.category-list {
  list-style: none;
}

.category-item {
  padding: 8px 0;
  color: #666;
  cursor: pointer;
  font-size: 14px;
}

.category-item:hover {
  color: #ff6700;
}

.category-item.active {
  color: #ff6700;
  font-weight: 500;
  background-color: #fff8f0;
  padding-left: 8px;
  border-left: 2px solid #ff6700;
}

.contact-info, .activity-info {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.contact-section, .activity-section {
  margin-top: 20px;
}
</style>