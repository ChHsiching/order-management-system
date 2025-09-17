#!/bin/bash

# Web Order Management System - Basic API Test
# 基础API测试脚本 - 验证核心API端点的功能

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/../core/test_framework.sh"

# 测试用户注册
test_user_registration() {
    local test_data='{"username":"test_user_'$(date +%s)'","password":"Test123456","email":"test'$(date +%s)'@example.com"}'
    test_api "用户注册" "POST" "/api/user/register" "$test_data" 201
}

# 测试用户登录
test_user_login() {
    local test_data='{"username":"test_admin","password":"Admin123456"}'
    test_api "用户登录" "POST" "/api/user/login" "$test_data" 200
}

# 测试获取用户信息
test_get_user_info() {
    test_api "获取用户信息" "GET" "/api/user/me" "" 200
}

# 测试获取分类列表
test_get_categories() {
    test_api "获取分类列表" "GET" "/api/categories" "" 200
}

# 测试获取分类详情
test_get_category_detail() {
    test_api "获取分类详情" "GET" "/api/categories/1" "" 200
}

# 测试获取菜品列表
test_get_menu_list() {
    test_api "获取菜品列表" "GET" "/api/menu/list" "" 200
}

# 测试获取菜品详情
test_get_menu_detail() {
    test_api "获取菜品详情" "GET" "/api/menu/1" "" 200
}

# 测试菜品搜索
test_menu_search() {
    test_api "菜品搜索" "GET" "/api/menu/search?keyword=测试" "" 200
}

# 测试获取推荐菜品
test_get_recommendations() {
    test_api "获取推荐菜品" "GET" "/api/menu/recommend" "" 200
}

# 测试创建订单
test_create_order() {
    local test_data='{"items":[{"menu_id":1,"quantity":1,"price":12.50}],"total_price":12.50,"address":"测试地址","phone":"13800138001"}'
    test_api "创建订单" "POST" "/api/order/create" "$test_data" 201
}

# 测试获取订单列表
test_get_order_list() {
    test_api "获取订单列表" "GET" "/api/order/list" "" 200
}

# 测试获取订单详情
test_get_order_detail() {
    # 先创建一个订单
    local test_data='{"items":[{"menu_id":1,"quantity":1,"price":12.50}],"total_price":12.50,"address":"测试地址","phone":"13800138001"}'
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$test_data" "$API_BASE/api/order/create")
    local order_id=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//' | jq -r '.id // 1')

    test_api "获取订单详情" "GET" "/api/order/$order_id" "" 200
}

# 测试更新订单状态
test_update_order_status() {
    # 先创建一个订单
    local test_data='{"items":[{"menu_id":1,"quantity":1,"price":12.50}],"total_price":12.50,"address":"测试地址","phone":"13800138001"}'
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$test_data" "$API_BASE/api/order/create")
    local order_id=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//' | jq -r '.id // 1')

    local update_data='{"status":"processing"}'
    test_api "更新订单状态" "PUT" "/api/order/$order_id/status" "$update_data" 200
}

# 测试取消订单
test_cancel_order() {
    # 先创建一个订单
    local test_data='{"items":[{"menu_id":1,"quantity":1,"price":12.50}],"total_price":12.50,"address":"测试地址","phone":"13800138001"}'
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$test_data" "$API_BASE/api/order/create")
    local order_id=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//' | jq -r '.id // 1')

    test_api "取消订单" "PUT" "/api/order/$order_id/cancel" "" 200
}

# 测试添加到购物车
test_add_to_cart() {
    local test_data='{"menu_id":1,"quantity":1}'
    test_api "添加到购物车" "POST" "/api/cart/add" "$test_data" 200
}

# 测试获取购物车
test_get_cart() {
    test_api "获取购物车" "GET" "/api/cart/list" "" 200
}

# 测试更新购物车
test_update_cart() {
    # 先添加一个商品到购物车
    local test_data='{"menu_id":1,"quantity":1}'
    curl -s -X POST -H "Content-Type: application/json" -d "$test_data" "$API_BASE/api/cart/add" > /dev/null

    local update_data='{"quantity":2}'
    test_api "更新购物车" "PUT" "/api/cart/update" "$update_data" 200
}

# 测试删除购物车项
test_remove_from_cart() {
    # 先添加一个商品到购物车
    local test_data='{"menu_id":1,"quantity":1}'
    curl -s -X POST -H "Content-Type: application/json" -d "$test_data" "$API_BASE/api/cart/add" > /dev/null

    test_api "删除购物车项" "DELETE" "/api/cart/1" "" 200
}

# 测试清空购物车
test_clear_cart() {
    test_api "清空购物车" "DELETE" "/api/cart/clear" "" 200
}

# 测试管理员获取用户列表
test_admin_get_users() {
    test_api "管理员获取用户列表" "GET" "/api/admin/users" "" 200
}

# 测试管理员获取分类管理
test_admin_manage_categories() {
    test_api "管理员获取分类管理" "GET" "/api/admin/categories" "" 200
}

# 测试管理员创建分类
test_admin_create_category() {
    local test_data='{"name":"测试分类_'$(date +%s)'","description":"这是一个测试分类"}'
    test_api "管理员创建分类" "POST" "/api/admin/categories" "$test_data" 201
}

# 测试管理员菜品管理
test_admin_manage_menu() {
    test_api "管理员菜品管理" "GET" "/api/admin/menu" "" 200
}

# 测试管理员创建菜品
test_admin_create_menu() {
    local test_data='{"name":"测试菜品_'$(date +%s)'","price":12.50,"category_id":1,"description":"这是一个测试菜品"}'
    test_api "管理员创建菜品" "POST" "/api/admin/menu" "$test_data" 201
}

# 测试管理员获取订单管理
test_admin_manage_orders() {
    test_api "管理员获取订单管理" "GET" "/api/admin/orders" "" 200
}

# 测试管理员获取系统统计
test_admin_get_stats() {
    test_api "管理员获取系统统计" "GET" "/api/admin/stats" "" 200
}

# 测试无效参数
test_invalid_parameters() {
    # 测试空参数
    test_api "空参数测试" "POST" "/api/user/register" "{}" 400

    # 测试无效JSON
    test_api "无效JSON测试" "POST" "/api/user/register" "invalid json" 400

    # 测试不存在的资源
    test_api "不存在的资源测试" "GET" "/api/nonexistent" "" 404
}

# 测试性能
test_performance() {
    log_info "开始性能测试..."

    local start_time=$(date +%s%N)

    # 执行10次API调用
    for i in {1..10}; do
        curl -s "$API_BASE/api/categories" > /dev/null
    done

    local end_time=$(date +%s%N)
    local duration=$(( (end_time - start_time) / 1000000 ))
    local avg_time=$(( duration / 10 ))

    log_info "平均响应时间: ${avg_time}ms"

    if [[ $avg_time -lt 1000 ]]; then
        log_success "性能测试通过: 平均响应时间 ${avg_time}ms < 1000ms"
        update_test_stats "PASS"
    else
        log_error "性能测试失败: 平均响应时间 ${avg_time}ms >= 1000ms"
        update_test_stats "FAIL"
    fi
}

# 主测试流程
main() {
    log_start "基础API测试"

    # 初始化测试环境
    setup_test_environment

    # 用户相关API测试
    log_info "执行用户相关API测试..."
    test_user_registration
    test_user_login
    test_get_user_info

    # 菜单相关API测试
    log_info "执行菜单相关API测试..."
    test_get_categories
    test_get_category_detail
    test_get_menu_list
    test_get_menu_detail
    test_menu_search
    test_get_recommendations

    # 订单相关API测试
    log_info "执行订单相关API测试..."
    test_create_order
    test_get_order_list
    test_get_order_detail
    test_update_order_status
    test_cancel_order

    # 购物车相关API测试
    log_info "执行购物车相关API测试..."
    test_add_to_cart
    test_get_cart
    test_update_cart
    test_remove_from_cart
    test_clear_cart

    # 管理员相关API测试
    log_info "执行管理员相关API测试..."
    test_admin_get_users
    test_admin_manage_categories
    test_admin_create_category
    test_admin_manage_menu
    test_admin_create_menu
    test_admin_manage_orders
    test_admin_get_stats

    # 异常情况测试
    log_info "执行异常情况测试..."
    test_invalid_parameters

    # 性能测试
    log_info "执行性能测试..."
    test_performance

    # 清理测试环境
    cleanup_test_environment

    log_end "基础API测试"

    # 显示测试统计
    show_test_stats
}

# 运行主函数
main "$@"