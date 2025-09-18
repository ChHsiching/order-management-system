#!/bin/bash

# ============================================================================
# Web订餐管理系统 - 前台菜单接口测试脚本
# 功能：专门测试新实现的前台菜单接口
# 使用方法：./test-frontend-menu.sh [localhost:8080]
# ============================================================================

BASE_URL=${1:-http://localhost:8080/WebOrderSystem}
CONTEXT_PATH="/WebOrderSystem"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "🧪 Web订餐管理系统 - 前台菜单接口测试"
echo "📡 测试地址: $BASE_URL"
echo "=================================="

# 测试函数
test_frontend_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"

    echo -e "${BLUE}测试${NC}: $name"
    echo -e "${YELLOW}方法${NC}: $method"
    echo -e "${YELLOW}地址${NC}: $url"

    if [ -n "$data" ]; then
        response=$(curl -s -X "$method" "$url" ${data:+-d "$data"} -H "Content-Type: application/json" -w "\nHTTP_CODE:%{http_code}")
    else
        response=$(curl -s -X "$method" "$url" ${data:+-d "$data"} -H "Content-Type: application/json" -w "\nHTTP_CODE:%{http_code}")
    fi

    # 分离响应体和状态码
    body=$(echo "$response" | sed -n '1,/HTTP_CODE:/p' | sed 's/HTTP_CODE:.*$//')
    http_code=$(echo "$response" | sed 's/.*HTTP_CODE://')

    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        echo -e "${GREEN}✅ 状态码: $http_code${NC}"
        # 尝试解析JSON响应
        if echo "$body" | jq . > /dev/null 2>&1; then
            local api_code=$(echo "$body" | jq -r '.code // empty')
            local api_message=$(echo "$body" | jq -r '.message // empty')
            if [ "$api_code" = "0" ] || [ -z "$api_code" ]; then
                echo -e "${GREEN}✅ API调用成功${NC}"
                if [ -n "$api_message" ]; then
                    echo -e "${YELLOW}消息${NC}: $api_message"
                fi
            else
                echo -e "${RED}❌ API返回错误: $api_code${NC}"
            fi
        fi
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

# 1. 测试获取所有可用菜品
test_frontend_api "获取所有可用菜品" "GET" "$BASE_URL/api/menu"

# 2. 测试根据ID获取菜品详情
test_frontend_api "获取菜品详情(ID:1)" "GET" "$BASE_URL/api/menu/1"

# 3. 测试根据分类获取菜品
test_frontend_api "获取分类菜品(分类ID:1)" "GET" "$BASE_URL/api/menu/category/1"

# 4. 测试获取推荐菜品
test_frontend_api "获取推荐菜品" "GET" "$BASE_URL/api/menu/recommended"

# 5. 测试搜索菜品
test_frontend_api "搜索菜品(关键词:汉堡)" "GET" "$BASE_URL/api/menu/search?keyword=汉堡"

# 6. 测试获取热销菜品
test_frontend_api "获取热销菜品(限制5个)" "GET" "$BASE_URL/api/menu/hot-sales?limit=5"

# 7. 测试不存在的菜品
test_frontend_api "获取不存在的菜品(ID:999)" "GET" "$BASE_URL/api/menu/999"

# 8. 测试空搜索关键词
test_frontend_api "空搜索关键词" "GET" "$BASE_URL/api/menu/search?keyword="

echo "🎉 前台菜单接口测试完成！"
echo ""
echo "📝 测试结果说明："
echo "  ✅ 所有接口应该返回200状态码"
echo "  ✅ 公开接口无需认证即可访问"
echo "  ✅ 推荐菜品只显示isRecommend=1的菜品"
echo "  ✅ 搜索功能支持模糊匹配"
echo "  ✅ 热销菜品按销量排序"
echo ""
echo "🔗 相关接口："
echo "  后台管理接口: $BASE_URL/admin/menu/*"
echo "  API文档: $BASE_URL/swagger-ui.html"