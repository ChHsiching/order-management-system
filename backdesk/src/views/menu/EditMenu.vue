<template>
  <div class="edit-menu">
    <div class="page-header">
      <el-button @click="$router.go(-1)" :icon="ArrowLeft">返回</el-button>
      <h1>编辑菜品</h1>
      <p>修改菜品信息</p>
    </div>

    <div class="form-container">
      <el-card>
        <el-form
          ref="menuFormRef"
          :model="menuForm"
          :rules="menuRules"
          label-width="100px"
          @submit.prevent="updateMenu"
        >
          <el-row :gutter="20">
            <el-col :xs="24" :lg="12">
              <el-form-item label="菜品名称" prop="name">
                <el-input
                  v-model="menuForm.name"
                  placeholder="请输入菜品名称"
                  :prefix-icon="Food"
                />
              </el-form-item>

              <el-form-item label="分类" prop="cateid">
                <el-select v-model="menuForm.cateid" placeholder="请选择分类">
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.catename"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="原价" prop="price1">
                <el-input-number
                  v-model="menuForm.price1"
                  :min="0"
                  :precision="2"
                  :step="0.1"
                  placeholder="0.00"
                  style="width: 100%;"
                />
              </el-form-item>

              <el-form-item label="售价" prop="price2">
                <el-input-number
                  v-model="menuForm.price2"
                  :min="0"
                  :precision="2"
                  :step="0.1"
                  placeholder="0.00 (留空表示无折扣)"
                  style="width: 100%;"
                />
              </el-form-item>

              <el-form-item label="库存" prop="kucun">
                <el-input-number
                  v-model="menuForm.kucun"
                  :min="0"
                  :precision="0"
                  placeholder="0"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :lg="12">
              <el-form-item label="菜品图片" prop="imgpath">
                <el-upload
                  class="menu-image-uploader"
                  action="/api/upload"
                  :show-file-list="false"
                  :on-success="handleImageSuccess"
                  :before-upload="beforeImageUpload"
                >
                  <img
                    v-if="menuForm.imgpath"
                    :src="menuForm.imgpath"
                    class="menu-image"
                  />
                  <el-icon v-else class="menu-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>

              <el-form-item label="推荐" prop="newstuijian">
                <el-switch
                  v-model="menuForm.newstuijian"
                  active-text="推荐"
                  inactive-text="不推荐"
                />
              </el-form-item>

              <el-form-item label="状态" prop="status">
                <el-switch
                  v-model="menuForm.status"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="在售"
                  inactive-text="停售"
                />
              </el-form-item>

              <el-form-item label="排序" prop="sort">
                <el-input-number
                  v-model="menuForm.sort"
                  :min="0"
                  :max="999"
                  placeholder="0"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="描述" prop="info5">
            <el-input
              v-model="menuForm.info5"
              type="textarea"
              :rows="3"
              placeholder="请输入菜品描述"
            />
          </el-form-item>

          <el-form-item label="详细介绍" prop="info1">
            <el-input
              v-model="menuForm.info1"
              type="textarea"
              :rows="4"
              placeholder="请输入详细介绍"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="updating" @click="updateMenu">
              更新菜品
            </el-button>
            <el-button @click="$router.go(-1)">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import { ArrowLeft, Food, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const menuId = route.params.id as string

// 表单引用
const menuFormRef = ref<FormInstance>()

// 加载状态
const updating = ref(false)

// 分类列表
const categories = ref([
  { id: 1, catename: '汉堡' },
  { id: 2, catename: '小食' },
  { id: 3, catename: '饮品' }
])

// 菜品表单
const menuForm = reactive({
  name: '',
  cateid: '',
  price1: 0,
  price2: null,
  kucun: 999,
  imgpath: '',
  newstuijian: false,
  status: 1,
  sort: 0,
  info5: '',
  info1: ''
})

// 表单验证规则
const menuRules: FormRules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '菜品名称长度在2-50个字符', trigger: 'blur' }
  ],
  cateid: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  price1: [
    { required: true, message: '请输入原价', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ],
  price2: [
    { type: 'number', min: 0, message: '售价不能小于0', trigger: 'blur' }
  ],
  info5: [
    { required: true, message: '请输入菜品描述', trigger: 'blur' }
  ]
}

// 图片上传成功
const handleImageSuccess: UploadProps['onSuccess'] = (response) => {
  menuForm.imgpath = response.url
  ElMessage.success('图片上传成功')
}

// 图片上传前验证
const beforeImageUpload: UploadProps['beforeUpload'] = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('图片只能是 JPG/PNG/GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 获取菜品信息
const getMenuInfo = async () => {
  try {
    // TODO: 从API获取实际菜品信息
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 模拟菜品数据
    Object.assign(menuForm, {
      name: '巨无霸汉堡',
      cateid: 1,
      price1: 30.00,
      price2: 25.00,
      kucun: 50,
      imgpath: '/images/burger.jpg',
      newstuijian: true,
      status: 1,
      sort: 1,
      info5: '美味可口的巨无霸汉堡，包含牛肉饼、生菜、番茄等',
      info1: '采用100%纯牛肉制作，搭配新鲜蔬菜和特制酱料，口感丰富，营养均衡。'
    })
  } catch (error) {
    console.error('获取菜品信息失败:', error)
    ElMessage.error('获取菜品信息失败')
  }
}

// 更新菜品
const updateMenu = async () => {
  if (!menuFormRef.value) return

  try {
    await menuFormRef.value.validate()
    updating.value = true

    // TODO: 调用API更新菜品
    await new Promise(resolve => setTimeout(resolve, 1500))

    ElMessage.success('菜品信息更新成功')
    router.push('/admin/menu')
  } catch (error) {
    console.error('更新菜品失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    updating.value = false
  }
}

// 获取分类列表
const getCategories = async () => {
  try {
    // TODO: 从API获取实际分类列表
    await new Promise(resolve => setTimeout(resolve, 500))
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 页面加载时获取数据
onMounted(async () => {
  await getCategories()
  await getMenuInfo()
})
</script>

<style scoped>
.edit-menu {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.form-container {
  max-width: 900px;
  margin: 0 auto;
}

.menu-image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.menu-image-uploader:hover {
  border-color: #409eff;
}

.menu-image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.menu-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}
</style>