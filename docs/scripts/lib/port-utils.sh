#!/bin/bash

# =============================================================================
# 订餐管理系统 - 端口工具函数库
# Port Utility Functions Library for Order Management System
# =============================================================================

# 加载通用工具函数库
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

# =============================================================================
# 端口配置
# =============================================================================

# 使用common.sh中定义的端口配置
# 从环境变量获取端口配置或使用默认值
BACKEND_PORT=${BACKEND_PORT:-8080}
FRONTDESK_PORT=${FRONTDESK_PORT:-5173}
BACKDESK_PORT=${BACKDESK_PORT:-5174}

# 项目使用的所有端口
readonly PROJECT_PORTS=("$BACKEND_PORT" "$FRONTDESK_PORT" "$BACKDESK_PORT")

# =============================================================================
# 端口查询函数
# =============================================================================

# 获取占用指定端口的进程ID
get_pid_by_port() {
    local port=$1
    if ! is_port_in_use "$port"; then
        echo ""
        return 1
    fi

    # 使用多种方法尝试获取PID
    local pid=""

    # 方法1: 使用 lsof
    if command -v lsof &> /dev/null; then
        pid=$(lsof -ti :"$port" 2>/dev/null | head -1)
    fi

    # 方法2: 使用 ss (如果 lsof 不可用)
    if [[ -z "$pid" ]] && command -v ss &> /dev/null; then
        pid=$(ss -ltnp | awk -v port=":$port$" '$4 ~ port {print $7}' | cut -d, -f2 | cut -d= -f2)
    fi

    # 方法3: 使用 netstat (如果 ss 也不可用)
    if [[ -z "$pid" ]] && command -v netstat &> /dev/null; then
        pid=$(netstat -ltnp 2>/dev/null | awk -v port=":$port$" '$4 ~ port {print $7}' | cut -d/ -f1)
    fi

    echo "$pid"
}

# 获取端口进程的详细信息
get_port_process_info() {
    local port=$1
    local pid=$(get_pid_by_port "$port")

    if [[ -z "$pid" ]]; then
        log_info "端口 $port 未被占用"
        return 1
    fi

    echo "端口 $port 进程信息:"
    echo "  PID: $pid"

    # 获取进程详情
    if command -v ps &> /dev/null; then
        local process_info=$(ps -p "$pid" -o pid,ppid,user,%cpu,%mem,cmd --no-headers 2>/dev/null)
        if [[ -n "$process_info" ]]; then
            echo "  进程详情: $process_info"
        fi
    fi

    # 获取进程启动时间
    if command -v lsof &> /dev/null; then
        local start_time=$(lsof -i :"$port" -F l 2>/dev/null | tail -1 | sed 's/^l//')
        if [[ -n "$start_time" ]]; then
            echo "  启动时间: $start_time"
        fi
    fi
}

# 获取所有项目端口的占用情况
get_all_ports_status() {
    log_info "检查项目端口占用情况..."
    print_separator

    for port in "${PROJECT_PORTS[@]}"; do
        if is_port_in_use "$port"; then
            local pid=$(get_pid_by_port "$port")
            local process_name=$(ps -p "$pid" -o comm= 2>/dev/null || echo "unknown")
            print_status "WARNING" "端口 $port 被占用 (PID: $pid, 进程: $process_name)"
        else
            print_status "INFO" "端口 $port 可用"
        fi
    done
    print_separator
}

# =============================================================================
# 端口检查函数
# =============================================================================

# 检查端口是否可用
check_port_availability() {
    local port=$1
    local service_name=${2:-"服务"}

    if is_port_in_use "$port"; then
        local pid=$(get_pid_by_port "$port")
        local process_name=$(ps -p "$pid" -o comm= 2>/dev/null || echo "unknown")
        log_error "$service_name 端口 $port 被进程 $process_name (PID: $pid) 占用"
        return 1
    else
        log_success "$service_name 端口 $port 可用"
        return 0
    fi
}

# 检查所有项目端口是否可用
check_all_ports_availability() {
    log_info "检查所有项目端口可用性..."
    local all_available=true

    for port in "${PROJECT_PORTS[@]}"; do
        if ! check_port_availability "$port"; then
            all_available=false
        fi
    done

    if $all_available; then
        log_success "所有项目端口都可用"
        return 0
    else
        log_error "部分端口被占用，请先清理"
        return 1
    fi
}

# 等待端口释放
wait_for_port_release() {
    local port=$1
    local timeout=${2:-30}
    local service_name=${3:-"服务"}

    log_info "等待 $service_name 端口 $port 释放..."

    local start_time=$(date +%s)
    while is_port_in_use "$port"; do
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))

        if ((elapsed > timeout)); then
            log_error "等待 $service_name 端口 $port 释放超时 ($timeout 秒)"
            return 1
        fi

        sleep 1
        printf "."
    done

    echo ""
    log_success "$service_name 端口 $port 已释放"
    return 0
}

# =============================================================================
# 端口清理函数
# =============================================================================

# 终止占用端口的进程
kill_port_process() {
    local port=$1
    local force=${2:-false}
    local service_name=${3:-"服务"}

    if ! is_port_in_use "$port"; then
        log_info "$service_name 端口 $port 未被占用"
        return 0
    fi

    local pid=$(get_pid_by_port "$port")
    if [[ -z "$pid" ]]; then
        log_warning "无法获取端口 $port 的进程ID"
        return 1
    fi

    # 获取进程信息用于日志
    local process_info=$(ps -p "$pid" -o comm= 2>/dev/null || echo "unknown")

    if $force; then
        log_warning "强制终止 $service_name 端口 $port 的进程 $process_info (PID: $pid)"
        kill -9 "$pid" 2>/dev/null || {
            log_error "强制终止进程失败 (PID: $pid)"
            return 1
        }
    else
        log_info "优雅终止 $service_name 端口 $port 的进程 $process_info (PID: $pid)"
        kill "$pid" 2>/dev/null || {
            log_warning "优雅终止失败，尝试强制终止"
            kill_port_process "$port" true "$service_name"
            return $?
        }
    fi

    # 等待进程完全退出
    if wait_for_process "$pid" 5; then
        log_success "成功终止 $service_name 端口 $port 的进程 (PID: $pid)"
        return 0
    else
        log_error "终止进程超时 (PID: $pid)"
        return 1
    fi
}

# 清理所有项目端口
cleanup_all_ports() {
    log_info "开始清理所有项目端口..."
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
            if kill_port_process "$port" false "$service_name"; then
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

# 交互式清理端口
interactive_port_cleanup() {
    log_info "交互式端口清理模式"
    get_all_ports_status

    for port in "${PROJECT_PORTS[@]}"; do
        if is_port_in_use "$port"; then
            get_port_process_info "$port"
            echo ""

            while true; do
                read -p "是否清理端口 $port? (y/n/a/s): " choice
                case $choice in
                    [Yy]*)
                        kill_port_process "$port" false "服务"
                        break
                        ;;
                    [Nn]*)
                        log_info "跳过端口 $port"
                        break
                        ;;
                    [Aa]*)
                        log_info "选择强制终止"
                        kill_port_process "$port" true "服务"
                        break
                        ;;
                    [Ss]*)
                        log_info "停止交互式清理"
                        return 0
                        ;;
                    *)
                        echo "请输入 y(是)/n(否)/a(强制)/s(停止)"
                        ;;
                esac
            done
            echo ""
        fi
    done

    log_success "交互式端口清理完成"
}

# =============================================================================
# 端口监控函数
# =============================================================================

# 监控端口状态变化
monitor_port() {
    local port=$1
    local duration=${2:-60}  # 默认监控60秒
    local interval=${3:-5}   # 默认每5秒检查一次

    log_info "开始监控端口 $port (持续 ${duration}秒，每 ${interval}秒检查一次)"

    local start_time=$(date +%s)
    local end_time=$((start_time + duration))

    while (( $(date +%s) < end_time )); do
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))

        if is_port_in_use "$port"; then
            local pid=$(get_pid_by_port "$port")
            local process_name=$(ps -p "$pid" -o comm= 2>/dev/null || echo "unknown")
            printf "[%02d:%02d] 端口 %d 被占用 (PID: %s, 进程: %s)\n" \
                   $((elapsed / 60)) $((elapsed % 60)) $port "$pid" "$process_name"
        else
            printf "[%02d:%02d] 端口 %d 可用\n" $((elapsed / 60)) $((elapsed % 60)) $port
        fi

        sleep "$interval"
    done

    log_success "端口监控完成"
}

# 生成端口状态报告
generate_port_report() {
    local report_file="${1:-${REPORT_DIR:-./reports}/port-status-$(date +%Y%m%d_%H%M%S).html}"

    ensure_directory "$(dirname "$report_file")"

    log_info "生成端口状态报告: $report_file"

    cat > "$report_file" << EOF
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>端口状态报告 - $(date '+%Y-%m-%d %H:%M:%S')</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f0f0f0; padding: 20px; border-radius: 5px; }
        .port-used { color: #d32f2f; }
        .port-free { color: #388e3c; }
        .port-info { margin: 10px 0; padding: 10px; border-left: 4px solid #ccc; }
        .timestamp { color: #666; font-size: 0.9em; }
    </style>
</head>
<body>
    <div class="header">
        <h1>订餐管理系统 - 端口状态报告</h1>
        <p class="timestamp">生成时间: $(date '+%Y-%m-%d %H:%M:%S')</p>
    </div>

    <h2>端口状态概览</h2>
EOF

    for port in "${PROJECT_PORTS[@]}"; do
        local service_name=""
        case $port in
            "$BACKEND_PORT")    service_name="后端服务" ;;
            "$FRONTDESK_PORT")  service_name="前台服务" ;;
            "$BACKDESK_PORT")   service_name="后台服务" ;;
            *)                  service_name="未知服务" ;;
        esac

        if is_port_in_use "$port"; then
            local pid=$(get_pid_by_port "$port")
            local process_info=$(ps -p "$pid" -o pid,ppid,user,%cpu,%mem,cmd --no-headers 2>/dev/null)

            cat >> "$report_file" << EOF
    <div class="port-info port-used">
        <h3>$service_name (端口: $port)</h3>
        <p><strong>状态:</strong> 被占用</p>
        <p><strong>进程ID:</strong> $pid</p>
        <p><strong>进程信息:</strong> <code>$process_info</code></p>
    </div>
EOF
        else
            cat >> "$report_file" << EOF
    <div class="port-info port-free">
        <h3>$service_name (端口: $port)</h3>
        <p><strong>状态:</strong> 可用</p>
    </div>
EOF
        fi
    done

    cat >> "$report_file" << EOF
</body>
</html>
EOF

    log_success "端口状态报告已生成: $report_file"
    echo "$report_file"
}

# =============================================================================
# 主函数和工具函数
# =============================================================================

# 显示端口使用帮助
show_port_help() {
    cat << EOF
端口工具函数库使用方法:

端口查询:
  get_pid_by_port <port>           # 获取端口进程ID
  get_port_process_info <port>     # 获取端口进程详细信息
  get_all_ports_status             # 获取所有端口状态

端口检查:
  check_port_availability <port>   # 检查端口是否可用
  check_all_ports_availability     # 检查所有端口可用性
  wait_for_port_release <port>     # 等待端口释放

端口清理:
  kill_port_process <port>         # 终止端口进程
  cleanup_all_ports                # 清理所有端口
  interactive_port_cleanup         # 交互式清理

端口监控:
  monitor_port <port>              # 监控端口状态
  generate_port_report [file]      # 生成端口报告

环境变量:
  BACKEND_PORT=8081               # 后端端口
  FRONTDESK_PORT=3000             # 前台端口
  BACKDESK_PORT=3001              # 后台端口
EOF
}

# 初始化端口工具
init_port_utils() {
    log_debug "初始化端口工具函数库"
    log_debug "端口配置: 后端=$BACKEND_PORT, 前台=$FRONTDESK_PORT, 后台=$BACKDESK_PORT"
}

# 如果脚本被直接执行，则显示帮助
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    init_script
    init_port_utils
    show_port_help
fi