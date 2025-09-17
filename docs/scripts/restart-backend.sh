#!/bin/bash

# ============================================================================
# Web订餐管理系统 - 后端重启脚本
# 功能：关闭旧服务、清理端口、重新编译、重启后端服务
# 使用方法：./restart-backend.sh [dev|prod] 默认dev环境
# ============================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
PORT=8080
PROFILE=${1:-dev}  # 默认使用dev环境
LOG_FILE="$PROJECT_ROOT/backend/restart.log"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

# 检查并停止占用端口的进程
stop_processes_on_port() {
    log_info "检查端口 $PORT 的占用情况..."

    if command -v lsof >/dev/null 2>&1; then
        PIDS=$(lsof -i :$PORT | awk 'NR>1{print $2}' | sort -u)
        if [ -n "$PIDS" ]; then
            log_warning "发现占用端口 $PORT 的进程: $PIDS"
            echo "$PIDS" | xargs -r kill -9
            log_success "已强制停止相关进程"
            sleep 2
        else
            log_info "端口 $PORT 未被占用"
        fi
    else
        log_warning "lsof 命令不可用，尝试使用其他方法..."
        # 使用 netstat 或 ss 检查
        if command -v netstat >/dev/null 2>&1; then
            PIDS=$(netstat -tlnp 2>/dev/null | grep :$PORT | awk '{print $7}' | cut -d'/' -f1)
            if [ -n "$PIDS" ]; then
                echo "$PIDS" | xargs -r kill -9
                log_success "已停止相关进程"
                sleep 2
            fi
        fi
    fi
}

# 清理 Maven 和 Spring Boot 进程
cleanup_maven_processes() {
    log_info "清理 Maven 和 Spring Boot 相关进程..."

    # 查找并停止 Maven 进程
    MAVEN_PIDS=$(pgrep -f "maven|spring-boot:run" || true)
    if [ -n "$MAVEN_PIDS" ]; then
        log_warning "发现 Maven 进程: $MAVEN_PIDS"
        echo "$MAVEN_PIDS" | xargs -r kill -9
        log_success "已停止 Maven 进程"
    fi

    # 查找并停止 Java Spring Boot 进程
    SPRING_PIDS=$(pgrep -f "spring-boot.*BackendApplication" || true)
    if [ -n "$SPRING_PIDS" ]; then
        log_warning "发现 Spring Boot 进程: $SPRING_PIDS"
        echo "$SPRING_PIDS" | xargs -r kill -9
        log_success "已停止 Spring Boot 进程"
    fi

    sleep 1
}

# 清理编译产物
clean_build() {
    log_info "清理编译产物..."
    cd "$BACKEND_DIR"

    # 运行 Maven 清理
    if mvn clean -q; then
        log_success "Maven 清理完成"
    else
        log_error "Maven 清理失败"
        exit 1
    fi
}

# 编译项目
compile_project() {
    log_info "开始编译项目..."
    cd "$BACKEND_DIR"

    if mvn compile -q; then
        log_success "项目编译成功"
    else
        log_error "项目编译失败"
        exit 1
    fi
}

# 启动后端服务
start_backend() {
    log_info "启动后端服务（环境：$PROFILE）..."
    cd "$BACKEND_DIR"

    # 清理旧的日志文件
    if [ -f "app-test.log" ]; then
        rm -f app-test.log
        log_info "已清理旧的日志文件"
    fi

    # 启动服务
    nohup mvn spring-boot:run -Dspring-boot.run.profiles="$PROFILE" -q > app-test.log 2>&1 &
    SPRING_PID=$!
    log_success "后端服务已启动，PID: $SPRING_PID"

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 15

    # 检查服务是否成功启动
    if check_service_health; then
        log_success "后端服务启动成功！"
        log_info "服务地址: http://localhost:$PORT/WebOrderSystem"
        log_info "Swagger文档: http://localhost:$PORT/WebOrderSystem/swagger-ui.html"
        log_info "健康检查: http://localhost:$PORT/WebOrderSystem/actuator/health"
        log_info "日志文件: $BACKEND_DIR/app-test.log"
        log_info "重启日志: $LOG_FILE"
    else
        log_error "后端服务启动失败！"
        log_info "请检查日志文件: $BACKEND_DIR/app-test.log"
        exit 1
    fi
}

# 检查服务健康状态
check_service_health() {
    local max_attempts=10
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:$PORT/WebOrderSystem/actuator/health" >/dev/null 2>&1; then
            return 0
        fi

        log_warning "服务尚未就绪，等待中... ($attempt/$max_attempts)"
        sleep 3
        attempt=$((attempt + 1))
    done

    return 1
}

# 显示测试命令
show_test_commands() {
    echo ""
    log_info "================ 测试命令 ================"
    echo -e "${YELLOW}公开接口测试:${NC}"
    echo "curl -s http://localhost:$PORT/WebOrderSystem/api/categories"
    echo ""
    echo -e "${YELLOW}后台接口测试 (需要认证):${NC}"
    echo "curl -u admin:admin -s http://localhost:$PORT/WebOrderSystem/admin/menu/categories"
    echo ""
    echo -e "${YELLOW}购物车测试:${NC}"
    echo "curl -s -X POST \"http://localhost:$PORT/WebOrderSystem/api/cart/add?username=admin&productId=1&quantity=2\""
    echo ""
    echo -e "${YELLOW}查看实时日志:${NC}"
    echo "tail -f $BACKEND_DIR/app-test.log"
    echo ""
    echo -e "${YELLOW}停止服务:${NC}"
    echo "pkill -f 'spring-boot.*BackendApplication'"
    echo "=============================================="
}

# 主函数
main() {
    log_info "开始重启后端服务..."
    log_info "项目根目录: $PROJECT_ROOT"
    log_info "后端目录: $BACKEND_DIR"
    log_info "运行环境: $PROFILE"

    # 创建日志文件
    mkdir -p "$(dirname "$LOG_FILE")"
    touch "$LOG_FILE"

    # 执行重启流程
    stop_processes_on_port
    cleanup_maven_processes
    clean_build
    compile_project
    start_backend
    show_test_commands

    log_success "后端重启脚本执行完成！"
}

# 捕获 Ctrl+C 信号
trap 'log_warning "脚本被中断"; exit 1' INT

# 运行主函数
main