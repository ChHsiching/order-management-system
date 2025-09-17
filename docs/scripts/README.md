# 开发辅助脚本

本目录包含用于开发过程中快速重启和测试后端服务的脚本。

## 脚本列表

### 1. restart-backend.sh
完整重启脚本，包含以下功能：
- 停止旧服务进程
- 清理端口占用
- 清理编译产物
- 重新编译项目
- 启动后端服务
- 健康检查
- 显示测试命令

**使用方法：**
```bash
# 开发环境重启
./restart-backend.sh dev

# 生产环境重启
./restart-backend.sh prod

# 默认开发环境
./restart-backend.sh
```

**特点：**
- ✅ 完整的清理流程
- ✅ 重新编译确保代码最新
- ✅ 详细的日志记录
- ✅ 健康检查确保服务正常启动
- ✅ 彩色输出便于调试
- ⚠️ 执行时间较长（2-3分钟）

### 2. quick-restart.sh
快速重启脚本，跳过编译步骤：
- 停止旧服务
- 清理端口
- 直接启动服务

**使用方法：**
```bash
./quick-restart.sh dev    # 开发环境
./quick-restart.sh prod   # 生产环境
./quick-restart.sh       # 默认开发环境
```

**特点：**
- ✅ 速度快（10-15秒）
- ✅ 适合频繁测试
- ❌ 不重新编译代码
- ❌ 如果代码有变更不会生效

### 3. test-apis.sh
API测试脚本，快速测试各种接口：
- 健康检查
- 公开接口测试
- 后台认证接口测试
- 购物车功能测试
- CRUD操作测试

**使用方法：**
```bash
# 测试默认地址 (localhost:8080)
./test-apis.sh

# 测试指定地址
./test-apis.sh http://192.168.1.100:8080/WebOrderSystem
```

**测试内容：**
- 健康检查接口
- 菜品分类查询（公开/后台）
- 菜品列表查询
- 购物车增删改查
- 分类和菜品的创建操作

## 使用场景

### 开发过程中的典型工作流：

1. **修改代码后完整测试：**
   ```bash
   ./restart-backend.sh dev
   ./test-apis.sh
   ```

2. **频繁调试接口：**
   ```bash
   ./quick-restart.sh dev
   # 手动测试特定接口
   curl -u admin:admin -s http://localhost:8080/WebOrderSystem/admin/menu/categories
   ```

3. **批量API测试：**
   ```bash
   ./restart-backend.sh dev
   ./test-apis.sh
   ```

4. **检查服务状态：**
   ```bash
   curl -s http://localhost:8080/WebOrderSystem/actuator/health
   ```

## 日志文件

- **应用日志：** `backend/app-test.log`
- **重启日志：** `backend/restart.log`
- **查看实时日志：** `tail -f backend/app-test.log`

## 常用测试命令

### 健康检查
```bash
curl -s http://localhost:8080/WebOrderSystem/actuator/health
```

### 公开接口测试
```bash
# 获取分类
curl -s http://localhost:8080/WebOrderSystem/api/categories

# 获取菜品列表
curl -s http://localhost:8080/WebOrderSystem/api/menu
```

### 后台接口测试（需要认证）
```bash
# 获取分类（后台）
curl -u admin:admin -s http://localhost:8080/WebOrderSystem/admin/menu/categories

# 获取菜品列表（后台）
curl -u admin:admin -s http://localhost:8080/WebOrderSystem/admin/menu/items
```

### 购物车测试
```bash
# 添加商品到购物车
curl -s -X POST "http://localhost:8080/WebOrderSystem/api/cart/add?username=admin&productId=1&quantity=2"

# 获取购物车
curl -s "http://localhost:8080/WebOrderSystem/api/cart?username=admin"
```

### Swagger文档
- 访问地址：http://localhost:8080/WebOrderSystem/swagger-ui.html
- 认证信息：admin/admin

## 故障排除

### 端口被占用
```bash
# 查看端口占用
lsof -i :8080

# 强制停止进程
kill -9 <PID>
```

### 服务启动失败
1. 检查日志：`tail -f backend/app-test.log`
2. 确认数据库连接正常
3. 检查配置文件是否正确

### 认证失败
1. 确认使用正确的用户名密码：admin/admin
2. 检查SecurityConfig配置
3. 查看Security相关日志

## 注意事项

1. 脚本需要在Linux/macOS环境下运行
2. 需要安装：curl, lsof, java, maven
3. 确保MySQL服务正在运行
4. 确保数据库`web_order`已创建
5. 首次运行建议使用完整重启脚本
6. 生产环境使用时请修改相关配置