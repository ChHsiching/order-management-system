<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="icon-container">
          <div class="lock-icon">🔒</div>
          <div class="key-icon">🔑</div>
        </div>
        <h2>后台管理登录</h2>
      </div>
      
      <div class="login-form">
        <div class="form-group">
          <label class="form-label">账号：</label>
          <input 
            type="text" 
            v-model="loginForm.username" 
            class="form-input"
            placeholder="请输入账号"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">密码：</label>
          <input 
            type="password" 
            v-model="loginForm.password" 
            class="form-input"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </div>
        
        <div class="form-buttons">
          <button @click="handleLogin" class="btn-primary">登陆</button>
          <button @click="handleReset" class="btn-secondary">重置</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import auth from '../utils/auth.js'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()

    const loginForm = ref({
      username: '',
      password: ''
    })

    // 处理登录逻辑
    const handleLogin = async () => {
      if (!loginForm.value.username || !loginForm.value.password) {
        ElMessage.error('请输入账号和密码')
        return
      }

      try {
        // 调用后端登录接口
        const response = await auth.login(loginForm.value.username, loginForm.value.password)

        if (response.code === 200) {
          ElMessage.success('登录成功')
          // 存储token到本地存储
          auth.setToken(response.data.token)
          router.push('/admin/menu-category')
        } else {
          ElMessage.error(response.message || '账号或密码错误')
        }
      } catch (error) {
        ElMessage.error('登录失败，请稍后重试')
        console.error('登录错误:', error)
      }
    }
    
    // 重置表单
    const handleReset = () => {
      loginForm.value = {
        username: '',
        password: ''
      }
    }
    
    return {
      loginForm,
      handleLogin,
      handleReset
    }
  }
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  background: white;
  border-radius: 10px;
  padding: 40px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.icon-container {
  margin-bottom: 20px;
  position: relative;
  height: 80px;
}

.lock-icon, .key-icon {
  font-size: 40px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.lock-icon {
  top: 0;
}

.key-icon {
  top: 30px;
  left: 60%;
}

.login-header h2 {
  color: #333;
  font-size: 24px;
  font-weight: normal;
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.form-label {
  width: 60px;
  text-align: right;
  margin-right: 15px;
  color: #666;
}

.form-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #409eff;
}

.form-buttons {
  text-align: center;
  margin-top: 30px;
}

.form-buttons button {
  margin: 0 10px;
  padding: 10px 25px;
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