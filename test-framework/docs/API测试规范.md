# Web订餐管理系统 API测试规范

## 概述

本文档定义了Web订餐管理系统的API测试标准和规范，确保所有API端点的功能正确性和稳定性。

## 测试范围

### 1. 用户管理API
- **用户注册** (`POST /api/user/register`)
- **用户登录** (`POST /api/user/login`)
- **用户信息查询** (`GET /api/user/me`)
- **用户信息更新** (`PUT /api/user/update`)
- **用户密码修改** (`PUT /api/user/password`)

### 2. 菜单管理API
- **分类查询** (`GET /api/categories`)
- **分类详情** (`GET /api/categories/{id}`)
- **菜品列表** (`GET /api/menu/list`)
- **菜品详情** (`GET /api/menu/{id}`)
- **菜品搜索** (`GET /api/menu/search`)
- **菜品推荐** (`GET /api/menu/recommend`)

### 3. 订单管理API
- **创建订单** (`POST /api/order/create`)
- **订单查询** (`GET /api/order/list`)
- **订单详情** (`GET /api/order/{id}`)
- **订单状态更新** (`PUT /api/order/{id}/status`)
- **订单取消** (`PUT /api/order/{id}/cancel`)

### 4. 购物车API
- **添加到购物车** (`POST /api/cart/add`)
- **购物车查询** (`GET /api/cart/list`)
- **更新购物车** (`PUT /api/cart/update`)
- **删除购物车项** (`DELETE /api/cart/{id}`)
- **清空购物车** (`DELETE /api/cart/clear`)

### 5. 管理员API
- **用户管理** (`GET|POST|PUT|DELETE /api/admin/users`)
- **菜单管理** (`GET|POST|PUT|DELETE /api/admin/menu`)
- **订单管理** (`GET|PUT /api/admin/orders`)
- **系统统计** (`GET /api/admin/stats`)

## 测试数据标准

### 1. 用户测试数据
```yaml
test_users:
  - username: "test_member_001"
    password: "Test123456"
    email: "test001@example.com"
    phone: "13800138001"
    role: "member"

  - username: "test_admin_001"
    password: "Admin123456"
    email: "admin001@example.com"
    role: "admin"
```

### 2. 菜单测试数据
```yaml
test_menu:
  - name: "测试菜品001"
    price: 12.50
    hot_price: 10.00
    category_id: 1
    description: "这是一个测试菜品"
    image_url: "/images/test001.jpg"

  - name: "测试菜品002"
    price: 25.00
    hot_price: 20.00
    category_id: 2
    description: "这是另一个测试菜品"
    image_url: "/images/test002.jpg"
```

### 3. 订单测试数据
```yaml
test_orders:
  - user_name: "test_member_001"
    items:
      - menu_id: 1
        quantity: 2
        price: 12.50
      - menu_id: 2
        quantity: 1
        price: 25.00
    total_price: 50.00
    address: "测试地址001"
    phone: "13800138001"
```

## 测试用例设计

### 1. 正常流程测试
- **成功创建用户**: 验证用户注册功能正常
- **成功登录**: 验证用户登录功能和token生成
- **成功查询菜单**: 验证菜单数据返回正确
- **成功创建订单**: 验证订单创建和数据持久化
- **成功支付订单**: 验证订单状态更新正确

### 2. 异常流程测试
- **重复注册**: 验证重复用户名注册处理
- **错误密码**: 验证登录失败处理
- **无效参数**: 验证参数校验和错误提示
- **越权访问**: 验证权限控制和403返回
- **资源不存在**: 验证404错误处理

### 3. 边界值测试
- **最大/最小值**: 测试价格、数量等数值边界
- **长度限制**: 测试用户名、密码等长度限制
- **空值处理**: 测试必填字段为空的情况
- **特殊字符**: 测试输入特殊字符的处理

### 4. 性能测试
- **响应时间**: API响应时间应在1秒以内
- **并发处理**: 模拟多用户同时请求
- **数据量测试**: 测试大量数据的查询性能

## 测试执行规范

### 1. 测试环境准备
```bash
# 确保后端服务运行
curl -f http://localhost:8080/WebOrderSystem/api-docs

# 确保数据库连接正常
mysql -h localhost -u root -p123456 -e "USE web_order; SELECT 1;"

# 初始化测试数据
./test-framework/scripts/core/data_manager.sh init
```

### 2. 测试执行顺序
1. **数据准备测试**: 创建测试用户、菜单数据
2. **认证测试**: 验证登录和token功能
3. **功能测试**: 验证各API端点的业务逻辑
4. **权限测试**: 验证不同角色的访问权限
5. **数据清理**: 清理测试数据

### 3. 测试结果验证
- **HTTP状态码**: 验证返回的状态码符合预期
- **响应数据**: 验证返回的数据格式和内容正确
- **数据库状态**: 验证数据库中的数据变化正确
- **日志记录**: 验证操作日志记录完整

## 测试报告要求

### 1. 报告格式
- **Markdown格式**: 便于GitHub显示和版本控制
- **HTML格式**: 便于详细查看和分享
- **JSON格式**: 便于程序处理和自动化分析

### 2. 报告内容
```markdown
# API测试报告

## 测试概览
- 测试时间: 2025-09-17 10:30:00
- 测试总数: 150
- 通过数量: 145
- 失败数量: 5
- 通过率: 96.7%

## 测试结果详情
### ✅ 通过测试
- 用户注册测试
- 用户登录测试
- 菜单查询测试
- ...

### ❌ 失败测试
- 订单创建测试 (期望: 200, 实际: 500)
- 支付接口测试 (期望: 200, 实际: 404)
- ...

## 性能统计
- 平均响应时间: 245ms
- 最慢接口: /api/order/list (1200ms)
- 最快接口: /api/categories (50ms)

## 建议和改进
- 优化订单查询性能
- 完善支付接口实现
- 加强参数验证
```

## 自动化测试脚本

### 1. 基础API测试脚本
```bash
#!/bin/bash
# test-framework/scripts/api/basic_api_test.sh

source "$(dirname "$0")/../core/test_framework.sh"

test_user_registration() {
    local test_data='{"username":"test_user_001","password":"Test123456","email":"test001@example.com"}'
    test_api "用户注册" "POST" "/api/user/register" "$test_data" 201
}

test_user_login() {
    local test_data='{"username":"test_user_001","password":"Test123456"}'
    test_api "用户登录" "POST" "/api/user/login" "$test_data" 200
}

# 主测试流程
main() {
    log_start "基础API测试"

    setup_test_environment

    test_user_registration
    test_user_login

    cleanup_test_environment
    log_end "基础API测试"
}

main "$@"
```

### 2. 权限测试脚本
```bash
#!/bin/bash
# test-framework/scripts/security/permission_test.sh

source "$(dirname "$0")/../core/test_framework.sh"

test_guest_access() {
    test_permission "guest" "游客访问分类" "GET" "/api/categories" "" 200
    test_permission "guest" "游客访问菜品" "GET" "/api/menu/list" "" 200
    test_permission "guest" "游客访问用户信息" "GET" "/api/user/me" "" 401
}

test_member_access() {
    test_permission "member" "会员访问订单" "GET" "/api/order/list" "" 200
    test_permission "member" "会员访问管理" "GET" "/api/admin/users" "" 403
}

# 主测试流程
main() {
    log_start "权限测试"

    setup_test_environment

    test_guest_access
    test_member_access

    cleanup_test_environment
    log_end "权限测试"
}

main "$@"
```

## 持续集成要求

### 1. 代码提交时自动触发
- 推送到develop分支: 执行API测试
- 创建Pull Request: 执行完整测试套件
- 定时执行: 每日凌晨执行完整测试

### 2. 质量门禁标准
- **基础测试**: 100%通过
- **API测试**: 通过率 > 90%
- **性能测试**: 响应时间 < 1秒
- **权限测试**: 权限验证 > 95%

### 3. 测试结果通知
- **成功通知**: 发送简要测试报告
- **失败通知**: 立即发送失败详情
- **性能告警**: 响应时间超过阈值时告警

## 最佳实践

### 1. 测试开发原则
- **独立性**: 每个测试应该独立运行
- **可重复性**: 测试结果应该稳定可靠
- **完整性**: 覆盖正常和异常情况
- **自动化**: 减少人工干预

### 2. 测试数据管理
- **数据隔离**: 使用专用测试数据库
- **数据清理**: 测试完成后清理数据
- **数据验证**: 确保测试数据正确性

### 3. 测试维护
- **定期更新**: 根据API变更更新测试
- **结果分析**: 分析测试失败原因
- **持续改进**: 优化测试覆盖率

---

**文档版本**: 1.0
**最后更新**: 2025-09-17
**维护者**: Web订餐管理系统开发团队