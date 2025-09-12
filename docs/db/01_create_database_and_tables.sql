-- ============================================================================
-- Web订餐管理系统 - 数据库创建脚本
-- 基于文档：docs/tech/Web订餐管理系统.docx 第5.4节数据库表设计
-- 创建顺序：无依赖表 → 有依赖表（遵循外键约束）
-- ============================================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `web_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `web_order`;

-- 2. 创建表（按依赖关系顺序创建）

-- 表1：管理员信息表（无外键依赖，优先创建）
-- 文档表5.1：存储用户、管理员、接单员信息
DROP TABLE IF EXISTS `administrators`;
CREATE TABLE `administrators` (
    `username` VARCHAR(255) NOT NULL COMMENT '用户名',
    `createtime` DATETIME NULL COMMENT '注册时间（非必须）',
    `email` VARCHAR(255) NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `phone` VARCHAR(255) NOT NULL COMMENT '联系方式',
    `qq` VARCHAR(255) NOT NULL COMMENT 'QQ号',
    `role` INT NULL DEFAULT 0 COMMENT '用户权限（0-会员/1-管理员/2-接单员）',
    PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员信息表';

-- 表2：分类信息表（无外键依赖，优先创建）
-- 文档表5.2：存储菜品分类信息
DROP TABLE IF EXISTS `ltypes`;
CREATE TABLE `ltypes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `catelock` INT NULL DEFAULT 0 COMMENT '删除标识（0-未删/1-已删）',
    `catename` VARCHAR(255) NOT NULL COMMENT '分类名',
    `address` VARCHAR(255) NOT NULL COMMENT '地址',
    `productname` VARCHAR(255) NOT NULL COMMENT '菜单名',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_catename` (`catename`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类信息表';

-- 表3：菜单信息表（依赖ltypes表）
-- 文档表5.5：存储菜品详细信息
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `createtime` DATETIME NOT NULL COMMENT '添加时间',
    `imgpath` VARCHAR(255) NOT NULL COMMENT '图片路径（菜品图片存储）',
    `info5` VARCHAR(255) NOT NULL COMMENT '菜品简介',
    `name` VARCHAR(255) NULL COMMENT '菜单名',
    `newstuijian` INT NULL DEFAULT 0 COMMENT '是否推荐（0-否/1-是）',
    `price1` DOUBLE NOT NULL COMMENT '原价',
    `price2` DOUBLE NOT NULL COMMENT '热销价',
    `productlock` INT NULL DEFAULT 0 COMMENT '商品是否删除（0-未删/1-已删）',
    `xiaoliang` INT NULL DEFAULT 0 COMMENT '销量',
    `cateid` BIGINT NOT NULL COMMENT '关联分类ID',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`cateid`) REFERENCES `ltypes`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单信息表';

-- 表4：订单信息表（依赖administrators表）
-- 文档表5.4：存储订单基本信息
DROP TABLE IF EXISTS `cg_info`;
CREATE TABLE `cg_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `address` VARCHAR(255) NOT NULL COMMENT '送货地址',
    `createtime` DATETIME NOT NULL COMMENT '添加时间',
    `orderid` VARCHAR(255) NOT NULL COMMENT '订单号',
    `phone` VARCHAR(255) NOT NULL COMMENT '联系电话',
    `status` INT NULL DEFAULT 0 COMMENT '订单状态（0-待受理/1-已受理）',
    `totalprice` DOUBLE NOT NULL COMMENT '总价格',
    `username` VARCHAR(255) NOT NULL COMMENT '订单所属用户',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_orderid` (`orderid`),
    FOREIGN KEY (`username`) REFERENCES `administrators`(`username`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单信息表';

-- 表5：订单条目表（依赖menu和cg_info表）
-- 文档表5.3：存储订单中的具体菜品条目
DROP TABLE IF EXISTS `the_order_entry`;
CREATE TABLE `the_order_entry` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `price` DOUBLE NOT NULL COMMENT '菜品单价',
    `productid` BIGINT NOT NULL COMMENT '关联菜单ID',
    `productname` VARCHAR(255) NOT NULL COMMENT '菜单名',
    `productnum` INT NULL DEFAULT 1 COMMENT '菜品数量',
    `orderid` VARCHAR(255) NOT NULL COMMENT '关联订单ID',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`productid`) REFERENCES `menu`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (`orderid`) REFERENCES `cg_info`(`orderid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单条目表';

-- 创建索引以提高查询性能
CREATE INDEX `idx_menu_cateid` ON `menu`(`cateid`);
CREATE INDEX `idx_menu_productlock` ON `menu`(`productlock`);
CREATE INDEX `idx_menu_newstuijian` ON `menu`(`newstuijian`);
CREATE INDEX `idx_cg_info_username` ON `cg_info`(`username`);
CREATE INDEX `idx_cg_info_status` ON `cg_info`(`status`);
CREATE INDEX `idx_cg_info_createtime` ON `cg_info`(`createtime`);
CREATE INDEX `idx_order_entry_orderid` ON `the_order_entry`(`orderid`);
CREATE INDEX `idx_order_entry_productid` ON `the_order_entry`(`productid`);

-- 脚本执行完成提示
SELECT 'Web订餐管理系统数据库和表结构创建完成！' AS message;
