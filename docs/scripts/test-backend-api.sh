#!/bin/bash

# =============================================================================
# 订餐管理系统 - 后端API测试脚本
# Backend API Test Script for Order Management System
# =============================================================================

# 加载工具函数库
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/api-test.sh"

# 初始化脚本
init_script
init_api_test

# =============================================================================
# API测试主函数
# =============================================================================

# 显示API测试帮助
show_api_test_help() {
    cat << EOF
后端API测试脚本使用方法:

基本测试:
  ./test-backend-api.sh                   # 运行完整API测试套件
  ./test-backend-api.sh --public          # 只测试公开API
  ./test-backend-api.sh --auth            # 测试认证API
  ./test-backend-api.sh --admin           # 测试管理员API
  ./test-backend-api.sh --user            # 测试用户API

服务检查:
  ./test-backend-api.sh --check           # 检查后端服务状态
  ./test-backend-api.sh --wait            # 等待后端服务可用

测试选项:
  ./test-backend-api.sh --quick           # 快速测试（关键API）
  ./test-backend-api.sh --full            # 完整测试（所有API）
  ./test-backend-api.sh --report          # 生成详细测试报告

输出控制:
  ./test-backend-api.sh --json            # JSON格式输出
  ./test-backend-api.sh --verbose         # 详细输出
  ./test-backend-api.sh --quiet           # 静默模式

示例:
  ./test-backend-api.sh --check --wait    # 检查并等待服务可用
  ./test-backend-api.sh --public --json   # 测试公开API并输出JSON
  ./test-backend-api.sh --full --report   # 运行完整测试并生成报告
EOF
}

# 解析命令行参数
parse_arguments() {
    local test_type="full"
    local check_only=false
    local wait_for_service=false
    local json_output=false
    local verbose=false
    local quiet=false
    local generate_report=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --public)
                test_type="public"
                shift
                ;;
            --auth)
                test_type="auth"
                shift
                ;;
            --admin)
                test_type="admin"
                shift
                ;;
            --user)
                test_type="user"
                shift
                ;;
            --check)
                check_only=true
                shift
                ;;
            --wait)
                wait_for_service=true
                shift
                ;;
            --quick)
                test_type="quick"
                shift
                ;;
            --full)
                test_type="full"
                shift
                ;;
            --report)
                generate_report=true
                shift
                ;;
            --json)
                json_output=true
                shift
                ;;
            --verbose)
                verbose=true
                shift
                ;;
            --quiet)
                quiet=true
                shift
                ;;
            --help|-h)
                show_api_test_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_api_test_help
                exit 1
                ;;
        esac
    done

    echo "$test_type|$check_only|$wait_for_service|$json_output|$verbose|$quiet|$generate_report"
}

# 检查后端服务状态
check_backend_service() {
    log_info "检查后端服务状态..."

    # 检查端口是否被占用
    if ! is_port_in_use "$BACKEND_PORT"; then
        log_error "后端服务端口 $BACKEND_PORT 未被占用"
        return 1
    fi

    # 检查API服务是否可用
    if ! check_api_service_available; then
        log_error "后端API服务不可用"
        return 1
    fi

    log_success "后端服务正常运行"
    return 0
}

# 等待后端服务可用
wait_for_backend_service() {
    local timeout=${1:-60}
    local interval=${2:-5}

    log_info "等待后端服务可用 (超时: ${timeout}秒)..."

    local start_time=$(date +%s)
    local end_time=$((start_time + timeout))

    while (( $(date +%s) < end_time )); do
        if check_backend_service >/dev/null 2>&1; then
            log_success "后端服务已可用"
            return 0
        fi

        sleep "$interval"
        printf "."
    done

    echo ""
    log_error "等待后端服务超时"
    return 1
}

# 运行快速测试（关键API）
run_quick_tests() {
    log_info "运行快速API测试..."
    print_separator

    reset_test_stats

    # 测试最基本的API
    test_get_all_menus
    test_get_all_categories

    # 如果测试通过，尝试认证测试
    if ((TESTS_FAILED == 0)); then
        test_user_login
        test_admin_login
    fi

    print_test_summary
}

# 运行公开API测试
run_public_api_tests() {
    log_info "运行公开API测试..."
    print_separator

    reset_test_stats

    test_get_all_menus
    test_get_recommended_menus
    test_get_hot_sales_menus
    test_get_all_categories

    print_test_summary
}

# 运行认证API测试
run_auth_api_tests() {
    log_info "运行认证API测试..."
    print_separator

    reset_test_stats

    test_user_register
    test_user_login
    test_admin_login

    print_test_summary
}

# 运行用户API测试
run_user_api_tests() {
    log_info "运行用户API测试..."
    print_separator

    reset_test_stats

    # 先登录获取token
    test_user_login

    if [[ -n "$USER_TOKEN" ]]; then
        test_get_user_info
    else
        log_error "用户登录失败，跳过需要认证的测试"
    fi

    print_test_summary
}

# 运行管理员API测试
run_admin_api_tests() {
    log_info "运行管理员API测试..."
    print_separator

    reset_test_stats

    # 先登录获取token
    test_admin_login

    if [[ -n "$ADMIN_TOKEN" ]]; then
        test_admin_get_menus
    else
        log_error "管理员登录失败，跳过管理员API测试"
    fi

    print_test_summary
}

# 运行完整API测试套件
run_full_api_tests() {
    log_info "运行完整API测试套件..."
    print_separator

    reset_test_stats

    # 检查必要命令
    check_required_commands curl

    # 运行所有测试
    run_public_api_tests
    run_auth_api_tests
    run_user_api_tests
    run_admin_api_tests

    # 打印最终摘要
    print_test_summary
    local summary_result=$?

    return $summary_result
}

# 生成综合测试报告
generate_comprehensive_report() {
    local report_file="${1:-${REPORT_DIR:-./reports}/backend-api-test-$(date +%Y%m%d_%H%M%S).json}"

    log_info "生成综合测试报告: $report_file"

    # 收集系统信息
    local system_info=$(cat << EOF
{
  "timestamp": "$(date -Iseconds)",
  "backend_port": $BACKEND_PORT,
  "api_base_url": "$API_BASE",
  "system_info": {
    "hostname": "$(hostname)",
    "os": "$(uname -s)",
    "architecture": "$(uname -m)"
  },
  "test_results": {
    "total": $TESTS_TOTAL,
    "passed": $TESTS_PASSED,
    "failed": $TESTS_FAILED,
    "success_rate": $((TESTS_TOTAL > 0 ? TESTS_PASSED * 100 / TESTS_TOTAL : 0))
  }
}
EOF
)

    ensure_directory "$(dirname "$report_file")"

    # 生成JSON报告
    local report_content="{
  \"test_suite\": \"Order Management System Backend API Tests\",
  \"generated_at\": \"$(date -Iseconds)\",
  \"environment\": $system_info,
  \"tests\": ["

    # 添加测试结果
    for ((i=0; i<${#TEST_RESULTS[@]}; i++)); do
        report_content+="${TEST_RESULTS[i]}"
        if ((i < ${#TEST_RESULTS[@]} - 1)); then
            report_content+=","
        fi
    done

    report_content+="]}"

    echo "$report_content" > "$report_file"
    log_success "综合测试报告已生成: $report_file"
    echo "$report_file"
}

# =============================================================================
# 主函数
# =============================================================================

main() {
    # 解析命令行参数
    local params=$(parse_arguments "$@")
    IFS='|' read -r test_type check_only wait_for_service json_output verbose quiet generate_report <<< "$params"

    # 设置输出模式
    if $quiet; then
        # 静默模式，只输出错误和最终结果
        exec >/dev/null
    fi

    if $verbose; then
        export DEBUG=true
        log_debug "启用详细输出模式"
    fi

    print_title "订餐管理系统 - 后端API测试工具"

    # 检查必要命令
    check_required_commands curl

    # 如果只需要检查服务状态
    if $check_only; then
        if check_backend_service; then
            log_success "后端服务检查通过"
            exit 0
        else
            log_error "后端服务检查失败"
            exit 1
        fi
    fi

    # 如果需要等待服务可用
    if $wait_for_service; then
        if ! wait_for_backend_service; then
            log_error "等待后端服务超时"
            exit 1
        fi
    fi

    # 运行相应的测试
    local test_result=0
    case $test_type in
        "public")
            run_public_api_tests
            test_result=$?
            ;;
        "auth")
            run_auth_api_tests
            test_result=$?
            ;;
        "admin")
            run_admin_api_tests
            test_result=$?
            ;;
        "user")
            run_user_api_tests
            test_result=$?
            ;;
        "quick")
            run_quick_tests
            test_result=$?
            ;;
        "full")
            run_full_api_tests
            test_result=$?
            ;;
        *)
            log_error "未知测试类型: $test_type"
            exit 1
            ;;
    esac

    # 生成报告
    if $generate_report; then
        generate_comprehensive_report
    fi

    # 输出JSON格式结果（如果需要）
    if $json_output; then
        echo "{\"total\":$TESTS_TOTAL,\"passed\":$TESTS_PASSED,\"failed\":$TESTS_FAILED,\"success_rate\":$((TESTS_TOTAL > 0 ? TESTS_PASSED * 100 / TESTS_TOTAL : 0))}"
    fi

    # 根据测试结果退出
    if ((test_result == 0 && TESTS_FAILED == 0)); then
        log_success "所有测试通过"
        exit 0
    else
        log_error "有测试失败"
        exit 1
    fi
}

# =============================================================================
# 脚本入口点
# =============================================================================

# 如果脚本被直接执行，则运行主函数
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi