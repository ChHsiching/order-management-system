#!/bin/bash

# Web Order Management System - Business Flow Integration Test
# 业务流程集成测试脚本 - 验证完整的用户业务流程

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/../core/test_framework.sh"

# 测试完整的用户注册到下单流程
test_complete_user_flow() {
    log_info "测试完整用户注册到下单流程..."

    local test_username="test_flow_user_$(date +%s)"
    local test_email="${test_username}@example.com"
    local test_phone="13800138$(date +%s | tail -c 4)"

    # 1. 用户注册
    local user_data="{\"username\":\"$test_username\",\"password\":\"Test123456\",\"email\":\"$test_email\",\"phone\":\"$test_phone\"}"
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "201" ]]; then
        log_success "用户注册成功"

        # 2. 用户登录
        local login_response=$(curl -s -X POST "$API_BASE/api/user/login?username=$test_username&password=Test123456")
        local login_success=$(echo "$login_response" | jq -r '.success // false')

        if [[ "$login_success" == "true" ]]; then
            log_success "用户登录成功"

            # 3. 查看菜单
            local menu_response=$(curl -s "$API_BASE/api/menu/list")
            local menu_count=$(echo "$menu_response" | jq '. | length // 0')

            if [[ "$menu_count" -gt 0 ]]; then
                log_success "获取菜单列表成功，发现 $menu_count 个菜品"

                # 4. 创建订单
                local first_menu_id=$(echo "$menu_response" | jq -r '.[0].id // 1')
                local order_data="{\"items\":[{\"menu_id\":$first_menu_id,\"quantity\":1,\"price\":12.50}],\"total_price\":12.50,\"address\":\"测试地址\",\"phone\":\"$test_phone\"}"
                local order_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$order_data" "$API_BASE/api/order/create")
                local order_code=$(echo "$order_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

                if [[ "$order_code" == "201" ]]; then
                    log_success "订单创建成功"

                    # 5. 验证数据库中的订单
                    local db_order_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM cg_info WHERE username='$test_username';")
                    if [[ "$db_order_count" -gt 0 ]]; then
                        log_success "数据库中订单记录验证通过"
                        update_test_stats "PASS"
                    else
                        log_error "数据库中未找到订单记录"
                        update_test_stats "FAIL"
                    fi
                else
                    log_error "订单创建失败，状态码: $order_code"
                    update_test_stats "FAIL"
                fi
            else
                log_error "获取菜单列表失败或菜单为空"
                update_test_stats "FAIL"
            fi
        else
            log_error "用户登录失败"
            update_test_stats "FAIL"
        fi
    else
        log_error "用户注册失败，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 测试购物车完整流程
test_shopping_cart_flow() {
    log_info "测试购物车完整流程..."

    local test_username="test_cart_user_$(date +%s)"

    # 1. 创建测试用户
    local user_data="{\"username\":\"$test_username\",\"password\":\"Test123456\",\"email\":\"${test_username}@example.com\",\"phone\":\"13800138001\"}"
    curl -s -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register" > /dev/null

    # 2. 用户登录
    curl -s -X POST "$API_BASE/api/user/login?username=$test_username&password=Test123456" > /dev/null

    # 3. 添加商品到购物车
    local cart_data="{\"menu_id\":1,\"quantity\":2}"
    local cart_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$cart_data" "$API_BASE/api/cart/add")
    local cart_code=$(echo "$cart_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$cart_code" == "200" ]]; then
        log_success "添加商品到购物车成功"

        # 4. 查看购物车
        local cart_list_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X GET "$API_BASE/api/cart/list")
        local cart_list_code=$(echo "$cart_list_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

        if [[ "$cart_list_code" == "200" ]]; then
            log_success "查看购物车成功"
            update_test_stats "PASS"
        else
            log_error "查看购物车失败，状态码: $cart_list_code"
            update_test_stats "FAIL"
        fi
    else
        log_error "添加商品到购物车失败，状态码: $cart_code"
        update_test_stats "FAIL"
    fi
}

# 测试管理员完整操作流程
test_admin_operations_flow() {
    log_info "测试管理员操作流程..."

    # 1. 管理员登录
    local admin_login_response=$(curl -s -X POST "$API_BASE/api/user/login?username=admin&password=admin")
    local admin_success=$(echo "$admin_login_response" | jq -r '.success // false')

    if [[ "$admin_success" == "true" ]]; then
        log_success "管理员登录成功"

        # 2. 查看用户列表
        local users_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X GET "$API_BASE/api/admin/users")
        local users_code=$(echo "$users_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

        if [[ "$users_code" == "200" ]]; then
            log_success "获取用户列表成功"

            # 3. 查看订单管理
            local orders_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X GET "$API_BASE/api/admin/orders")
            local orders_code=$(echo "$orders_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

            if [[ "$orders_code" == "200" ]]; then
                log_success "获取订单管理成功"

                # 4. 查看系统统计
                local stats_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X GET "$API_BASE/api/admin/stats")
                local stats_code=$(echo "$stats_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

                if [[ "$stats_code" == "200" ]]; then
                    log_success "获取系统统计成功"
                    update_test_stats "PASS"
                else
                    log_error "获取系统统计失败，状态码: $stats_code"
                    update_test_stats "FAIL"
                fi
            else
                log_error "获取订单管理失败，状态码: $orders_code"
                update_test_stats "FAIL"
            fi
        else
            log_error "获取用户列表失败，状态码: $users_code"
            update_test_stats "FAIL"
        fi
    else
        log_error "管理员登录失败"
        update_test_stats "FAIL"
    fi
}

# 测试异常情况处理
test_exception_handling() {
    log_info "测试异常情况处理..."

    # 1. 测试重复用户注册
    local duplicate_data="{\"username\":\"duplicate_test\",\"password\":\"Test123456\",\"email\":\"duplicate@test.com\",\"phone\":\"13800138001\"}"

    # 第一次注册
    curl -s -X POST -H "Content-Type: application/json" -d "$duplicate_data" "$API_BASE/api/user/register" > /dev/null

    # 第二次注册（应该失败）
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$duplicate_data" "$API_BASE/api/user/register")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "400" || "$http_code" == "409" ]]; then
        log_success "重复用户注册被正确拒绝"
        update_test_stats "PASS"
    else
        log_error "重复用户注册未被正确处理，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 测试数据一致性验证
test_data_consistency() {
    log_info "测试数据一致性验证..."

    # 1. 验证分类和菜品的关联关系
    local invalid_menus=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM menu WHERE typeID NOT IN (SELECT id FROM ltypes);")

    if [[ "$invalid_menus" -eq "0" ]]; then
        log_success "分类和菜品关联关系一致"
        update_test_stats "PASS"
    else
        log_error "发现 $invalid_menus 个无效的分类引用"
        update_test_stats "FAIL"
    fi
}

# 主测试流程
main() {
    log_start "业务流程集成测试"

    # 初始化测试环境
    setup_test_environment

    # 执行集成测试
    test_complete_user_flow
    test_shopping_cart_flow
    test_admin_operations_flow
    test_exception_handling
    test_data_consistency

    # 清理测试环境
    cleanup_test_environment

    log_end "业务流程集成测试"

    # 显示测试统计
    show_test_stats
}

# 运行主函数
main "$@"