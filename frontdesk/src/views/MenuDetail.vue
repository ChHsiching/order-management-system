<template>
  <div class="placeholder-page">
    <el-result
      icon="info"
      title="页面开发中"
      sub-title="此功能正在开发中，敬请期待"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
// 占位页面
</script>

<style scoped>
.placeholder-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>