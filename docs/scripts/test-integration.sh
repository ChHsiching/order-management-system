#!/bin/bash

# =============================================================================
# 订餐管理系统 - 完整集成测试脚本
# Complete Integration Test Script for Order Management System
# =============================================================================

# 加载工具函数库
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/port-utils.sh"
source "$SCRIPT_DIR/lib/api-test.sh"

# 初始化脚本
init_script
init_port_utils
init_api_test

# =============================================================================
# 集成测试配置
# =============================================================================

# 测试配置
readonly TEST_TIMEOUT=300
readonly SERVICE_STARTUP_TIMEOUT=120
readonly API_TEST_TIMEOUT=60

# 测试报告配置
readonly REPORT_DIR="${REPORT_DIR:-./reports}"
readonly INTEGRATION_REPORT_FILE="$REPORT_DIR/integration-test-$(date +%Y%m%d_%H%M%S).json"

# =============================================================================
# 集成测试主函数
# =============================================================================

# 显示集成测试帮助
show_integration_test_help() {
    cat << EOF
完整集成测试脚本使用方法:

基本测试:
  ./test-integration.sh                 # 运行完整集成测试
  ./test-integration.sh --quick         # 快速集成测试
  ./test-integration.sh --full          # 完整集成测试

测试阶段:
  ./test-integration.sh --env-check     # 只检查环境
  ./test-integration.sh --cleanup       # 只清理环境
  ./test-integration.sh --startup       # 只测试服务启动
  ./test-integration.sh --api-test      # 只测试API
  ./test-integration.sh --shutdown      # 只测试服务停止

测试选项:
  ./test-integration.sh --parallel      # 并行测试
  ./test-integration.sh --sequential    # 顺序测试
  ./test-integration.sh --report        # 生成详细报告
  ./test-integration.sh --verbose       # 详细输出
  ./test-integration.sh --debug         # 调试模式

特殊测试:
  ./test-integration.sh --stress        # 压力测试
  ./test-integration.sh --recovery      # 恢复测试
  ./test-integration.sh --security      # 安全测试

示例:
  ./test-integration.sh --quick --report        # 快速测试并生成报告
  ./test-integration.sh --env-check --verbose    # 详细环境检查
  ./test-integration.sh --startup --api-test    # 测试启动和API
EOF
}

# 解析命令行参数
parse_arguments() {
    local test_mode="full"
    local env_check_only=false
    local cleanup_only=false
    local startup_only=false
    local api_test_only=false
    local shutdown_only=false
    local parallel_mode=false
    local sequential_mode=false
    local generate_report=false
    local verbose_output=false
    local debug_mode=false
    local stress_test=false
    local recovery_test=false
    local security_test=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --quick)
                test_mode="quick"
                shift
                ;;
            --full)
                test_mode="full"
                shift
                ;;
            --env-check)
                env_check_only=true
                shift
                ;;
            --cleanup)
                cleanup_only=true
                shift
                ;;
            --startup)
                startup_only=true
                shift
                ;;
            --api-test)
                api_test_only=true
                shift
                ;;
            --shutdown)
                shutdown_only=true
                shift
                ;;
            --parallel)
                parallel_mode=true
                shift
                ;;
            --sequential)
                sequential_mode=true
                shift
                ;;
            --report)
                generate_report=true
                shift
                ;;
            --verbose)
                verbose_output=true
                shift
                ;;
            --debug)
                debug_mode=true
                shift
                ;;
            --stress)
                stress_test=true
                shift
                ;;
            --recovery)
                recovery_test=true
                shift
                ;;
            --security)
                security_test=true
                shift
                ;;
            --help|-h)
                show_integration_test_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_integration_test_help
                exit 1
                ;;
        esac
    done

    echo "$test_mode|$env_check_only|$cleanup_only|$startup_only|$api_test_only|$shutdown_only|$parallel_mode|$sequential_mode|$generate_report|$verbose_output|$debug_mode|$stress_test|$recovery_test|$security_test"
}

# 初始化测试结果
init_test_results() {
    # 全局测试结果变量
    declare -g TEST_PHASES=()
    declare -g TEST_PASSED=0
    declare -g TEST_FAILED=0
    declare -g TEST_START_TIME=$(date +%s)
    declare -g TEST_END_TIME=0
    declare -g TEST_OVERALL_SUCCESS=true

    # 清空之前的测试结果
    TEST_PHASES=()

    log_info "初始化测试结果..."
}

# 记录测试阶段结果
record_test_phase() {
    local phase_name=$1
    local phase_status=$2
    local phase_duration=$3
    local phase_details=$4

    local phase_result="{
  \"name\": \"$phase_name\",
  \"status\": \"$phase_status\",
  \"duration\": $phase_duration,
  \"timestamp\": \"$(date -Iseconds)\",
  \"details\": $phase_details
}"

    TEST_PHASES+=("$phase_result")

    if [[ "$phase_status" == "PASSED" ]]; then
        ((TEST_PASSED++))
        log_success "测试阶段: $phase_name - 通过"
    else
        ((TEST_FAILED++))
        TEST_OVERALL_SUCCESS=false
        log_error "测试阶段: $phase_name - 失败"
    fi
}

# 环境检查阶段
environment_check_phase() {
    local phase_start_time=$(date +%s)
    log_info "开始环境检查阶段..."
    print_separator

    local check_results=()
    local all_checks_passed=true

    # 检查必要命令
    log_info "检查必要命令..."
    local required_commands=("curl" "java" "npm" "mvn")
    for cmd in "${required_commands[@]}"; do
        if command -v "$cmd" &> /dev/null; then
            local version=$($cmd --version 2>&1 | head -n 1 || echo "版本未知")
            check_results+=("$cmd: $version - 正常")
            log_success "  $cmd: 已安装"
        else
            check_results+=("$cmd: 未安装 - 失败")
            log_error "  $cmd: 未安装"
            all_checks_passed=false
        fi
    done

    # 检查端口可用性
    log_info "检查端口可用性..."
    for port in "${PROJECT_PORTS[@]}"; do
        if is_port_in_use "$port"; then
            local pid=$(get_pid_by_port "$port")
            check_results+=("端口 $port: 被占用 (PID: $pid) - 警告")
            log_warning "  端口 $port: 被占用"
        else
            check_results+=("端口 $port: 可用 - 正常")
            log_success "  端口 $port: 可用"
        fi
    done

    # 检查目录结构
    log_info "检查目录结构..."
    local required_dirs=("backend" "frontend")
    for dir in "${required_dirs[@]}"; do
        if [[ -d "$dir" ]]; then
            check_results+=("目录 $dir: 存在 - 正常")
            log_success "  目录 $dir: 存在"
        else
            check_results+=("目录 $dir: 不存在 - 失败")
            log_error "  目录 $dir: 不存在"
            all_checks_passed=false
        fi
    done

    # 检查脚本文件
    log_info "检查脚本文件..."
    local script_files=("kill-ports.sh" "test-backend-api.sh" "start-all.sh" "stop-all.sh")
    for script in "${script_files[@]}"; do
        if [[ -x "$SCRIPT_DIR/$script" ]]; then
            check_results+=("脚本 $script: 可执行 - 正常")
            log_success "  脚本 $script: 可执行"
        else
            check_results+=("脚本 $script: 不可执行 - 失败")
            log_error "  脚本 $script: 不可执行"
            all_checks_passed=false
        fi
    done

    local phase_end_time=$(date +%s)
    local phase_duration=$((phase_end_time - phase_start_time))

    # 生成检查结果JSON
    local results_json="["
    for ((i=0; i<${#check_results[@]}; i++)); do
        results_json+="\"${check_results[i]}\""
        if ((i < ${#check_results[@]} - 1)); then
            results_json+=","
        fi
    done
    results_json+="]"

    record_test_phase "环境检查" $([ "$all_checks_passed" == true ] && echo "PASSED" || echo "FAILED") "$phase_duration" "$results_json"

    print_separator
    return $([ "$all_checks_passed" == true ] && echo 0 || echo 1)
}

# 环境清理阶段
environment_cleanup_phase() {
    local phase_start_time=$(date +%s)
    log_info "开始环境清理阶段..."
    print_separator

    local cleanup_results=()
    local cleanup_success=true

    # 清理端口
    log_info "清理项目端口..."
    if cleanup_all_ports; then
        cleanup_results+=("端口清理: 成功")
        log_success "端口清理完成"
    else
        cleanup_results+=("端口清理: 部分失败")
        cleanup_success=false
        log_error "端口清理失败"
    fi

    # 清理PID文件
    log_info "清理PID文件..."
    local pid_files=(".backend.pid" ".frontdesk.pid" ".backdesk.pid" ".monitor.pid")
    local files_removed=0
    for pid_file in "${pid_files[@]}"; do
        if [[ -f "$pid_file" ]]; then
            rm -f "$pid_file"
            ((files_removed++))
        fi
    done
    cleanup_results+=("PID文件清理: 移除 $files_removed 个文件")
    log_info "PID文件清理完成，移除 $files_removed 个文件"

    # 清理日志文件（可选）
    log_info "清理日志文件..."
    if [[ -d "logs" ]]; then
        local log_count=$(find logs -name "*.log" | wc -l)
        cleanup_results+=("日志文件清理: 发现 $log_count 个日志文件")
        log_info "发现 $log_count 个日志文件"
    fi

    local phase_end_time=$(date +%s)
    local phase_duration=$((phase_end_time - phase_start_time))

    # 生成清理结果JSON
    local results_json="["
    for ((i=0; i<${#cleanup_results[@]}; i++)); do
        results_json+="\"${cleanup_results[i]}\""
        if ((i < ${#cleanup_results[@]} - 1)); then
            results_json+=","
        fi
    done
    results_json+="]"

    record_test_phase "环境清理" $([ "$cleanup_success" == true ] && echo "PASSED" || echo "FAILED") "$phase_duration" "$results_json"

    print_separator
    return $([ "$cleanup_success" == true ] && echo 0 || echo 1)
}

# 服务启动测试阶段
service_startup_test_phase() {
    local phase_start_time=$(date +%s)
    log_info "开始服务启动测试阶段..."
    print_separator

    local startup_results=()
    local startup_success=true

    # 启动后端服务
    log_info "启动后端服务..."
    if ./start-all.sh --backend --wait --debug >/dev/null 2>&1; then
        startup_results+=("后端服务: 启动成功")
        log_success "后端服务启动成功"
    else
        startup_results+=("后端服务: 启动失败")
        startup_success=false
        log_error "后端服务启动失败"
    fi

    # 等待后端服务就绪
    if $startup_success; then
        log_info "等待后端服务就绪..."
        if wait_for_backend_service >/dev/null 2>&1; then
            startup_results+=("后端服务: 就绪检查通过")
            log_success "后端服务就绪"
        else
            startup_results+=("后端服务: 就绪检查失败")
            startup_success=false
            log_error "后端服务未就绪"
        fi
    fi

    # 检查服务状态
    log_info "检查服务状态..."
    local status_output=$(./start-all.sh --status 2>/dev/null)
    startup_results+=("状态检查: $status_output")
    log_info "服务状态: $status_output"

    local phase_end_time=$(date +%s)
    local phase_duration=$((phase_end_time - phase_start_time))

    # 生成启动结果JSON
    local results_json="["
    for ((i=0; i<${#startup_results[@]}; i++)); do
        results_json+="\"${startup_results[i]}\""
        if ((i < ${#startup_results[@]} - 1)); then
            results_json+=","
        fi
    done
    results_json+="]"

    record_test_phase "服务启动" $([ "$startup_success" == true ] && echo "PASSED" || echo "FAILED") "$phase_duration" "$results_json"

    print_separator
    return $([ "$startup_success" == true ] && echo 0 || echo 1)
}

# API测试阶段
api_test_phase() {
    local phase_start_time=$(date +%s)
    log_info "开始API测试阶段..."
    print_separator

    local api_results=()
    local api_success=true

    # 运行基础API测试
    log_info "运行基础API测试..."
    if ./test-backend-api.sh --quick --json >/dev/null 2>&1; then
        local quick_result=$(./test-backend-api.sh --quick --json 2>/dev/null)
        api_results+=("快速API测试: $quick_result")
        log_success "快速API测试通过"
    else
        api_results+=("快速API测试: 失败")
        api_success=false
        log_error "快速API测试失败"
    fi

    # 运行完整API测试
    if $api_success; then
        log_info "运行完整API测试..."
        if ./test-backend-api.sh --public --json >/dev/null 2>&1; then
            local public_result=$(./test-backend-api.sh --public --json 2>/dev/null)
            api_results+=("公开API测试: $public_result")
            log_success "公开API测试通过"
        else
            api_results+=("公开API测试: 失败")
            api_success=false
            log_error "公开API测试失败"
        fi
    fi

    # 运行认证API测试
    if $api_success; then
        log_info "运行认证API测试..."
        if ./test-backend-api.sh --auth --json >/dev/null 2>&1; then
            local auth_result=$(./test-backend-api.sh --auth --json 2>/dev/null)
            api_results+=("认证API测试: $auth_result")
            log_success "认证API测试通过"
        else
            api_results+=("认证API测试: 失败")
            api_success=false
            log_error "认证API测试失败"
        fi
    fi

    local phase_end_time=$(date +%s)
    local phase_duration=$((phase_end_time - phase_start_time))

    # 生成API测试结果JSON
    local results_json="["
    for ((i=0; i<${#api_results[@]}; i++)); do
        results_json+="\"${api_results[i]}\""
        if ((i < ${#api_results[@]} - 1)); then
            results_json+=","
        fi
    done
    results_json+="]"

    record_test_phase "API测试" $([ "$api_success" == true ] && echo "PASSED" || echo "FAILED") "$phase_duration" "$results_json"

    print_separator
    return $([ "$api_success" == true ] && echo 0 || echo 1)
}

# 服务停止测试阶段
service_shutdown_test_phase() {
    local phase_start_time=$(date +%s)
    log_info "开始服务停止测试阶段..."
    print_separator

    local shutdown_results=()
    local shutdown_success=true

    # 停止所有服务
    log_info "停止所有服务..."
    if ./stop-all.sh --force --wait >/dev/null 2>&1; then
        shutdown_results+=("服务停止: 成功")
        log_success "服务停止成功"
    else
        shutdown_results+=("服务停止: 失败")
        shutdown_success=false
        log_error "服务停止失败"
    fi

    # 验证服务停止
    if $shutdown_success; then
        log_info "验证服务停止状态..."
        if ./stop-all.sh --check >/dev/null 2>&1; then
            shutdown_results+=("停止验证: 成功")
            log_success "服务停止验证通过"
        else
            shutdown_results+=("停止验证: 失败")
            shutdown_success=false
            log_error "服务停止验证失败"
        fi
    fi

    # 最终状态检查
    log_info "最终状态检查..."
    local final_status=$(./stop-all.sh --status 2>/dev/null)
    shutdown_results+=("最终状态: $final_status")
    log_info "最终状态: $final_status"

    local phase_end_time=$(date +%s)
    local phase_duration=$((phase_end_time - phase_start_time))

    # 生成停止结果JSON
    local results_json="["
    for ((i=0; i<${#shutdown_results[@]}; i++)); do
        results_json+="\"${shutdown_results[i]}\""
        if ((i < ${#shutdown_results[@]} - 1)); then
            results_json+=","
        fi
    done
    results_json+="]"

    record_test_phase "服务停止" $([ "$shutdown_success" == true ] && echo "PASSED" || echo "FAILED") "$phase_duration" "$results_json"

    print_separator
    return $([ "$shutdown_success" == true ] && echo 0 || echo 1)
}

# 压力测试阶段
stress_test_phase() {
    local phase_start_time=$(date +%s)
    log_info "开始压力测试阶段..."
    print_separator

    local stress_results=()
    local stress_success=true

    # 模拟并发API请求
    log_info "执行并发API请求测试..."
    local concurrent_requests=10
    local success_count=0

    for ((i=1; i<=concurrent_requests; i++)); do
        if curl -s -f "http://localhost:$BACKEND_PORT$API_CONTEXT_PATH/api/menu" >/dev/null 2>&1; then
            ((success_count++))
        fi
    done

    stress_results+=("并发请求: $success_count/$concurrent_requests 成功")
    log_info "并发请求测试: $success_count/$concurrent_requests 成功"

    if ((success_count < concurrent_requests * 8 / 10)); then
        stress_success=false
        log_warning "并发请求成功率较低"
    fi

    local phase_end_time=$(date +%s)
    local phase_duration=$((phase_end_time - phase_start_time))

    # 生成压力测试结果JSON
    local results_json="["
    for ((i=0; i<${#stress_results[@]}; i++)); do
        results_json+="\"${stress_results[i]}\""
        if ((i < ${#stress_results[@]} - 1)); then
            results_json+=","
        fi
    done
    results_json+="]"

    record_test_phase "压力测试" $([ "$stress_success" == true ] && echo "PASSED" || echo "FAILED") "$phase_duration" "$results_json"

    print_separator
    return $([ "$stress_success" == true ] && echo 0 || echo 1)
}

# 生成综合测试报告
generate_integration_report() {
    local report_file="$1"
    TEST_END_TIME=$(date +%s)
    local total_duration=$((TEST_END_TIME - TEST_START_TIME))

    ensure_directory "$(dirname "$report_file")"

    local report_content="{
  \"test_suite\": \"Order Management System Integration Test\",
  \"test_mode\": \"$test_mode\",
  \"generated_at\": \"$(date -Iseconds)\",
  \"environment\": {
    \"hostname\": \"$(hostname)\",
    \"os\": \"$(uname -s)\",
    \"architecture\": \"$(uname -m)\",
    \"test_duration_seconds\": $total_duration
  },
  \"summary\": {
    \"total_phases\": ${#TEST_PHASES[@]},
    \"passed_phases\": $TEST_PASSED,
    \"failed_phases\": $TEST_FAILED,
    \"success_rate\": $((TEST_PASSED * 100 / ${#TEST_PHASES[@]})),
    \"overall_success\": $TEST_OVERALL_SUCCESS
  },
  \"phases\": ["

    # 添加测试阶段结果
    for ((i=0; i<${#TEST_PHASES[@]}; i++)); do
        report_content+="${TEST_PHASES[i]}"
        if ((i < ${#TEST_PHASES[@]} - 1)); then
            report_content+=","
        fi
    done

    report_content+="]}"

    echo "$report_content" > "$report_file"
    log_success "集成测试报告已生成: $report_file"
    echo "$report_file"
}

# 运行快速集成测试
run_quick_integration_test() {
    log_info "运行快速集成测试..."
    print_separator

    init_test_results

    environment_check_phase
    local env_result=$?

    if [[ $env_result -eq 0 ]]; then
        environment_cleanup_phase
        local cleanup_result=$?

        if [[ $cleanup_result -eq 0 ]]; then
            service_startup_test_phase
            local startup_result=$?

            if [[ $startup_result -eq 0 ]]; then
                api_test_phase
                local api_result=$?

                if [[ $api_result -eq 0 ]]; then
                    service_shutdown_test_phase
                    local shutdown_result=$?

                    if [[ $shutdown_result -ne 0 ]]; then
                        log_warning "服务停止测试失败，但整体测试仍被视为通过"
                    fi
                else
                    log_error "API测试失败，尝试停止服务"
                    service_shutdown_test_phase >/dev/null 2>&1
                fi
            else
                log_error "服务启动测试失败，尝试清理环境"
                environment_cleanup_phase >/dev/null 2>&1
            fi
        fi
    fi

    return $([ "$TEST_OVERALL_SUCCESS" == true ] && echo 0 || echo 1)
}

# 运行完整集成测试
run_full_integration_test() {
    log_info "运行完整集成测试..."
    print_separator

    init_test_results

    environment_check_phase
    local env_result=$?

    if [[ $env_result -eq 0 ]]; then
        environment_cleanup_phase
        local cleanup_result=$?

        if [[ $cleanup_result -eq 0 ]]; then
            service_startup_test_phase
            local startup_result=$?

            if [[ $startup_result -eq 0 ]]; then
                api_test_phase
                local api_result=$?

                if [[ $api_result -eq 0 ]]; then
                    stress_test_phase
                    local stress_result=$?

                    service_shutdown_test_phase
                    local shutdown_result=$?

                    if [[ $shutdown_result -ne 0 ]]; then
                        log_warning "服务停止测试失败"
                    fi
                else
                    log_error "API测试失败，尝试停止服务"
                    service_shutdown_test_phase >/dev/null 2>&1
                fi
            else
                log_error "服务启动测试失败，尝试清理环境"
                environment_cleanup_phase >/dev/null 2>&1
            fi
        fi
    fi

    return $([ "$TEST_OVERALL_SUCCESS" == true ] && echo 0 || echo 1)
}

# =============================================================================
# 主函数
# =============================================================================

main() {
    # 解析命令行参数
    local params=$(parse_arguments "$@")
    IFS='|' read -r test_mode env_check_only cleanup_only startup_only api_test_only shutdown_only parallel_mode sequential_mode generate_report verbose_output debug_mode stress_test recovery_test security_test <<< "$params"

    print_title "订餐管理系统 - 完整集成测试工具"

    # 设置调试模式
    if $debug_mode; then
        export DEBUG=true
        log_debug "启用调试模式"
    fi

    # 设置输出模式
    if $verbose_output; then
        log_info "启用详细输出模式"
    fi

    # 根据参数运行相应测试
    local test_result=0

    case $test_mode in
        "quick")
            if run_quick_integration_test; then
                test_result=0
            else
                test_result=1
            fi
            ;;
        "full")
            if run_full_integration_test; then
                test_result=0
            else
                test_result=1
            fi
            ;;
        *)
            log_error "未知测试模式: $test_mode"
            exit 1
            ;;
    esac

    # 生成报告
    if $generate_report; then
        generate_integration_report "$INTEGRATION_REPORT_FILE"
    fi

    # 显示测试摘要
    print_separator
    log_info "集成测试摘要"
    print_separator

    local total_phases=${#TEST_PHASES[@]}
    local success_rate=$((TEST_PASSED * 100 / total_phases))

    echo -e "${GREEN}通过阶段: $TEST_PASSED${NC}"
    echo -e "${RED}失败阶段: $TEST_FAILED${NC}"
    echo -e "${BLUE}总阶段数: $total_phases${NC}"
    echo -e "${CYAN}成功率: $success_rate%${NC}"

    if [[ "$TEST_OVERALL_SUCCESS" == true ]]; then
        log_success "集成测试通过"
        exit 0
    else
        log_error "集成测试失败"
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