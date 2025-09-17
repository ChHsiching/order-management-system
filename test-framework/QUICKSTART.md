# Web订餐管理系统测试框架 - 快速开始指南

## 概述

这是一个为Web订餐管理系统设计的完整自动化测试框架，提供API测试、安全测试、数据库测试和集成测试能力。

## 🚀 快速开始

### 1. 环境准备

确保以下环境已经准备就绪：

```bash
# 检查后端服务
curl -f http://localhost:8080/WebOrderSystem/api-docs

# 检查数据库连接
mysql -h localhost -u root -p123456 -e "USE web_order; SELECT 1;"

# 检查必要工具
which curl mysql jq bc
```

### 2. 一键启动测试

```bash
# 进入项目根目录
cd /home/chhsich/Git/Mine/order-management-system

# 检查测试环境
./test-framework/scripts/core/test_runner.sh --check-env

# 运行完整测试套件
./test-framework/scripts/core/test_runner.sh --all

# 生成测试报告
./test-framework/scripts/core/test_runner.sh --report markdown
```

## 📋 测试类型

### API功能测试
```bash
# 测试所有API端点
./test-framework/scripts/core/test_runner.sh --type api

# 测试特定API功能
./test-framework/scripts/core/test_runner.sh --file ../api/basic_api_test.sh
```

### 安全权限测试
```bash
# 测试多角色权限控制
./test-framework/scripts/core/test_runner.sh --type security

# 测试越权访问和输入验证
./test-framework/scripts/core/test_runner.sh --file ../security/permission_test.sh
```

### 数据库验证测试
```bash
# 测试数据完整性和约束
./test-framework/scripts/core/test_runner.sh --type database

# 测试性能和负载能力
./test-framework/scripts/core/test_runner.sh --file ../database/performance_test.sh
```

### 集成业务流程测试
```bash
# 测试完整业务流程
./test-framework/scripts/core/test_runner.sh --type integration

# 测试端到端用户体验
./test-framework/scripts/core/test_runner.sh --file ../integration/end_to_end_test.sh
```

## 🎯 核心功能

### 1. 统一测试运行器
```bash
# 运行所有测试
./test-framework/scripts/core/test_runner.sh --all

# 并行执行测试
./test-framework/scripts/core/test_runner.sh --all --parallel

# 只显示错误信息
./test-framework/scripts/core/test_runner.sh --all --errors-only
```

### 2. 测试数据管理
```bash
# 初始化测试数据
./test-framework/scripts/core/data_manager.sh init

# 创建数据快照
./test-framework/scripts/core/data_manager.sh snapshot before_test

# 恢复数据快照
./test-framework/scripts/core/data_manager.sh restore before_test

# 清理测试数据
./test-framework/scripts/core/data_manager.sh clean
```

### 3. 环境检查
```bash
# 检查测试环境
./test-framework/scripts/core/test_runner.sh --check-env

# 检查特定依赖
which curl mysql jq bc openssl
```

## 📊 测试覆盖范围

### API测试覆盖
- ✅ 用户管理 (注册、登录、信息管理)
- ✅ 菜单管理 (分类、菜品、搜索、推荐)
- ✅ 订单管理 (创建、查询、状态更新)
- ✅ 购物车管理 (增删改查)
- ✅ 管理员功能 (用户、菜单、订单管理)

### 安全测试覆盖
- ✅ 多角色权限控制 (游客、会员、管理员)
- ✅ 越权访问检测
- ✅ 输入验证和SQL注入防护
- ✅ 会话管理和认证安全

### 数据库测试覆盖
- ✅ 数据完整性验证
- ✅ 外键约束检查
- ✅ 事务完整性测试
- ✅ 性能基准测试

### 集成测试覆盖
- ✅ 完整用户流程 (注册→登录→下单)
- ✅ 管理员操作流程
- ✅ 并发访问和负载测试
- ✅ 异常情况处理

## 🔧 配置选项

### 测试运行配置
```bash
# 指定配置文件
./test-framework/scripts/core/test_runner.sh -c config/test_config.yml

# 启用调试模式
./test-framework/scripts/core/test_runner.sh --debug

# 指定日志级别
./test-framework/scripts/core/test_runner.sh --level DEBUG
```

### 报告生成配置
```bash
# 生成Markdown报告
./test-framework/scripts/core/test_runner.sh --report markdown

# 生成HTML报告
./test-framework/scripts/core/test_runner.sh --report html

# 生成JSON报告
./test-framework/scripts/core/test_runner.sh --report json
```

## 📈 测试结果分析

### 查看测试报告
```bash
# 查看最新测试报告
cat test-framework/scripts/core/reports/test_report_*.md

# 查看详细日志
tail -f test-framework/scripts/core/logs/test_framework.log
```

### 性能指标
- **API响应时间**: 优秀 < 500ms, 良好 < 1000ms
- **数据库查询**: 优秀 < 10ms, 良好 < 50ms
- **并发处理**: 优秀 < 1000ms (50并发用户)
- **错误率**: 要求 < 5%

## 🐛 常见问题

### 服务启动失败
```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 检查Java进程
ps aux | grep java
```

### 数据库连接失败
```bash
# 测试数据库连接
mysql -h localhost -u root -p123456 -e "USE web_order; SELECT 1;"

# 检查数据库状态
systemctl status mysql
```

### 测试脚本权限问题
```bash
# 赋予执行权限
chmod +x test-framework/scripts/**/*.sh
```

## 🚀 CI/CD集成

### GitHub Actions示例
```yaml
name: Automated Testing
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start backend
        run: |
          cd backend
          ./mvnw spring-boot:run &
          sleep 30
      - name: Run tests
        run: ./test-framework/scripts/core/test_runner.sh --all
      - name: Generate report
        run: ./test-framework/scripts/core/test_runner.sh --report markdown
```

### 质量门禁
```yaml
quality_gates:
  min_pass_rate: 90
  max_response_time: 1000
  max_error_rate: 0.05
  critical_issues_allowed: 0
```

## 📚 相关文档

- [详细使用指南](docs/测试框架使用指南.md)
- [API测试规范](docs/API测试规范.md)
- [权限测试规范](docs/权限测试规范.md)
- [测试场景示例](docs/测试场景示例.md)
- [配置说明](config/test_config.yml)

## 🔄 持续改进

1. **定期执行**: 建立定期的测试执行机制
2. **失败分析**: 及时分析测试失败的原因并修复
3. **覆盖优化**: 根据测试结果不断改进测试覆盖范围
4. **性能监控**: 关注性能指标的变化趋势

## 📞 技术支持

如果在使用测试框架时遇到问题：

1. 查看本文档的故障排除部分
2. 检查GitHub Issues中是否有类似问题
3. 创建新的Issue描述问题详情
4. 提供相关的日志和错误信息

---

**版本**: 1.0.0
**最后更新**: 2025-09-17
**维护者**: Web订餐管理系统开发团队