#!/bin/bash

# Web Order Management System - Data Validation Test
# 数据验证测试脚本 - 验证数据完整性和业务规则

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/../core/test_framework.sh"

# 测试用户数据验证
test_user_data_validation() {
    log_info "测试用户数据验证..."

    # 测试无效邮箱格式
    local invalid_email_data="{\"username\":\"test_invalid_email\",\"password\":\"Test123456\",\"email\":\"invalid-email\",\"phone\":\"13800138001\"}"
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$invalid_email_data" "$API_BASE/api/user/register")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" ]]; then
        log_success "无效邮箱格式被正确拒绝"
        update_test_stats "PASS"
    else
        log_error "无效邮箱格式未被正确处理，状态码: $http_code"
        update_test_stats "FAIL"
    fi

    # 测试空密码
    local empty_password_data="{\"username\":\"test_empty_pass\",\"password\":\"\",\"email\":\"test@example.com\",\"phone\":\"13800138001\"}"
    response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$empty_password_data" "$API_BASE/api/user/register")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" ]]; then
        log_success "空密码被正确拒绝"
        update_test_stats "PASS"
    else
        log_error "空密码未被正确处理，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 测试订单数据验证
test_order_data_validation() {
    log_info "测试订单数据验证..."

    # 创建测试用户用于订单验证
    local test_username="test_order_val_$(date +%s)"
    local user_data="{\"username\":\"$test_username\",\"password\":\"Test123456\",\"email\":\"${test_username}@example.com\",\"phone\":\"13800138001\"}"
    curl -s -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register" > /dev/null

    # 登录获取权限
    curl -s -X POST "$API_BASE/api/user/login?username=$test_username&password=Test123456" > /dev/null

    # 测试空订单
    local empty_order_data="{\"items\":[],\"total_price\":0,\"address\":\"\",\"phone\":\"\"}"
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$empty_order_data" "$API_BASE/api/order/create")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" ]]; then
        log_success "空订单被正确拒绝"
        update_test_stats "PASS"
    else
        log_error "空订单未被正确处理，状态码: $http_code"
        update_test_stats "FAIL"
    fi

    # 测试无效价格
    local invalid_price_data="{\"items\":[{\"menu_id\":1,\"quantity\":1,\"price\":-1}],\"total_price\":-1,\"address\":\"测试地址\",\"phone\":\"13800138001\"}"
    response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$invalid_price_data" "$API_BASE/api/order/create")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" || "$http_code" == "422" ]]; then
        log_success "无效价格被正确拒绝"
        update_test_stats "PASS"
    else
        log_error "无效价格未被正确处理，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 测试数据库约束
test_database_constraints() {
    log_info "测试数据库约束..."

    # 测试用户名唯一性约束
    local username="test_unique_$(date +%s)"
    local user_data="{\"username\":\"$username\",\"password\":\"Test123456\",\"email\":\"${username}@example.com\",\"phone\":\"13800138001\"}"

    # 第一次插入
    curl -s -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register" > /dev/null

    # 第二次插入（应该失败）
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" || "$http_code" == "409" ]]; then
        log_success "用户名唯一性约束生效"
        update_test_stats "PASS"
    else
        log_error "用户名唯一性约束未生效，状态码: $http_code"
        update_test_stats "FAIL"
    fi

    # 测试手机号唯一性约束
    local phone="13800138$(date +%s | tail -c 4)"
    local user1_data="{\"username\":\"user1_$(date +%s)\",\"password\":\"Test123456\",\"email\":\"user1@example.com\",\"phone\":\"$phone\"}"
    local user2_data="{\"username\":\"user2_$(date +%s)\",\"password\":\"Test123456\",\"email\":\"user2@example.com\",\"phone\":\"$phone\"}"

    # 第一个用户
    curl -s -X POST -H "Content-Type: application/json" -d "$user1_data" "$API_BASE/api/user/register" > /dev/null

    # 第二个用户（相同手机号）
    response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$user2_data" "$API_BASE/api/user/register")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" || "$http_code" == "409" ]]; then
        log_success "手机号唯一性约束生效"
        update_test_stats "PASS"
    else
        log_error "手机号唯一性约束未生效，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 测试数据长度限制
test_data_length_limits() {
    log_info "测试数据长度限制..."

    # 测试过长的用户名
    local long_username="test_user_$(date +%s)_$(openssl rand -hex 20)"
    local user_data="{\"username\":\"$long_username\",\"password\":\"Test123456\",\"email\":\"test@example.com\",\"phone\":\"13800138001\"}"
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" ]]; then
        log_success "过长用户名被正确拒绝"
        update_test_stats "PASS"
    else
        log_error "过长用户名未被正确处理，状态码: $http_code"
        update_test_stats "FAIL"
    fi

    # 测试过短的密码
    local short_password_data="{\"username\":\"test_short_pass\",\"password\":\"123\",\"email\":\"test@example.com\",\"phone\":\"13800138001\"}"
    response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$short_password_data" "$API_BASE/api/user/register")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" ]]; then
        log_success "过短密码被正确拒绝"
        update_test_stats "PASS"
    else
        log_error "过短密码未被正确处理，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 测试特殊字符处理
test_special_characters() {
    log_info "测试特殊字符处理..."

    # 测试用户名包含特殊字符
    local special_username="test_user_$(date +%s)_特殊字符"
    local user_data="{\"username\":\"$special_username\",\"password\":\"Test123456\",\"email\":\"test@example.com\",\"phone\":\"13800138001\"}"
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "201" ]]; then
        log_success "特殊字符用户名处理正常"
        update_test_stats "PASS"
    else
        log_error "特殊字符用户名处理异常，状态码: $http_code"
        update_test_stats "FAIL"
    fi

    # 测试SQL注入防护
    local sql_injection_data="{\"username\":\"test_user_$(date +%s)\",\"password\":\"Test123456\",\"email\":\"test@example.com',' OR '1'='1\",\"phone\":\"13800138001\"}"
    response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$sql_injection_data" "$API_BASE/api/user/register")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" ]]; then
        log_success "SQL注入攻击被正确防护"
        update_test_stats "PASS"
    else
        log_error "SQL注入防护可能存在问题，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 主测试流程
main() {
    log_start "数据验证测试"

    # 初始化测试环境
    setup_test_environment

    # 执行数据验证测试
    test_user_data_validation
    test_order_data_validation
    test_database_constraints
    test_data_length_limits
    test_special_characters

    # 清理测试环境
    cleanup_test_environment

    log_end "数据验证测试"

    # 显示测试统计
    show_test_stats
}

# 运行主函数
main "$@"