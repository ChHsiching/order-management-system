#!/bin/bash

# Web Order Management System - Test Framework Core Library
# 核心测试框架库 - 提供统一的测试执行和管理功能

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 全局变量
API_BASE="http://localhost:8080/WebOrderSystem"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_LOG_FILE="$SCRIPT_DIR/logs/test_framework.log"
TEST_START_TIME=$(date +%s)
TEST_TOTAL=0
TEST_PASSED=0
TEST_FAILED=0
TEST_SKIPPED=0

# 确保日志目录存在
mkdir -p "$SCRIPT_DIR/logs"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$TEST_LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$TEST_LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$TEST_LOG_FILE"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$TEST_LOG_FILE"
}

# 测试结果统计
update_test_stats() {
    local result="$1"
    TEST_TOTAL=$((TEST_TOTAL + 1))

    case "$result" in
        "PASS") TEST_PASSED=$((TEST_PASSED + 1)) ;;
        "FAIL") TEST_FAILED=$((TEST_FAILED + 1)) ;;
        "SKIP") TEST_SKIPPED=$((TEST_SKIPPED + 1)) ;;
    esac
}

# 开始测试
log_start() {
    local test_name="$1"
    log_info "开始测试: $test_name"
    echo "======================================" | tee -a "$TEST_LOG_FILE"
}

# 结束测试
log_end() {
    local test_name="$1"
    local duration=$(( $(date +%s) - TEST_START_TIME ))
    log_info "结束测试: $test_name (耗时: ${duration}s)"
    echo "======================================" | tee -a "$TEST_LOG_FILE"
}

# 测试环境准备
setup_test_environment() {
    log_info "准备测试环境..."

    # 创建日志目录
    mkdir -p logs

    # 检查后端服务状态
    if ! curl -f "$API_BASE/api-docs" > /dev/null 2>&1; then
        log_error "后端服务未启动，请启动服务后重试"
        return 1
    fi

    # 检查数据库连接
    if ! mysql -h localhost -u root -p123456 -e "USE web_order; SELECT 1;" > /dev/null 2>&1; then
        log_error "数据库连接失败"
        return 1
    fi

    log_success "测试环境准备完成"
    return 0
}

# 清理测试环境
cleanup_test_environment() {
    log_info "清理测试环境..."

    # 清理测试数据
    mysql -h localhost -u root -p123456 -e "USE web_order; DELETE FROM administrators WHERE username LIKE 'test_%';" 2>/dev/null || true
    mysql -h localhost -u root -p123456 -e "USE web_order; DELETE FROM the_order_entry WHERE order_id IN (SELECT id FROM cg_info WHERE user_name LIKE 'test_%');" 2>/dev/null || true
    mysql -h localhost -u root -p123456 -e "USE web_order; DELETE FROM cg_info WHERE user_name LIKE 'test_%';" 2>/dev/null || true

    log_success "测试环境清理完成"
}

# API测试函数
test_api() {
    local name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_code="${5:-200}"

    log_info "执行API测试: $name"

    # 构建请求
    local curl_cmd="curl -s -w \"HTTP_CODE:%{http_code}\" -X \"$method\""
    curl_cmd="$curl_cmd -H \"Content-Type: application/json\""

    if [[ -n "$data" ]]; then
        curl_cmd="$curl_cmd -d '$data'"
    fi

    curl_cmd="$curl_cmd \"$API_BASE$url\""

    # 执行请求
    local response=$(eval "$curl_cmd")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    local body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')

    # 验证结果
    if [[ "$http_code" == "$expected_code" ]]; then
        log_success "API测试通过: $name (状态码: $http_code)"
        update_test_stats "PASS"
        return 0
    else
        log_error "API测试失败: $name (期望: $expected_code, 实际: $http_code)"
        log_error "响应内容: $body"
        update_test_stats "FAIL"
        return 1
    fi
}

# 权限测试函数
test_permission() {
    local role="$1"
    local description="$2"
    local method="$3"
    local url="$4"
    local data="$5"
    local expected_code="$6"

    log_info "执行权限测试: $role - $description"

    # 根据角色设置认证头
    local auth_header=""
    case "$role" in
        "admin")
            auth_header="-H \"Authorization: Bearer admin_token\""  # 需要根据实际认证方式调整
            ;;
        "member")
            auth_header="-H \"Authorization: Bearer member_token\""  # 需要根据实际认证方式调整
            ;;
        "guest")
            auth_header=""
            ;;
    esac

    # 构建请求
    local curl_cmd="curl -s -w \"HTTP_CODE:%{http_code}\" -X \"$method\""
    curl_cmd="$curl_cmd -H \"Content-Type: application/json\""

    if [[ -n "$auth_header" ]]; then
        curl_cmd="$curl_cmd $auth_header"
    fi

    if [[ -n "$data" ]]; then
        curl_cmd="$curl_cmd -d '$data'"
    fi

    curl_cmd="$curl_cmd \"$API_BASE$url\""

    # 执行请求
    local response=$(eval "$curl_cmd")
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    local body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')

    # 验证结果
    if [[ "$http_code" == "$expected_code" ]]; then
        log_success "权限测试通过: $role - $description (状态码: $http_code)"
        update_test_stats "PASS"
        return 0
    else
        log_error "权限测试失败: $role - $description (期望: $expected_code, 实际: $http_code)"
        log_error "响应内容: $body"
        update_test_stats "FAIL"
        return 1
    fi
}

# 数据库验证函数
verify_database_count() {
    local table="$1"
    local condition="$2"
    local expected_count="$3"

    log_info "验证数据库计数: $table WHERE $condition = $expected_count"

    local query="SELECT COUNT(*) FROM $table"
    if [[ -n "$condition" ]]; then
        query="$query WHERE $condition"
    fi

    local actual_count=$(mysql -h localhost -u root -p123456 -s -e "USE web_order; $query;" 2>/dev/null)

    if [[ "$actual_count" == "$expected_count" ]]; then
        log_success "数据库验证通过: $table 计数正确 (期望: $expected_count, 实际: $actual_count)"
        update_test_stats "PASS"
        return 0
    else
        log_error "数据库验证失败: $table 计数错误 (期望: $expected_count, 实际: $actual_count)"
        update_test_stats "FAIL"
        return 1
    fi
}

# 生成测试报告
generate_test_report() {
    local report_file="reports/test_report_$(date +%Y%m%d_%H%M%S).md"

    mkdir -p reports

    log_info "生成测试报告: $report_file"

    cat > "$report_file" << EOF
# Web订餐管理系统测试报告

## 测试概览
- **测试时间**: $(date '+%Y-%m-%d %H:%M:%S')
- **测试总数**: $TEST_TOTAL
- **通过数量**: $TEST_PASSED
- **失败数量**: $TEST_FAILED
- **跳过数量**: $TEST_SKIPPED
- **通过率**: $(( TEST_TOTAL > 0 ? TEST_PASSED * 100 / TEST_TOTAL : 0 ))%

## 测试结果
### ✅ 通过测试 ($TEST_PASSED)
$(grep -A 1 "SUCCESS" "$TEST_LOG_FILE" | grep -v "SUCCESS" | sed 's/^/- /')

### ❌ 失败测试 ($TEST_FAILED)
$(grep -A 1 "ERROR" "$TEST_LOG_FILE" | grep -v "ERROR" | sed 's/^/- /')

## 详细日志
详见日志文件: $TEST_LOG_FILE

---
*报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')*
EOF

    log_success "测试报告生成完成: $report_file"
    echo "$report_file"
}

# 显示测试统计
show_test_stats() {
    echo ""
    echo "======================================"
    echo "测试统计结果"
    echo "======================================"
    echo -e "总测试数: ${BLUE}$TEST_TOTAL${NC}"
    echo -e "通过: ${GREEN}$TEST_PASSED${NC}"
    echo -e "失败: ${RED}$TEST_FAILED${NC}"
    echo -e "跳过: ${YELLOW}$TEST_SKIPPED${NC}"
    echo -e "通过率: ${BLUE}$(( TEST_TOTAL > 0 ? TEST_PASSED * 100 / TEST_TOTAL : 0 ))%${NC}"
    echo "======================================"
    echo ""
}

# 初始化测试框架
init_test_framework() {
    log_info "初始化测试框架..."

    # 创建必要目录
    mkdir -p logs
    mkdir -p reports
    mkdir -p data

    # 清空日志文件
    echo "Web Order Management System Test Framework Log" > "$TEST_LOG_FILE"
    echo "测试开始时间: $(date '+%Y-%m-%d %H:%M:%S')" >> "$TEST_LOG_FILE"

    log_success "测试框架初始化完成"
}

# 导出函数供其他脚本使用
export -f log_info log_success log_error log_warn
export -f update_test_stats log_start log_end
export -f setup_test_environment cleanup_test_environment
export -f test_api test_permission verify_database_count
export -f generate_test_report show_test_stats init_test_framework