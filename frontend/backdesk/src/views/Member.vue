<template>
  <div class="member-container">
    <div class="page-header">
      <h2>会员管理</h2>
    </div>
    
    <!-- 会员列表 -->
    <div class="card">
      <h3>会员列表</h3>
      <table class="table">
        <thead>
          <tr>
            <th>序号</th>
            <th>账号</th>
            <th>密码</th>
            <th>姓名</th>
            <th>性别</th>
            <th>年龄</th>
            <th>住址</th>
            <th>电话</th>
            <th>信誉度</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(member, index) in memberList" :key="member.id">
            <td>{{ index + 1 }}</td>
            <td>{{ member.username }}</td>
            <td>{{ member.password }}</td>
            <td>{{ member.realName }}</td>
            <td>{{ member.gender }}</td>
            <td>{{ member.age }}</td>
            <td>{{ member.address }}</td>
            <td>{{ member.phone }}</td>
            <td>{{ member.creditScore }}</td>
            <td>
              <button @click="handleDeleteMember(member.id)" class="btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Member',
  setup() {
    const memberList = ref([])
    
    // 模拟会员数据
    const mockMembers = [
      {
        id: 1,
        username: 'wenzhifu',
        password: '000000',
        realName: '文之秀',
        gender: '女',
        age: 33,
        address: '北京市',
        phone: '13666666666',
        creditScore: 5
      },
      {
        id: 2,
        username: 'liqian',
        password: '000000',
        realName: '李强',
        gender: '男',
        age: 20,
        address: '上海市',
        phone: '13777777777',
        creditScore: 0
      },
      {
        id: 3,
        username: 'test11',
        password: 'test11',
        realName: '文之秀',
        gender: '女',
        age: 22,
        address: '江苏省南京市',
        phone: '13888888888',
        creditScore: 0
      },
      {
        id: 4,
        username: 'test22',
        password: 'test22',
        realName: '高哥',
        gender: '女',
        age: 28,
        address: '江苏省南京市',
        phone: '13888888888',
        creditScore: 0
      }
    ]
    
    // 获取会员列表
    const fetchMemberList = async () => {
      try {
        // TODO: 调用后端接口获取会员列表
        // const response = await axios.get('/api/admin/members')
        // memberList.value = response.data
        
        // 模拟数据
        memberList.value = [...mockMembers]
      } catch (error) {
        ElMessage.error('获取会员列表失败')
        console.error('获取会员列表错误:', error)
      }
    }
    
    // 删除会员
    const handleDeleteMember = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除该会员吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // TODO: 调用后端接口删除会员
        // await axios.delete(`/api/admin/members/${id}`)
        
        // 模拟删除成功
        memberList.value = memberList.value.filter(item => item.id !== id)
        ElMessage.success('删除会员成功')
      } catch (error) {
        // 用户取消删除或删除失败
        if (error !== 'cancel') {
          ElMessage.error('删除会员失败')
          console.error('删除会员错误:', error)
        }
      }
    }
    
    onMounted(() => {
      fetchMemberList()
    })
    
    return {
      memberList,
      handleDeleteMember
    }
  }
}
</script>

<style scoped>
.member-container {
  max-width: 1400px;
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

.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
  font-size: 14px;
}

.table th,
.table td {
  padding: 10px 8px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
  word-break: break-all;
}

.table th {
  background-color: #f5f7fa;
  font-weight: bold;
  white-space: nowrap;
}

.table tr:hover {
  background-color: #f5f7fa;
}

.table td:nth-child(7) {
  max-width: 120px;
}

.table td:nth-child(8) {
  max-width: 120px;
}

.btn-danger {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-danger:hover {
  background-color: #f78989;
}
</style> 