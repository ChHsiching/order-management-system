#!/bin/bash

# ============================================================================
# Web订餐管理系统 - Issue #10 后台核心接口测试脚本
# 功能：测试管理员登录和菜单类别CRUD功能
# 测试地址：http://localhost:8080/WebOrderSystem
# ============================================================================

BASE_URL="http://localhost:8080/WebOrderSystem"
CONTEXT_PATH="/WebOrderSystem"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "🧪 Issue #10 后台核心接口测试"
echo "📡 测试地址: $BASE_URL"
echo "=================================="

# 测试函数
test_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local auth="$5"

    echo -e "${BLUE}测试${NC}: $name"
    echo -e "${YELLOW}方法${NC}: $method"
    echo -e "${YELLOW}地址${NC}: $url"

    if [ -n "$auth" ]; then
        response=$(curl -s -u "$auth" -X "$method" "$url" ${data:+-d "$data"} -H "Content-Type: application/json" -w "\nHTTP_CODE:%{http_code}")
    else
        response=$(curl -s -X "$method" "$url" ${data:+-d "$data"} -H "Content-Type: application/json" -w "\nHTTP_CODE:%{http_code}")
    fi

    # 分离响应体和状态码
    body=$(echo "$response" | sed -n '1,/HTTP_CODE:/p' | sed 's/HTTP_CODE:.*$//')
    http_code=$(echo "$response" | sed 's/.*HTTP_CODE://')

    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        echo -e "${GREEN}✅ 状态码: $http_code${NC}"
    else
        echo -e "${RED}❌ 状态码: $http_code${NC}"
    fi

    if [ -n "$body" ] && [ "$body" != "" ]; then
        echo -e "${YELLOW}响应${NC}:"
        echo "$body" | head -c 500
        if [ ${#body} -gt 500 ]; then
            echo "..."
        fi
    fi
    echo -e "${BLUE}----------------------------------${NC}"
    echo ""
}

echo "🔐 测试管理员登录功能"
echo "=================================="

# 1. 测试管理员登录 - 正确凭据
test_api "管理员登录（正确凭据）" "POST" "$BASE_URL/api/admin/login?username=admin&password=admin" "" ""

# 2. 测试管理员登录 - 错误凭据
test_api "管理员登录（错误凭据）" "POST" "$BASE_URL/api/admin/login?username=admin&password=wrong" "" ""

echo "📋 测试菜单类别CRUD功能"
echo "=================================="

# 3. 测试获取所有类别列表
test_api "获取所有类别列表" "GET" "$BASE_URL/api/admin/menuCategory/getList" "" "admin:admin"

# 4. 测试获取激活的类别
test_api "获取激活的类别" "GET" "$BASE_URL/api/admin/menuCategory/active" "" "admin:admin"

# 5. 测试添加新类别
test_api "添加新类别" "POST" "$BASE_URL/api/admin/menuCategory/add" '{
  "cateName": "测试分类",
  "address": "测试地址",
  "productName": "测试产品",
  "cateLock": 0
}' "admin:admin"

# 6. 测试根据ID获取类别详情
test_api "获取类别详情" "GET" "$BASE_URL/api/admin/menuCategory/1" "" "admin:admin"

# 7. 测试更新类别信息
test_api "更新类别信息" "PUT" "$BASE_URL/api/admin/menuCategory/1" '{
  "cateName": "更新后的分类",
  "address": "更新后的地址",
  "productName": "更新后的产品",
  "cateLock": 0
}' "admin:admin"

# 8. 测试按类别名删除
test_api "按类别名删除" "DELETE" "$BASE_URL/api/admin/menuCategory/delete?categoryName=更新后的分类" "" "admin:admin"

# 9. 测试获取类别统计信息
test_api "获取类别统计信息" "GET" "$BASE_URL/api/admin/menuCategory/statistics" "" "admin:admin"

echo "🔒 测试权限控制"
echo "=================================="

# 10. 测试未认证访问管理员接口
test_api "未认证访问类别列表" "GET" "$BASE_URL/api/admin/menuCategory/getList" "" ""

echo "🎉 测试完成！"
echo ""
echo "📝 验证要点："
echo "  1. 管理员登录只允许role=1的用户通过"
echo "  2. 菜单类别CRUD操作需要ADMIN权限"
echo "  3. 未认证用户无法访问管理员接口"
echo "  4. 所有操作能在数据库中正确反映"