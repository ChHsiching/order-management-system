#!/bin/bash

# Web Order Management System - Test Runner
# 测试运行器 - 统一的测试执行和管理

set -e  # 遇到错误时退出

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/test_framework.sh"

# 默认配置
DEFAULT_CONFIG="$SCRIPT_DIR/../../config/test_config.yml"
DEFAULT_PARALLEL=false
DEFAULT_REPORT_TYPE="markdown"
DEFAULT_LOG_LEVEL="INFO"

# 显示帮助信息
show_help() {
    cat << EOF
Web订餐管理系统测试运行器

使用方法:
    $0 [选项] [测试类型]

选项:
    -h, --help              显示此帮助信息
    -a, --all               运行所有测试
    -t, --type TYPE         指定测试类型 (api|security|database|integration)
    -f, --file FILE         运行特定测试文件
    -c, --config CONFIG     指定配置文件 (默认: $DEFAULT_CONFIG)
    -p, --parallel          并行执行测试
    -r, --report TYPE       报告类型 (markdown|html|json)
    -l, --level LEVEL       日志级别 (DEBUG|INFO|WARN|ERROR)
    --check-env             检查测试环境
    --debug                 启用调试模式
    --errors-only           只显示错误信息

测试类型:
    api                     API功能测试
    security                安全测试
    database                数据库测试
    integration             集成测试

示例:
    $0 --all                           # 运行所有测试
    $0 --type api                      # 运行API测试
    $0 --file user_api_test.sh         # 运行特定测试文件
    $0 --type security --parallel      # 并行执行安全测试
    $0 --check-env                     # 检查测试环境

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
            -a|--all)
                RUN_ALL=true
                shift
                ;;
            -t|--type)
                TEST_TYPE="$2"
                shift 2
                ;;
            -f|--file)
                TEST_FILE="$2"
                shift 2
                ;;
            -c|--config)
                CONFIG_FILE="$2"
                shift 2
                ;;
            -p|--parallel)
                PARALLEL=true
                shift
                ;;
            -r|--report)
                REPORT_TYPE="$2"
                shift 2
                ;;
            -l|--level)
                LOG_LEVEL="$2"
                shift 2
                ;;
            --check-env)
                CHECK_ENV=true
                shift
                ;;
            --debug)
                DEBUG=true
                LOG_LEVEL="DEBUG"
                shift
                ;;
            --errors-only)
                ERRORS_ONLY=true
                shift
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# 检查测试环境
check_test_environment() {
    log_info "检查测试环境..."

    local issues=0

    # 检查必要工具
    local required_tools=("curl" "mysql" "jq" "bc")
    for tool in "${required_tools[@]}"; do
        if ! command -v "$tool" >/dev/null 2>&1; then
            log_error "缺少必要工具: $tool"
            issues=$((issues + 1))
        fi
    done

    # 检查Java环境
    if ! java -version >/dev/null 2>&1; then
        log_error "Java环境未配置"
        issues=$((issues + 1))
    fi

    # 检查后端服务
    if ! curl -f "$API_BASE/api-docs" >/dev/null 2>&1; then
        log_error "后端服务未启动或无法访问"
        issues=$((issues + 1))
    fi

    # 检查数据库连接
    if ! mysql -h localhost -u root -p123456 -e "USE web_order; SELECT 1;" >/dev/null 2>&1; then
        log_error "数据库连接失败"
        issues=$((issues + 1))
    fi

    # 检查测试框架文件
    local required_files=("$SCRIPT_DIR/test_framework.sh")
    for file in "${required_files[@]}"; do
        if [[ ! -f "$file" ]]; then
            log_error "缺少测试框架文件: $file"
            issues=$((issues + 1))
        fi
    done

    if [[ $issues -eq 0 ]]; then
        log_success "测试环境检查通过"
        return 0
    else
        log_error "测试环境检查失败，发现 $issues 个问题"
        return 1
    fi
}

# 运行API测试
run_api_tests() {
    log_start "API测试"

    local api_tests=(
        "basic_api_test.sh"
        "user_api_test.sh"
        "menu_api_test.sh"
        "order_api_test.sh"
    )

    for test_file in "${api_tests[@]}"; do
        local test_path="$SCRIPT_DIR/../api/$test_file"
        if [[ -f "$test_path" ]]; then
            log_info "运行API测试: $test_file"
            bash "$test_path"
        else
            log_warn "API测试文件不存在: $test_path"
        fi
    done

    log_end "API测试"
}

# 运行安全测试
run_security_tests() {
    log_start "安全测试"

    local security_tests=(
        "permission_test.sh"
        "privilege_escalation_test.sh"
        "authentication_test.sh"
    )

    for test_file in "${security_tests[@]}"; do
        local test_path="$SCRIPT_DIR/../security/$test_file"
        if [[ -f "$test_path" ]]; then
            log_info "运行安全测试: $test_file"
            bash "$test_path"
        else
            log_warn "安全测试文件不存在: $test_path"
        fi
    done

    log_end "安全测试"
}

# 运行数据库测试
run_database_tests() {
    log_start "数据库测试"

    local database_tests=(
        "db_integrity_test.sh"
        "data_validation_test.sh"
        "performance_test.sh"
    )

    for test_file in "${database_tests[@]}"; do
        local test_path="$SCRIPT_DIR/../database/$test_file"
        if [[ -f "$test_path" ]]; then
            log_info "运行数据库测试: $test_file"
            bash "$test_path"
        else
            log_warn "数据库测试文件不存在: $test_path"
        fi
    done

    log_end "数据库测试"
}

# 运行集成测试
run_integration_tests() {
    log_start "集成测试"

    local integration_tests=(
        "business_flow_test.sh"
        "end_to_end_test.sh"
    )

    for test_file in "${integration_tests[@]}"; do
        local test_path="$SCRIPT_DIR/../integration/$test_file"
        if [[ -f "$test_path" ]]; then
            log_info "运行集成测试: $test_file"
            bash "$test_path"
        else
            log_warn "集成测试文件不存在: $test_path"
        fi
    done

    log_end "集成测试"
}

# 运行特定测试文件
run_specific_test() {
    local test_file="$1"
    log_start "特定测试: $test_file"

    if [[ -f "$test_file" ]]; then
        log_info "运行测试文件: $test_file"
        bash "$test_file"
    else
        log_error "测试文件不存在: $test_file"
        return 1
    fi

    log_end "特定测试: $test_file"
}

# 并行运行测试
run_parallel_tests() {
    log_info "并行执行测试..."

    local pids=()

    # 并行运行不同类型的测试
    run_api_tests &
    pids+=($!)

    run_security_tests &
    pids+=($!)

    run_database_tests &
    pids+=($!)

    # 等待所有测试完成
    for pid in "${pids[@]}"; do
        wait "$pid"
    done

    log_info "并行测试执行完成"
}

# 主函数
main() {
    # 设置默认值
    CONFIG_FILE="${CONFIG_FILE:-$DEFAULT_CONFIG}"
    PARALLEL="${PARALLEL:-$DEFAULT_PARALLEL}"
    REPORT_TYPE="${REPORT_TYPE:-$DEFAULT_REPORT_TYPE}"
    LOG_LEVEL="${LOG_LEVEL:-$DEFAULT_LOG_LEVEL}"

    # 解析命令行参数
    parse_args "$@"

    # 初始化测试框架
    init_test_framework

    # 检查测试环境
    if [[ "$CHECK_ENV" == "true" ]]; then
        check_test_environment
        exit $?
    fi

    # 设置测试环境
    setup_test_environment

    # 根据参数运行测试
    if [[ "$RUN_ALL" == "true" ]]; then
        log_info "运行所有测试..."
        if [[ "$PARALLEL" == "true" ]]; then
            run_parallel_tests
        else
            run_api_tests
            run_security_tests
            run_database_tests
            run_integration_tests
        fi
    elif [[ -n "$TEST_TYPE" ]]; then
        case "$TEST_TYPE" in
            "api")
                run_api_tests
                ;;
            "security")
                run_security_tests
                ;;
            "database")
                run_database_tests
                ;;
            "integration")
                run_integration_tests
                ;;
            *)
                log_error "未知的测试类型: $TEST_TYPE"
                show_help
                exit 1
                ;;
        esac
    elif [[ -n "$TEST_FILE" ]]; then
        run_specific_test "$TEST_FILE"
    else
        log_error "请指定要运行的测试类型或文件"
        show_help
        exit 1
    fi

    # 清理测试环境
    cleanup_test_environment

    # 显示测试统计
    show_test_stats

    # 生成测试报告
    local report_file=$(generate_test_report)

    # 显示报告路径
    log_info "测试报告已生成: $report_file"

    # 根据测试结果设置退出码
    if [[ $TEST_FAILED -gt 0 ]]; then
        log_error "测试完成，但有 $TEST_FAILED 个测试失败"
        exit 1
    else
        log_success "测试完成，所有测试通过"
        exit 0
    fi
}

# 运行主函数
main "$@"