#!/bin/bash

# 前后端联调测试脚本 - 验证前端API调用
# 测试前端是否能正确获取后端数据

echo "========================================"
echo "  前后端联调测试 - 前端API调用验证"
echo "========================================"

# API基础URL
FRONTEND_BASE_URL="http://localhost:5173"
BACKEND_BASE_URL="http://localhost:8080/WebOrderSystem"

echo ""
echo "1. 测试前端API代理状态"
echo "----------------------------------------"

# 测试分类API
echo "测试分类API (/api/categories):"
categories_response=$(curl -s "$FRONTEND_BASE_URL/api/categories")
if [[ $categories_response == *"cateName"* ]]; then
    echo "✅ 分类API代理成功"
    echo "   返回数据示例: $(echo $categories_response | head -c 100)..."
else
    echo "❌ 分类API代理失败"
fi

# 测试菜单API
echo ""
echo "测试菜单API (/api/menu):"
menu_response=$(curl -s "$FRONTEND_BASE_URL/api/menu")
if [[ $menu_response == *"code\":0"* ]]; then
    echo "✅ 菜单API代理成功"
    menu_count=$(echo $menu_response | jq '.data | length' 2>/dev/null || echo "N/A")
    echo "   返回菜品数量: $menu_count"
else
    echo "❌ 菜单API代理失败"
fi

# 测试推荐菜品API
echo ""
echo "测试推荐菜品API (/api/menu/recommended):"
recommended_response=$(curl -s "$FRONTEND_BASE_URL/api/menu/recommended")
if [[ $recommended_response == *"data"* ]]; then
    echo "✅ 推荐菜品API代理成功"
    recommended_count=$(echo $recommended_response | jq '.data | length' 2>/dev/null || echo "N/A")
    echo "   返回推荐菜品数量: $recommended_count"
else
    echo "❌ 推荐菜品API代理失败"
fi

# 测试单个菜品API
echo ""
echo "测试单个菜品API (/api/menu/1):"
single_menu_response=$(curl -s "$FRONTEND_BASE_URL/api/menu/1")
if [[ $single_menu_response == *"name"* ]]; then
    echo "✅ 单个菜品API代理成功"
    menu_name=$(echo $single_menu_response | jq -r '.data.name' 2>/dev/null || echo "N/A")
    echo "   返回菜品名称: $menu_name"
else
    echo "❌ 单个菜品API代理失败"
fi

# 测试分类菜品API
echo ""
echo "测试分类菜品API (/api/menu/category/1):"
category_menu_response=$(curl -s "$FRONTEND_BASE_URL/api/menu/category/1")
if [[ $category_menu_response == *"data"* ]]; then
    echo "✅ 分类菜品API代理成功"
    category_menu_count=$(echo $category_menu_response | jq '.data | length' 2>/dev/null || echo "N/A")
    echo "   返回分类菜品数量: $category_menu_count"
else
    echo "❌ 分类菜品API代理失败"
fi

# 测试用户登录API
echo ""
echo "测试用户登录API (/api/user/login):"
login_response=$(curl -s -X POST "$FRONTEND_BASE_URL/api/user/login" \
    -H "Content-Type: application/json" \
    -d '{"username": "newuser123", "password": "pass123"}')

if [[ $login_response == *"token"* ]]; then
    echo "✅ 用户登录API代理成功"
    token=$(echo $login_response | jq -r '.token' 2>/dev/null || echo "N/A")
    username=$(echo $login_response | jq -r '.userInfo.username' 2>/dev/null || echo "N/A")
    echo "   返回用户名: $username"
    echo "   返回token长度: ${#token}"
else
    echo "❌ 用户登录API代理失败"
fi

echo ""
echo "2. 对比前后端API响应"
echo "----------------------------------------"

# 对比分类API
echo "对比分类API响应:"
frontend_categories=$(curl -s "$FRONTEND_BASE_URL/api/categories" | jq '.[0].cateName' 2>/dev/null)
backend_categories=$(curl -s "$BACKEND_BASE_URL/api/categories" | jq '.[0].cateName' 2>/dev/null)

if [[ "$frontend_categories" == "$backend_categories" ]]; then
    echo "✅ 分类API前后端响应一致: $frontend_categories"
else
    echo "❌ 分类API前后端响应不一致"
    echo "   前端: $frontend_categories"
    echo "   后端: $backend_categories"
fi

# 对比菜单API
echo ""
echo "对比菜单API响应:"
frontend_menu_count=$(curl -s "$FRONTEND_BASE_URL/api/menu" | jq '.data | length' 2>/dev/null)
backend_menu_count=$(curl -s "$BACKEND_BASE_URL/api/menu" | jq '.data | length' 2>/dev/null)

if [[ "$frontend_menu_count" == "$backend_menu_count" ]]; then
    echo "✅ 菜单API前后端响应一致，菜品数量: $frontend_menu_count"
else
    echo "❌ 菜单API前后端响应不一致"
    echo "   前端菜品数量: $frontend_menu_count"
    echo "   后端菜品数量: $backend_menu_count"
fi

echo ""
echo "3. 验证前端页面可访问性"
echo "----------------------------------------"

# 检查前端主页
frontend_status=$(curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_BASE_URL")
if [[ $frontend_status == "200" ]]; then
    echo "✅ 前端主页可访问 (HTTP $frontend_status)"
else
    echo "❌ 前端主页访问失败 (HTTP $frontend_status)"
fi

# 检查前端资源文件
js_status=$(curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_BASE_URL/src/main.ts")
if [[ $js_status == "200" ]]; then
    echo "✅ 前端JavaScript资源可访问 (HTTP $js_status)"
else
    echo "❌ 前端JavaScript资源访问失败 (HTTP $js_status)"
fi

echo ""
echo "========================================"
echo "  测试总结"
echo "========================================"

# 统计测试结果
success_count=0
total_tests=8

# 重新执行测试以统计成功次数
if [[ $(curl -s "$FRONTEND_BASE_URL/api/categories") == *"cateName"* ]]; then ((success_count++)); fi
if [[ $(curl -s "$FRONTEND_BASE_URL/api/menu") == *"code\":0"* ]]; then ((success_count++)); fi
if [[ $(curl -s "$FRONTEND_BASE_URL/api/menu/recommended") == *"data"* ]]; then ((success_count++)); fi
if [[ $(curl -s "$FRONTEND_BASE_URL/api/menu/1") == *"name"* ]]; then ((success_count++)); fi
if [[ $(curl -s "$FRONTEND_BASE_URL/api/menu/category/1") == *"data"* ]]; then ((success_count++)); fi
if [[ $(curl -s -X POST "$FRONTEND_BASE_URL/api/user/login" -H "Content-Type: application/json" -d '{"username": "newuser123", "password": "pass123"}') == *"token"* ]]; then ((success_count++)); fi
if [[ $frontend_status == "200" ]]; then ((success_count++)); fi
if [[ $js_status == "200" ]]; then ((success_count++)); fi

echo "通过测试: $success_count/$total_tests"
echo "成功率: $((success_count * 100 / total_tests))%"

if [[ $success_count -eq $total_tests ]]; then
    echo "🎉 所有测试通过！前后端联调成功！"
    exit 0
else
    echo "⚠️  存在测试失败，需要进一步调试"
    exit 1
fi