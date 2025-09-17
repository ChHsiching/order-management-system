# 《Web订餐管理系统.docx》核心信息提炼（数据库+application.yml配置）
## 一、数据库设计提炼（源自文档第5章“数据库设计”，重点5.4节）
### 1. 核心说明
- 数据库类型：MySQL（文档5.1节“成本低、安全性高、操作简单”）；  
- 数据库名：`web_order`（适配后续配置与建表逻辑）；  
- 表数量：5张，严格按“无依赖→有依赖”顺序创建（外键关联需遵循文档定义）。

### 2. 5张数据表结构（100%匹配文档5.4节“数据库表设计”）
#### 表1：管理员信息表（`administrators`，文档表5.1）
| 字段名       | 数据类型    | 主键 | 非空 | 字段说明（文档定义）                  | 备注                  |
|--------------|-------------|------|------|---------------------------------------|-----------------------|
| username     | VARCHAR(255)| 是   | 是   | 用户名                                | 文档指定为主键        |
| createtime   | DATETIME    | 否   | 否   | 注册时间（非必须）                    | -                     |
| email        | VARCHAR(255)| 否   | 是   | 邮箱                                  | -                     |
| password     | VARCHAR(255)| 否   | 是   | 密码                                  | -                     |
| phone        | VARCHAR(255)| 否   | 是   | 联系方式                              | -                     |
| qq           | VARCHAR(255)| 否   | 是   | QQ号                                  | -                     |
| role         | INT         | 否   | 否   | 用户权限（0-会员/1-管理员/2-接单员）  | 文档2.4节权限区分     |

#### 表2：分类信息表（`ltypes`，文档表5.2）
| 字段名       | 数据类型    | 主键 | 非空 | 字段说明（文档定义）                  | 备注                  |
|--------------|-------------|------|------|---------------------------------------|-----------------------|
| id           | BIGINT      | 否   | 否   | 主键                                  | -                     |
| catelock     | INT         | 否   | 否   | 删除标识（0-未删/1-已删）             | -                     |
| catename     | VARCHAR(255)| 是   | 是   | 分类名                                | 文档指定为主键        |
| address      | VARCHAR(255)| 否   | 是   | 地址                                  | -                     |
| productname  | VARCHAR(255)| 否   | 是   | 菜单名                                | -                     |

#### 表3：菜单信息表（`menu`，文档表5.5）
| 字段名       | 数据类型    | 主键 | 非空 | 字段说明（文档定义）                  | 备注                  |
|--------------|-------------|------|------|---------------------------------------|-----------------------|
| id           | BIGINT      | 是   | 否   | 主键                                  | 文档指定为主键        |
| createtime   | DATETIME    | 否   | 是   | 添加时间                              | -                     |
| imgpath      | VARCHAR(255)| 否   | 是   | 图片路径（菜品图片存储）              | 文档6.2.2节图片上传需求 |
| info5        | VARCHAR(255)| 否   | 是   | 菜品简介                              | -                     |
| name         | VARCHAR(255)| 否   | 否   | 菜单名                                | -                     |
| newstuijian  | INT         | 否   | 否   | 是否推荐（0-否/1-是）                | -                     |
| price1       | DOUBLE      | 否   | 是   | 原价                                  | -                     |
| price2       | DOUBLE      | 否   | 是   | 热销价                                | -                     |
| productlock  | INT         | 否   | 否   | 商品是否删除（0-未删/1-已删）         | -                     |
| xiaoliang    | INT         | 否   | 否   | 销量                                  | -                     |
| cateid       | BIGINT      | 否   | 是   | 关联分类ID                            | 外键，关联`ltypes.id` |

#### 表4：订单信息表（`cg_info`，文档表5.4）
| 字段名       | 数据类型    | 主键 | 非空 | 字段说明（文档定义）                  | 备注                  |
|--------------|-------------|------|------|---------------------------------------|-----------------------|
| id           | BIGINT      | 否   | 否   | 主键                                  | -                     |
| address      | VARCHAR(255)| 否   | 是   | 送货地址                              | -                     |
| createtime   | DATETIME    | 否   | 是   | 添加时间                              | -                     |
| orderid      | VARCHAR(255)| 否   | 是   | 订单号                                | -                     |
| phone        | VARCHAR(255)| 是   | 是   | 联系电话                              | 文档指定为主键        |
| status       | INT         | 否   | 否   | 订单状态（0-待受理/1-已受理）         | 文档6.2.4节订单管理   |
| totalprice   | DOUBLE      | 否   | 是   | 总价格                                | -                     |
| username     | VARCHAR(255)| 否   | 是   | 订单所属用户                          | 外键，关联`administrators.username` |

#### 表5：订单条目表（`the_order_entry`，文档表5.3）
| 字段名       | 数据类型    | 主键 | 非空 | 字段说明（文档定义）                  | 备注                  |
|--------------|-------------|------|------|---------------------------------------|-----------------------|
| id           | BIGINT      | 是   | 否   | 主键                                  | 文档指定为主键        |
| price        | DOUBLE      | 否   | 是   | 菜品单价                              | -                     |
| productid    | BIGINT      | 否   | 是   | 关联菜单ID                            | 外键，关联`menu.id`   |
| productname  | VARCHAR(255)| 否   | 是   | 菜单名                                | -                     |
| productnum   | INT         | 否   | 否   | 菜品数量                              | -                     |
| orderid      | VARCHAR(255)| 否   | 是   | 关联订单ID                            | 外键，关联`cg_info.orderid` |

### 3. 外键关联规则（文档明确要求）
1. `menu.cateid` → `ltypes.id`（菜单关联分类）；  
2. `cg_info.username` → `administrators.username`（订单关联用户）；  
3. `the_order_entry.productid` → `menu.id`（订单条目关联菜单）；  
4. `the_order_entry.orderid` → `cg_info.orderid`（订单条目关联订单）。


## 二、application.yml配置内容（基于文档技术栈+数据库设计）
### 核心说明
- 技术栈适配：Spring Boot 3.2.5（JDK17）、MyBatis-Plus 3.5.5、MySQL 8.0、Tomcat（内置）；  
- 配置项均源自文档需求（如服务器、数据库、接口文档），无文档外冗余配置。

```yaml
# 1. 服务器配置（文档3.1节“B/S结构”+开头技术栈“Tomcat”）
server:
  port: 8080  # 端口，适配常规Web访问
  servlet:
    context-path: /WebOrderSystem  # 项目访问根路径，贴合B/S结构跳转
  tomcat:
    uri-encoding: UTF-8  # 避免中文乱码（订单地址、菜品名等含中文）

# 2. 数据库配置（文档5.1节“MySQL数据库”+上述表设计）
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver  # MySQL 8.0驱动
    url: jdbc:mysql://localhost:3306/web_order?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    # 数据库名web_order，与上述设计一致；serverTimezone适配MySQL 8.0
    username: root  # 本地MySQL用户名（需根据实际环境修改）
    password: 123456  # 本地MySQL密码（需根据实际环境修改）
    type: com.zaxxer.hikari.HikariDataSource  # 连接池，保证性能

# 3. MyBatis-Plus配置（适配文档表CRUD，替代JPA）
mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml  # Mapper XML文件路径（存储自定义SQL）
  type-aliases-package: com.weborder.entity  # 实体类别名包（简化XML配置）
  configuration:
    map-underscore-to-camel-case: true  # 下划线转驼峰（如数据库imgpath→实体类imgpath）
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 打印SQL日志（调试表操作）
  global-config:
    db-config:
      id-type: AUTO  # 主键自增（匹配上述表主键规则）

# 4. 日志配置（便于调试文档功能，如订单提交、菜品查询）
logging:
  level:
    root: INFO
    com.weborder.mapper: DEBUG  # Mapper接口日志级别（查看SQL执行详情）
    org.springframework.web: INFO  # Web请求日志（排查接口访问问题）
```


## 三、使用说明（交给Cursor时补充）
1. 数据库部分：基于文档5.4节提炼，建表时需按“`administrators`→`ltypes`→`menu`→`cg_info`→`the_order_entry`”顺序执行（避免外键报错）；  
2. application.yml部分：需根据实际本地环境修改`spring.datasource.username`/`password`，确保与本地MySQL账号一致；  
3. 所有配置与表结构均严格匹配文档需求，无额外扩展内容，可直接用于后续开发。