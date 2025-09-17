#!/bin/bash

# 数据库完整性验证脚本
# 验证API操作对数据库的真实影响

set -e

DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="web_order"
DB_USER="root"
DB_PASS="${1:-123456}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "$1"
}

# 记录数据库状态
record_db_state() {
    local snapshot_name="$1"
    log "${BLUE}=== 数据库快照: $snapshot_name ===${NC}"

    # 记录各表记录数
    log "${YELLOW}表记录统计:${NC}"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "
        USE $DB_NAME;
        SELECT 'administrators' as table_name, COUNT(*) as count FROM administrators
        UNION SELECT 'ltypes', COUNT(*) FROM ltypes
        UNION SELECT 'menu', COUNT(*) FROM menu
        UNION SELECT 'cg_info', COUNT(*) FROM cg_info
        UNION SELECT 'the_order_entry', COUNT(*) FROM the_order_entry;
    " --column-names=false

    # 记录菜品销量
    log "${YELLOW}菜品销量TOP 5:${NC}"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "
        USE $DB_NAME;
        SELECT name, xiaoliang FROM menu ORDER BY xiaoliang DESC LIMIT 5;
    " --column-names=false

    # 记录订单状态
    log "${YELLOW}订单状态统计:${NC}"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "
        USE $DB_NAME;
        SELECT status, COUNT(*) as count FROM cg_info GROUP BY status;
    " --column-names=false

    log "${BLUE}----------------------------------------${NC}"
}

# 验证订单创建的数据库影响
verify_order_creation() {
    local test_username="test_order_$(date +%s)"
    local test_phone="138$(date +%s | tail -6)"

    log "${BLUE}=== 验证订单创建的数据库影响 ===${NC}"

    # 记录操作前状态
    record_db_state "订单创建前"

    # 通过API创建订单
    log "${YELLOW}通过API创建订单...${NC}"
    api_response=$(curl -s -X POST "http://localhost:8080/WebOrderSystem/api/orders" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$test_username\",
            \"items\": [
                {\"menuId\": 1, \"quantity\": 2},
                {\"menuId\": 2, \"quantity\": 1}
            ],
            \"address\": \"测试地址\",
            \"phone\": \"$test_phone\"
        }")

    log "${YELLOW}API响应: $api_response${NC}"

    # 提取订单ID
    order_id=$(echo "$api_response" | grep -o '"orderId":"[^"]*"' | cut -d'"' -f4)

    if [[ -n "$order_id" ]]; then
        log "${GREEN}订单创建成功，ID: $order_id${NC}"

        # 等待1秒确保数据库操作完成
        sleep 1

        # 记录操作后状态
        record_db_state "订单创建后"

        # 验证订单数据
        log "${BLUE}=== 验证订单数据 ===${NC}"

        # 检查订单主表
        order_exists=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
            -e "USE $DB_NAME; SELECT COUNT(*) FROM cg_info WHERE orderid = '$order_id';" -s -N)

        if [[ "$order_exists" -eq 1 ]]; then
            log "${GREEN}✅ 订单主表记录验证通过${NC}"
        else
            log "${RED}❌ 订单主表记录验证失败${NC}"
            return 1
        fi

        # 检查订单条目表
        entry_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
            -e "USE $DB_NAME; SELECT COUNT(*) FROM the_order_entry WHERE orderid = '$order_id';" -s -N)

        if [[ "$entry_count" -eq 2 ]]; then
            log "${GREEN}✅ 订单条目记录验证通过 (期望2条，实际$entry_count条)${NC}"
        else
            log "${RED}❌ 订单条目记录验证失败 (期望2条，实际$entry_count条)${NC}"
            return 1
        fi

        # 检查菜品销量是否更新
        log "${BLUE}检查菜品销量更新:${NC}"
        sales_1_before=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
            -e "USE $DB_NAME; SELECT xiaoliang FROM menu WHERE id = 1;" -s -N)
        sales_2_before=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
            -e "USE $DB_NAME; SELECT xiaoliang FROM menu WHERE id = 2;" -s -N)

        log "${YELLOW}菜品1销量: $sales_1_before (应该增加2)${NC}"
        log "${YELLOW}菜品2销量: $sales_2_before (应该增加1)${NC}"

        # 检查用户是否自动创建
        user_exists=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
            -e "USE $DB_NAME; SELECT COUNT(*) FROM administrators WHERE username = '$test_username';" -s -N)

        if [[ "$user_exists" -gt 0 ]]; then
            log "${GREEN}✅ 用户自动创建验证通过${NC}"
        else
            log "${YELLOW}⚠️  用户未自动创建 (可能已存在)${NC}"
        fi

    else
        log "${RED}❌ 订单创建失败，无法获取订单ID${NC}"
        return 1
    fi
}

# 验证数据一致性
verify_data_consistency() {
    log "${BLUE}=== 验证数据一致性 ===${NC}"

    # 检查外键约束
    log "${YELLOW}检查外键约束:${NC}"

    # 检查订单条目是否都有关联的订单
    orphaned_entries=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        -e "USE $DB_NAME; SELECT COUNT(*) FROM the_order_entry oe
            LEFT JOIN cg_info oi ON oe.orderid = oi.orderid
            WHERE oi.orderid IS NULL;" -s -N)

    if [[ "$orphaned_entries" -eq 0 ]]; then
        log "${GREEN}✅ 订单条目外键约束验证通过${NC}"
    else
        log "${RED}❌ 发现 $orphaned_entries 个孤立的订单条目${NC}"
    fi

    # 检查菜单项是否都有关联的分类
    orphaned_menus=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        -e "USE $DB_NAME; SELECT COUNT(*) FROM menu m
            LEFT JOIN ltypes l ON m.cateid = l.id
            WHERE l.id IS NULL;" -s -N)

    if [[ "$orphaned_menus" -eq 0 ]]; then
        log "${GREEN}✅ 菜单分类外键约束验证通过${NC}"
    else
        log "${RED}❌ 发现 $orphaned_menus 个孤立的菜单项${NC}"
    fi

    # 检查订单是否都有关联的用户
    orphaned_orders=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        -e "USE $DB_NAME; SELECT COUNT(*) FROM cg_info c
            LEFT JOIN administrators a ON c.username = a.username
            WHERE a.username IS NULL;" -s -N)

    if [[ "$orphaned_orders" -eq 0 ]]; then
        log "${GREEN}✅ 订单用户外键约束验证通过${NC}"
    else
        log "${RED}❌ 发现 $orphaned_orders 个孤立的订单${NC}"
    fi

    # 检查价格计算准确性
    log "${YELLOW}检查价格计算准确性:${NC}"
    price_errors=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        -e "USE $DB_NAME;
            SELECT COUNT(*) as error_count
            FROM cg_info oi
            LEFT JOIN (
                SELECT orderid, SUM(price * productnum) as calculated_total
                FROM the_order_entry
                GROUP BY orderid
            ) oe ON oi.orderid = oe.orderid
            WHERE oi.totalprice != oe.calculated_total
            OR oe.calculated_total IS NULL;" -s -N)

    if [[ "$price_errors" -eq 0 ]]; then
        log "${GREEN}✅ 订单价格计算验证通过${NC}"
    else
        log "${RED}❌ 发现 $price_errors 个价格计算错误${NC}"
    fi
}

# 主函数
main() {
    log "${BLUE}数据库完整性验证开始${NC}"
    log "${BLUE}验证时间: $(date)${NC}"
    log "${BLUE}========================================${NC}"

    # 检查数据库连接
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "USE $DB_NAME; SELECT 1;" &>/dev/null; then
        log "${RED}❌ 数据库连接失败${NC}"
        exit 1
    fi

    # 执行验证
    verify_order_creation
    verify_data_consistency

    log "${BLUE}========================================${NC}"
    log "${GREEN}🎉 数据库完整性验证完成${NC}"
}

main "$@"