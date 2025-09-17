#!/bin/bash

# Web订餐管理系统多身份权限自动化测试脚本
# 基于PDF文档权限规定，验证游客、会员、管理员三种角色的API访问权限
# 使用方法: ./test_permissions.sh [数据库密码]

set -e  # 遇到错误立即退出

# 配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="web_order"
DB_USER="root"
DB_PASS="${1:-123456}"  # 默认密码123456
API_BASE="http://localhost:8080/WebOrderSystem"
LOG_FILE="/tmp/permissions_test_$(date +%Y%m%d_%H%M%S).log"

# 测试用户数据
GUEST_USER=""  # 游客无用户信息
MEMBER_USER="testmember"
MEMBER_PASS="123456"
ADMIN_USER="admin"
ADMIN_PASS="admin"

# 测试数据
USER_DATA='{"username":"test_'$(date +%s)'","email":"test@example.com","password":"123456","phone":"13900139000","qq":"123456","role":0}'
ORDER_DATA='{"username":"testuser","items":[{"menuId":1,"quantity":2}],"address":"测试地址","phone":"13800138000"}'
CATEGORY_DATA='{"cateName":"测试分类","address":"测试地址","productName":"测试产品"}'

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 测试计数器
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

# 权限测试结果数组
declare -A GUEST_RESULTS
declare -A MEMBER_RESULTS
declare -A ADMIN_RESULTS
declare -A PRIVILEGE_ESCALATION_RESULTS

# 日志函数
log() {
    echo -e "$1" | tee -a "$LOG_FILE"
}

# 权限测试函数
test_permission() {
    local role="$1"
    local description="$2"
    local method="$3"
    local url="$4"
    local data="$5"
    local expected_code="$6"
    local test_type="$7"  # ALLOWED or FORBIDDEN

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    log "${CYAN}[$role] 测试 $TOTAL_TESTS: $description${NC}"
    log "${YELLOW}方法: $method${NC}"
    log "${YELLOW}URL: $url${NC}"
    log "${YELLOW}期望: $test_type (状态码: $expected_code)${NC}"

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

    # 判断测试结果
    if [[ "$http_code" -eq "$expected_code" ]]; then
        log "${GREEN}✅ 通过 - HTTP状态码: $http_code${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))

        # 记录到对应角色的结果数组
        case "$role" in
            "GUEST") GUEST_RESULTS["$description"]="PASS" ;;
            "MEMBER") MEMBER_RESULTS["$description"]="PASS" ;;
            "ADMIN") ADMIN_RESULTS["$description"]="PASS" ;;
            "ESCALATION") PRIVILEGE_ESCALATION_RESULTS["$description"]="PASS" ;;
        esac
    else
        log "${RED}❌ 失败 - 期望状态码: $expected_code, 实际: $http_code${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))

        # 记录失败结果
        case "$role" in
            "GUEST") GUEST_RESULTS["$description"]="FAIL" ;;
            "MEMBER") MEMBER_RESULTS["$description"]="FAIL" ;;
            "ADMIN") ADMIN_RESULTS["$description"]="FAIL" ;;
            "ESCALATION") PRIVILEGE_ESCALATION_RESULTS["$description"]="FAIL" ;;
        esac
    fi

    log "${BLUE}响应体:${NC}"
    echo "$body" | head -c 300
    log "${BLUE}----------------------------------------${NC}"
    echo
}

# 用户认证函数
authenticate_user() {
    local username="$1"
    local password="$2"
    local auth_url="$3"

    local response=$(curl -s -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=$username&password=$password" \
        "$auth_url")

    echo "$response"
}

# 数据库验证函数
verify_database_setup() {
    log "${BLUE}=== 数据库权限设置验证 ===${NC}"

    # 检查表结构
    log "${YELLOW}检查administrators表结构:${NC}"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        -e "USE $DB_NAME; DESCRIBE administrators;" 2>/dev/null | tee -a "$LOG_FILE"

    log "${YELLOW}检查用户权限分布:${NC}"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        -e "USE $DB_NAME; SELECT username, role, CASE WHEN role = 0 THEN '会员' WHEN role = 1 THEN '管理员' ELSE '未知' END as role_name FROM administrators;" 2>/dev/null | tee -a "$LOG_FILE"

    log "${BLUE}========================================${NC}"
    echo
}

# 游客权限测试
test_guest_permissions() {
    log "${PURPLE}=== 游客权限测试 ===${NC}"

    # 游客允许访问的API
    log "${GREEN}游客应该能够访问的API:${NC}"
    test_permission "GUEST" "获取分类列表" "GET" "$API_BASE/api/categories" "" 200 "ALLOWED"
    test_permission "GUEST" "获取分类详情" "GET" "$API_BASE/api/categories/1" "" 200 "ALLOWED"
    test_permission "GUEST" "按名称获取分类" "GET" "$API_BASE/api/categories/name/汉堡类" "" 200 "ALLOWED"

    # 游客禁止访问的API
    log "${RED}游客被禁止访问的API:${NC}"
    test_permission "GUEST" "用户注册" "POST" "$API_BASE/api/user/register" "$USER_DATA" 403 "FORBIDDEN"
    test_permission "GUEST" "用户登录" "POST" "$API_BASE/api/user/login?username=test&password=123" "" 403 "FORBIDDEN"
    test_permission "GUEST" "获取用户信息" "GET" "$API_BASE/api/user/me" "" 403 "FORBIDDEN"
    test_permission "GUEST" "创建订单" "POST" "$API_BASE/api/orders" "$ORDER_DATA" 403 "FORBIDDEN"
    test_permission "GUEST" "获取用户订单" "GET" "$API_BASE/api/orders/user/testuser" "" 403 "FORBIDDEN"
    test_permission "GUEST" "管理员登录" "POST" "$API_BASE/admin/login?username=admin&password=admin" "" 403 "FORBIDDEN"
    test_permission "GUEST" "获取所有会员" "GET" "$API_BASE/admin/members" "" 403 "FORBIDDEN"
    test_permission "GUEST" "菜单管理" "GET" "$API_BASE/admin/menu/items" "" 403 "FORBIDDEN"

    log "${PURPLE}=== 游客权限测试完成 ===${NC}"
    echo
}

# 会员权限测试
test_member_permissions() {
    log "${PURPLE}=== 会员权限测试 ===${NC}"

    # 首先测试用户注册
    log "${GREEN}会员注册测试:${NC}"
    test_permission "MEMBER" "用户注册" "POST" "$API_BASE/api/user/register" "$USER_DATA" 200 "ALLOWED"

    # 测试用户登录
    log "${GREEN}会员登录测试:${NC}"
    local login_data="username=$MEMBER_USER&password=$MEMBER_PASS"
    test_permission "MEMBER" "用户登录" "POST" "$API_BASE/api/user/login?$login_data" "" 200 "ALLOWED"

    # 会员允许访问的API
    log "${GREEN}会员应该能够访问的API:${NC}"
    test_permission "MEMBER" "获取用户信息" "GET" "$API_BASE/api/user/me" "" 200 "ALLOWED"
    test_permission "MEMBER" "创建订单" "POST" "$API_BASE/api/orders" "$ORDER_DATA" 200 "ALLOWED"
    test_permission "MEMBER" "获取用户订单" "GET" "$API_BASE/api/orders/user/$MEMBER_USER" "" 200 "ALLOWED"
    test_permission "MEMBER" "获取订单详情" "GET" "$API_BASE/api/orders/ORDER20240901001" "" 200 "ALLOWED"

    # 会员禁止访问的API
    log "${RED}会员被禁止访问的API:${NC}"
    test_permission "MEMBER" "管理员登录" "POST" "$API_BASE/admin/login?username=admin&password=admin" "" 403 "FORBIDDEN"
    test_permission "MEMBER" "获取所有会员" "GET" "$API_BASE/admin/members" "" 403 "FORBIDDEN"
    test_permission "MEMBER" "菜单管理" "GET" "$API_BASE/admin/menu/items" "" 403 "FORBIDDEN"
    test_permission "MEMBER" "菜单类别管理" "GET" "$API_BASE/admin/menu/categories" "" 403 "FORBIDDEN"

    log "${PURPLE}=== 会员权限测试完成 ===${NC}"
    echo
}

# 管理员权限测试
test_admin_permissions() {
    log "${PURPLE}=== 管理员权限测试 ===${NC}"

    # 管理员登录
    log "${GREEN}管理员登录测试:${NC}"
    local admin_login="username=$ADMIN_USER&password=$ADMIN_PASS"
    test_permission "ADMIN" "管理员登录" "POST" "$API_BASE/admin/login?$admin_login" "" 200 "ALLOWED"

    # 管理员允许访问的API
    log "${GREEN}管理员应该能够访问的API:${NC}"
    test_permission "ADMIN" "获取所有会员" "GET" "$API_BASE/admin/members" "" 200 "ALLOWED"
    test_permission "ADMIN" "获取特定会员" "GET" "$API_BASE/admin/members/$MEMBER_USER" "" 200 "ALLOWED"
    test_permission "ADMIN" "菜单管理" "GET" "$API_BASE/admin/menu/items" "" 200 "ALLOWED"
    test_permission "ADMIN" "菜单类别管理" "GET" "$API_BASE/admin/menu/categories" "" 200 "ALLOWED"

    # 管理员也应该能访问会员API
    test_permission "ADMIN" "用户注册" "POST" "$API_BASE/api/user/register" "$USER_DATA" 200 "ALLOWED"
    test_permission "ADMIN" "创建订单" "POST" "$API_BASE/api/orders" "$ORDER_DATA" 200 "ALLOWED"

    log "${PURPLE}=== 管理员权限测试完成 ===${NC}"
    echo
}

# 越权测试
test_privilege_escalation() {
    log "${RED}=== 权限越权测试 ===${NC}"

    # 测试会员尝试访问管理员API
    log "${YELLOW}会员尝试访问管理员API:${NC}"
    test_permission "ESCALATION" "会员访问管理员API" "GET" "$API_BASE/admin/members" "" 403 "FORBIDDEN"
    test_permission "ESCALATION" "会员访问菜单管理" "GET" "$API_BASE/admin/menu/items" "" 403 "FORBIDDEN"
    test_permission "ESCALATION" "会员访问会员管理" "GET" "$API_BASE/admin/members" "" 403 "FORBIDDEN"

    # 测试游客尝试访问会员API
    log "${YELLOW}游客尝试访问会员API:${NC}"
    test_permission "ESCALATION" "游客访问用户信息" "GET" "$API_BASE/api/user/me" "" 403 "FORBIDDEN"
    test_permission "ESCALATION" "游客访问订单API" "GET" "$API_BASE/api/orders/user/testuser" "" 403 "FORBIDDEN"
    test_permission "ESCALATION" "游客访问创建订单" "POST" "$API_BASE/api/orders" "$ORDER_DATA" 403 "FORBIDDEN"

    log "${RED}=== 越权测试完成 ===${NC}"
    echo
}

# 生成权限测试报告
generate_permission_report() {
    log "${CYAN}=== 权限测试报告 ===${NC}"

    # 游客权限总结
    log "${BLUE}游客权限测试结果:${NC}"
    for test_desc in "${!GUEST_RESULTS[@]}"; do
        local result="${GUEST_RESULTS[$test_desc]}"
        if [[ "$result" == "PASS" ]]; then
            log "${GREEN}✅ $test_desc${NC}"
        else
            log "${RED}❌ $test_desc${NC}"
        fi
    done

    # 会员权限总结
    log "${BLUE}会员权限测试结果:${NC}"
    for test_desc in "${!MEMBER_RESULTS[@]}"; do
        local result="${MEMBER_RESULTS[$test_desc]}"
        if [[ "$result" == "PASS" ]]; then
            log "${GREEN}✅ $test_desc${NC}"
        else
            log "${RED}❌ $test_desc${NC}"
        fi
    done

    # 管理员权限总结
    log "${BLUE}管理员权限测试结果:${NC}"
    for test_desc in "${!ADMIN_RESULTS[@]}"; do
        local result="${ADMIN_RESULTS[$test_desc]}"
        if [[ "$result" == "PASS" ]]; then
            log "${GREEN}✅ $test_desc${NC}"
        else
            log "${RED}❌ $test_desc${NC}"
        fi
    done

    # 越权测试总结
    log "${BLUE}越权测试结果:${NC}"
    for test_desc in "${!PRIVILEGE_ESCALATION_RESULTS[@]}"; do
        local result="${PRIVILEGE_ESCALATION_RESULTS[$test_desc]}"
        if [[ "$result" == "PASS" ]]; then
            log "${GREEN}✅ $test_desc${NC}"
        else
            log "${RED}❌ $test_desc${NC}"
        fi
    done

    log "${CYAN}========================================${NC}"
}

# 主函数
main() {
    log "${CYAN}Web订餐管理系统多身份权限自动化测试开始${NC}"
    log "${CYAN}测试时间: $(date)${NC}"
    log "${CYAN}日志文件: $LOG_FILE${NC}"
    log "${CYAN}========================================${NC}"

    # 检查服务是否运行
    if ! curl -s "$API_BASE/api-docs" >/dev/null 2>&1; then
        log "${RED}❌ 服务未启动，请先启动Spring Boot应用${NC}"
        exit 1
    fi

    # 检查数据库连接
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "USE $DB_NAME; SELECT 1;" &>/dev/null; then
        log "${RED}❌ 数据库连接失败${NC}"
        exit 1
    fi

    # 执行测试
    verify_database_setup
    test_guest_permissions
    test_member_permissions
    test_admin_permissions
    test_privilege_escalation

    # 生成报告
    generate_permission_report

    # 总体统计
    log "${CYAN}========================================${NC}"
    log "${CYAN}权限测试总体统计${NC}"
    log "${CYAN}========================================${NC}"
    log "${BLUE}总测试数: $TOTAL_TESTS${NC}"
    log "${GREEN}通过: $PASSED_TESTS${NC}"
    log "${RED}失败: $FAILED_TESTS${NC}"
    log "${YELLOW}跳过: $SKIPPED_TESTS${NC}"

    if [[ $FAILED_TESTS -eq 0 ]]; then
        log "${GREEN}🎉 所有权限测试通过！系统权限配置正确。${NC}"
        exit_code=0
    else
        log "${RED}❌ 有 $FAILED_TESTS 个权限测试失败，需要修复Spring Security配置。${NC}"
        exit_code=1
    fi

    log "${CYAN}详细日志请查看: $LOG_FILE${NC}"
    exit $exit_code
}

# 执行主函数
main "$@"