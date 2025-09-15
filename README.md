# Web订餐管理系统 / Web Order Management System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

基于Spring Boot的Web订餐管理系统，为餐厅运营提供完整的订单管理、菜单管理和用户管理功能。

A Spring Boot-based web order management system designed for restaurant operations, providing complete order management, menu management, and user management capabilities.

## 🌟 主要特性 / Key Features

- 📊 **订单管理** / Order Management - 完整的订单生命周期管理
- 🍽️ **菜单管理** / Menu Management - 菜品分类、价格、库存管理
- 👥 **用户管理** / User Management - 会员注册、权限控制
- 🔐 **安全认证** / Security - Spring Security安全框架
- 📱 **响应式设计** / Responsive Design - 支持多设备访问
- 📖 **API文档** / API Documentation - 集成Swagger文档

## 🛠️ 技术栈 / Technology Stack

### 后端 / Backend
- **框架**: Spring Boot 3.5.5
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.14
- **安全**: Spring Security
- **文档**: SpringDoc OpenAPI (Swagger)
- **构建**: Maven 3.8+

### 项目结构 / Project Structure
```
order-management-system/
├── backend/                    # Spring Boot后端应用
│   ├── src/main/java/tech/chhsich/backend/
│   │   ├── controller/         # REST API控制器
│   │   ├── service/           # 业务逻辑层
│   │   ├── mapper/            # MyBatis数据访问层
│   │   ├── entity/            # 数据库实体
│   │   └── config/            # 配置类
│   ├── src/main/resources/
│   │   ├── application.yml    # 主配置文件
│   │   ├── static/            # 静态资源
│   │   ├── templates/         # Thymeleaf模板
│   │   └── mapper/            # MyBatis XML映射
│   └── pom.xml
├── docs/                      # 文档
│   └── tech/
└── .github/                   # GitHub工作流和模板
```

## 🚀 快速开始 / Quick Start

### 环境要求 / Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Git

### 安装步骤 / Installation Steps

1. **克隆仓库** / Clone the repository
   ```bash
   git clone https://github.com/ChHsiching/order-management-system.git
   cd order-management-system
   ```

2. **数据库设置** / Database Setup
   ```sql
   -- 创建数据库
   CREATE DATABASE web_order;

   -- 执行数据库脚本
   mysql -u root -p web_order < docs/db/01_create_database_and_tables.sql
   mysql -u root -p web_order < docs/db/02_insert_sample_data.sql
   ```

3. **配置应用** / Application Configuration
   ```bash
   cd backend
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   # 编辑 application.yml，配置数据库连接信息
   ```

4. **构建和运行** / Build and Run
   ```bash
   # 编译项目
   mvn clean compile

   # 运行测试
   mvn test

   # 启动应用
   mvn spring-boot:run
   ```

5. **访问应用** / Access the Application
   - 应用地址: http://localhost:8080/WebOrderSystem
   - API文档: http://localhost:8080/WebOrderSystem/swagger-ui.html
   - 默认管理员: admin / admin

## 📖 API文档 / API Documentation

系统集成了Swagger API文档，启动后可通过以下地址访问：

- **Swagger UI**: http://localhost:8080/WebOrderSystem/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/WebOrderSystem/api-docs

## 🤝 贡献指南 / Contributing

我们欢迎所有形式的贡献！请阅读 [贡献指南](CONTRIBUTING.md) 了解如何参与项目开发。

We welcome all forms of contributions! Please read the [Contributing Guide](CONTRIBUTING.md) to learn how to participate in project development.

### 开发规范 / Development Standards
- Git提交规范: `[gitmoji] type(module): description`
- 分支管理: `main`/`develop`/`feature`/`bugfix`
- 代码规范: 遵循项目既定命名约定

详细规范请参考: [开发规范.md](docs/tech/开发规范.md)

## 📄 许可证 / License

本项目采用 MIT 许可证 - 详情请查看 [LICENSE](LICENSE) 文件。

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔒 安全政策 / Security Policy

请查看 [安全政策](SECURITY.md) 了解如何报告安全漏洞。

Please see the [Security Policy](SECURITY.md) for information on how to report security vulnerabilities.

## 📞 联系我们 / Contact Us

- 项目维护者: ChHsiching
- 邮箱: hsichingchang@gmail.com
- 项目主页: https://github.com/ChHsiching/order-management-system

## 🙏 致谢 / Acknowledgments

- Spring Boot团队提供优秀的框架支持
- MyBatis团队提供持久层解决方案
- 所有贡献者和使用者

---

## 📊 项目统计 / Project Statistics

![GitHub Stars](https://img.shields.io/github/stars/ChHsiching/order-management-system?style=social)
![GitHub Forks](https://img.shields.io/github/forks/ChHsiching/order-management-system?style=social)
![GitHub Issues](https://img.shields.io/github/issues/ChHsiching/order-management-system)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/ChHsiching/order-management-system)

## 📝 更新日志 / Changelog

### [v1.0.0] - 2024-09-15
- 初始版本发布
- 基础订单管理功能
- 用户认证系统
- 菜单管理功能