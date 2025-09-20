#!/bin/bash

# =============================================================================
# 订餐管理系统 - 项目停止脚本
# Project Stop Script for Order Management System
# =============================================================================

# 加载工具函数库
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/port-utils.sh"

# 初始化脚本
init_script
init_port_utils

# =============================================================================
# 项目停止配置
# =============================================================================

# 进程PID文件 (使用绝对路径)
readonly BACKEND_PID_FILE="$PID_DIR/.backend.pid"
readonly FRONTDESK_PID_FILE="$PID_DIR/.frontdesk.pid"
readonly BACKDESK_PID_FILE="$PID_DIR/.backdesk.pid"
readonly MONITOR_PID_FILE="$PID_DIR/.monitor.pid"

# 停止超时时间（秒）
readonly STOP_TIMEOUT=30
readonly STOP_CHECK_INTERVAL=2

# =============================================================================
# 服务停止函数
# =============================================================================

# 显示停止帮助
show_stop_help() {
    cat << EOF
项目停止脚本使用方法:

基本停止:
  ./stop-all.sh                     # 停止所有服务
  ./stop-all.sh --backend           # 停止后端服务
  ./stop-all.sh --frontend          # 停止前端服务
  ./stop-all.sh --frontdesk         # 停止前台服务
  ./stop-all.sh --backdesk          # 停止后台服务
  ./stop-all.sh --monitor           # 停止监控服务

停止选项:
  ./stop-all.sh --force             # 强制停止服务
  ./stop-all.sh --clean             # 停止服务并清理文件
  ./stop-all.sh --wait              # 等待服务完全停止
  ./stop-all.sh --check             # 检查服务状态

信息查询:
  ./stop-all.sh --status            # 显示服务状态
  ./stop-all.sh --help              # 显示帮助信息

示例:
  ./stop-all.sh --force --clean     # 强制停止所有服务并清理文件
  ./stop-all.sh --wait              # 等待所有服务完全停止
  ./stop-all.sh --backend --force   # 强制停止后端服务
EOF
}

# 解析命令行参数
parse_arguments() {
    local stop_backend=false
    local stop_frontdesk=false
    local stop_backdesk=false
    local stop_monitor=false
    local force_stop=false
    local clean_files=false
    local wait_for_stop=false
    local check_status=false
    local show_status=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --backend)
                stop_backend=true
                shift
                ;;
            --frontend)
                stop_frontdesk=true
                stop_backdesk=true
                shift
                ;;
            --frontdesk)
                stop_frontdesk=true
                shift
                ;;
            --backdesk)
                stop_backdesk=true
                shift
                ;;
            --monitor)
                stop_monitor=true
                shift
                ;;
            --force)
                force_stop=true
                shift
                ;;
            --clean)
                clean_files=true
                shift
                ;;
            --wait)
                wait_for_stop=true
                shift
                ;;
            --check)
                check_status=true
                shift
                ;;
            --status)
                show_status=true
                shift
                ;;
            --help|-h)
                show_stop_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_stop_help
                exit 1
                ;;
        esac
    done

    # 如果没有指定具体服务，默认停止所有服务
    if ! $stop_backend && ! $stop_frontdesk && ! $stop_backdesk && ! $stop_monitor; then
        stop_backend=true
        stop_frontdesk=true
        stop_backdesk=true
        stop_monitor=true
    fi

    echo "$stop_backend|$stop_frontdesk|$stop_backdesk|$stop_monitor|$force_stop|$clean_files|$wait_for_stop|$check_status|$show_status"
}

# 读取进程PID
read_pid() {
    local pid_file=$1
    if [[ -f "$pid_file" ]]; then
        cat "$pid_file"
    else
        echo ""
    fi
}

# 检查进程是否运行
is_service_running() {
    local pid_file=$1
    local service_name=$2

    local pid=$(read_pid "$pid_file")
    if [[ -z "$pid" ]]; then
        return 1
    fi

    if is_process_running "$pid"; then
        return 0
    else
        log_warning "$service_name 进程 $pid 不存在，清理PID文件"
        rm -f "$pid_file"
        return 1
    fi
}

# 停止指定服务
stop_service() {
    local pid_file=$1
    local service_name=$2
    local force_stop=$3

    local pid=$(read_pid "$pid_file")
    if [[ -z "$pid" ]]; then
        log_info "$service_name 未运行"
        return 0
    fi

    if ! is_process_running "$pid"; then
        log_info "$service_name 进程 $pid 不存在，清理PID文件"
        rm -f "$pid_file"
        return 0
    fi

    log_info "停止 $service_name (PID: $pid)..."

    if $force_stop; then
        log_info "强制停止 $service_name..."
        kill -9 "$pid" 2>/dev/null || {
            log_warning "强制停止 $service_name 失败"
            return 1
        }
    else
        log_info "优雅停止 $service_name..."
        kill "$pid" 2>/dev/null || {
            log_warning "优雅停止失败，尝试强制停止"
            kill -9 "$pid" 2>/dev/null || {
                log_error "强制停止 $service_name 失败"
                return 1
            }
        }
    fi

    # 等待进程停止
    local timeout=10
    local start_time=$(date +%s)
    local end_time=$((start_time + timeout))

    while is_process_running "$pid"; do
        local current_time=$(date +%s)
        if ((current_time > end_time)); then
            log_error "$service_name 停止超时"
            return 1
        fi
        sleep 1
        printf "."
    done

    echo ""
    log_success "$service_name 已停止"
    rm -f "$pid_file"
    return 0
}

# 停止后端服务
stop_backend_service() {
    local force_stop=$1

    log_info "停止后端服务..."

    # 检查后端目录是否存在
    if [[ ! -d "$BACKEND_DIR" ]]; then
        log_warning "后端目录不存在: $BACKEND_DIR"
    fi

    if stop_service "$BACKEND_PID_FILE" "后端服务" "$force_stop"; then
        # 检查端口是否释放
        if is_port_in_use "$BACKEND_PORT"; then
            log_warning "后端端口 $BACKEND_PORT 仍被占用，尝试强制清理"
            kill_port_process "$BACKEND_PORT" "$force_stop" "后端服务"
        fi
        return 0
    else
        return 1
    fi
}

# 停止前台服务
stop_frontdesk_service() {
    local force_stop=$1

    log_info "停止前台服务..."

    # 检查前台目录是否存在
    if [[ ! -d "$FRONTDESK_DIR" ]]; then
        log_warning "前台目录不存在: $FRONTDESK_DIR"
    fi

    if stop_service "$FRONTDESK_PID_FILE" "前台服务" "$force_stop"; then
        # 检查端口是否释放
        if is_port_in_use "$FRONTDESK_PORT"; then
            log_warning "前台端口 $FRONTDESK_PORT 仍被占用，尝试强制清理"
            kill_port_process "$FRONTDESK_PORT" "$force_stop" "前台服务"
        fi
        return 0
    else
        return 1
    fi
}

# 停止后台服务
stop_backdesk_service() {
    local force_stop=$1

    log_info "停止后台服务..."

    # 检查后台目录是否存在
    if [[ ! -d "$BACKDESK_DIR" ]]; then
        log_warning "后台目录不存在: $BACKDESK_DIR"
    fi

    if stop_service "$BACKDESK_PID_FILE" "后台服务" "$force_stop"; then
        # 检查端口是否释放
        if is_port_in_use "$BACKDESK_PORT"; then
            log_warning "后台端口 $BACKDESK_PORT 仍被占用，尝试强制清理"
            kill_port_process "$BACKDESK_PORT" "$force_stop" "后台服务"
        fi
        return 0
    else
        return 1
    fi
}

# 停止监控服务
stop_monitor_service() {
    local force_stop=$1

    log_info "停止监控服务..."
    if stop_service "$MONITOR_PID_FILE" "监控服务" "$force_stop"; then
        return 0
    else
        return 1
    fi
}

# 清理临时文件
cleanup_temp_files() {
    log_info "清理临时文件..."

    # 清理PID文件
    rm -f "$BACKEND_PID_FILE" "$FRONTDESK_PID_FILE" "$BACKDESK_PID_FILE" "$MONITOR_PID_FILE"

    # 清理日志文件（可选）
    if $clean_files; then
        log_info "清理日志文件..."
        rm -f logs/*.log 2>/dev/null || true
    fi

    log_success "临时文件清理完成"
}

# 显示服务状态
show_services_status() {
    log_info "服务状态检查..."
    print_separator

    # 检查后端服务
    if [[ -f "$BACKEND_PID_FILE" ]]; then
        local backend_pid=$(cat "$BACKEND_PID_FILE")
        if is_process_running "$backend_pid"; then
            local process_info=$(ps -p "$backend_pid" -o comm= 2>/dev/null || echo "unknown")
            print_status "SUCCESS" "后端服务运行中 (PID: $backend_pid, 进程: $process_info)"
        else
            print_status "ERROR" "后端服务已停止 (PID文件存在但进程不存在)"
        fi
    else
        if is_port_in_use "$BACKEND_PORT"; then
            print_status "WARNING" "后端端口被占用，但无PID文件"
        else
            print_status "INFO" "后端服务未运行"
        fi
    fi

    # 检查前台服务
    if [[ -f "$FRONTDESK_PID_FILE" ]]; then
        local frontdesk_pid=$(cat "$FRONTDESK_PID_FILE")
        if is_process_running "$frontdesk_pid"; then
            local process_info=$(ps -p "$frontdesk_pid" -o comm= 2>/dev/null || echo "unknown")
            print_status "SUCCESS" "前台服务运行中 (PID: $frontdesk_pid, 进程: $process_info)"
        else
            print_status "ERROR" "前台服务已停止 (PID文件存在但进程不存在)"
        fi
    else
        if is_port_in_use "$FRONTDESK_PORT"; then
            print_status "WARNING" "前台端口被占用，但无PID文件"
        else
            print_status "INFO" "前台服务未运行"
        fi
    fi

    # 检查后台服务
    if [[ -f "$BACKDESK_PID_FILE" ]]; then
        local backdesk_pid=$(cat "$BACKDESK_PID_FILE")
        if is_process_running "$backdesk_pid"; then
            local process_info=$(ps -p "$backdesk_pid" -o comm= 2>/dev/null || echo "unknown")
            print_status "SUCCESS" "后台服务运行中 (PID: $backdesk_pid, 进程: $process_info)"
        else
            print_status "ERROR" "后台服务已停止 (PID文件存在但进程不存在)"
        fi
    else
        if is_port_in_use "$BACKDESK_PORT"; then
            print_status "WARNING" "后台端口被占用，但无PID文件"
        else
            print_status "INFO" "后台服务未运行"
        fi
    fi

    # 检查监控服务
    if [[ -f "$MONITOR_PID_FILE" ]]; then
        local monitor_pid=$(cat "$MONITOR_PID_FILE")
        if is_process_running "$monitor_pid"; then
            local process_info=$(ps -p "$monitor_pid" -o comm= 2>/dev/null || echo "unknown")
            print_status "SUCCESS" "监控服务运行中 (PID: $monitor_pid, 进程: $process_info)"
        else
            print_status "ERROR" "监控服务已停止 (PID文件存在但进程不存在)"
        fi
    else
        print_status "INFO" "监控服务未运行"
    fi

    print_separator
}

# 等待服务完全停止
wait_for_services_stop() {
    log_info "等待服务完全停止..."
    print_separator

    local timeout=$STOP_TIMEOUT
    local start_time=$(date +%s)
    local end_time=$((start_time + timeout))

    local all_stopped=false

    while (( $(date +%s) < end_time )); do
        all_stopped=true

        # 检查后端服务
        if is_service_running "$BACKEND_PID_FILE" "后端服务"; then
            all_stopped=false
        fi

        # 检查前台服务
        if is_service_running "$FRONTDESK_PID_FILE" "前台服务"; then
            all_stopped=false
        fi

        # 检查后台服务
        if is_service_running "$BACKDESK_PID_FILE" "后台服务"; then
            all_stopped=false
        fi

        # 检查监控服务
        if is_service_running "$MONITOR_PID_FILE" "监控服务"; then
            all_stopped=false
        fi

        if $all_stopped; then
            log_success "所有服务已停止"
            return 0
        fi

        sleep $STOP_CHECK_INTERVAL
        printf "."
    done

    echo ""
    log_warning "等待服务停止超时"
    return 1
}

# 检查服务停止状态
check_services_stopped() {
    log_info "检查服务停止状态..."
    print_separator

    local all_stopped=true
    local services_with_issues=()

    # 检查后端服务
    if is_service_running "$BACKEND_PID_FILE" "后端服务"; then
        all_stopped=false
        services_with_issues+=("后端服务")
    fi

    # 检查前台服务
    if is_service_running "$FRONTDESK_PID_FILE" "前台服务"; then
        all_stopped=false
        services_with_issues+=("前台服务")
    fi

    # 检查后台服务
    if is_service_running "$BACKDESK_PID_FILE" "后台服务"; then
        all_stopped=false
        services_with_issues+=("后台服务")
    fi

    # 检查监控服务
    if is_service_running "$MONITOR_PID_FILE" "监控服务"; then
        all_stopped=false
        services_with_issues+=("监控服务")
    fi

    if $all_stopped; then
        log_success "所有服务已正常停止"
        return 0
    else
        log_error "以下服务未正常停止: ${services_with_issues[*]}"
        return 1
    fi
}

# =============================================================================
# 主函数
# =============================================================================

main() {
    # 解析命令行参数
    local params=$(parse_arguments "$@")
    IFS='|' read -r stop_backend stop_frontdesk stop_backdesk stop_monitor force_stop clean_files wait_for_stop check_status show_status <<< "$params"

    print_title "订餐管理系统 - 项目停止工具"

    # 显示服务状态
    if $show_status; then
        show_services_status
        exit 0
    fi

    # 检查服务状态
    if $check_status; then
        if check_services_stopped; then
            exit 0
        else
            exit 1
        fi
    fi

    # 停止服务
    local all_stopped=true
    local stop_results=()

    if $stop_monitor; then
        if stop_monitor_service "$force_stop"; then
            stop_results+=("监控服务: 成功")
        else
            stop_results+=("监控服务: 失败")
            all_stopped=false
        fi
    fi

    if $stop_backend; then
        if stop_backend_service "$force_stop"; then
            stop_results+=("后端服务: 成功")
        else
            stop_results+=("后端服务: 失败")
            all_stopped=false
        fi
    fi

    if $stop_frontdesk; then
        if stop_frontdesk_service "$force_stop"; then
            stop_results+=("前台服务: 成功")
        else
            stop_results+=("前台服务: 失败")
            all_stopped=false
        fi
    fi

    if $stop_backdesk; then
        if stop_backdesk_service "$force_stop"; then
            stop_results+=("后台服务: 成功")
        else
            stop_results+=("后台服务: 失败")
            all_stopped=false
        fi
    fi

    # 等待服务停止
    if $wait_for_stop && ! $all_stopped; then
        if wait_for_services_stop; then
            all_stopped=true
        fi
    fi

    # 清理临时文件
    if $clean_files; then
        cleanup_temp_files
    fi

    # 显示停止结果
    print_separator
    log_info "服务停止结果:"
    for result in "${stop_results[@]}"; do
        if [[ "$result" == *"成功"* ]]; then
            log_success "  $result"
        else
            log_error "  $result"
        fi
    done
    print_separator

    # 显示最终状态
    show_services_status

    if $all_stopped; then
        log_success "项目停止完成"
        exit 0
    else
        log_error "部分服务停止失败"
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