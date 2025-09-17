#!/bin/bash

# ============================================================================
# Web订餐管理系统 - API测试脚本
# 功能：快速测试各种API接口
# 使用方法：./test-apis.sh [localhost:8080]
# ============================================================================

BASE_URL=${1:-http://localhost:8080/WebOrderSystem}
CONTEXT_PATH="/WebOrderSystem"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "🧪 Web订餐管理系统 - API测试"
echo "📡 测试地址: $BASE_URL"
echo "=================================="

# 测试函数
test_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local auth="$4"
    local data="$5"

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
        echo "$body" | head -c 500  # 限制输出长度
        if [ ${#body} -gt 500 ]; then
            echo "..."
        fi
    fi
    echo -e "${BLUE}----------------------------------${NC}"
    echo ""
}

# 1. 测试健康检查
test_api "健康检查" "GET" "$BASE_URL/actuator/health" ""

# 2. 测试公开接口 - 获取分类
test_api "获取菜品分类（公开）" "GET" "$BASE_URL/api/categories" ""

# 3. 测试后台接口 - 需要认证
test_api "获取菜品分类（后台）" "GET" "$BASE_URL/admin/menu/categories" "admin:admin"

# 4. 测试获取菜品列表
test_api "获取所有菜品" "GET" "$BASE_URL/admin/menu/items" "admin:admin"

# 5. 测试获取单个菜品
test_api "获取单个菜品" "GET" "$BASE_URL/admin/menu/items/1" "admin:admin"

# 6. 测试购物车功能
test_api "添加商品到购物车" "POST" "$BASE_URL/api/cart/add?username=admin&productId=1&quantity=2" "admin:admin"

# 7. 测试获取购物车
test_api "获取用户购物车" "GET" "$BASE_URL/api/cart?username=admin" "admin:admin"

# 8. 测试创建分类
test_api "创建分类" "POST" "$BASE_URL/admin/menu/categories" "admin:admin" '{
  "cateName": "测试分类",
  "cateLock": 0,
  "address": "测试地址",
  "productName": "测试产品"
}'

# 9. 测试创建菜品
test_api "创建菜品" "POST" "$BASE_URL/admin/menu/items" "admin:admin" '{
  "name": "测试菜品",
  "imgPath": "/images/test.jpg",
  "productLock": 0
}'

echo "🎉 API测试完成！"
echo ""
echo "📝 更多测试命令："
echo "  查看详细信息: curl -s <URL> | jq ."
echo "  测试其他接口: 参考 Swagger 文档"
echo "  测试文件上传: 参考文件上传API文档"