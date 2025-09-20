#!/bin/bash

# =============================================================================
# 订餐管理系统 - 端口清理脚本
# Port Cleanup Script for Order Management System
# =============================================================================

# 加载端口工具函数库
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/port-utils.sh"

# 初始化脚本
init_script
init_port_utils

# =============================================================================
# 端口清理主函数
# =============================================================================

# 显示端口清理帮助
show_port_cleanup_help() {
    cat << EOF
端口清理脚本使用方法:

基本清理:
  ./kill-ports.sh                    # 清理所有项目端口
  ./kill-ports.sh --port 8081       # 清理指定端口
  ./kill-ports.sh --backend          # 只清理后端端口
  ./kill-ports.sh --frontend         # 清理前端端口

交互模式:
  ./kill-ports.sh --interactive      # 交互式清理端口
  ./kill-ports.sh --force            # 强制清理所有端口

信息查询:
  ./kill-ports.sh --status           # 显示所有端口状态
  ./kill-ports.sh --monitor          # 监控端口状态变化
  ./kill-ports.sh --report           # 生成端口状态报告

示例:
  ./kill-ports.sh --port 8081 --force    # 强制清理8081端口
  ./kill-ports.sh --interactive         # 交互式清理所有端口
  ./kill-ports.sh --status              # 查看当前端口状态
EOF
}

# 解析命令行参数
parse_arguments() {
    local port=""
    local service=""
    local interactive=false
    local force=false
    local status_only=false
    local monitor=false
    local report=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --port)
                port="$2"
                shift 2
                ;;
            --backend)
                service="backend"
                shift
                ;;
            --frontend)
                service="frontend"
                shift
                ;;
            --interactive|-i)
                interactive=true
                shift
                ;;
            --force|-f)
                force=true
                shift
                ;;
            --status|-s)
                status_only=true
                shift
                ;;
            --monitor|-m)
                monitor=true
                shift
                ;;
            --report|-r)
                report=true
                shift
                ;;
            --help|-h)
                show_port_cleanup_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_port_cleanup_help
                exit 1
                ;;
        esac
    done

    echo "$port|$service|$interactive|$force|$status_only|$monitor|$report"
}

# 清理指定端口
cleanup_specific_port() {
    local port=$1
    local force=$2

    log_info "清理端口 $port..."

    if ! is_port_in_use "$port"; then
        log_info "端口 $port 未被占用"
        return 0
    fi

    # 显示端口进程信息
    get_port_process_info "$port"
    echo ""

    if $force; then
        kill_port_process "$port" true "服务"
    else
        kill_port_process "$port" false "服务"
    fi

    local result=$?
    if [[ $result -eq 0 ]]; then
        log_success "端口 $port 清理完成"
    else
        log_error "端口 $port 清理失败"
    fi

    return $result
}

# 清理服务相关端口
cleanup_service_ports() {
    local service=$1
    local force=$2

    case $service in
        "backend")
            log_info "清理后端服务端口..."
            cleanup_specific_port "$BACKEND_PORT" "$force"
            ;;
        "frontend")
            log_info "清理前端服务端口..."
            cleanup_specific_port "$FRONTDESK_PORT" "$force"
            cleanup_specific_port "$BACKDESK_PORT" "$force"
            ;;
        *)
            log_error "未知服务: $service"
            return 1
            ;;
    esac
}

# 清理所有项目端口
cleanup_all_ports() {
    local force=$1

    log_info "开始清理所有项目端口..."
    print_separator

    local success_count=0
    local fail_count=0

    for port in "${PROJECT_PORTS[@]}"; do
        local service_name=""
        case $port in
            "$BACKEND_PORT")    service_name="后端" ;;
            "$FRONTDESK_PORT")  service_name="前台" ;;
            "$BACKDESK_PORT")   service_name="后台" ;;
            *)                  service_name="未知" ;;
        esac

        if is_port_in_use "$port"; then
            log_info "清理 $service_name 端口 $port..."
            if kill_port_process "$port" "$force" "$service_name"; then
                ((success_count++))
            else
                # 尝试强制终止
                if kill_port_process "$port" true "$service_name"; then
                    ((success_count++))
                else
                    ((fail_count++))
                fi
            fi
        else
            log_info "$service_name 端口 $port 未被占用"
            ((success_count++))
        fi
    done

    print_separator
    log_success "端口清理完成: 成功 $success_count 个, 失败 $fail_count 个"

    if ((fail_count > 0)); then
        return 1
    else
        return 0
    fi
}

# 监控端口状态
monitor_ports_status() {
    local duration=${1:-60}  # 默认监控60秒
    local interval=${2:-5}   # 默认每5秒检查一次

    log_info "开始监控项目端口状态 (持续 ${duration}秒，每 ${interval}秒检查一次)..."
    print_separator

    # 并行监控所有端口
    for port in "${PROJECT_PORTS[@]}"; do
        (
            local service_name=""
            case $port in
                "$BACKEND_PORT")    service_name="后端" ;;
                "$FRONTDESK_PORT")  service_name="前台" ;;
                "$BACKDESK_PORT")   service_name="后台" ;;
                *)                  service_name="未知" ;;
            esac

            monitor_port "$port" "$duration" "$interval" | while read -r line; do
                printf "[%s] %s\n" "$service_name" "$line"
            done
        ) &
    done

    # 等待所有后台进程完成
    wait
    print_separator
    log_success "端口监控完成"
}

# =============================================================================
# 主函数
# =============================================================================

main() {
    # 解析命令行参数
    local params=$(parse_arguments "$@")
    IFS='|' read -r port service interactive force status_only monitor report <<< "$params"

    print_title "订餐管理系统 - 端口清理工具"

    # 根据参数执行相应操作
    if $status_only; then
        get_all_ports_status
    elif $monitor; then
        monitor_ports_status
    elif $report; then
        generate_port_report
    elif $interactive; then
        interactive_port_cleanup
    elif [[ -n "$port" ]]; then
        cleanup_specific_port "$port" "$force"
    elif [[ -n "$service" ]]; then
        cleanup_service_ports "$service" "$force"
    else
        # 默认清理所有端口
        if cleanup_all_ports "$force"; then
            log_success "所有端口清理完成"
            exit 0
        else
            log_error "部分端口清理失败"
            exit 1
        fi
    fi
}

# =============================================================================
# 脚本入口点
# =============================================================================

# 如果脚本被直接执行，则运行主函数
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi