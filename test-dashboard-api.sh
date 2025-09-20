#!/bin/bash

# 控制台页面API专项测试脚本
echo "🧪 测试控制台页面API..."
echo "=================================="

# 1. 获取JWT Token
echo "1. 获取管理员JWT Token..."
TOKEN_RESPONSE=$(curl -s -X POST "http://localhost:8080/WebOrderSystem/api/admin/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin")

TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ 获取Token失败"
    echo "$TOKEN_RESPONSE"
    exit 1
fi

echo "✅ Token获取成功"

# 2. 测试Dashboard统计数据API
echo ""
echo "2. 测试Dashboard统计数据API..."
DASHBOARD_RESPONSE=$(curl -s -X GET "http://localhost:8080/WebOrderSystem/api/admin/statistics/dashboard" \
  -H "Authorization: Bearer $TOKEN")

echo "📊 Dashboard响应:"
echo "$DASHBOARD_RESPONSE" | jq '.' 2>/dev/null || echo "$DASHBOARD_RESPONSE"

# 检查数据完整性
TODAY_SALES=$(echo "$DASHBOARD_RESPONSE" | grep -o '"todaySales":[0-9.]*' | cut -d':' -f2)
TOTAL_USERS=$(echo "$DASHBOARD_RESPONSE" | grep -o '"totalUsers":[0-9]*' | cut -d':' -f2)
TOTAL_MENUS=$(echo "$DASHBOARD_RESPONSE" | grep -o '"totalMenus":[0-9]*' | cut -d':' -f2)

echo "📈 今日销售额: ¥$TODAY_SALES"
echo "👥 总用户数: $TOTAL_USERS"
echo "🍽️ 总菜品数: $TOTAL_MENUS"

# 3. 测试最新订单API
echo ""
echo "3. 测试最新订单API..."
ORDERS_RESPONSE=$(curl -s -X GET "http://localhost:8080/WebOrderSystem/api/admin/statistics/recent-orders?limit=5" \
  -H "Authorization: Bearer $TOKEN")

echo "📋 最新订单响应（前3条）:"
echo "$ORDERS_RESPONSE" | jq '.data[:3] | map({orderId, username, totalPrice, status})' 2>/dev/null || echo "$ORDERS_RESPONSE"

# 计算订单数量
ORDER_COUNT=$(echo "$ORDERS_RESPONSE" | grep -o '"orderId":"[^"]*"' | wc -l)
echo "📊 返回订单数量: $ORDER_COUNT"

# 4. 验证前端服务
echo ""
echo "4. 验证前端服务..."
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5174/)
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo "✅ 前端服务正常运行"
else
    echo "❌ 前端服务异常，状态码: $FRONTEND_STATUS"
fi

# 5. 验证控制台页面路由
echo ""
echo "5. 验证控制台页面路由..."
DASHBOARD_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5174/dashboard)
if [ "$DASHBOARD_STATUS" = "200" ]; then
    echo "✅ 控制台页面路由正常"
else
    echo "❌ 控制台页面路由异常，状态码: $DASHBOARD_STATUS"
fi

echo ""
echo "=================================="
echo "🎉 控制台API测试完成！"
echo ""
echo "📊 关键数据："
echo "   - 今日销售额: ¥$TODAY_SALES"
echo "   - 总用户数: $TOTAL_USERS"
echo "   - 总菜品数: $TOTAL_MENUS"
echo "   - 最新订单: $ORDER_COUNT 条"
echo "   - 前端服务: 状态 $FRONTEND_STATUS"
echo "   - 控制台页面: 状态 $DASHBOARD_STATUS"
echo ""
echo "🌐 访问地址:"
echo "   - 控制台页面: http://localhost:5174/dashboard"
echo "   - API文档: http://localhost:8080/WebOrderSystem/swagger-ui.html"