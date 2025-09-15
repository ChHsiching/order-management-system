# 贡献指南 / Contributing Guide

感谢您对Web订餐管理系统的兴趣！我们欢迎所有形式的贡献。

Thank you for your interest in contributing to the Web Order Management System! We welcome all forms of contributions.

## 📋 目录 / Table of Contents

- [🤝 如何贡献](#如何贡献)
- [🐛 报告Bug](#报告bug)
- [✨ 请求新功能](#请求新功能)
- [💻 代码贡献](#代码贡献)
- [📝 Git规范](#git规范)
- [🌿 分支管理](#分支管理)
- [🔍 Pull Request流程](#pull-request流程)
- [📏 代码规范](#代码规范)
- [🧪 测试要求](#测试要求)
- [📚 文档贡献](#文档贡献)

## 🤝 如何贡献 / How to Contribute

### 1. 报告问题 / Report Issues
- 使用GitHub Issues报告bug或请求新功能
- 使用现有的Issue模板提供详细信息
- 搜索现有问题避免重复报告

### 2. 贡献代码 / Contribute Code
- Fork项目仓库
- 创建功能分支
- 提交代码更改
- 创建Pull Request

### 3. 改进文档 / Improve Documentation
- 修正拼写错误
- 添加API文档
- 完善使用说明

## 🐛 报告Bug / Reporting Bugs

### Bug报告模板 / Bug Report Template

使用GitHub Issues时，请包含以下信息：

```markdown
## Bug描述 / Bug Description
简要描述bug问题

## 复现步骤 / Steps to Reproduce
1. 执行操作A
2. 执行操作B
3. 观察到错误结果

## 期望行为 / Expected Behavior
描述应该发生的正确行为

## 实际行为 / Actual Behavior
描述实际观察到的行为

## 环境信息 / Environment Information
- 操作系统: [例如 Windows 10, Ubuntu 20.04]
- 浏览器: [例如 Chrome 96, Firefox 95]
- Java版本: [例如 Java 17.0.1]
- 项目版本: [例如 v1.0.0]

## 错误日志 / Error Logs
```log
# 粘贴相关错误日志
```

## 附加信息 / Additional Information
任何其他有助于理解问题的信息
```

## ✨ 请求新功能 / Requesting Features

### 功能请求模板 / Feature Request Template

```markdown
## 功能概述 / Feature Summary
简要描述请求的功能

## 问题背景 / Problem Statement
这个功能解决了什么问题？

## 解决方案建议 / Proposed Solution
建议的实现方案

## 替代方案 / Alternative Solutions
考虑过的其他解决方案

## 附加信息 / Additional Information
相关截图、文档链接等
```

## 💻 代码贡献 / Code Contributions

### 开发环境设置 / Development Environment Setup

1. **Fork项目** / Fork the Repository
   ```bash
   # 1. Fork项目到您的GitHub账户
   # 2. 克隆您的Fork
   git clone https://github.com/YOUR_USERNAME/order-management-system.git
   cd order-management-system
   ```

2. **配置上游仓库** / Configure Upstream Repository
   ```bash
   git remote add upstream https://github.com/ChHsiching/order-management-system.git
   ```

3. **安装依赖** / Install Dependencies
   ```bash
   cd backend
   mvn clean install
   ```

### 代码开发流程 / Code Development Workflow

1. **同步最新代码** / Sync Latest Code
   ```bash
   git checkout develop
   git pull upstream develop
   ```

2. **创建功能分支** / Create Feature Branch
   ```bash
   git checkout -b feature/模块-功能名
   # 或 bugfix/模块-问题描述
   ```

3. **开发代码** / Develop Code
   ```bash
   # 编写代码和测试
   mvn clean compile
   mvn test
   ```

4. **提交代码** / Commit Code
   ```bash
   git add .
   git commit -m "✨ feat(模块): 功能描述"
   ```

5. **推送到Fork** / Push to Fork
   ```bash
   git push origin feature/模块-功能名
   ```

6. **创建Pull Request** / Create Pull Request

## 📝 Git规范 / Git Commit Standards

### 提交格式 / Commit Format

```bash
[gitmoji] 类型(模块): 核心描述
```

### 常用Gitmoji和类型 / Common Gitmoji and Types

| Gitmoji | 类型 | 适用场景 | 示例 |
|---------|------|----------|------|
| ✨ | feat | 新增功能 | `✨ feat(会员): 新增手机号验证码注册功能` |
| 🐛 | fix | 修复bug | `🐛 fix(订单): 修复订单金额计算错误` |
| 🎨 | style | 样式调整 | `🎨 style(菜品): 优化菜品卡片布局` |
| ♻️ | refactor | 重构代码 | `♻️ refactor(支付): 拆分支付接口冗余逻辑` |
| 📝 | docs | 文档更新 | `📝 docs(接口): 补充订单提交接口说明` |
| ✅ | test | 测试相关 | `✅ test(登录): 新增密码错误测试用例` |
| 🔥 | remove | 删除代码 | `🔥 remove(旧功能): 删除废弃的线下支付代码` |
| 📦 | chore | 构建相关 | `📦 chore(环境): 升级MySQL驱动版本` |

### 提交前检查 / Pre-commit Checklist

- [ ] 本地测试通过
- [ ] 代码符合项目规范
- [ ] 单次提交只做一件事
- [ ] 提交信息清晰明确
- [ ] 避免提交配置文件

## 🌿 分支管理 / Branch Management

### 分支类型 / Branch Types

| 分支名 | 用途 | 权限 |
|--------|------|------|
| `main` | 生产环境代码 | 禁止直接提交 |
| `develop` | 开发主分支 | 禁止直接提交 |
| `feature/模块-功能` | 功能开发 | 开发者专用 |
| `bugfix/模块-问题` | Bug修复 | 开发者专用 |
| `test/测试目的` | 测试分支 | 开发者专用 |

### 分支操作流程 / Branch Operations

#### 创建功能分支 / Create Feature Branch
```bash
# 1. 切换到develop分支
git checkout develop

# 2. 拉取最新代码
git pull upstream develop

# 3. 创建功能分支
git checkout -b feature/会员-注册
```

#### 合并功能分支 / Merge Feature Branch
```bash
# 1. 推送分支
git push origin feature/会员-注册

# 2. 在GitHub创建PR，目标分支为develop

# 3. PR合并后删除分支
git checkout develop
git branch -d feature/会员-注册
git push origin --delete feature/会员-注册
```

## 🔍 Pull Request流程 / Pull Request Process

### PR标题格式 / PR Title Format

```bash
[类型] 模块-核心功能
```

示例：`[feat] 会员-新增手机号验证码注册`

### PR内容要求 / PR Content Requirements

```markdown
### 1. 功能说明 / Feature Description
- 新增了什么功能
- 解决了什么问题
- 实现思路简述

### 2. 测试情况 / Testing Status
- 测试场景1: 描述 → 结果
- 测试场景2: 描述 → 结果
- 自动化测试结果

### 3. 依赖说明 / Dependencies
- 需要配置的内容
- 依赖的其他模块
- 数据库变更等

### 4. 相关Issues / Related Issues
- Closes #123
- Related #456
```

### PR检查清单 / PR Checklist

- [ ] 代码编译通过
- [ ] 所有测试通过
- [ ] 代码符合项目规范
- [ ] 已更新相关文档
- [ ] 提交信息符合规范
- [ ] 功能测试通过
- [ ] 性能影响评估

## 📏 代码规范 / Code Standards

### 命名规范 / Naming Conventions

| 类型 | 规范 | 示例 | 错误示例 |
|------|------|------|----------|
| 类名 | 大驼峰 | `UserService` | `userService` |
| 方法名 | 小驼峰 | `getUserById()` | `GetUserById()` |
| 变量名 | 小驼峰 | `userId` | `user_id` |
| 常量 | 全大写+下划线 | `MAX_ORDER_NUM` | `maxOrderNum` |
| 数据库表 | 小写+下划线 | `user_info` | `UserInfo` |
| 数据库字段 | 小写+下划线 | `user_id` | `userId` |

### 代码风格 / Code Style

- 使用4个空格缩进
- 类和方法使用JavaDoc注释
- 避免过长的行（建议120字符以内）
- 使用有意义的变量名和方法名
- 遵循SOLID原则

### 注释规范 / Comment Standards

```java
/**
 * 用户服务类
 * 提供用户相关的业务逻辑处理
 */
@Service
public class UserService {

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息，不存在时返回null
     */
    public User getUserById(Long userId) {
        // 方法实现
    }
}
```

## 🧪 测试要求 / Testing Requirements

### 单元测试 / Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_ShouldReturnUser() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Test User", result.getName());
    }
}
```

### 测试覆盖率 / Test Coverage

- 目标覆盖率：80%以上
- 核心业务逻辑：90%以上
- 工具：JaCoCo

### 集成测试 / Integration Testing

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 生成测试覆盖率报告
mvn clean test jacoco:report
```

## 📚 文档贡献 / Documentation Contributions

### API文档 / API Documentation

- 使用Swagger注解
- 提供参数说明
- 包含示例请求和响应

### 代码注释 / Code Comments

- 复杂逻辑需要注释说明
- 公共API必须有JavaDoc
- 配置项需要说明用途

### README文档 / README Documentation

- 更新安装说明
- 添加新功能介绍
- 修正错误信息

## 🎯 贡献者认可 / Contributor Recognition

所有贡献者都会在以下地方获得认可：

- 项目README的贡献者列表
- 发布公告中的致谢
- 代码提交历史记录

## 📞 获取帮助 / Getting Help

如果您在贡献过程中遇到问题：

- 查看项目文档：[docs/tech/开发规范.md](docs/tech/开发规范.md)
- 提交Issue：使用GitHub Issues
- 联系维护者：hsichingchang@gmail.com

---

*本贡献指南基于项目实际开发规范制定，最后更新：2024年9月15日*

*This contributing guide is based on the project's actual development standards, last updated: September 15, 2024*