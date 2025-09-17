#!/bin/bash

# Web Order Management System - Test Data Manager
# 测试数据管理器 - 负责测试数据的生命周期管理

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/test_framework.sh"

# 配置文件
CONFIG_FILE="$SCRIPT_DIR/../../config/test_config.yml"
DATA_SEED_FILE="$SCRIPT_DIR/../../data/seed_data.sql"

# 数据库连接配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="web_order"
DB_USER="root"
DB_PASS="123456"

# 显示帮助信息
show_help() {
    cat << EOF
Web订餐管理系统测试数据管理器

使用方法:
    $0 [命令] [选项]

命令:
    init                    初始化测试数据
    clean                   清理测试数据
    reset                   重置测试数据
    create-scenario NAME     创建特定测试场景
    verify                  验证数据状态
    snapshot [NAME]         创建数据快照
    restore [NAME]          恢复数据快照
    export [FILE]           导出测试数据
    import [FILE]           导入测试数据

选项:
    -h, --help              显示此帮助信息
    -c, --config FILE       指定配置文件
    -v, --verbose           详细输出
    --dry-run               试运行模式

示例:
    $0 init                                    # 初始化测试数据
    $0 clean                                   # 清理测试数据
    $0 create-scenario user_registration       # 创建用户注册测试场景
    $0 verify                                  # 验证数据状态
    $0 snapshot before_test                    # 创建测试前快照
    $0 restore before_test                     # 恢复测试前快照

EOF
}

# 解析命令行参数
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -c|--config)
                CONFIG_FILE="$2"
                shift 2
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            *)
                COMMAND="$1"
                shift
                ;;
        esac
    done
}

# 数据库连接函数
db_connect() {
    if [[ "$DRY_RUN" == "true" ]]; then
        echo "DRY RUN: mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME"
        return 0
    fi

    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" 2>/dev/null
}

# 执行SQL语句
execute_sql() {
    local sql="$1"
    if [[ "$DRY_RUN" == "true" ]]; then
        echo "DRY RUN: $sql"
        return 0
    fi

    db_connect -e "$sql"
}

# 检查数据库连接
check_db_connection() {
    log_info "检查数据库连接..."

    if ! execute_sql "SELECT 1;" >/dev/null 2>&1; then
        log_error "数据库连接失败"
        return 1
    fi

    log_success "数据库连接正常"
    return 0
}

# 初始化测试数据
init_test_data() {
    log_info "初始化测试数据..."

    # 检查数据库连接
    check_db_connection || exit 1

    # 创建测试数据
    log_info "创建测试用户..."
    execute_sql "
    INSERT IGNORE INTO administrators (username, password, role, email, phone, status) VALUES
    ('test_member_001', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMy.MrqK3a7M8PvKQ5J5zXeYvKJQ8X5z6Zi', 'member', 'member001@test.com', '13800138001', 1),
    ('test_member_002', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMy.MrqK3a7M8PvKQ5J5zXeYvKJQ8X5z6Zi', 'member', 'member002@test.com', '13800138002', 1),
    ('test_admin_001', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMy.MrqK3a7M8PvKQ5J5zXeYvKJQ8X5z6Zi', 'admin', 'admin001@test.com', '13800138003', 1);
    "

    log_info "创建测试分类..."
    execute_sql "
    INSERT IGNORE INTO ltypes (lname, catelock) VALUES
    ('测试分类001', 0),
    ('测试分类002', 0),
    ('测试分类003', 0);
    "

    log_info "创建测试菜品..."
    execute_sql "
    INSERT IGNORE INTO menu (lname, price, hotPrice, typeID, productLock, description, image, xiaoliang) VALUES
    ('测试菜品001', 12.50, 10.00, 1, 0, '这是一个测试菜品', '/images/test001.jpg', 0),
    ('测试菜品002', 25.00, 20.00, 1, 0, '这是另一个测试菜品', '/images/test002.jpg', 0),
    ('测试菜品003', 15.00, 12.00, 2, 0, '这是第三个测试菜品', '/images/test003.jpg', 0),
    ('测试菜品004', 30.00, 25.00, 2, 0, '这是第四个测试菜品', '/images/test004.jpg', 0),
    ('测试菜品005', 8.00, 6.00, 3, 0, '这是第五个测试菜品', '/images/test005.jpg', 0);
    "

    log_success "测试数据初始化完成"
}

# 清理测试数据
clean_test_data() {
    log_info "清理测试数据..."

    # 检查数据库连接
    check_db_connection || exit 1

    # 清理测试订单项
    log_info "清理测试订单项..."
    execute_sql "
    DELETE FROM the_order_entry
    WHERE order_id IN (
        SELECT id FROM cg_info
        WHERE user_name LIKE 'test_%' OR
               user_name IN ('test_member_001', 'test_member_002')
    );
    "

    # 清理测试订单
    log_info "清理测试订单..."
    execute_sql "
    DELETE FROM cg_info
    WHERE user_name LIKE 'test_%' OR
               user_name IN ('test_member_001', 'test_member_002');
    "

    # 清理测试用户
    log_info "清理测试用户..."
    execute_sql "
    DELETE FROM administrators
    WHERE username LIKE 'test_%' OR
               username IN ('test_member_001', 'test_member_002', 'test_admin_001');
    "

    log_success "测试数据清理完成"
}

# 重置测试数据
reset_test_data() {
    log_info "重置测试数据..."
    clean_test_data
    init_test_data
    log_success "测试数据重置完成"
}

# 创建测试场景
create_test_scenario() {
    local scenario_name="$1"

    case "$scenario_name" in
        "user_registration")
            log_info "创建用户注册测试场景..."
            # 清理特定的测试数据
            execute_sql "DELETE FROM administrators WHERE username = 'test_reg_user';"
            ;;
        "order_creation")
            log_info "创建订单创建测试场景..."
            # 确保有测试用户和菜品
            execute_sql "
            INSERT IGNORE INTO administrators (username, password, role, email, phone, status)
            VALUES ('test_order_user', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMy.MrqK3a7M8PvKQ5J5zXeYvKJQ8X5z6Zi', 'member', 'order@test.com', '13800138001', 1);
            "
            ;;
        "admin_operations")
            log_info "创建管理员操作测试场景..."
            # 确保有管理员用户
            execute_sql "
            INSERT IGNORE INTO administrators (username, password, role, email, phone, status)
            VALUES ('test_admin_ops', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMy.MrqK3a7M8PvKQ5J5zXeYvKJQ8X5z6Zi', 'admin', 'admin_ops@test.com', '13800138002', 1);
            "
            ;;
        *)
            log_error "未知的测试场景: $scenario_name"
            echo "可用的测试场景:"
            echo "  user_registration  - 用户注册测试场景"
            echo "  order_creation     - 订单创建测试场景"
            echo "  admin_operations   - 管理员操作测试场景"
            exit 1
            ;;
    esac

    log_success "测试场景 '$scenario_name' 创建完成"
}

# 验证数据状态
verify_data_state() {
    log_info "验证测试数据状态..."

    local issues=0

    # 检查用户表
    local user_count=$(execute_sql "SELECT COUNT(*) FROM administrators WHERE username LIKE 'test_%';" | tail -n 1)
    log_info "测试用户数量: $user_count"

    # 检查分类表
    local category_count=$(execute_sql "SELECT COUNT(*) FROM ltypes WHERE lname LIKE '测试分类%';" | tail -n 1)
    log_info "测试分类数量: $category_count"

    # 检查菜品表
    local menu_count=$(execute_sql "SELECT COUNT(*) FROM menu WHERE lname LIKE '测试菜品%';" | tail -n 1)
    log_info "测试菜品数量: $menu_count"

    # 检查数据一致性
    local orphaned_orders=$(execute_sql "SELECT COUNT(*) FROM cg_info WHERE user_name NOT IN (SELECT username FROM administrators);" | tail -n 1)
    if [[ "$orphaned_orders" -gt 0 ]]; then
        log_warn "发现孤立订单记录: $orphaned_orders"
        issues=$((issues + 1))
    fi

    local orphaned_order_entries=$(execute_sql "SELECT COUNT(*) FROM the_order_entry WHERE order_id NOT IN (SELECT id FROM cg_info);" | tail -n 1)
    if [[ "$orphaned_order_entries" -gt 0 ]]; then
        log_warn "发现孤立订单项记录: $orphaned_order_entries"
        issues=$((issues + 1))
    fi

    if [[ $issues -eq 0 ]]; then
        log_success "数据状态验证通过"
        return 0
    else
        log_error "数据状态验证失败，发现 $issues 个问题"
        return 1
    fi
}

# 创建数据快照
create_snapshot() {
    local snapshot_name="${1:-$(date +%Y%m%d_%H%M%S)}"
    local snapshot_file="$SCRIPT_DIR/../../data/snapshots/${snapshot_name}.sql"

    log_info "创建数据快照: $snapshot_name"

    mkdir -p "$SCRIPT_DIR/../../data/snapshots"

    if [[ "$DRY_RUN" == "true" ]]; then
        echo "DRY RUN: mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME > $snapshot_file"
        return 0
    fi

    mysqldump -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" > "$snapshot_file"

    if [[ $? -eq 0 ]]; then
        log_success "数据快照创建完成: $snapshot_file"
        return 0
    else
        log_error "数据快照创建失败"
        return 1
    fi
}

# 恢复数据快照
restore_snapshot() {
    local snapshot_name="$1"
    local snapshot_file="$SCRIPT_DIR/../../data/snapshots/${snapshot_name}.sql"

    if [[ ! -f "$snapshot_file" ]]; then
        log_error "快照文件不存在: $snapshot_file"
        return 1
    fi

    log_info "恢复数据快照: $snapshot_name"

    if [[ "$DRY_RUN" == "true" ]]; then
        echo "DRY RUN: mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < $snapshot_file"
        return 0
    fi

    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$snapshot_file"

    if [[ $? -eq 0 ]]; then
        log_success "数据快照恢复完成: $snapshot_name"
        return 0
    else
        log_error "数据快照恢复失败"
        return 1
    fi
}

# 导出测试数据
export_data() {
    local export_file="${1:-$(date +%Y%m%d_%H%M%S)_export.sql}"

    log_info "导出测试数据到: $export_file"

    if [[ "$DRY_RUN" == "true" ]]; then
        echo "DRY RUN: 导出测试数据到 $export_file"
        return 0
    fi

    # 只导出测试相关的数据
    cat > "$export_file" << 'EOF'
-- Web订餐管理系统测试数据导出
-- 导出时间: $(date)
-- 包含表: administrators, ltypes, menu, cg_info, the_order_entry

SET FOREIGN_KEY_CHECKS = 0;

EOF

    # 导出测试用户
    echo "-- 导出测试用户" >> "$export_file"
    execute_sql "SELECT * FROM administrators WHERE username LIKE 'test_%';" | sed 's/^/INSERT INTO administrators VALUES /;s/$/;/' >> "$export_file"

    # 导出测试分类
    echo "-- 导出测试分类" >> "$export_file"
    execute_sql "SELECT * FROM ltypes WHERE lname LIKE '测试分类%';" | sed 's/^/INSERT INTO ltypes VALUES /;s/$/;/' >> "$export_file"

    # 导出测试菜品
    echo "-- 导出测试菜品" >> "$export_file"
    execute_sql "SELECT * FROM menu WHERE lname LIKE '测试菜品%';" | sed 's/^/INSERT INTO menu VALUES /;s/$/;/' >> "$export_file"

    echo "SET FOREIGN_KEY_CHECKS = 1;" >> "$export_file"

    log_success "测试数据导出完成: $export_file"
}

# 导入测试数据
import_data() {
    local import_file="$1"

    if [[ ! -f "$import_file" ]]; then
        log_error "导入文件不存在: $import_file"
        return 1
    fi

    log_info "导入测试数据从: $import_file"

    if [[ "$DRY_RUN" == "true" ]]; then
        echo "DRY RUN: mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < $import_file"
        return 0
    fi

    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$import_file"

    if [[ $? -eq 0 ]]; then
        log_success "测试数据导入完成: $import_file"
        return 0
    else
        log_error "测试数据导入失败"
        return 1
    fi
}

# 显示数据统计
show_data_stats() {
    log_info "测试数据统计:"

    echo "========================================"
    echo "测试数据统计"
    echo "========================================"

    local user_count=$(execute_sql "SELECT COUNT(*) FROM administrators WHERE username LIKE 'test_%';" | tail -n 1)
    echo "测试用户: $user_count"

    local category_count=$(execute_sql "SELECT COUNT(*) FROM ltypes WHERE lname LIKE '测试分类%';" | tail -n 1)
    echo "测试分类: $category_count"

    local menu_count=$(execute_sql "SELECT COUNT(*) FROM menu WHERE lname LIKE '测试菜品%';" | tail -n 1)
    echo "测试菜品: $menu_count"

    local order_count=$(execute_sql "SELECT COUNT(*) FROM cg_info WHERE user_name LIKE 'test_%';" | tail -n 1)
    echo "测试订单: $order_count"

    local order_entry_count=$(execute_sql "SELECT COUNT(*) FROM the_order_entry WHERE order_id IN (SELECT id FROM cg_info WHERE user_name LIKE 'test_%');" | tail -n 1)
    echo "测试订单项: $order_entry_count"

    echo "========================================"
}

# 主函数
main() {
    # 解析命令行参数
    parse_args "$@"

    # 设置默认配置
    VERBOSE="${VERBOSE:-false}"
    DRY_RUN="${DRY_RUN:-false}"

    # 初始化测试框架
    init_test_framework

    # 执行相应的命令
    case "$COMMAND" in
        "init")
            init_test_data
            ;;
        "clean")
            clean_test_data
            ;;
        "reset")
            reset_test_data
            ;;
        "create-scenario")
            if [[ -z "$1" ]]; then
                log_error "请指定测试场景名称"
                show_help
                exit 1
            fi
            create_test_scenario "$1"
            ;;
        "verify")
            verify_data_state
            ;;
        "snapshot")
            create_snapshot "$1"
            ;;
        "restore")
            if [[ -z "$1" ]]; then
                log_error "请指定快照名称"
                show_help
                exit 1
            fi
            restore_snapshot "$1"
            ;;
        "export")
            export_data "$1"
            ;;
        "import")
            if [[ -z "$1" ]]; then
                log_error "请指定导入文件"
                show_help
                exit 1
            fi
            import_data "$1"
            ;;
        "stats")
            show_data_stats
            ;;
        "")
            log_error "请指定命令"
            show_help
            exit 1
            ;;
        *)
            log_error "未知命令: $COMMAND"
            show_help
            exit 1
            ;;
    esac
}

# 运行主函数
main "$@"