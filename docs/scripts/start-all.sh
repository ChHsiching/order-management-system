#!/bin/bash

# =============================================================================
# 订餐管理系统 - 项目启动脚本
# Project Startup Script for Order Management System
# =============================================================================

# 加载工具函数库
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/port-utils.sh"

# 初始化脚本
init_script
init_port_utils

# =============================================================================
# 项目启动配置
# =============================================================================

# 服务启动超时时间（秒）
readonly STARTUP_TIMEOUT=60
readonly HEALTH_CHECK_INTERVAL=5

# 进程PID文件 (使用绝对路径)
readonly BACKEND_PID_FILE="$PID_DIR/.backend.pid"
readonly FRONTDESK_PID_FILE="$PID_DIR/.frontdesk.pid"
readonly BACKDESK_PID_FILE="$PID_DIR/.backdesk.pid"

# 日志文件配置 (使用绝对路径)
readonly BACKEND_LOG="$LOG_DIR/backend.log"
readonly FRONTDESK_LOG="$LOG_DIR/frontdesk.log"
readonly BACKDESK_LOG="$LOG_DIR/backdesk.log"

# API配置
readonly API_CONTEXT_PATH="/WebOrderSystem"

# =============================================================================
# 服务启动函数
# =============================================================================

# 显示启动帮助
show_startup_help() {
    cat << EOF
项目启动脚本使用方法:

基本启动:
  ./start-all.sh                    # 启动所有服务
  ./start-all.sh --backend          # 只启动后端服务
  ./start-all.sh --frontend         # 启动前端服务
  ./start-all.sh --frontdesk        # 启动前台服务
  ./start-all.sh --backdesk         # 启动后台服务

启动选项:
  ./start-all.sh --clean            # 清理端口后启动
  ./start-all.sh --check            # 启动前检查环境
  ./start-all.sh --wait             # 等待服务完全启动
  ./start-all.sh --monitor          # 启动后监控服务状态

调试选项:
  ./start-all.sh --debug            # 调试模式启动
  ./start-all.sh --dev              # 开发模式启动
  ./start-all.sh --prod             # 生产模式启动

信息查询:
  ./start-all.sh --status           # 显示服务状态
  ./start-all.sh --help             # 显示帮助信息

示例:
  ./start-all.sh --clean --wait     # 清理端口并等待服务启动
  ./start-all.sh --backend --debug  # 调试模式启动后端
  ./start-all.sh --check --monitor  # 检查环境并监控服务
EOF
}

# 解析命令行参数
parse_arguments() {
    local start_backend=false
    local start_frontdesk=false
    local start_backdesk=false
    local clean_ports=false
    local check_env=false
    local wait_for_startup=false
    local monitor_services=false
    local debug_mode=false
    local dev_mode=false
    local prod_mode=false
    local show_status=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --backend)
                start_backend=true
                shift
                ;;
            --frontend)
                start_frontdesk=true
                start_backdesk=true
                shift
                ;;
            --frontdesk)
                start_frontdesk=true
                shift
                ;;
            --backdesk)
                start_backdesk=true
                shift
                ;;
            --clean)
                clean_ports=true
                shift
                ;;
            --check)
                check_env=true
                shift
                ;;
            --wait)
                wait_for_startup=true
                shift
                ;;
            --monitor)
                monitor_services=true
                shift
                ;;
            --debug)
                debug_mode=true
                shift
                ;;
            --dev)
                dev_mode=true
                shift
                ;;
            --prod)
                prod_mode=true
                shift
                ;;
            --status)
                show_status=true
                shift
                ;;
            --help|-h)
                show_startup_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_startup_help
                exit 1
                ;;
        esac
    done

    # 如果没有指定具体服务，默认启动所有服务
    if ! $start_backend && ! $start_frontdesk && ! $start_backdesk; then
        start_backend=true
        start_frontdesk=true
        start_backdesk=true
    fi

    echo "$start_backend|$start_frontdesk|$start_backdesk|$clean_ports|$check_env|$wait_for_startup|$monitor_services|$debug_mode|$dev_mode|$prod_mode|$show_status"
}

# 检查启动环境
check_startup_environment() {
    log_info "检查项目启动环境..."
    print_separator

    local env_ok=true

    # 检查必要命令
    check_required_commands curl java npm mvn

    # 检查Java版本
    if command -v java &> /dev/null; then
        local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        log_success "Java版本: $java_version"
    else
        log_error "Java未安装"
        env_ok=false
    fi

    # 检查Node.js和npm
    if command -v node &> /dev/null; then
        local node_version=$(node --version)
        log_success "Node.js版本: $node_version"
    else
        log_error "Node.js未安装"
        env_ok=false
    fi

    if command -v npm &> /dev/null; then
        local npm_version=$(npm --version)
        log_success "npm版本: $npm_version"
    else
        log_error "npm未安装"
        env_ok=false
    fi

    # 检查Maven
    if command -v mvn &> /dev/null; then
        local mvn_version=$(mvn --version | head -n 1)
        log_success "Maven版本: $mvn_version"
    else
        log_error "Maven未安装"
        env_ok=false
    fi

    # 检查项目目录结构 (使用绝对路径)
    if [[ -d "$BACKEND_DIR" ]]; then
        log_success "后端目录存在: $BACKEND_DIR"
    else
        log_error "后端目录不存在: $BACKEND_DIR"
        env_ok=false
    fi

    if [[ -d "$FRONTDESK_DIR" ]]; then
        log_success "前台目录存在: $FRONTDESK_DIR"
    else
        log_error "前台目录不存在: $FRONTDESK_DIR"
        env_ok=false
    fi

    if [[ -d "$BACKDESK_DIR" ]]; then
        log_success "后台目录存在: $BACKDESK_DIR"
    else
        log_error "后台目录不存在: $BACKDESK_DIR"
        env_ok=false
    fi

    print_separator

    if $env_ok; then
        log_success "环境检查通过"
        return 0
    else
        log_error "环境检查失败"
        return 1
    fi
}

# 清理端口和PID文件
cleanup_before_start() {
    log_info "清理端口和进程文件..."

    # 清理端口
    cleanup_all_ports >/dev/null 2>&1

    # 清理PID文件
    rm -f "$BACKEND_PID_FILE" "$FRONTDESK_PID_FILE" "$BACKDESK_PID_FILE"

    log_success "清理完成"
}

# 启动后端服务
start_backend_service() {
    local debug_mode=$1
    local dev_mode=$2
    local prod_mode=$3

    log_info "启动后端服务..."

    # 检查后端目录
    if [[ ! -d "$BACKEND_DIR" ]]; then
        log_error "后端目录不存在: $BACKEND_DIR"
        return 1
    fi

    # 检查后端文件
    if [[ ! -f "$BACKEND_DIR/pom.xml" ]]; then
        log_error "后端项目文件不存在: $BACKEND_DIR/pom.xml"
        return 1
    fi

    # 确保日志目录存在
    ensure_directory "$LOG_DIR"

    # 构建启动命令 (使用绝对路径)
    local backend_cmd="cd \"$BACKEND_DIR\" && mvn spring-boot:run"

    if $debug_mode; then
        backend_cmd="$backend_cmd -Dspring-boot.run.profiles=dev -Ddebug=true"
    elif $dev_mode; then
        backend_cmd="$backend_cmd -Dspring-boot.run.profiles=dev"
    elif $prod_mode; then
        backend_cmd="$backend_cmd -Dspring-boot.run.profiles=prod"
    else
        backend_cmd="$backend_cmd -Dspring-boot.run.profiles=dev"
    fi

    # 启动后端服务
    log_info "执行: $backend_cmd"
    eval "$backend_cmd > \"$BACKEND_LOG\" 2>&1 &"
    local backend_pid=$!

    # 保存PID
    echo "$backend_pid" > "$BACKEND_PID_FILE"
    log_success "后端服务已启动 (PID: $backend_pid)"

    # 等待服务启动
    wait_for_backend_service
}

# 等待后端服务启动
wait_for_backend_service() {
    log_info "等待后端服务启动..."

    local timeout=30
    local start_time=$(date +%s)
    local end_time=$((start_time + timeout))

    while (( $(date +%s) < end_time )); do
        if check_api_service_available >/dev/null 2>&1; then
            log_success "后端服务已就绪"
            return 0
        fi

        sleep 2
        printf "."
    done

    echo ""
    log_warning "后端服务启动超时"
    return 1
}

# 启动前台服务
start_frontdesk_service() {
    local debug_mode=$1

    log_info "启动前台服务..."

    # 检查前台目录
    if [[ ! -d "$FRONTDESK_DIR" ]]; then
        log_error "前台目录不存在: $FRONTDESK_DIR"
        return 1
    fi

    # 确保日志目录存在
    ensure_directory "$LOG_DIR"

    # 检查package.json
    if [[ ! -f "$FRONTDESK_DIR/package.json" ]]; then
        log_error "前台package.json不存在: $FRONTDESK_DIR/package.json"
        return 1
    fi

    # 安装依赖（如果需要）
    if [[ ! -d "$FRONTDESK_DIR/node_modules" ]]; then
        log_info "安装前台依赖..."
        cd "$FRONTDESK_DIR" && npm install > "$FRONTDESK_LOG" 2>&1
        cd - > /dev/null
    fi

    # 启动前台服务
    log_info "启动前台开发服务器..."
    cd "$FRONTDESK_DIR"

    local frontdesk_cmd="npm run dev"
    if $debug_mode; then
        frontdesk_cmd="NODE_ENV=development npm run dev"
    fi

    eval "$frontdesk_cmd > \"$FRONTDESK_LOG\" 2>&1 &"
    local frontdesk_pid=$!

    cd - > /dev/null

    # 保存PID
    echo "$frontdesk_pid" > "$FRONTDESK_PID_FILE"
    log_success "前台服务已启动 (PID: $frontdesk_pid)"

    # 等待服务启动
    wait_for_frontdesk_service
}

# 等待前台服务启动
wait_for_frontdesk_service() {
    log_info "等待前台服务启动..."

    local timeout=20
    local start_time=$(date +%s)
    local end_time=$((start_time + timeout))

    while (( $(date +%s) < end_time )); do
        if is_port_in_use "$FRONTDESK_PORT"; then
            log_success "前台服务已就绪"
            return 0
        fi

        sleep 2
        printf "."
    done

    echo ""
    log_warning "前台服务启动超时"
    return 1
}

# 启动后台服务
start_backdesk_service() {
    local debug_mode=$1

    log_info "启动后台服务..."

    # 检查后台目录
    if [[ ! -d "$BACKDESK_DIR" ]]; then
        log_error "后台目录不存在: $BACKDESK_DIR"
        return 1
    fi

    # 确保日志目录存在
    ensure_directory "$LOG_DIR"

    # 检查package.json
    if [[ ! -f "$BACKDESK_DIR/package.json" ]]; then
        log_error "后台package.json不存在: $BACKDESK_DIR/package.json"
        return 1
    fi

    # 安装依赖（如果需要）
    if [[ ! -d "$BACKDESK_DIR/node_modules" ]]; then
        log_info "安装后台依赖..."
        cd "$BACKDESK_DIR" && npm install > "$BACKDESK_LOG" 2>&1
        cd - > /dev/null
    fi

    # 启动后台服务
    log_info "启动后台开发服务器..."
    cd "$BACKDESK_DIR"

    local backdesk_cmd="npm run dev"
    if $debug_mode; then
        backdesk_cmd="NODE_ENV=development npm run dev"
    fi

    eval "$backdesk_cmd > \"$BACKDESK_LOG\" 2>&1 &"
    local backdesk_pid=$!

    cd - > /dev/null

    # 保存PID
    echo "$backdesk_pid" > "$BACKDESK_PID_FILE"
    log_success "后台服务已启动 (PID: $backdesk_pid)"

    # 等待服务启动
    wait_for_backdesk_service
}

# 等待后台服务启动
wait_for_backdesk_service() {
    log_info "等待后台服务启动..."

    local timeout=20
    local start_time=$(date +%s)
    local end_time=$((start_time + timeout))

    while (( $(date +%s) < end_time )); do
        if is_port_in_use "$BACKDESK_PORT"; then
            log_success "后台服务已就绪"
            return 0
        fi

        sleep 2
        printf "."
    done

    echo ""
    log_warning "后台服务启动超时"
    return 1
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
            rm -f "$BACKEND_PID_FILE"
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
            rm -f "$FRONTDESK_PID_FILE"
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
            rm -f "$BACKDESK_PID_FILE"
        fi
    else
        if is_port_in_use "$BACKDESK_PORT"; then
            print_status "WARNING" "后台端口被占用，但无PID文件"
        else
            print_status "INFO" "后台服务未运行"
        fi
    fi

    print_separator
}

# 监控服务状态
monitor_services() {
    log_info "开始监控服务状态..."
    print_separator

    # 后台监控
    (
        while true; do
            show_services_status
            sleep 30
        done
    ) &

    local monitor_pid=$!
    log_info "服务监控已启动 (PID: $monitor_pid)"
    echo "$monitor_pid" > ".monitor.pid"
}

# =============================================================================
# 主函数
# =============================================================================

main() {
    # 解析命令行参数
    local params=$(parse_arguments "$@")
    IFS='|' read -r start_backend start_frontdesk start_backdesk clean_ports check_env wait_for_startup monitor_services debug_mode dev_mode prod_mode show_status <<< "$params"

    print_title "订餐管理系统 - 项目启动工具"

    # 设置调试模式
    if $debug_mode; then
        export DEBUG=true
        log_debug "启用调试模式"
    fi

    # 显示服务状态
    if $show_status; then
        show_services_status
        exit 0
    fi

    # 检查环境
    if $check_env; then
        if ! check_startup_environment; then
            log_error "环境检查失败，无法启动服务"
            exit 1
        fi
    fi

    # 清理端口
    if $clean_ports; then
        cleanup_before_start
    fi

    # 检查端口冲突
    log_info "检查端口可用性..."
    local ports_ok=true
    if $start_backend && is_port_in_use "$BACKEND_PORT"; then
        log_error "后端端口 $BACKEND_PORT 被占用"
        ports_ok=false
    fi
    if $start_frontdesk && is_port_in_use "$FRONTDESK_PORT"; then
        log_error "前台端口 $FRONTDESK_PORT 被占用"
        ports_ok=false
    fi
    if $start_backdesk && is_port_in_use "$BACKDESK_PORT"; then
        log_error "后台端口 $BACKDESK_PORT 被占用"
        ports_ok=false
    fi

    if ! $ports_ok; then
        log_error "端口冲突，请使用 --clean 选项清理端口"
        exit 1
    fi

    # 启动服务
    local all_started=true

    if $start_backend; then
        if ! start_backend_service "$debug_mode" "$dev_mode" "$prod_mode"; then
            log_error "后端服务启动失败"
            all_started=false
        fi
    fi

    if $start_frontdesk; then
        if ! start_frontdesk_service "$debug_mode"; then
            log_error "前台服务启动失败"
            all_started=false
        fi
    fi

    if $start_backdesk; then
        if ! start_backdesk_service "$debug_mode"; then
            log_error "后台服务启动失败"
            all_started=false
        fi
    fi

    # 等待所有服务启动
    if $wait_for_startup && $all_started; then
        log_info "等待所有服务启动完成..."
        sleep 5
        show_services_status
    fi

    # 启动监控
    if $monitor_services; then
        monitor_services
    fi

    # 显示最终状态
    show_services_status

    if $all_started; then
        log_success "项目启动完成"
        print_separator
        log_info "访问地址:"
        log_info "  后端API: http://localhost:$BACKEND_PORT$API_CONTEXT_PATH"
        log_info "  前台界面: http://localhost:$FRONTDESK_PORT"
        log_info "  后台界面: http://localhost:$BACKDESK_PORT"
        print_separator
        exit 0
    else
        log_error "部分服务启动失败"
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