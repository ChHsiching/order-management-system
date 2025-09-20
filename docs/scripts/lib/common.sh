#!/bin/bash

# =============================================================================
# 订餐管理系统 - 通用工具函数库
# Common Utility Functions Library for Order Management System
# =============================================================================

# 设置脚本严格模式
set -euo pipefail

# =============================================================================
# 项目路径配置 (硬编码绝对路径)
# =============================================================================

# 项目根目录
readonly PROJECT_ROOT="/home/chhsich/Git/Mine/order-management-system"

# 后端项目路径
readonly BACKEND_DIR="$PROJECT_ROOT/backend"

# 前端项目路径
readonly FRONTDESK_DIR="$PROJECT_ROOT/frontdesk"

# 后台管理项目路径
readonly BACKDESK_DIR="$PROJECT_ROOT/backdesk"

# 脚本目录
readonly SCRIPTS_DIR="$PROJECT_ROOT/docs/scripts"

# 日志目录
readonly LOG_DIR="$PROJECT_ROOT/logs"

# PID文件目录
readonly PID_DIR="$PROJECT_ROOT"

# 报告目录
readonly REPORT_DIR="$PROJECT_ROOT/reports"

# =============================================================================
# 端口配置
# =============================================================================

readonly DEFAULT_BACKEND_PORT=8080
readonly DEFAULT_FRONTDESK_PORT=5173
readonly DEFAULT_BACKDESK_PORT=5174

# 从环境变量获取端口配置或使用默认值
BACKEND_PORT=${BACKEND_PORT:-$DEFAULT_BACKEND_PORT}
FRONTDESK_PORT=${FRONTDESK_PORT:-$DEFAULT_FRONTDESK_PORT}
BACKDESK_PORT=${BACKDESK_PORT:-$DEFAULT_BACKDESK_PORT}

# =============================================================================
# 路径验证函数
# =============================================================================

# 验证项目结构
validate_project_structure() {
    local validation_errors=()

    # 检查项目根目录
    if [[ ! -d "$PROJECT_ROOT" ]]; then
        validation_errors+=("项目根目录不存在: $PROJECT_ROOT")
    fi

    # 检查后端目录
    if [[ ! -d "$BACKEND_DIR" ]]; then
        validation_errors+=("后端目录不存在: $BACKEND_DIR")
    elif [[ ! -f "$BACKEND_DIR/pom.xml" ]]; then
        validation_errors+=("后端项目文件不存在: $BACKEND_DIR/pom.xml")
    fi

    # 检查前台目录
    if [[ ! -d "$FRONTDESK_DIR" ]]; then
        validation_errors+=("前台目录不存在: $FRONTDESK_DIR")
    elif [[ ! -f "$FRONTDESK_DIR/package.json" ]]; then
        validation_errors+=("前台项目文件不存在: $FRONTDESK_DIR/package.json")
    fi

    # 检查后台目录
    if [[ ! -d "$BACKDESK_DIR" ]]; then
        validation_errors+=("后台目录不存在: $BACKDESK_DIR")
    elif [[ ! -f "$BACKDESK_DIR/package.json" ]]; then
        validation_errors+=("后台项目文件不存在: $BACKDESK_DIR/package.json")
    fi

    # 检查脚本目录
    if [[ ! -d "$SCRIPTS_DIR" ]]; then
        validation_errors+=("脚本目录不存在: $SCRIPTS_DIR")
    fi

    # 如果有错误，输出并退出
    if [[ ${#validation_errors[@]} -gt 0 ]]; then
        log_error "项目结构验证失败:"
        for error in "${validation_errors[@]}"; do
            log_error "  - $error"
        done
        return 1
    fi

    log_success "项目结构验证通过"
    return 0
}

# 获取服务路径
get_service_path() {
    local service_name=$1
    case $service_name in
        "backend")    echo "$BACKEND_DIR" ;;
        "frontdesk")  echo "$FRONTDESK_DIR" ;;
        "backdesk")   echo "$BACKDESK_DIR" ;;
        *)            log_error "未知服务: $service_name"; return 1 ;;
    esac
}

# 获取服务PID文件路径
get_pid_file_path() {
    local service_name=$1
    case $service_name in
        "backend")    echo "$PID_DIR/.backend.pid" ;;
        "frontdesk")  echo "$PID_DIR/.frontdesk.pid" ;;
        "backdesk")   echo "$PID_DIR/.backdesk.pid" ;;
        "monitor")    echo "$PID_DIR/.monitor.pid" ;;
        *)            log_error "未知服务: $service_name"; return 1 ;;
    esac
}

# 获取服务日志文件路径
get_log_file_path() {
    local service_name=$1
    case $service_name in
        "backend")    echo "$LOG_DIR/backend.log" ;;
        "frontdesk")  echo "$LOG_DIR/frontdesk.log" ;;
        "backdesk")   echo "$LOG_DIR/backdesk.log" ;;
        *)            log_error "未知服务: $service_name"; return 1 ;;
    esac
}

# 检查服务文件是否存在
check_service_files() {
    local service_name=$1
    local service_path=$(get_service_path "$service_name")

    if [[ -z "$service_path" ]]; then
        return 1
    fi

    case $service_name in
        "backend")
            if [[ ! -f "$service_path/pom.xml" ]]; then
                log_error "后端项目文件不存在: $service_path/pom.xml"
                return 1
            fi
            ;;
        "frontdesk"|"backdesk")
            if [[ ! -f "$service_path/package.json" ]]; then
                log_error "前端项目文件不存在: $service_path/package.json"
                return 1
            fi
            ;;
    esac

    return 0
}

# =============================================================================
# 颜色定义
# =============================================================================
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly CYAN='\033[0;36m'
readonly MAGENTA='\033[0;35m'
readonly WHITE='\033[1;37m'
readonly NC='\033[0m' # No Color

# =============================================================================
# 日志函数
# =============================================================================

# 信息日志
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "${LOG_FILE:-/tmp/order-management.log}"
}

# 成功日志
log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "${LOG_FILE:-/tmp/order-management.log}"
}

# 警告日志
log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "${LOG_FILE:-/tmp/order-management.log}"
}

# 错误日志
log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "${LOG_FILE:-/tmp/order-management.log}"
}

# 调试日志
log_debug() {
    if [[ "${DEBUG:-false}" == "true" ]]; then
        echo -e "${CYAN}[DEBUG]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "${LOG_FILE:-/tmp/order-management.log}"
    fi
}

# =============================================================================
# 状态输出函数
# =============================================================================

# 打印带颜色的状态信息
print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO")    echo -e "${BLUE}[INFO]${NC} $message" ;;
        "SUCCESS") echo -e "${GREEN}[SUCCESS]${NC} $message" ;;
        "WARNING") echo -e "${YELLOW}[WARNING]${NC} $message" ;;
        "ERROR")   echo -e "${RED}[ERROR]${NC} $message" ;;
        *)        echo -e "${WHITE}[UNKNOWN]${NC} $message" ;;
    esac
}

# 打印进度条
print_progress() {
    local current=$1
    local total=$2
    local width=50
    local percentage=$((current * 100 / total))
    local completed=$((current * width / total))

    printf "["
    printf "%*s" $completed | tr ' ' '='
    printf "%*s" $((width - completed)) | tr ' ' ' '
    printf "] %d%% (%d/%d)\r" $percentage $current $total
}

# 打印分隔线
print_separator() {
    echo -e "${CYAN}========================================${NC}"
}

# 打印标题
print_title() {
    echo -e "${MAGENTA}$1${NC}"
    print_separator
}

# =============================================================================
# 错误处理函数
# =============================================================================

# 错误处理函数
handle_error() {
    local exit_code=$?
    local line_number=$1
    local command_name=$2
    log_error "脚本在第 $line_number 行执行命令 '$command_name' 时失败，退出码: $exit_code"
    exit $exit_code
}

# 设置错误陷阱
setup_error_handling() {
    trap 'handle_error ${LINENO} "${BASH_COMMAND}"' ERR
}

# 检查命令是否存在
check_command() {
    local cmd=$1
    if ! command -v "$cmd" &> /dev/null; then
        log_error "命令 '$cmd' 未找到，请安装后再试"
        exit 1
    fi
}

# 检查必要命令
check_required_commands() {
    local commands=("$@")
    for cmd in "${commands[@]}"; do
        check_command "$cmd"
    done
    log_success "所有必要命令都已安装: ${commands[*]}"
}

# =============================================================================
# 文件和目录操作
# =============================================================================

# 确保目录存在
ensure_directory() {
    local dir=$1
    if [[ ! -d "$dir" ]]; then
        mkdir -p "$dir"
        log_info "创建目录: $dir"
    fi
}

# 删除文件或目录（安全确认）
safe_remove() {
    local path=$1
    if [[ -e "$path" ]]; then
        log_warning "即将删除: $path"
        read -p "确认删除吗？(y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            rm -rf "$path"
            log_success "已删除: $path"
        else
            log_info "取消删除: $path"
        fi
    else
        log_info "路径不存在: $path"
    fi
}

# 备份文件
backup_file() {
    local file=$1
    if [[ -f "$file" ]]; then
        local backup="${file}.$(date +%Y%m%d_%H%M%S).backup"
        cp "$file" "$backup"
        log_success "备份文件: $file -> $backup"
        echo "$backup"
    else
        log_error "文件不存在: $file"
        return 1
    fi
}

# =============================================================================
# 时间和格式化函数
# =============================================================================

# 获取时间戳
get_timestamp() {
    date "+%Y-%m-%d %H:%M:%S"
}

# 获取文件时间戳
get_file_timestamp() {
    local file=$1
    if [[ -f "$file" ]]; then
        date -r "$file" "+%Y-%m-%d %H:%M:%S"
    else
        echo "1970-01-01 00:00:00"
    fi
}

# 格式化持续时间
format_duration() {
    local seconds=$1
    local hours=$((seconds / 3600))
    local minutes=$(((seconds % 3600) / 60))
    local secs=$((seconds % 60))

    if ((hours > 0)); then
        printf "%02d:%02d:%02d" $hours $minutes $secs
    else
        printf "%02d:%02d" $minutes $secs
    fi
}

# =============================================================================
# 配置管理函数
# =============================================================================

# 加载配置文件
load_config() {
    local config_file=$1
    if [[ -f "$config_file" ]]; then
        source "$config_file"
        log_success "加载配置文件: $config_file"
    else
        log_warning "配置文件不存在: $config_file，使用默认配置"
    fi
}

# 设置环境变量（带默认值）
set_env_with_default() {
    local var_name=$1
    local default_value=$2
    export "${var_name}=${!var_name:-$default_value}"
    log_debug "设置环境变量: $var_name=${!var_name}"
}

# =============================================================================
# 进程管理函数
# =============================================================================

# 检查进程是否运行
is_process_running() {
    local pid=$1
    if kill -0 "$pid" 2>/dev/null; then
        return 0
    else
        return 1
    fi
}

# 等待进程结束
wait_for_process() {
    local pid=$1
    local timeout=${2:-30}
    local start_time=$(date +%s)

    while is_process_running "$pid"; do
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))

        if ((elapsed > timeout)); then
            log_error "等待进程 $pid 超时 ($timeout 秒)"
            return 1
        fi

        sleep 1
    done

    return 0
}

# =============================================================================
# 网络工具函数
# =============================================================================

# 检查端口是否被占用
is_port_in_use() {
    local port=$1
    if lsof -i :"$port" >/dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# 等待端口可用
wait_for_port() {
    local port=$1
    local timeout=${2:-30}
    local start_time=$(date +%s)

    while is_port_in_use "$port"; do
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_time))

        if ((elapsed > timeout)); then
            log_error "等待端口 $port 可用超时 ($timeout 秒)"
            return 1
        fi

        sleep 1
    done

    return 0
}

# =============================================================================
# 脚本信息函数
# =============================================================================

# 显示脚本信息
show_script_info() {
    local script_name=$0
    local script_version=${VERSION:-"1.0.0"}
    local script_description=${DESCRIPTION:-"订餐管理系统脚本"}

    print_title "$script_description"
    echo "脚本名称: $script_name"
    echo "版本: $script_version"
    echo "作者: ${AUTHOR:-"ChHsiching"}"
    echo "创建时间: $(get_timestamp)"
    print_separator
}

# 显示帮助信息
show_help() {
    local help_text=$1
    echo -e "${CYAN}使用方法:${NC}"
    echo "$help_text"
    print_separator
}

# =============================================================================
# 初始化函数
# =============================================================================

# 初始化日志文件
init_logging() {
    local log_file=${LOG_FILE:-"/tmp/order-management-$(date +%Y%m%d).log"}
    export LOG_FILE="$log_file"
    ensure_directory "$(dirname "$log_file")"
    log_info "初始化日志文件: $log_file"
}

# 初始化脚本
init_script() {
    setup_error_handling
    init_logging

    # 验证项目结构
    if ! validate_project_structure; then
        log_error "项目结构验证失败，脚本退出"
        exit 1
    fi

    # 设置默认环境变量
    set_env_with_default "DEBUG" "false"
    set_env_with_default "BACKEND_PORT" "$DEFAULT_BACKEND_PORT"
    set_env_with_default "FRONTDESK_PORT" "$DEFAULT_FRONTDESK_PORT"
    set_env_with_default "BACKDESK_PORT" "$DEFAULT_BACKDESK_PORT"

    # 确保必要的目录存在
    ensure_directory "$LOG_DIR"
    ensure_directory "$PID_DIR"
    ensure_directory "$REPORT_DIR"

    log_info "脚本初始化完成"
    log_debug "项目路径: $PROJECT_ROOT"
    log_debug "后端路径: $BACKEND_DIR"
    log_debug "前台路径: $FRONTDESK_DIR"
    log_debug "后台路径: $BACKDESK_DIR"
    log_debug "端口配置: 后端=$BACKEND_PORT, 前台=$FRONTDESK_PORT, 后台=$BACKDESK_PORT"
}

# =============================================================================
# 清理函数
# =============================================================================

# 清理临时文件
cleanup_temp_files() {
    local temp_dir=${TEMP_DIR:-"/tmp/order-management"}
    if [[ -d "$temp_dir" ]]; then
        rm -rf "$temp_dir"
        log_info "清理临时文件: $temp_dir"
    fi
}

# 脚本退出时的清理
cleanup_on_exit() {
    local exit_code=$?
    if [[ $exit_code -eq 0 ]]; then
        log_success "脚本正常退出"
    else
        log_error "脚本异常退出，退出码: $exit_code"
    fi
    cleanup_temp_files
}

# 设置退出陷阱
setup_exit_trap() {
    trap cleanup_on_exit EXIT
}

# =============================================================================
# 主初始化
# =============================================================================

# 如果脚本被直接执行（而不是被source），则进行初始化
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    init_script
    setup_exit_trap
fi