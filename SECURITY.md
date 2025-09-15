# 安全政策 / Security Policy

## 🛡️ 安全承诺 / Security Commitment

Web订餐管理系统的开发和维护团队致力于保护用户数据和系统安全。我们认真对待所有安全报告，并会及时响应和修复已知的安全问题。

The Web Order Management System development and maintenance team is committed to protecting user data and system security. We take all security reports seriously and will respond promptly to fix known security issues.

## 🚨 报告安全漏洞 / Reporting Security Vulnerabilities

如果您发现了安全漏洞，请通过以下方式联系我们：

If you discover a security vulnerability, please contact us through the following methods:

### 📧 首选联系方式 / Preferred Contact Method
- **邮箱**: hsichingchang@gmail.com
- **PGP密钥**: 可根据要求提供

### 📝 报告内容要求 / Report Content Requirements

请您的安全报告包含以下信息：

Please include the following information in your security report:

```markdown
## 漏洞概述 / Vulnerability Overview
- 漏洞类型（如XSS、SQL注入、权限绕过等）
- 影响的组件和版本
- 严重性评级（低/中/高/严重）

## 复现步骤 / Reproduction Steps
1. 详细的环境配置
2. 逐步的复现过程
3. 预期vs实际行为

## 影响范围 / Impact Scope
- 受影响的功能模块
- 潜在的数据泄露风险
- 系统可用性影响

## 建议修复方案 / Suggested Fix
- 您的修复建议
- 相关的安全配置建议
```

### ⏰ 响应时间 / Response Time

我们承诺在以下时间内响应安全报告：

We commit to responding to security reports within the following timeframes:

- **严重漏洞**: 24小时内响应
- **高危漏洞**: 48小时内响应
- **中危漏洞**: 72小时内响应
- **低危漏洞**: 5个工作日内响应

## 🔒 安全最佳实践 / Security Best Practices

### 开发安全 / Development Security

#### 1. 输入验证 / Input Validation
```java
// 示例：输入验证
@Valid
public class UserRegistrationDto {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

#### 2. SQL注入防护 / SQL Injection Protection
```java
// 使用MyBatis参数化查询
@Select("SELECT * FROM users WHERE username = #{username}")
User findByUsername(String username);

// 避免字符串拼接
// 危险：不要这样做
// @Select("SELECT * FROM users WHERE username = '" + username + "'")
```

#### 3. 密码安全 / Password Security
```yaml
# application.yml - 密码编码配置
spring:
  security:
    user:
      password: {noop}admin  # 仅开发环境使用
```

### 配置安全 / Configuration Security

#### 1. 敏感信息保护 / Sensitive Data Protection
```yaml
# 环境变量配置
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/web_order}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
```

#### 2. 文件上传安全 / File Upload Security
```java
// 文件上传验证
@PostMapping("/upload")
public ResponseEntity<?> uploadFile(
    @RequestParam("file") MultipartFile file) {

    // 验证文件类型
    if (!file.getContentType().startsWith("image/")) {
        return ResponseEntity.badRequest().body("只允许上传图片文件");
    }

    // 验证文件大小
    if (file.getSize() > 10 * 1024 * 1024) { // 10MB
        return ResponseEntity.badRequest().body("文件大小不能超过10MB");
    }

    // 继续处理...
}
```

### 网络安全 / Network Security

#### 1. HTTPS配置 / HTTPS Configuration
```yaml
# 生产环境HTTPS配置
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

#### 2. CORS配置 / CORS Configuration
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://yourdomain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

## 📋 安全检查清单 / Security Checklist

### 开发阶段 / Development Phase

- [ ] 输入验证和输出编码
- [ ] 参数化查询防止SQL注入
- [ ] 敏感数据加密存储
- [ ] 错误信息不暴露敏感信息
- [ ] 文件上传安全检查
- [ ] 会话管理安全

### 部署阶段 / Deployment Phase

- [ ] 使用HTTPS协议
- [ ] 数据库连接安全配置
- [ ] 服务器安全加固
- [ ] 日志监控配置
- [ ] 备份策略实施
- [ ] 访问权限控制

### 运维阶段 / Operations Phase

- [ ] 定期安全更新
- [ ] 漏洞扫描执行
- [ ] 访问日志审计
- [ ] 应急响应演练
- [ ] 安全意识培训

## 🔧 已知安全问题 / Known Security Issues

### 当前版本 / Current Version (v1.0.0)

| 问题 | 严重性 | 状态 | 修复时间 |
|------|--------|------|----------|
| 默认管理员密码 | 中 | 已记录 | 需用户修改 |
| 开发环境调试模式 | 低 | 已记录 | 生产环境禁用 |
| 缺少API限流 | 中 | 计划修复 | v1.1.0 |

### 修复进度 / Fix Progress

- ✅ 基础认证实施
- ✅ 输入验证框架
- 🔄 API限流开发中
- 📅 审计日志计划中

## 🚀 安全更新 / Security Updates

### 更新策略 / Update Strategy

1. **紧急安全补丁**: 立即发布，包含详细的安全公告
2. **计划安全更新**: 定期发布，包含在版本更新说明中
3. **安全公告**: 通过GitHub Security Advisories发布

### 支持周期 / Support Period

- **当前版本**: v1.0.x - 安全支持至2025年9月
- **安全修复**: 所有版本的安全问题都会在合理时间内修复
- **向后兼容**: 安全更新通常保持向后兼容

## 📞 联系信息 / Contact Information

### 安全团队 / Security Team

- **安全负责人**: ChHsiching
- **邮箱**: hsichingchang@gmail.com
- **GitHub**: https://github.com/ChHsiching
- **项目地址**: https://github.com/ChHsiching/order-management-system

### 相关资源 / Related Resources

- [GitHub Security Advisories](https://github.com/ChHsiching/order-management-system/security/advisories)
- [OWASP安全指南](https://owasp.org/www-project-top-ten/)
- [Spring Security文档](https://docs.spring.io/spring-security/reference/)

## 📜 安全政策更新 / Policy Updates

本安全政策可能会根据项目发展和安全需求的变化进行更新。重大变更将会：

This security policy may be updated based on project development and changing security requirements. Major changes will:

- 在项目公告中提前通知
- 提供变更说明和生效时间
- 在GitHub Releases中记录变更历史

---

*最后更新: 2024年9月15日 / Last updated: September 15, 2024*