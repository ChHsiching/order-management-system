---
## 📋 Pull Request 标准模板 / Standard PR Template

### 📝 基本信息 / Basic Information
- **PR类型**: [ ] 新功能 [ ] Bug修复 [ ] 文档更新 [ ] 重构 [ ] 其他
- **相关Issue**: 请关联相关Issue号码 (例如: Closes #123)
- **目标分支**: `develop`
- **审查者**: @ChHsiching

---

## 🎯 功能说明 / Feature Description

### 简要概述 / Brief Overview
（用1-2句话描述这个PR做了什么）

### 详细说明 / Detailed Description
（详细说明实现的功能、解决的问题、技术方案等）

### 变更内容 / Changes Made
- [ ] 新增功能模块: _________
- [ ] 修复Bug: _________
- [ ] 优化性能: _________
- [ ] 代码重构: _________
- [ ] 文档更新: _________
- [ ] 其他: _________

---

## 🧪 测试情况 / Testing Status

### 功能测试 / Functional Testing
- [ ] 核心功能测试通过 ✅
- [ ] 边界条件测试通过 ✅
- [ ] 异常情况处理测试通过 ✅
- [ ] 用户界面交互正常 ✅

### 测试场景 / Test Scenarios
| 测试场景 | 输入数据 | 预期结果 | 实际结果 | 状态 |
|----------|----------|----------|----------|------|
| 场景1 | | | | ✅/❌ |
| 场景2 | | | | ✅/❌ |
| 场景3 | | | | ✅/❌ |

### 自动化测试 / Automated Testing
```bash
# 运行测试命令和结果
mvn test
# 测试覆盖率
mvn clean test jacoco:report
```

### 兼容性测试 / Compatibility Testing
- [ ] 浏览器兼容性: Chrome, Firefox, Safari, Edge
- [ ] 设备兼容性: 桌面端, 移动端
- [ ] 数据库兼容性: MySQL 8.0+

---

## 📋 检查清单 / Checklist

### 代码质量 / Code Quality
- [ ] 代码符合项目命名规范 ✅
- [ ] 方法复杂度合理 ✅
- [ ] 重复代码已消除 ✅
- [ ] 代码注释充分 ✅
- [ ] 日志记录适当 ✅

### 提交规范 / Commit Standards
- [ ] 提交信息格式正确: `[gitmoji] type(module): description` ✅
- [ ] 每次提交只做一件事 ✅
- [ ] 提交历史清晰 ✅

### 文档更新 / Documentation Updates
- [ ] API文档已更新 ✅
- [ ] README已更新 ✅
- [ ] 注释文档完整 ✅
- [ ] 配置说明清晰 ✅

### 性能影响 / Performance Impact
- [ ] 性能测试已执行 ✅
- [ ] 无明显性能下降 ✅
- [ ] 内存使用合理 ✅
- [ ] 数据库查询优化 ✅

---

## 🔧 依赖说明 / Dependencies

### 新增依赖 / New Dependencies
```xml
<!-- 如果有新增依赖，请在这里列出 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>example-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 配置变更 / Configuration Changes
```yaml
# application.yml 配置变更
spring:
  # 新增或修改的配置项
  some:
    config: value
```

### 数据库变更 / Database Changes
```sql
-- 数据库表结构变更
ALTER TABLE table_name ADD COLUMN new_column VARCHAR(255);

-- 新增索引
CREATE INDEX idx_table_name_column ON table_name(column_name);
```

### 环境要求 / Environment Requirements
- [ ] 需要特定的Java版本: _________
- [ ] 需要特定的数据库版本: _________
- [ ] 需要额外的系统配置: _________

---

## 🚀 部署说明 / Deployment Instructions

### 部署步骤 / Deployment Steps
1. _________
2. _________
3. _________

### 回滚方案 / Rollback Plan
- 如果出现问题，可以回滚到哪个版本: _________
- 回滚步骤: _________

---

## 📸 截图展示 / Screenshots

### 功能展示 / Feature Demonstration
（如果有UI变更，请提供截图）

### 测试结果 / Test Results
（测试运行结果截图或日志）

---

## 📊 相关资源 / Related Resources

### Issues链接 / Issue Links
- Closes #123
- Related #456
- Depends on #789

### 文档链接 / Documentation Links
- [设计文档]()
- [API文档]()
- [用户手册]()

### 参考资源 / Reference Resources
- [技术文档1]()
- [相关项目]()

---

## 🤔 问题讨论 / Discussion Points

### 已知问题 / Known Issues
- [ ] 问题1: _________
- [ ] 问题2: _________

### 待办事项 / TODO Items
- [ ] 待优化点1: _________
- [ ] 待优化点2: _________

### 后续计划 / Future Plans
- _________
- _________

---

## 🎉 额外说明 / Additional Information

### 特别感谢 / Special Thanks
- 感谢 _________ 的帮助和建议
- 参考 _________ 的实现方案

### 学习收获 / Learning Outcomes
- 在这个PR中学到的技术点
- 解决的技术难题

---

## ✅ 最终确认 / Final Confirmation

我确认：

- [ ] 我已经阅读并遵守了[贡献指南](../CONTRIBUTING.md)
- [ ] 我已经阅读并遵守了[行为准则](../CODE_OF_CONDUCT.md)
- [ ] 我的代码已经过充分测试
- [ ] 我已经更新了相关文档
- [ ] 我理解这个PR可能会被要求修改
- [ ] 我同意维护者可能会对代码进行必要的调整

---

**审查者请关注:**
- 代码质量和规范性
- 功能实现的完整性
- 测试覆盖的充分性
- 文档更新的完整性
- 性能影响评估

*请在审查时使用GitHub的Review功能，对代码行进行评论和建议。*