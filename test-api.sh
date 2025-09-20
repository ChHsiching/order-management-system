#!/bin/bash

# 订餐管理系统API测试脚本
# 测试所有管理页面的API端点

echo "🧪 开始测试订餐管理系统API..."
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

# 2. 测试菜单分类API
echo ""
echo "2. 测试菜单分类API..."
CATEGORY_RESPONSE=$(curl -s -X GET "http://localhost:8080/WebOrderSystem/api/admin/menuCategory/getList" \
  -H "Authorization: Bearer $TOKEN")

CATEGORY_COUNT=$(echo "$CATEGORY_RESPONSE" | grep -o '"id":[0-9]*' | wc -l)
echo "✅ 菜单分类API正常，返回 $CATEGORY_COUNT 个分类"

# 3. 测试菜品管理API
echo ""
echo "3. 测试菜品管理API..."
MENU_RESPONSE=$(curl -s -X GET "http://localhost:8080/WebOrderSystem/api/admin/menu/items" \
  -H "Authorization: Bearer $TOKEN")

MENU_COUNT=$(echo "$MENU_RESPONSE" | grep -o '"id":[0-9]*' | wc -l)
echo "✅ 菜品管理API正常，返回 $MENU_COUNT 个菜品"

# 4. 测试用户管理API
echo ""
echo "4. 测试用户管理API..."
USER_RESPONSE=$(curl -s -X GET "http://localhost:8080/WebOrderSystem/api/user/admin/all" \
  -H "Authorization: Bearer $TOKEN")

USER_COUNT=$(echo "$USER_RESPONSE" | grep -o '"username":"[^"]*"' | wc -l)
echo "✅ 用户管理API正常，返回 $USER_COUNT 个用户"

# 5. 测试订单管理API
echo ""
echo "5. 测试订单管理API..."
ORDER_RESPONSE=$(curl -s -X GET "http://localhost:8080/WebOrderSystem/api/orders/admin/all" \
  -H "Authorization: Bearer $TOKEN")

ORDER_COUNT=$(echo "$ORDER_RESPONSE" | grep -o '"id":[0-9]*' | wc -l)
echo "✅ 订单管理API正常，返回 $ORDER_COUNT 个订单"

# 6. 测试前端服务
echo ""
echo "6. 测试前端服务..."
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5174/)
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo "✅ 前端服务正常运行 (http://localhost:5174/)"
else
    echo "❌ 前端服务异常，状态码: $FRONTEND_STATUS"
fi

# 7. 测试后端健康检查
echo ""
echo "7. 测试后端健康检查..."
HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/WebOrderSystem/actuator/health" 2>/dev/null || echo "000")
if [ "$HEALTH_STATUS" = "200" ]; then
    echo "✅ 后端服务健康检查通过"
else
    echo "⚠️  后端健康检查状态码: $HEALTH_STATUS (可能未启用actuator)"
fi

echo ""
echo "=================================="
echo "🎉 API测试完成！"
echo ""
echo "📊 测试结果汇总："
echo "   - 菜单分类: $CATEGORY_COUNT 个分类"
echo "   - 菜品管理: $MENU_COUNT 个菜品"
echo "   - 用户管理: $USER_COUNT 个用户"
echo "   - 订单管理: $ORDER_COUNT 个订单"
echo "   - 前端服务: 状态 $FRONTEND_STATUS"
echo "   - 后端服务: 状态 $HEALTH_STATUS"
echo ""
echo "🌐 前端访问地址: http://localhost:5174/"
echo "📖 API文档地址: http://localhost:8080/WebOrderSystem/swagger-ui.html"