# 前后端联调测试记录

## 测试环境
- 后端服务：http://localhost:8080/WebOrderSystem
- 前台服务：http://localhost:5173
- 后台服务：http://localhost:5174
- 测试时间：2025-09-20 01:10:00

## API测试结果

### 1. 用户注册API测试
**接口路径：** `POST /api/user/register`

**测试命令：**
```bash
curl -X POST http://localhost:8080/WebOrderSystem/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser123", "password": "pass123", "email": "newuser@example.com", "phone": "13912345678", "qq": "987654321"}'
```

**测试结果：**
```json
{
  "username": "newuser123",
  "createTime": "2025-09-20T01:14:03.354354955",
  "email": "newuser@example.com",
  "password": "$2a$10$.uMdz0Ei9o8vcLxPCRzPzOhAJWqbEO421wumg5QbsZxCMQLtUm9jG",
  "phone": "13912345678",
  "qq": "987654321",
  "role": 0
}
```

**验证结果：** ✅ 成功
- 用户注册功能正常
- 密码已加密存储
- 返回完整的用户信息

### 2. 用户登录API测试
**接口路径：** `POST /api/user/login`

**测试命令：**
```bash
curl -X POST http://localhost:8080/WebOrderSystem/api/user/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=newuser123&password=pass123"
```

**测试结果：**
```json
{
  "username": "newuser123",
  "createTime": "2025-09-20T01:14:03",
  "email": "newuser@example.com",
  "phone": "13912345678",
  "qq": "987654321",
  "role": 0
}
```

**验证结果：** ✅ 成功
- 用户登录功能正常
- 返回用户基本信息（不包含密码）

### 3. 管理员登录API测试
**接口路径：** `POST /admin/login`

**测试命令：**
```bash
curl -X POST http://localhost:8080/WebOrderSystem/admin/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin"
```

**测试结果：** 无返回内容

**验证结果：** ❌ 需要进一步调试

### 4. 公开API测试
**接口路径：** `GET /api/menu`

**测试命令：**
```bash
curl -s http://localhost:8080/WebOrderSystem/api/menu | head -c 200
```

**测试结果：**
```json
{"code":0,"message":"success","data":[{"id":1,"createTime":"2025-09-18T18:38:40","imgPath":"/images/burger1.jpg","name":"巨无霸汉堡","productLock":0},{"id":2,"createTime":"2025-09-18T18:38:40","i
```

**验证结果：** ✅ 成功
- 菜单API正常工作
- 返回正确的数据格式

### 5. 分类API测试
**接口路径：** `GET /api/categories`

**测试命令：**
```bash
curl -s http://localhost:8080/WebOrderSystem/api/categories | jq '.'
```

**测试结果：**
```json
[
  {
    "id": 1,
    "cateLock": 0,
    "cateName": "汉堡套餐",
    "address": "主餐区",
    "productName": "汉堡系列"
  },
  {
    "id": 2,
    "cateLock": 0,
    "cateName": "小食套餐",
    "address": "小食区",
    "productName": "小食系列"
  },
  ...
]
```

**验证结果：** ✅ 成功
- 返回11个分类
- 分类数据结构正确

### 6. 单个菜品查询API测试
**接口路径：** `GET /api/menu/{id}`

**测试命令：**
```bash
curl -s http://localhost:8080/WebOrderSystem/api/menu/1 | jq '.'
```

**测试结果：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "createTime": "2025-09-18T18:38:40",
    "imgPath": "/images/burger1.jpg",
    "info": "经典巨无霸汉堡，双层牛肉饼",
    "name": "巨无霸汉堡",
    "isRecommend": 1,
    "originalPrice": 25.0,
    "hotPrice": 22.0,
    "productLock": 0,
    "sales": 150,
    "categoryId": 1
  }
}
```

**验证结果：** ✅ 成功
- 返回完整的菜品详细信息
- 包含价格、推荐状态、销量等信息

### 7. 根据分类查询菜品API测试
**接口路径：** `GET /api/menu/category/{categoryId}`

**测试命令：**
```bash
curl -s http://localhost:8080/WebOrderSystem/api/menu/category/1 | jq '.'
```

**测试结果：**
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "createTime": "2025-09-18T18:38:40",
      "imgPath": "/images/burger1.jpg",
      "name": "巨无霸汉堡",
      "productLock": 0
    },
    {
      "id": 2,
      "createTime": "2025-09-18T18:38:40",
      "imgPath": "/images/burger2.jpg",
      "name": "香辣鸡腿堡",
      "productLock": 0
    },
    {
      "id": 3,
      "createTime": "2025-09-18T18:38:40",
      "imgPath": "/images/burger3.jpg",
      "name": "鳕鱼堡",
      "productLock": 0
    },
    {
      "id": 4,
      "createTime": "2025-09-18T18:38:40",
      "imgPath": "/images/burger4.jpg",
      "name": "牛肉汉堡",
      "productLock": 0
    }
  ]
}
```

**验证结果：** ✅ 成功
- 正确返回分类1下的4个汉堡类菜品
- 分类查询功能正常

## 测试账户信息
### 普通用户账户
- 用户名：newuser123
- 密码：pass123
- 邮箱：newuser@example.com
- 手机：13912345678
- QQ：987654321
- 角色：普通用户（role: 0）

### 管理员账户
- 用户名：admin
- 密码：admin
- 角色：管理员（role: 1）

## 前端请求示例

### 用户注册请求
```javascript
// 前端注册请求示例
const registerData = {
  username: "newuser123",
  password: "pass123",
  email: "newuser@example.com",
  phone: "13912345678",
  qq: "987654321"
};

fetch('http://localhost:8080/WebOrderSystem/api/user/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(registerData)
})
.then(response => response.json())
.then(data => console.log(data));
```

### 用户登录请求
```javascript
// 前端登录请求示例
const loginData = new URLSearchParams();
loginData.append('username', 'newuser123');
loginData.append('password', 'pass123');

fetch('http://localhost:8080/WebOrderSystem/api/user/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  body: loginData
})
.then(response => response.json())
.then(data => console.log(data));
```

## 注意事项
1. 用户注册需要所有必填字段：username, password, email, phone, qq
2. 用户登录使用 form-urlencoded 格式
3. 密码在后台会进行BCrypt加密
4. 用户角色：0为普通用户，1为管理员
5. 需要进一步测试管理员登录功能

### 8. 购物车API测试
**接口路径：** `GET /api/cart?username={username}`

**测试命令：**
```bash
curl "http://localhost:8080/WebOrderSystem/api/cart?username=newuser123"
```

**测试结果：** 无返回内容

**验证结果：** ❌ 需要进一步调试

### 9. 添加商品到购物车API测试
**接口路径：** `POST /api/cart/add`

**测试命令：**
```bash
curl -X POST "http://localhost:8080/WebOrderSystem/api/cart/add" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=newuser123&productId=1&quantity=2"
```

**测试结果：** 无返回内容

**验证结果：** ❌ 需要进一步调试

### 10. 获取购物车列表API测试（兼容接口）
**接口路径：** `GET /api/cart/list?username={username}`

**测试命令：**
```bash
curl -s "http://localhost:8080/WebOrderSystem/api/cart/list?username=newuser123"
```

**测试结果：** 无返回内容

**验证结果：** ❌ 需要进一步调试

## 前端请求示例（菜单相关）

### 获取菜单数据
```javascript
// 获取完整菜单
fetch('http://localhost:8080/WebOrderSystem/api/menu')
  .then(response => response.json())
  .then(data => console.log(data));

// 获取单个菜品详情
fetch('http://localhost:8080/WebOrderSystem/api/menu/1')
  .then(response => response.json())
  .then(data => console.log(data));

// 根据分类获取菜品
fetch('http://localhost:8080/WebOrderSystem/api/menu/category/1')
  .then(response => response.json())
  .then(data => console.log(data));

// 获取分类列表
fetch('http://localhost:8080/WebOrderSystem/api/categories')
  .then(response => response.json())
  .then(data => console.log(data));
```

### 购物车操作示例
```javascript
// 获取购物车
fetch('http://localhost:8080/WebOrderSystem/api/cart?username=newuser123')
  .then(response => response.json())
  .then(data => console.log(data));

// 添加商品到购物车
const cartData = new URLSearchParams();
cartData.append('username', 'newuser123');
cartData.append('productId', '1');
cartData.append('quantity', '2');

fetch('http://localhost:8080/WebOrderSystem/api/cart/add', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded'
  },
  body: cartData
})
.then(response => response.json())
.then(data => console.log(data));
```

## 已完成测试功能
✅ **用户注册和登录系统**
- 用户注册API正常工作
- 用户登录API正常工作
- 数据验证功能正常

✅ **菜单浏览系统**
- 完整菜单获取API正常
- 单个菜品查询API正常
- 分类查询API正常
- 根据分类查询菜品API正常

## 待解决问题
1. 管理员登录API返回异常
2. 购物车API无响应，需要进一步调试
3. 需要实现JWT token认证机制
4. 需要测试需要认证的API接口
5. 需要测试订单相关功能

## 下一步测试计划
1. 调试购物车API问题
2. 测试后台管理功能
3. 进行前后端联调测试
4. 完整端到端功能测试