<template>
  <div class="password-change-container">
    <div class="page-header">
      <h2>密码修改</h2>
    </div>
    
    <!-- 密码修改表单 -->
    <div class="card">
      <h3>密码修改</h3>
      <div class="form-container">
        <div class="form-group">
          <label class="form-label">登录名：</label>
          <input 
            type="text" 
            v-model="passwordForm.username" 
            class="form-input"
            readonly
            placeholder="系统管理员"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">原密码：</label>
          <input 
            type="password" 
            v-model="passwordForm.oldPassword" 
            class="form-input"
            placeholder="请输入原密码"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">新密码：</label>
          <input 
            type="password" 
            v-model="passwordForm.newPassword" 
            class="form-input"
            placeholder="请输入新密码"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">确认密码：</label>
          <input 
            type="password" 
            v-model="passwordForm.confirmPassword" 
            class="form-input"
            placeholder="请再次输入新密码"
          />
        </div>
        
        <div class="form-buttons">
          <button @click="handleSubmit" class="btn-primary">修改</button>
          <button @click="handleReset" class="btn-secondary">重置</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'PasswordChange',
  setup() {
    const passwordForm = reactive({
      username: '系统管理员',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 表单验证
    const validateForm = () => {
      if (!passwordForm.oldPassword) {
        ElMessage.error('请输入原密码')
        return false
      }
      
      if (!passwordForm.newPassword) {
        ElMessage.error('请输入新密码')
        return false
      }
      
      if (passwordForm.newPassword.length < 6) {
        ElMessage.error('新密码长度不能少于6位')
        return false
      }
      
      if (!passwordForm.confirmPassword) {
        ElMessage.error('请确认新密码')
        return false
      }
      
      if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        ElMessage.error('两次输入的新密码不一致')
        return false
      }
      
      if (passwordForm.oldPassword === passwordForm.newPassword) {
        ElMessage.error('新密码不能与原密码相同')
        return false
      }
      
      return true
    }
    
    // 提交修改
    const handleSubmit = async () => {
      if (!validateForm()) {
        return
      }
      
      try {
        // TODO: 调用后端接口修改密码
        // await axios.put('/api/admin/change-password', {
        //   oldPassword: passwordForm.oldPassword,
        //   newPassword: passwordForm.newPassword
        // })
        
        // 模拟密码修改成功
        if (passwordForm.oldPassword === '123456') {
          ElMessage.success('密码修改成功')
          handleReset()
        } else {
          ElMessage.error('原密码错误')
        }
      } catch (error) {
        ElMessage.error('密码修改失败，请稍后重试')
        console.error('密码修改错误:', error)
      }
    }
    
    // 重置表单
    const handleReset = () => {
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
    
    return {
      passwordForm,
      handleSubmit,
      handleReset
    }
  }
}
</script>

<style scoped>
.password-change-container {
  max-width: 800px;
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
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.form-label {
  display: inline-block;
  width: 100px;
  text-align: right;
  margin-right: 15px;
  font-weight: bold;
  color: #333;
}

.form-input {
  flex: 1;
  max-width: 300px;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #409eff;
}

.form-input[readonly] {
  background-color: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
}

.form-buttons {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.form-buttons button {
  margin: 0 10px;
  padding: 10px 30px;
  font-size: 14px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.btn-secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(144, 147, 153, 0.3);
}
</style> 