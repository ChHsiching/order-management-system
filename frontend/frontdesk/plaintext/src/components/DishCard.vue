npm run dev
<!--  菜品卡片组件  -->

<template>
  <div class="dish-card" @click="$emit('to-detail', dish.id)">
    <div class="dish-card-img">
      <img :src="dish.imgpath" :alt="dish.name" />
      <div class="dish-tag" v-if="dish.newstuijian === 1">推荐</div>
    </div>
    <div class="dish-card-info">
      <h3 class="dish-card-name">{{ dish.name }}</h3>
      <div class="dish-card-price">
        <span class="price-hot">¥{{ dish.price2 }}</span>
        <span class="price-original" v-if="dish.price1 > dish.price2">¥{{ dish.price1 }}</span>
      </div>
      <div class="dish-card-sales">
        已售 {{ dish.xiaoliang }} 份
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  dish: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

defineEmits(['to-detail'])
</script>

<style scoped>
.dish-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
}

.dish-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.dish-card-img {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.dish-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.dish-card:hover .dish-card-img img {
  transform: scale(1.05);
}

.dish-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #ff4d4f;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.dish-card-info {
  padding: 15px;
}

.dish-card-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dish-card-price {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.price-hot {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: bold;
}

.price-original {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.dish-card-sales {
  font-size: 12px;
  color: #999;
}
</style>