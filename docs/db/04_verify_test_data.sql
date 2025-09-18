-- ============================================================================
-- Web订餐管理系统 - 测试数据验证脚本
-- 验证测试数据是否正确插入，为CI测试提供数据验证
-- ============================================================================

USE `web_order`;

-- 验证分类数据
SELECT '分类数据验证' as verification;
SELECT
    COUNT(*) as total_categories,
    SUM(CASE WHEN catelock = 0 THEN 1 ELSE 0 END) as active_categories
FROM ltypes;

-- 显示所有分类
SELECT '分类列表' as category_list;
SELECT id, catename, catelock FROM ltypes ORDER BY id;

-- 验证菜单数据
SELECT '菜单数据验证' as verification;
SELECT
    COUNT(*) as total_menus,
    SUM(CASE WHEN productlock = 0 THEN 1 ELSE 0 END) as active_menus,
    SUM(CASE WHEN newstuijian = 1 AND productlock = 0 THEN 1 ELSE 0 END) as recommended_menus,
    SUM(xiaoliang) as total_sales
FROM menu;

-- 显示推荐菜品
SELECT '推荐菜品列表' as recommended_list;
SELECT id, name, newstuijian, xiaoliang, cateid
FROM menu
WHERE newstuijian = 1 AND productlock = 0
ORDER BY xiaoliang DESC;

-- 显示热销菜品（销量前5）
SELECT '热销菜品列表' as hot_sales_list;
SELECT id, name, xiaoliang, price1, price2
FROM menu
WHERE productlock = 0
ORDER BY xiaoliang DESC
LIMIT 5;

-- 验证管理员数据
SELECT '管理员数据验证' as verification;
SELECT COUNT(*) as total_users FROM administrators;

-- 验证订单数据
SELECT '订单数据验证' as verification;
SELECT
    COUNT(*) as total_orders,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pending_orders,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as processed_orders
FROM cg_info;

-- 数据验证完成提示
SELECT '测试数据验证完成！' AS message;