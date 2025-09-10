-- ============================================================================
-- Web订餐管理系统 - 麦当劳样例数据插入脚本
-- 基于文档：docs/tech/Web订餐管理系统.docx 第5.4节数据库表设计
-- 插入顺序：无依赖表 → 有依赖表（遵循外键约束）
-- 使用麦当劳真实菜品数据，包含汉堡、鸡肉、薯条、甜品、饮料等分类
-- ============================================================================

USE `web_order`;

-- 清理现有数据（开发测试用，生产环境请谨慎使用）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `the_order_entry`;
TRUNCATE TABLE `cg_info`;
TRUNCATE TABLE `menu`;
TRUNCATE TABLE `ltypes`;
TRUNCATE TABLE `administrators`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 插入管理员信息表数据
-- 包含不同角色：会员(0)、管理员(1)、接单员(2)
INSERT INTO `administrators` (`username`, `createtime`, `email`, `password`, `phone`, `qq`, `role`) VALUES
('admin', NOW(), 'admin@weborder.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTrEfp8UcWMYI7AirLW5VKMhG1PoIyJ6', '13800138000', '1000001', 1),
('manager', NOW(), 'manager@weborder.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTrEfp8UcWMYI7AirLW5VKMhG1PoIyJ6', '13800138001', '1000002', 1),
('waiter01', NOW(), 'waiter01@weborder.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTrEfp8UcWMYI7AirLW5VKMhG1PoIyJ6', '13800138002', '1000003', 2),
('waiter02', NOW(), 'waiter02@weborder.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTrEfp8UcWMYI7AirLW5VKMhG1PoIyJ6', '13800138003', '1000004', 2),
('user001', NOW(), 'user001@weborder.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTrEfp8UcWMYI7AirLW5VKMhG1PoIyJ6', '13800138004', '1000005', 0),
('user002', NOW(), 'user002@weborder.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTrEfp8UcWMYI7AirLW5VKMhG1PoIyJ6', '13800138005', '1000006', 0),
('user003', NOW(), 'user003@weborder.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTrEfp8UcWMYI7AirLW5VKMhG1PoIyJ6', '13800138006', '1000007', 0);

-- 2. 插入分类信息表数据
-- 麦当劳菜品分类
INSERT INTO `ltypes` (`catename`, `catelock`, `address`, `productname`) VALUES
('汉堡类', 0, '麦当劳全球连锁', '汉堡系列'),
('鸡肉类', 0, '麦当劳全球连锁', '鸡肉系列'),
('薯条小食', 0, '麦当劳全球连锁', '薯条小食系列'),
('甜品', 0, '麦当劳全球连锁', '甜品系列'),
('饮料', 0, '麦当劳全球连锁', '饮料系列'),
('早餐类', 0, '麦当劳全球连锁', '早餐系列'),
('沙拉轻食', 0, '麦当劳全球连锁', '沙拉轻食系列'),
('咖啡类', 0, '麦当劳全球连锁', '咖啡系列');

-- 3. 插入菜单信息表数据
-- 获取分类ID并插入麦当劳菜品信息
INSERT INTO `menu` (`createtime`, `imgpath`, `info5`, `name`, `newstuijian`, `price1`, `price2`, `productlock`, `xiaoliang`, `cateid`) VALUES
-- 汉堡类系列
(NOW(), '/images/bigmac.jpg', '麦当劳经典汉堡，双层牛肉饼，特制酱料', '巨无霸', 1, 23.00, 21.00, 0, 1256, (SELECT id FROM ltypes WHERE catename = '汉堡类')),
(NOW(), '/images/quarter_pounder.jpg', '1/4磅新鲜牛肉，洋葱、酸瓜、芥末酱', '1/4磅牛肉堡', 1, 26.00, 24.00, 0, 834, (SELECT id FROM ltypes WHERE catename = '汉堡类')),
(NOW(), '/images/cheeseburger.jpg', '经典芝士汉堡，简单美味的选择', '芝士汉堡', 0, 16.00, 15.00, 0, 567, (SELECT id FROM ltypes WHERE catename = '汉堡类')),
(NOW(), '/images/double_cheeseburger.jpg', '双层芝士汉堡，双倍牛肉双倍满足', '双层芝士汉堡', 1, 22.00, 20.00, 0, 445, (SELECT id FROM ltypes WHERE catename = '汉堡类')),
(NOW(), '/images/filet_o_fish.jpg', '深海鱼柳堡，酥脆鱼柳配塔塔酱', '深海鱼柳堡', 0, 19.00, 18.00, 0, 234, (SELECT id FROM ltypes WHERE catename = '汉堡类')),

-- 鸡肉类系列
(NOW(), '/images/mcnuggets_4pc.jpg', '4块麦乐鸡，外酥内嫩，搭配蘸酱', '麦乐鸡(4块)', 1, 15.00, 13.50, 0, 789, (SELECT id FROM ltypes WHERE catename = '鸡肉类')),
(NOW(), '/images/mcnuggets_6pc.jpg', '6块麦乐鸡，经典美味，更多享受', '麦乐鸡(6块)', 1, 20.00, 18.50, 0, 923, (SELECT id FROM ltypes WHERE catename = '鸡肉类')),
(NOW(), '/images/spicy_chicken_burger.jpg', '香辣鸡腿堡，香辣酥脆，口感丰富', '香辣鸡腿堡', 1, 21.00, 19.50, 0, 667, (SELECT id FROM ltypes WHERE catename = '鸡肉类')),
(NOW(), '/images/crispy_chicken_burger.jpg', '原味板烧鸡腿堡，嫩滑鸡腿肉', '原味板烧鸡腿堡', 0, 21.00, 19.50, 0, 445, (SELECT id FROM ltypes WHERE catename = '鸡肉类')),

-- 薯条小食系列
(NOW(), '/images/french_fries_s.jpg', '小份薯条，金黄香脆，经典搭配', '薯条(小)', 1, 9.00, 8.50, 0, 1567, (SELECT id FROM ltypes WHERE catename = '薯条小食')),
(NOW(), '/images/french_fries_m.jpg', '中份薯条，香脆可口，分量适中', '薯条(中)', 1, 12.00, 11.50, 0, 1234, (SELECT id FROM ltypes WHERE catename = '薯条小食')),
(NOW(), '/images/french_fries_l.jpg', '大份薯条，超值分量，适合分享', '薯条(大)', 0, 15.00, 14.50, 0, 789, (SELECT id FROM ltypes WHERE catename = '薯条小食')),
(NOW(), '/images/apple_pie.jpg', '苹果派，香甜酥脆，温暖甜品', '苹果派', 1, 8.00, 7.50, 0, 345, (SELECT id FROM ltypes WHERE catename = '薯条小食')),

-- 甜品系列
(NOW(), '/images/mcflurry_oreo.jpg', 'OREO麦旋风，奥利奥碎配香草冰淇淋', 'OREO麦旋风', 1, 13.00, 12.00, 0, 456, (SELECT id FROM ltypes WHERE catename = '甜品')),
(NOW(), '/images/soft_serve.jpg', '新地，香草味软冰淇淋，清爽甜蜜', '香草新地', 0, 7.00, 6.50, 0, 567, (SELECT id FROM ltypes WHERE catename = '甜品')),
(NOW(), '/images/chocolate_pie.jpg', '巧克力派，浓郁巧克力，甜蜜享受', '巧克力派', 0, 8.00, 7.50, 0, 234, (SELECT id FROM ltypes WHERE catename = '甜品')),

-- 饮料系列
(NOW(), '/images/coke_s.jpg', '小杯可口可乐，经典碳酸饮料', '可口可乐(小)', 1, 7.00, 6.50, 0, 2134, (SELECT id FROM ltypes WHERE catename = '饮料')),
(NOW(), '/images/coke_m.jpg', '中杯可口可乐，清爽解腻', '可口可乐(中)', 1, 9.00, 8.50, 0, 1876, (SELECT id FROM ltypes WHERE catename = '饮料')),
(NOW(), '/images/orange_juice.jpg', '鲜橙汁，天然果汁，维C丰富', '鲜橙汁', 0, 11.00, 10.50, 0, 445, (SELECT id FROM ltypes WHERE catename = '饮料')),
(NOW(), '/images/sprite.jpg', '雪碧，柠檬味汽水，清新怡人', '雪碧', 0, 7.00, 6.50, 0, 1234, (SELECT id FROM ltypes WHERE catename = '饮料')),

-- 早餐类系列
(NOW(), '/images/egg_mcmuffin.jpg', '满福堡加蛋，经典早餐，营养丰富', '满福堡加蛋', 1, 14.00, 13.00, 0, 567, (SELECT id FROM ltypes WHERE catename = '早餐类')),
(NOW(), '/images/sausage_mcmuffin.jpg', '猪柳满福堡，香嫩猪柳，早餐首选', '猪柳满福堡', 1, 12.00, 11.50, 0, 445, (SELECT id FROM ltypes WHERE catename = '早餐类')),
(NOW(), '/images/hotcakes.jpg', '热香饼，松软香甜，配枫糖浆', '热香饼', 0, 16.00, 15.00, 0, 234, (SELECT id FROM ltypes WHERE catename = '早餐类')),

-- 沙拉轻食系列
(NOW(), '/images/caesar_salad.jpg', '凯撒沙拉，新鲜蔬菜，健康选择', '凯撒沙拉', 0, 18.00, 17.00, 0, 156, (SELECT id FROM ltypes WHERE catename = '沙拉轻食')),
(NOW(), '/images/grilled_chicken_salad.jpg', '烤鸡沙拉，低脂高蛋白，营养均衡', '烤鸡沙拉', 1, 22.00, 21.00, 0, 234, (SELECT id FROM ltypes WHERE catename = '沙拉轻食')),

-- 咖啡类系列
(NOW(), '/images/americano.jpg', '美式咖啡，纯正咖啡香，提神醒脑', '美式咖啡', 1, 12.00, 11.50, 0, 678, (SELECT id FROM ltypes WHERE catename = '咖啡类')),
(NOW(), '/images/cappuccino.jpg', '卡布奇诺，绵密奶泡，香浓醇厚', '卡布奇诺', 1, 16.00, 15.50, 0, 445, (SELECT id FROM ltypes WHERE catename = '咖啡类')),
(NOW(), '/images/latte.jpg', '拿铁咖啡，牛奶与咖啡的完美融合', '拿铁咖啡', 0, 18.00, 17.50, 0, 567, (SELECT id FROM ltypes WHERE catename = '咖啡类'));

-- 4. 插入订单信息表数据
-- 创建一些样例订单
INSERT INTO `cg_info` (`address`, `createtime`, `orderid`, `phone`, `status`, `totalprice`, `username`) VALUES
('北京市朝阳区三里屯街道10号楼201室', '2024-09-01 12:30:00', 'ORDER20240901001', '13800138004', 1, 85.00, 'user001'),
('上海市浦东新区陆家嘴环路100号办公楼A座15层', '2024-09-02 18:45:00', 'ORDER20240902001', '13800138005', 1, 123.00, 'user002'),
('深圳市南山区科技园南区深南大道1001号', '2024-09-03 19:20:00', 'ORDER20240903001', '13800138006', 0, 67.00, 'user003'),
('广州市天河区珠江新城花城大道15号', '2024-09-08 13:15:00', 'ORDER20240908001', '13800138007', 0, 156.00, 'user001'),
('杭州市西湖区文三路268号西湖国际科技大厦', '2024-09-09 20:30:00', 'ORDER20240909001', '13800138008', 1, 89.00, 'user002');

-- 5. 插入订单条目表数据
-- 为上述订单添加具体的麦当劳菜品条目
-- 订单1: ORDER20240901001 (user001, 总价85.00)
INSERT INTO `the_order_entry` (`price`, `productid`, `productname`, `productnum`, `orderid`) VALUES
(21.00, (SELECT id FROM menu WHERE name = '巨无霸'), '巨无霸', 2, 'ORDER20240901001'),
(13.50, (SELECT id FROM menu WHERE name = '麦乐鸡(4块)'), '麦乐鸡(4块)', 1, 'ORDER20240901001'),
(11.50, (SELECT id FROM menu WHERE name = '薯条(中)'), '薯条(中)', 1, 'ORDER20240901001'),
(8.50, (SELECT id FROM menu WHERE name = '可口可乐(中)'), '可口可乐(中)', 1, 'ORDER20240901001');

-- 订单2: ORDER20240902001 (user002, 总价123.00)
INSERT INTO `the_order_entry` (`price`, `productid`, `productname`, `productnum`, `orderid`) VALUES
(24.00, (SELECT id FROM menu WHERE name = '1/4磅牛肉堡'), '1/4磅牛肉堡', 2, 'ORDER20240902001'),
(19.50, (SELECT id FROM menu WHERE name = '香辣鸡腿堡'), '香辣鸡腿堡', 1, 'ORDER20240902001'),
(18.50, (SELECT id FROM menu WHERE name = '麦乐鸡(6块)'), '麦乐鸡(6块)', 1, 'ORDER20240902001'),
(14.50, (SELECT id FROM menu WHERE name = '薯条(大)'), '薯条(大)', 1, 'ORDER20240902001'),
(12.00, (SELECT id FROM menu WHERE name = 'OREO麦旋风'), 'OREO麦旋风', 1, 'ORDER20240902001'),
(10.50, (SELECT id FROM menu WHERE name = '鲜橙汁'), '鲜橙汁', 1, 'ORDER20240902001');

-- 订单3: ORDER20240903001 (user003, 总价67.00)
INSERT INTO `the_order_entry` (`price`, `productid`, `productname`, `productnum`, `orderid`) VALUES
(20.00, (SELECT id FROM menu WHERE name = '双层芝士汉堡'), '双层芝士汉堡', 1, 'ORDER20240903001'),
(21.00, (SELECT id FROM menu WHERE name = '烤鸡沙拉'), '烤鸡沙拉', 1, 'ORDER20240903001'),
(11.50, (SELECT id FROM menu WHERE name = '美式咖啡'), '美式咖啡', 1, 'ORDER20240903001'),
(14.50, (SELECT id FROM menu WHERE name = '薯条(大)'), '薯条(大)', 1, 'ORDER20240903001');

-- 订单4: ORDER20240908001 (user001, 总价156.00)
INSERT INTO `the_order_entry` (`price`, `productid`, `productname`, `productnum`, `orderid`) VALUES
(21.00, (SELECT id FROM menu WHERE name = '巨无霸'), '巨无霸', 2, 'ORDER20240908001'),
(24.00, (SELECT id FROM menu WHERE name = '1/4磅牛肉堡'), '1/4磅牛肉堡', 2, 'ORDER20240908001'),
(19.50, (SELECT id FROM menu WHERE name = '香辣鸡腿堡'), '香辣鸡腿堡', 1, 'ORDER20240908001'),
(18.50, (SELECT id FROM menu WHERE name = '麦乐鸡(6块)'), '麦乐鸡(6块)', 1, 'ORDER20240908001'),
(11.50, (SELECT id FROM menu WHERE name = '薯条(中)'), '薯条(中)', 2, 'ORDER20240908001'),
(15.50, (SELECT id FROM menu WHERE name = '卡布奇诺'), '卡布奇诺', 1, 'ORDER20240908001'),
(8.50, (SELECT id FROM menu WHERE name = '可口可乐(中)'), '可口可乐(中)', 2, 'ORDER20240908001');

-- 订单5: ORDER20240909001 (user002, 总价89.00)
INSERT INTO `the_order_entry` (`price`, `productid`, `productname`, `productnum`, `orderid`) VALUES
(18.00, (SELECT id FROM menu WHERE name = '深海鱼柳堡'), '深海鱼柳堡', 1, 'ORDER20240909001'),
(13.00, (SELECT id FROM menu WHERE name = '满福堡加蛋'), '满福堡加蛋', 1, 'ORDER20240909001'),
(15.00, (SELECT id FROM menu WHERE name = '热香饼'), '热香饼', 1, 'ORDER20240909001'),
(13.50, (SELECT id FROM menu WHERE name = '麦乐鸡(4块)'), '麦乐鸡(4块)', 1, 'ORDER20240909001'),
(17.50, (SELECT id FROM menu WHERE name = '拿铁咖啡'), '拿铁咖啡', 1, 'ORDER20240909001'),
(11.50, (SELECT id FROM menu WHERE name = '薯条(中)'), '薯条(中)', 1, 'ORDER20240909001');

-- 麦当劳数据插入完成后的统计信息查询
SELECT 'Web订餐管理系统麦当劳样例数据插入完成！' AS message;

SELECT 
    '管理员信息表' AS table_name, 
    COUNT(*) AS record_count,
    '包含管理员、接单员、会员用户' AS description
FROM administrators
UNION ALL
SELECT 
    '分类信息表' AS table_name, 
    COUNT(*) AS record_count,
    '包含麦当劳8大分类：汉堡、鸡肉、薯条小食等' AS description
FROM ltypes
UNION ALL
SELECT 
    '菜单信息表' AS table_name, 
    COUNT(*) AS record_count,
    '包含麦当劳经典菜品，含推荐菜品和销量' AS description
FROM menu
UNION ALL
SELECT 
    '订单信息表' AS table_name, 
    COUNT(*) AS record_count,
    '包含已受理和待受理的麦当劳订单' AS description
FROM cg_info
UNION ALL
SELECT 
    '订单条目表' AS table_name, 
    COUNT(*) AS record_count,
    '包含所有订单的麦当劳菜品详情' AS description
FROM the_order_entry;
