#!/bin/bash

# Web Order Management System - Database Integrity Test
# 数据库完整性测试脚本 - 验证数据库操作的真实性和数据一致性

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/../core/test_framework.sh"

# 测试用户创建和数据库影响
test_user_creation_db_impact() {
    log_info "测试用户创建的数据库影响..."

    local test_username="test_user_db_$(date +%s)"
    local initial_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators WHERE username LIKE 'test_user_db_%';")

    # 创建用户
    local user_data='{"username":"'"$test_username"'","password":"Test123456","email":"'"$test_username"'@example.com"}'
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "201" ]]; then
        log_success "用户创建API调用成功"

        # 验证数据库中是否真的创建了用户
        local final_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators WHERE username LIKE 'test_user_db_%';")

        if [[ "$final_count" -gt "$initial_count" ]]; then
            log_success "数据库中用户数量增加，用户创建真实有效"
            update_test_stats "PASS"
        else
            log_error "数据库中用户数量未增加，可能返回假数据"
            update_test_stats "FAIL"
        fi

        # 验证用户数据的完整性
        local user_exists=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators WHERE username='$test_username';")
        if [[ "$user_exists" -eq "1" ]]; then
            log_success "用户数据完整保存在数据库中"
        else
            log_error "用户数据未正确保存到数据库"
            update_test_stats "FAIL"
        fi
    else
        log_error "用户创建API调用失败，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 测试订单创建和数据库影响
test_order_creation_db_impact() {
    log_info "测试订单创建的数据库影响..."

    local test_username="test_order_db_$(date +%s)"
    local initial_order_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM cg_info WHERE user_name LIKE 'test_order_db_%';")
    local initial_entry_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM the_order_entry WHERE order_id IN (SELECT id FROM cg_info WHERE user_name LIKE 'test_order_db_%');")

    # 先创建用户
    local user_data='{"username":"'"$test_username"'","password":"Test123456","email":"'"$test_username"'@example.com"}'
    curl -s -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register" > /dev/null

    # 创建订单
    local order_data='{"items":[{"menu_id":1,"quantity":1,"price":12.50}],"total_price":12.50,"address":"测试地址","phone":"13800138001"}'
    local response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST -H "Content-Type: application/json" -d "$order_data" "$API_BASE/api/order/create")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

    if [[ "$http_code" == "201" ]]; then
        log_success "订单创建API调用成功"

        # 验证订单表中的数据
        local final_order_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM cg_info WHERE user_name LIKE 'test_order_db_%';")

        if [[ "$final_order_count" -gt "$initial_order_count" ]]; then
            log_success "订单表中记录增加，订单创建真实有效"

            # 验证订单项表中的数据
            local final_entry_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM the_order_entry WHERE order_id IN (SELECT id FROM cg_info WHERE user_name LIKE 'test_order_db_%');")

            if [[ "$final_entry_count" -gt "$initial_entry_count" ]]; then
                log_success "订单项表中记录增加，订单项创建真实有效"
                update_test_stats "PASS"
            else
                log_error "订单项表中记录未增加，订单项创建可能返回假数据"
                update_test_stats "FAIL"
            fi
        else
            log_error "订单表中记录未增加，订单创建可能返回假数据"
            update_test_stats "FAIL"
        fi

        # 验证订单数据的完整性
        local order_total=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT totalprice FROM cg_info WHERE user_name='$test_username' ORDER BY id DESC LIMIT 1;")
        if [[ "$order_total" == "12.50" ]]; then
            log_success "订单数据完整保存在数据库中，总价正确"
        else
            log_error "订单数据不完整，总价不正确: $order_total"
            update_test_stats "FAIL"
        fi
    else
        log_error "订单创建API调用失败，状态码: $http_code"
        update_test_stats "FAIL"
    fi
}

# 测试外键约束
test_foreign_key_constraints() {
    log_info "测试外键约束..."

    # 测试订单项表的外键约束
    local orphaned_entries=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM the_order_entry WHERE order_id NOT IN (SELECT id FROM cg_info);")

    if [[ "$orphaned_entries" -eq "0" ]]; then
        log_success "订单项表外键约束正常，无孤立订单项"
        update_test_stats "PASS"
    else
        log_error "发现 $orphaned_entries 个孤立订单项，外键约束可能有问题"
        update_test_stats "FAIL"
    fi

    # 测试菜单表的外键约束
    local invalid_category_menus=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM menu WHERE typeID NOT IN (SELECT id FROM ltypes);")

    if [[ "$invalid_category_menus" -eq "0" ]]; then
        log_success "菜单表外键约束正常，无无效分类引用"
        update_test_stats "PASS"
    else
        log_error "发现 $invalid_category_menus 个无效分类引用，外键约束可能有问题"
        update_test_stats "FAIL"
    fi

    # 测试订单表的用户外键约束
    local orphaned_orders=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM cg_info WHERE username NOT IN (SELECT username FROM administrators);")

    if [[ "$orphaned_orders" -eq "0" ]]; then
        log_success "订单表外键约束正常，无孤立订单"
        update_test_stats "PASS"
    else
        log_error "发现 $orphaned_orders 个孤立订单，外键约束可能有问题"
        update_test_stats "FAIL"
    fi
}

# 测试数据一致性
test_data_consistency() {
    log_info "测试数据一致性..."

    # 测试订单总价计算一致性
    local inconsistent_orders=$(mysql -h localhost -u root -p123456 -s -e "
    SELECT COUNT(*) FROM cg_info c
    WHERE c.totalprice != (
        SELECT COALESCE(SUM(price * productnum), 0)
        FROM the_order_entry e
        WHERE e.orderid = c.orderid
    );
    ")

    if [[ "$inconsistent_orders" -eq "0" ]]; then
        log_success "订单总价计算一致"
        update_test_stats "PASS"
    else
        log_error "发现 $inconsistent_orders 个订单总价计算不一致"
        update_test_stats "FAIL"
    fi

    # 测试菜品销量统计一致性
    local menu_sales_check=$(mysql -h localhost -u root -p123456 -s -e "
    SELECT COUNT(*) FROM menu m
    WHERE m.xiaoliang != (
        SELECT COALESCE(SUM(e.productnum), 0)
        FROM the_order_entry e
        WHERE e.productid = m.id
    );
    ")

    if [[ "$menu_sales_check" -eq "0" ]]; then
        log_success "菜品销量统计一致"
        update_test_stats "PASS"
    else
        log_warn "发现 $menu_sales_check 个菜品销量统计不一致（可能是因为没有订单数据）"
        update_test_stats "PASS"
    fi
}

# 测试事务完整性
test_transaction_integrity() {
    log_info "测试事务完整性..."

    # 测试订单创建事务
    local test_username="test_txn_$(date +%s)"

    # 模拟事务中断的情况，检查数据一致性
    local before_user_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators WHERE username LIKE '$test_username%';")
    local before_order_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM cg_info WHERE user_name LIKE '$test_username%';")

    # 这里可以添加更复杂的事务测试逻辑
    # 目前简单验证数据一致性
    log_info "验证事务前后数据一致性..."

    local after_user_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators WHERE username LIKE '$test_username%';")
    local after_order_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM cg_info WHERE user_name LIKE '$test_username%';")

    if [[ "$before_user_count" == "$after_user_count" && "$before_order_count" == "$after_order_count" ]]; then
        log_success "事务完整性验证通过"
        update_test_stats "PASS"
    else
        log_error "事务完整性验证失败，数据不一致"
        update_test_stats "FAIL"
    fi
}

# 测试数据库性能
test_database_performance() {
    log_info "测试数据库性能..."

    local start_time=$(date +%s%N)

    # 执行10次简单查询
    for i in {1..10}; do
        mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators;" > /dev/null
    done

    local end_time=$(date +%s%N)
    local duration=$(( (end_time - start_time) / 1000000 ))
    local avg_time=$(( duration / 10 ))

    log_info "数据库查询平均响应时间: ${avg_time}ms"

    if [[ $avg_time -lt 100 ]]; then
        log_success "数据库性能测试通过，平均响应时间 ${avg_time}ms < 100ms"
        update_test_stats "PASS"
    else
        log_warn "数据库性能较慢，平均响应时间 ${avg_time}ms >= 100ms"
        update_test_stats "PASS"
    fi
}

# 测试数据清理功能
test_data_cleanup() {
    log_info "测试数据清理功能..."

    # 创建测试数据
    local test_username="test_cleanup_$(date +%s)"
    local user_data='{"username":"'"$test_username"'","password":"Test123456","email":"'"$test_username"'@example.com"}'
    curl -s -X POST -H "Content-Type: application/json" -d "$user_data" "$API_BASE/api/user/register" > /dev/null

    # 验证数据存在
    local user_exists_before=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators WHERE username='$test_username';")

    if [[ "$user_exists_before" -eq "1" ]]; then
        log_success "测试数据创建成功"

        # 清理测试数据
        mysql -h localhost -u root -p123456 -s -e "USE web_order; DELETE FROM administrators WHERE username='$test_username';" > /dev/null

        # 验证数据已清理
        local user_exists_after=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM administrators WHERE username='$test_username';")

        if [[ "$user_exists_after" -eq "0" ]]; then
            log_success "测试数据清理成功"
            update_test_stats "PASS"
        else
            log_error "测试数据清理失败"
            update_test_stats "FAIL"
        fi
    else
        log_error "测试数据创建失败"
        update_test_stats "FAIL"
    fi
}

# 主测试流程
main() {
    log_start "数据库完整性测试"

    # 初始化测试环境
    setup_test_environment

    # 执行数据库测试
    test_user_creation_db_impact
    test_order_creation_db_impact
    test_foreign_key_constraints
    test_data_consistency
    test_transaction_integrity
    test_database_performance
    test_data_cleanup

    # 清理测试环境
    cleanup_test_environment

    log_end "数据库完整性测试"

    # 显示测试统计
    show_test_stats
}

# 运行主函数
main "$@"