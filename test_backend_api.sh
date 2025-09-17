#!/bin/bash

# Web订餐管理系统后端API自动化测试脚本
# 用于test/backend-testing分支的永久性测试
# 使用方法: ./api_test.sh [数据库密码]

set -e  # 遇到错误立即退出

# 配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="web_order"
DB_USER="root"
DB_PASS="${1:-123456}"  # 默认密码123456
API_BASE="http://localhost:8080/WebOrderSystem"
LOG_FILE="/tmp/api_test_$(date +%Y%m%d_%H%M%S).log"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试计数器
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 日志函数
log() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

# 测试函数
test_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_code="${5:-200}"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    log "${BLUE}测试 $TOTAL_TESTS: $name${NC}"
    log "${YELLOW}方法: $method${NC}"
    log "${YELLOW}URL: $url${NC}"

    if [[ -n "$data" ]]; then
        log "${YELLOW}数据: $data${NC}"
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X "$method" \
                    -H "Content-Type: application/json" \
                    -d "$data" "$url" 2>/dev/null)
    else
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X "$method" "$url" 2>/dev/null)
    fi

    # 提取HTTP状态码和响应体
    http_code=$(echo "$response" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)
    body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')

    if [[ "$http_code" -eq "$expected_code" ]]; then
        log "${GREEN}✅ 通过 - HTTP状态码: $http_code${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))

        # 验证响应数据不为空或占位符
        if [[ -n "$body" && "$body" != *"待实现"* && "$body" != *"null"* ]]; then
            log "${GREEN}✅ 响应数据有效${NC}"
            echo "$body" | jq . >/dev/null 2>&1 && log "${GREEN}✅ JSON格式正确${NC}" || log "${YELLOW}⚠️  非JSON响应${NC}"
        else
            log "${YELLOW}⚠️  响应为空或占位符${NC}"
        fi
    else
        log "${RED}❌ 失败 - 期望状态码: $expected_code, 实际: $http_code${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi

    log "${BLUE}响应体:${NC}"
    echo "$body" | head -c 500
    log "${BLUE}----------------------------------------${NC}"
    echo
}

# 数据库测试函数
test_database() {
    log "${BLUE}=== 数据库连接测试 ===${NC}"

    # 测试数据库连接
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "USE $DB_NAME; SELECT 1;" &>/dev/null; then
        log "${GREEN}✅ 数据库连接成功${NC}"

        # 测试表结构
        log "${BLUE}=== 验证表结构 ===${NC}"
        tables=("administrators" "ltypes" "menu" "cg_info" "the_order_entry")
        for table in "${tables[@]}"; do
            count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
                     -e "USE $DB_NAME; SELECT COUNT(*) FROM $table;" -s -N 2>/dev/null || echo "0")
            log "${BLUE}表 $table: $count 条记录${NC}"
        done

        # 验证数据完整性
        log "${BLUE}=== 验证数据完整性 ===${NC}"
        # 检查订单和订单条目的关联
        orphaned_entries=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
                            -e "USE $DB_NAME; SELECT COUNT(*) FROM the_order_entry oe
                                LEFT JOIN cg_info oi ON oe.orderid = oi.orderid
                                WHERE oi.orderid IS NULL;" -s -N 2>/dev/null || echo "ERROR")

        if [[ "$orphaned_entries" -eq 0 ]]; then
            log "${GREEN}✅ 订单条目数据完整性验证通过${NC}"
        else
            log "${RED}❌ 发现 $orphaned_entries 个孤立的订单条目${NC}"
        fi

    else
        log "${RED}❌ 数据库连接失败${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# 用户管理API测试
test_user_apis() {
    log "${BLUE}=== 用户管理API测试 ===${NC}"

    # 测试用户注册
    test_api "用户注册" "POST" "$API_BASE/api/user/register" \
        '{"username":"test_'$(date +%s)'","email":"test@example.com","password":"123456","phone":"13800138000","qq":"123456","role":0}'

    # 测试用户登录
    test_api "用户登录" "POST" "$API_BASE/api/user/login?username=testuser&password=123456" ""

    # 测试获取用户信息
    test_api "获取用户信息" "GET" "$API_BASE/api/user/me" "" "401"  # 未登录状态
}

# 分类管理API测试
test_category_apis() {
    log "${BLUE}=== 分类管理API测试 ===${NC}"

    # 获取所有分类
    test_api "获取所有分类" "GET" "$API_BASE/api/categories" ""

    # 获取分类详情
    test_api "获取分类详情" "GET" "$API_BASE/api/categories/1" ""

    # 按名称获取分类
    test_api "按名称获取分类" "GET" "$API_BASE/api/categories/name/汉堡类" ""
}

# 订单管理API测试
test_order_apis() {
    log "${BLUE}=== 订单管理API测试 ===${NC}"

    # 创建订单
    test_api "创建订单" "POST" "$API_BASE/api/orders" \
        '{"username":"testuser","items":[{"menuId":1,"quantity":2}],"address":"测试地址","phone":"13800138000"}'

    # 获取用户订单
    test_api "获取用户订单" "GET" "$API_BASE/api/orders/user/testuser" ""

    # 获取订单详情
    test_api "获取订单详情" "GET" "$API_BASE/api/orders/ORDER20240901001" ""

    # 获取订单项
    test_api "获取订单项" "GET" "$API_BASE/api/orders/ORDER20240901001/items" ""
}

# 菜单管理API测试
test_menu_apis() {
    log "${BLUE}=== 菜单管理API测试 ===${NC}"

    # 测试后台菜单接口（当前应为占位符）
    test_api "获取菜单类别" "GET" "$API_BASE/admin/menu/categories" ""
    test_api "获取菜品信息" "GET" "$API_BASE/admin/menu/items" ""
}

# 管理员API测试
test_admin_apis() {
    log "${BLUE}=== 管理员API测试 ===${NC}"

    # 管理员登录
    test_api "管理员登录" "POST" "$API_BASE/admin/login?username=admin&password=admin" ""

    # 获取所有会员
    test_api "获取所有会员" "GET" "$API_BASE/admin/members" "" "401"  # 未登录状态

    # 获取特定会员
    test_api "获取特定会员" "GET" "$API_BASE/admin/members/testuser" "" "401"  # 未登录状态
}

# 缺失API测试（应该返回404）
test_missing_apis() {
    log "${BLUE}=== 缺失API测试 ===${NC}"

    # 购物车相关API（应该缺失）
    test_api "购物车API" "GET" "$API_BASE/api/cart" "" "404"
    test_api "菜单详情API" "GET" "$API_BASE/api/menus/1" "" "404"
    test_api "订单状态更新" "PUT" "$API_BASE/api/orders/ORDER20240901001/status" "" "404"
}

# 主函数
main() {
    log "${BLUE}Web订餐管理系统API自动化测试开始${NC}"
    log "${BLUE}测试时间: $(date)${NC}"
    log "${BLUE}日志文件: $LOG_FILE${NC}"
    log "${BLUE}========================================${NC}"

    # 检查服务是否运行
    if ! curl -s "$API_BASE/api-docs" >/dev/null 2>&1; then
        log "${RED}❌ 服务未启动，请先启动Spring Boot应用${NC}"
        exit 1
    fi

    # 执行所有测试
    test_database
    test_user_apis
    test_category_apis
    test_order_apis
    test_menu_apis
    test_admin_apis
    test_missing_apis

    # 生成测试报告
    log "${BLUE}========================================${NC}"
    log "${BLUE}测试报告${NC}"
    log "${BLUE}========================================${NC}"
    log "${BLUE}总测试数: $TOTAL_TESTS${NC}"
    log "${GREEN}通过: $PASSED_TESTS${NC}"
    log "${RED}失败: $FAILED_TESTS${NC}"

    if [[ $FAILED_TESTS -eq 0 ]]; then
        log "${GREEN}🎉 所有测试通过！${NC}"
        exit_code=0
    else
        log "${RED}❌ 有 $FAILED_TESTS 个测试失败${NC}"
        exit_code=1
    fi

    log "${BLUE}详细日志请查看: $LOG_FILE${NC}"
    exit $exit_code
}

# 执行主函数
main "$@"