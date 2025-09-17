# Web订餐管理系统测试框架

## 概述

这是一个为Web订餐管理系统设计的完整自动化测试框架，提供API测试、安全测试、数据库测试和集成测试能力。

## 目录结构

```
test-framework/
├── README.md                      # 本文档
├── docs/                          # 测试文档
│   ├── 测试框架使用指南.md         # 详细使用指南
│   ├── API测试规范.md              # API测试标准
│   └── 权限测试规范.md              # 权限测试标准
├── scripts/                        # 测试脚本
│   ├── core/                       # 核心测试库
│   │   ├── test_framework.sh      # 测试框架核心库
│   │   ├── test_runner.sh         # 测试运行器
│   │   └── data_manager.sh        # 测试数据管理器
│   └── api/                       # API测试脚本
│       └── basic_api_test.sh      # 基础API测试
├── config/                         # 测试配置
│   ├── test_config.yml            # 测试环境配置
│   └── api_endpoints.yml          # API端点配置
├── data/                           # 测试数据
│   └── snapshots/                 # 数据快照目录
└── reports/                        # 测试报告
    └── templates/                  # 报告模板
        └── test_report_template.md # 测试报告模板
```

## 快速开始

### 1. 环境准备

确保以下环境已经准备就绪：

```bash
# 检查必要的工具
which curl        # HTTP请求工具
which mysql        # 数据库客户端
which jq           # JSON处理工具（可选）

# 检查Java环境
java -version

# 检查后端服务
curl -f http://localhost:8080/WebOrderSystem/api-docs
```

### 2. 启动测试环境

```bash
# 进入项目根目录
cd /home/chhsich/Git/Mine/order-management-system

# 启动后端服务
cd backend
./mvnw spring-boot:run &
sleep 30  # 等待服务启动

# 验证服务启动
curl -f http://localhost:8080/WebOrderSystem/api-docs
```

### 3. 运行基础测试

```bash
# 返回项目根目录
cd ..

# 运行完整的API测试
./test-framework/scripts/api/basic_api_test.sh

# 使用测试运行器执行所有测试
./test-framework/scripts/core/test_runner.sh --all

# 执行特定类型的测试
./test-framework/scripts/core/test_runner.sh --type api
./test-framework/scripts/core/test_runner.sh --type security

# 管理测试数据
./test-framework/scripts/core/data_manager.sh init
./test-framework/scripts/core/data_manager.sh clean
./test-framework/scripts/core/data_manager.sh verify
```

## 核心功能

### 1. 测试运行器 (test_runner.sh)

统一的测试执行入口，支持多种运行模式：

```bash
# 运行所有测试
./test-framework/scripts/core/test_runner.sh --all

# 运行特定类型测试
./test-framework/scripts/core/test_runner.sh --type api
./test-framework/scripts/core/test_runner.sh --type security
./test-framework/scripts/core/test_runner.sh --type database
./test-framework/scripts/core/test_runner.sh --type integration

# 运行特定测试文件
./test-framework/scripts/core/test_runner.sh --file basic_api_test.sh

# 并行执行测试
./test-framework/scripts/core/test_runner.sh --parallel

# 生成详细报告
./test-framework/scripts/core/test_runner.sh --report detailed
```

### 2. 测试数据管理器 (data_manager.sh)

完整的测试数据生命周期管理：

```bash
# 初始化测试数据
./test-framework/scripts/core/data_manager.sh init

# 清理测试数据
./test-framework/scripts/core/data_manager.sh clean

# 重置测试数据
./test-framework/scripts/core/data_manager.sh reset

# 创建测试场景
./test-framework/scripts/core/data_manager.sh create-scenario user_registration
./test-framework/scripts/core/data_manager.sh create-scenario order_creation

# 验证数据状态
./test-framework/scripts/core/data_manager.sh verify

# 创建和恢复数据快照
./test-framework/scripts/core/data_manager.sh snapshot before_test
./test-framework/scripts/core/data_manager.sh restore before_test

# 导出和导入数据
./test-framework/scripts/core/data_manager.sh export backup.sql
./test-framework/scripts/core/data_manager.sh import backup.sql
```

### 3. 测试框架核心库 (test_framework.sh)

提供完整的测试基础设施：

- **日志系统**: 分级日志记录，支持颜色输出
- **测试统计**: 自动统计通过率、失败率等
- **API测试**: 统一的API测试函数
- **权限测试**: 多角色权限验证
- **数据库验证**: 数据一致性检查
- **报告生成**: 多格式测试报告生成

## 测试类型

### 1. API测试

验证各个API端点的功能正确性：

```bash
# 运行基础API测试
./test-framework/scripts/api/basic_api_test.sh

# 使用测试运行器
./test-framework/scripts/core/test_runner.sh --type api
```

**覆盖范围**：
- 用户管理（注册、登录、信息管理）
- 菜单管理（分类、菜品、搜索、推荐）
- 订单管理（创建、查询、状态更新）
- 购物车管理（增删改查）
- 管理员功能（用户、菜单、订单管理）

### 2. 权限测试

验证系统的权限控制和安全性：

```bash
# 运行权限测试
./test-framework/scripts/core/test_runner.sh --type security
```

**权限矩阵**：
- **游客权限**: 只能访问公开API（分类查询、菜品浏览）
- **会员权限**: 访问用户功能和订单管理
- **管理员权限**: 访问所有管理功能

**安全测试**：
- 越权访问测试
- 会话管理测试
- 认证安全测试
- 输入验证测试

### 3. 数据库测试

验证数据库操作的真实性和完整性：

```bash
# 运行数据库测试
./test-framework/scripts/core/test_runner.sh --type database
```

**测试内容**：
- 数据完整性验证
- 外键约束检查
- 事务完整性测试
- 性能基准测试

### 4. 集成测试

验证完整的业务流程：

```bash
# 运行集成测试
./test-framework/scripts/core/test_runner.sh --type integration
```

**业务流程**：
- 用户注册流程
- 点餐下单流程
- 支付处理流程
- 管理员操作流程

## 配置文件

### 1. 测试环境配置 (config/test_config.yml)

```yaml
environment:
  name: "test"
  debug: true
  log_level: "INFO"

database:
  host: "localhost"
  port: 3306
  name: "web_order"
  username: "root"
  password: "123456"

api:
  base_url: "http://localhost:8080/WebOrderSystem"
  timeout: 30
  retry_count: 3
```

### 2. API端点配置 (config/api_endpoints.yml)

定义所有API端点的详细信息和权限要求：

```yaml
endpoints:
  user:
    register:
      path: "/api/user/register"
      method: "POST"
      auth: "none"
      expected_status: 201
```

## 测试报告

### 1. 报告生成

测试框架支持多种格式的测试报告：

```bash
# 生成Markdown格式报告（默认）
./test-framework/scripts/core/test_runner.sh --report markdown

# 生成HTML格式报告
./test-framework/scripts/core/test_runner.sh --report html

# 生成JSON格式报告
./test-framework/scripts/core/test_runner.sh --report json
```

### 2. 报告内容

每个测试报告包含：

- **测试概览**: 执行时间、通过率、关键指标
- **详细结果**: 各测试模块的执行情况
- **问题列表**: 失败测试的详细信息和修复建议
- **趋势分析**: 与历史结果的对比和趋势
- **性能统计**: 响应时间、错误率等性能指标

### 3. 报告示例

```markdown
# Web订餐管理系统测试报告

## 测试概览
- 测试时间: 2025-09-17 10:30:00
- 测试总数: 150
- 通过数量: 145
- 失败数量: 5
- 通过率: 96.7%

## 测试结果
### ✅ 通过测试 (145)
- 用户注册测试
- 用户登录测试
- 菜单查询测试
- ...

### ❌ 失败测试 (5)
- 订单创建测试 (期望: 201, 实际: 500)
- 支付接口测试 (期望: 200, 实际: 404)
- ...
```

## 持续集成

### 1. GitHub Actions集成

测试框架可以与GitHub Actions无缝集成：

```yaml
# .github/workflows/test.yml
name: Automated Testing
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run tests
        run: ./test-framework/scripts/core/test_runner.sh --all
      - name: Generate report
        run: ./test-framework/scripts/core/test_runner.sh --report markdown
```

### 2. 质量门禁

配置测试通过率阈值，确保代码质量：

```yaml
quality_gates:
  min_pass_rate: 90
  max_response_time: 1000
  max_error_rate: 0.01
  critical_issues_allowed: 0
```

## 最佳实践

### 1. 测试开发原则

- **独立性**: 每个测试应该独立运行，不依赖其他测试的状态
- **可重复性**: 测试应该可以重复执行并产生相同的结果
- **明确性**: 测试名称和描述应该清楚说明测试的目的
- **完整性**: 测试应该覆盖正常情况和异常情况

### 2. 测试数据管理

- **数据隔离**: 使用专用的测试数据，避免影响生产数据
- **清理机制**: 测试完成后自动清理测试数据
- **数据完整性**: 确保测试数据符合业务规则和约束

### 3. 测试执行策略

- **定期执行**: 建立定期的测试执行机制
- **失败分析**: 及时分析测试失败的原因并修复
- **持续改进**: 根据测试结果不断改进测试覆盖范围

## 故障排除

### 1. 常见问题

**服务启动失败**:
```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 检查Java进程
ps aux | grep java

# 查看应用日志
tail -f logs/web-order-system.log
```

**数据库连接失败**:
```bash
# 测试数据库连接
mysql -h localhost -u root -p123456 -e "USE web_order; SELECT 1;"

# 检查数据库状态
systemctl status mysql
```

**测试失败**:
```bash
# 查看详细测试日志
tail -f logs/test_framework.log

# 检查测试环境
./test-framework/scripts/core/test_runner.sh --check-env

# 重新初始化测试数据
./test-framework/scripts/core/data_manager.sh reset
```

### 2. 调试模式

启用详细调试信息：

```bash
# 启用调试模式
export DEBUG_TEST_FRAMEWORK=1

# 运行测试并输出详细日志
./test-framework/scripts/core/test_runner.sh --debug

# 只输出失败测试的详细信息
./test-framework/scripts/core/test_runner.sh --errors-only
```

### 3. 性能优化

**并行测试执行**:
```bash
# 启用并行执行
export TEST_PARALLEL=4
./test-framework/scripts/core/test_runner.sh --parallel
```

**测试数据缓存**:
```bash
# 启用数据缓存
export TEST_DATA_CACHE=1
./test-framework/scripts/core/test_runner.sh --cache
```

## 扩展开发

### 1. 添加新的测试脚本

基于模板创建新的测试脚本：

```bash
# 复制测试脚本模板
cp test-framework/scripts/api/basic_api_test.sh test-framework/scripts/api/custom_test.sh

# 编辑测试脚本
vim test-framework/scripts/api/custom_test.sh
```

### 2. 自定义测试函数

使用测试框架提供的辅助函数：

```bash
# 使用日志函数
log_info "信息消息"
log_error "错误消息"

# 使用测试验证函数
test_api "测试名称" "POST" "/api/endpoint" '{"data":"value"}' 200

# 使用数据库验证函数
verify_database_count "table_name" "condition" "expected_count"
```

### 3. 配置扩展

在配置文件中添加自定义配置：

```yaml
# test_config.yml
custom_tests:
  enabled: true
  endpoints:
    - name: "自定义测试"
      path: "/api/custom"
      method: "GET"
      expected_status: 200
```

## 贡献指南

### 1. 添加新测试

1. 确定测试类型和范围
2. 创建相应的测试脚本
3. 更新配置文件（如需要）
4. 测试脚本的功能正确性
5. 提交PR并描述测试内容

### 2. 框架改进

如果发现框架需要改进的地方：

1. 在GitHub Issues中创建改进建议
2. 描述具体的问题和改进方案
3. 提供代码示例（如果可能）
4. 参与讨论和实现

## 支持和反馈

如果在使用测试框架时遇到问题：

1. 查看本文档的故障排除部分
2. 检查GitHub Issues中是否有类似问题
3. 创建新的Issue描述问题详情
4. 提供相关的日志和错误信息

---

**文档版本**: 1.0
**最后更新**: 2025-09-17
**维护者**: Web订餐管理系统开发团队