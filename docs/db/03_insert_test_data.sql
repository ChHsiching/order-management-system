-- ============================================================================
-- Web订餐管理系统 - 测试数据插入脚本
-- 为CI/CD测试提供必要的测试数据，确保集成测试正常运行
-- 基于docs/db/01_create_database_and_tables.sql的表结构
-- ============================================================================

USE `web_order`;

-- 清理现有测试数据（避免重复插入）
DELETE FROM `the_order_entry`;
DELETE FROM `cg_info`;
DELETE FROM `menu`;
DELETE FROM `ltypes`;
DELETE FROM `administrators`;

-- 重置自增ID
ALTER TABLE `the_order_entry` AUTO_INCREMENT = 1;
ALTER TABLE `cg_info` AUTO_INCREMENT = 1;
ALTER TABLE `menu` AUTO_INCREMENT = 1;
ALTER TABLE `ltypes` AUTO_INCREMENT = 1;

-- 1. 插入测试分类数据
INSERT INTO `ltypes` (`id`, `catelock`, `catename`, `address`, `productname`) VALUES
(1, 0, '汉堡套餐', '主餐区', '汉堡系列'),
(2, 0, '小食套餐', '小食区', '小食系列'),
(3, 0, '饮品套餐', '饮品区', '饮品系列'),
(4, 0, '甜品套餐', '甜品区', '甜品系列'),
(5, 0, '家庭套餐', '套餐区', '家庭系列');

-- 2. 插入测试管理员数据
INSERT INTO `administrators` (`username`, `createtime`, `email`, `password`, `phone`, `qq`, `role`) VALUES
('test_admin', NOW(), 'admin@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIIT', '13800138000', '123456', 1),
('test_user', NOW(), 'user@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIIT', '13800138001', '123457', 0);

-- 3. 插入测试菜单数据
INSERT INTO `menu` (`id`, `createtime`, `imgpath`, `info5`, `name`, `newstuijian`, `price1`, `price2`, `productlock`, `xiaoliang`, `cateid`) VALUES
-- 汉堡套餐 (分类ID: 1)
(1, NOW(), '/images/burger1.jpg', '经典巨无霸汉堡，双层牛肉饼', '巨无霸汉堡', 1, 25.00, 22.00, 0, 150, 1),
(2, NOW(), '/images/burger2.jpg', '香辣鸡腿汉堡，鲜嫩多汁', '香辣鸡腿堡', 1, 20.00, 18.00, 0, 120, 1),
(3, NOW(), '/images/burger3.jpg', '鳕鱼汉堡，鲜美鳕鱼肉', '鳕鱼堡', 0, 22.00, 20.00, 0, 80, 1),
(4, NOW(), '/images/burger4.jpg', '牛肉汉堡，经典口味', '牛肉汉堡', 0, 18.00, 16.00, 0, 200, 1),

-- 小食套餐 (分类ID: 2)
(5, NOW(), '/images/snack1.jpg', '金黄薯条，外脆内软', '薯条', 0, 8.00, 6.00, 0, 300, 2),
(6, NOW(), '/images/snack2.jpg', '香辣鸡翅，6块装', '鸡翅', 1, 15.00, 12.00, 0, 180, 2),
(7, NOW(), '/images/snack3.jpg', '鸡块，8块装', '鸡块', 0, 12.00, 10.00, 0, 160, 2),

-- 饮品套餐 (分类ID: 3)
(8, NOW(), '/images/drink1.jpg', '冰爽可口可乐', '可口可乐', 0, 6.00, 5.00, 0, 400, 3),
(9, NOW(), '/images/drink2.jpg', '香滑热咖啡', '咖啡', 1, 12.00, 10.00, 0, 90, 3),
(10, NOW(), '/images/drink3.jpg', '鲜榨橙汁', '橙汁', 1, 8.00, 7.00, 0, 110, 3),

-- 甜品套餐 (分类ID: 4)
(11, NOW(), '/images/dessert1.jpg', '香草冰淇淋', '冰淇淋', 1, 10.00, 8.00, 0, 140, 4),
(12, NOW(), '/images/dessert2.jpg', '巧克力蛋糕', '蛋糕', 0, 15.00, 13.00, 0, 70, 4),

-- 家庭套餐 (分类ID: 5)
(13, NOW(), '/images/family1.jpg', '全家桶套餐', '全家桶', 1, 88.00, 78.00, 0, 60, 5),
(14, NOW(), '/images/family2.jpg', '双人套餐', '双人套餐', 0, 58.00, 48.00, 0, 45, 5),

-- 已下架菜品 (用于测试过滤逻辑)
(15, NOW(), '/images/old1.jpg', '旧款汉堡', '旧款汉堡', 0, 15.00, 12.00, 1, 20, 1);

-- 4. 插入测试订单数据
INSERT INTO `cg_info` (`id`, `address`, `createtime`, `orderid`, `phone`, `status`, `totalprice`, `username`) VALUES
(1, '北京市朝阳区某某街道123号', NOW(), 'ORDER001', '13800138000', 1, 45.00, 'test_user'),
(2, '上海市浦东新区某某路456号', NOW(), 'ORDER002', '13800138001', 0, 68.00, 'test_user'),
(3, '广州市天河区某某大道789号', NOW(), 'ORDER003', '13800138002', 1, 88.00, 'test_user');

-- 5. 插入测试订单条目数据
INSERT INTO `the_order_entry` (`id`, `price`, `productid`, `productname`, `productnum`, `orderid`) VALUES
(1, 22.00, 1, '巨无霸汉堡', 1, 'ORDER001'),
(2, 6.00, 5, '薯条', 2, 'ORDER001'),
(3, 5.00, 8, '可口可乐', 2, 'ORDER001'),
(4, 18.00, 2, '香辣鸡腿堡', 2, 'ORDER002'),
(5, 10.00, 7, '鸡块', 1, 'ORDER002'),
(6, 7.00, 10, '橙汁', 2, 'ORDER002'),
(7, 78.00, 13, '全家桶', 1, 'ORDER003'),
(8, 10.00, 9, '咖啡', 1, 'ORDER003');

-- 数据插入完成提示
SELECT '测试数据插入完成！' AS message;

-- 显示插入的数据统计
SELECT
    (SELECT COUNT(*) FROM ltypes WHERE catelock = 0) as active_categories,
    (SELECT COUNT(*) FROM menu WHERE productlock = 0) as active_menus,
    (SELECT COUNT(*) FROM menu WHERE newstuijian = 1 AND productlock = 0) as recommended_menus,
    (SELECT COUNT(*) FROM administrators) as total_users,
    (SELECT COUNT(*) FROM cg_info) as total_orders;