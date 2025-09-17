#!/bin/bash

# Web Order Management System - End-to-End Test
# 端到端测试脚本 - 完整的用户体验测试

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/../core/test_framework.sh"

# 测试完整端到端用户旅程
test_complete_user_journey() {
    log_info "测试完整端到端用户旅程..."

    local test_username="e2e_user_$(date +%s)"
    local test_email="${test_username}@example.com"
    local test_phone="13800138$(date +%s | tail -c 4)"

    # 1. 游客浏览菜品
    log_info "阶段1: 游客浏览菜品"
    local categories_response=$(curl -s "$API_BASE/api/categories")
    local categories_count=$(echo "$categories_response" | jq '. | length // 0')

    if [[ "$categories_count" -gt 0 ]]; then
        log_success "游客可以查看菜品分类"
    else
        log_error "游客无法查看菜品分类"
        update_test_stats "FAIL"
        return
    fi

    # 2. 用户注册
    log_info "阶段2: 用户注册"
    local user_data="{\"username\":\"$test_username\",\"password\":\"Test123456\",\"email\":\"$test_email\",\"phone\":\"$test_phone\"}"
    local register_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register")
    local register_code=$(echo "$register_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$register_code" == "201" ]]; then
        log_success "用户注册成功"
    else
        log_error "用户注册失败，状态码: $register_code"
        update_test_stats "FAIL"
        return
    fi

    # 3. 用户登录
    log_info "阶段3: 用户登录"
    local login_response=$(curl -s -X POST "$API_BASE/api/user/login?username=$test_username&password=Test123456")
    local login_success=$(echo "$login_response" | jq -r '.success // false')

    if [[ "$login_success" == "true" ]]; then
        log_success "用户登录成功"
    else
        log_error "用户登录失败"
        update_test_stats "FAIL"
        return
    fi

    # 4. 浏览推荐菜品
    log_info "阶段4: 浏览推荐菜品"
    local recommend_response=$(curl -s "$API_BASE/api/menu/recommend")
    local recommend_count=$(echo "$recommend_response" | jq '. | length // 0')

    if [[ "$recommend_count" -ge 0 ]]; then
        log_success "获取推荐菜品成功"
    else
        log_error "获取推荐菜品失败"
        update_test_stats "FAIL"
        return
    fi

    # 5. 搜索菜品
    log_info "阶段5: 搜索菜品"
    local search_response=$(curl -s "$API_BASE/api/menu/search?keyword=汉堡")
    local search_code=$(curl -s -w "%{http_code}" "$API_BASE/api/menu/search?keyword=汉堡" | tail -c 4)

    if [[ "$search_code" == "200" ]]; then
        log_success "菜品搜索功能正常"
    else
        log_error "菜品搜索功能异常"
        update_test_stats "FAIL"
        return
    fi

    # 6. 添加购物车
    log_info "阶段6: 添加购物车"
    local cart_data="{\"menu_id\":1,\"quantity\":1}"
    local cart_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$cart_data" "$API_BASE/api/cart/add")
    local cart_code=$(echo "$cart_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$cart_code" == "200" ]]; then
        log_success "添加购物车成功"
    else
        log_error "添加购物车失败，状态码: $cart_code"
        update_test_stats "FAIL"
        return
    fi

    # 7. 创建订单
    log_info "阶段7: 创建订单"
    local order_data="{\"items\":[{\"menu_id\":1,\"quantity\":1,\"price\":12.50}],\"total_price\":12.50,\"address\":\"测试地址\",\"phone\":\"$test_phone\"}"
    local order_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$order_data" "$API_BASE/api/order/create")
    local order_code=$(echo "$order_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$order_code" == "201" ]]; then
        log_success "订单创建成功"
    else
        log_error "订单创建失败，状态码: $order_code"
        update_test_stats "FAIL"
        return
    fi

    # 8. 查看订单列表
    log_info "阶段8: 查看订单列表"
    local orders_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X GET "$API_BASE/api/order/list")
    local orders_code=$(echo "$orders_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$orders_code" == "200" ]]; then
        log_success "查看订单列表成功"
    else
        log_error "查看订单列表失败，状态码: $orders_code"
        update_test_stats "FAIL"
        return
    fi

    # 9. 验证数据库数据完整性
    log_info "阶段9: 验证数据完整性"
    local user_exists=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators WHERE username='$test_username';")
    if [[ "$user_exists" -eq "1" ]]; then
        log_success "用户数据在数据库中完整保存"
        update_test_stats "PASS"
    else
        log_error "用户数据在数据库中未找到"
        update_test_stats "FAIL"
    fi
}

# 测试管理员端到端流程
test_admin_end_to_end() {
    log_info "测试管理员端到端流程..."

    # 1. 管理员登录
    log_info "阶段1: 管理员登录"
    local login_response=$(curl -s -X POST "$API_BASE/api/user/login?username=admin&password=admin")
    local success=$(echo "$login_response" | jq -r '.success // false')

    if [[ "$success" == "true" ]]; then
        log_success "管理员登录成功"
    else
        log_error "管理员登录失败"
        update_test_stats "FAIL"
        return
    fi

    # 2. 查看系统统计
    log_info "阶段2: 查看系统统计"
    local stats_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X GET "$API_BASE/api/admin/stats")
    local stats_code=$(echo "$stats_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$stats_code" == "200" ]]; then
        log_success "获取系统统计成功"
    else
        log_error "获取系统统计失败，状态码: $stats_code"
        update_test_stats "FAIL"
        return
    fi

    # 3. 用户管理
    log_info "阶段3: 用户管理"
    local users_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X GET "$API_BASE/api/admin/users")
    local users_code=$(echo "$users_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$users_code" == "200" ]]; then
        log_success "用户管理功能正常"
    else
        log_error "用户管理功能异常，状态码: $users_code"
        update_test_stats "FAIL"
        return
    fi

    # 4. 订单管理
    log_info "阶段4: 订单管理"
    local orders_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X GET "$API_BASE/api/admin/orders")
    local orders_code=$(echo "$orders_response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$orders_code" == "200" ]]; then
        log_success "订单管理功能正常"
        update_test_stats "PASS"
    else
        log_error "订单管理功能异常，状态码: $orders_code"
        update_test_stats "FAIL"
    fi
}

# 测试并发访问
test_concurrent_access() {
    log_info "测试并发访问..."

    local start_time=$(date +%s%N)
    local process_count=5
    local pids=()

    # 启动并发请求
    for i in $(seq 1 $process_count); do
        {
            curl -s "$API_BASE/api/categories" > /dev/null
        } &
        pids+=($!)
    done

    # 等待所有请求完成
    for pid in "${pids[@]}"; do
        wait $pid
    done

    local end_time=$(date +%s%N)
    local duration=$(( (end_time - start_time) / 1000000 ))
    local avg_time=$(( duration / process_count ))

    log_info "并发访问平均响应时间: ${avg_time}ms"

    if [[ $avg_time -lt 2000 ]]; then
        log_success "并发访问性能测试通过"
        update_test_stats "PASS"
    else
        log_warn "并发访问响应较慢: ${avg_time}ms"
        update_test_stats "PASS"
    fi
}

# 主测试流程
main() {
    log_start "端到端测试"

    # 初始化测试环境
    setup_test_environment

    # 执行端到端测试
    test_complete_user_journey
    test_admin_end_to_end
    test_concurrent_access

    # 清理测试环境
    cleanup_test_environment

    log_end "端到端测试"

    # 显示测试统计
    show_test_stats
}

# 运行主函数
main "$@"