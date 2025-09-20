#!/bin/bash

# =============================================================================
# 订餐管理系统 - API测试函数库
# API Test Functions Library for Order Management System
# =============================================================================

# 加载通用工具函数库
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

# =============================================================================
# API配置
# =============================================================================

# API基础配置
readonly API_BASE_URL="http://localhost:${BACKEND_PORT:-8080}"
readonly API_CONTEXT_PATH="/WebOrderSystem"
readonly API_BASE="${API_BASE_URL}${API_CONTEXT_PATH}"

# 超时配置（秒）
readonly API_TIMEOUT=10
readonly API_CONNECT_TIMEOUT=5

# 测试用户配置
readonly TEST_USER_USERNAME="testuser"
readonly TEST_USER_PASSWORD="testpass123"
readonly TEST_ADMIN_USERNAME="admin"
readonly TEST_ADMIN_PASSWORD="admin"

# API端点配置
readonly API_ENDPOINTS=(
    # 公开API
    "GET:/api/menu"
    "GET:/api/menu/recommended"
    "GET:/api/menu/hot-sales"
    "GET:/api/categories"

    # 用户认证API
    "POST:/api/user/login"
    "POST:/api/user/register"

    # 管理员认证API
    "POST:/api/admin/login"

    # 需要认证的API
    "GET:/api/user/info"
)

# 管理员API端点
readonly ADMIN_API_ENDPOINTS=(
    "GET:/admin/menu/items"
    "GET:/admin/menu/categories"
    "POST:/admin/menu/items"
    "PUT:/admin/menu/items/1"
    "DELETE:/admin/menu/items/1"
)

# =============================================================================
# 认证令牌管理
# =============================================================================

# 存储认证令牌的全局变量
USER_TOKEN=""
ADMIN_TOKEN=""

# 设置认证令牌
set_auth_token() {
    local token_type=$1
    local token_value=$2

    case $token_type in
        "user")
            USER_TOKEN="$token_value"
            log_debug "设置用户令牌: ${token_value:0:20}..."
            ;;
        "admin")
            ADMIN_TOKEN="$token_value"
            log_debug "设置管理员令牌: ${token_value:0:20}..."
            ;;
        *)
            log_error "未知的令牌类型: $token_type"
            return 1
            ;;
    esac
}

# 获取认证头
get_auth_header() {
    local token_type=$1

    case $token_type in
        "user")
            if [[ -n "$USER_TOKEN" ]]; then
                echo "Authorization: Bearer $USER_TOKEN"
            else
                log_error "用户令牌未设置"
                return 1
            fi
            ;;
        "admin")
            if [[ -n "$ADMIN_TOKEN" ]]; then
                echo "Authorization: Bearer $ADMIN_TOKEN"
            else
                log_error "管理员令牌未设置"
                return 1
            fi
            ;;
        *)
            log_error "未知的令牌类型: $token_type"
            return 1
            ;;
    esac
}

# =============================================================================
# HTTP请求函数
# =============================================================================

# 通用HTTP请求函数
make_request() {
    local method=$1
    local endpoint=$2
    local data=${3:-""}
    local auth_type=${4:-"none"}
    local headers_file=$(mktemp)
    local response_file=$(mktemp)

    # 构建完整的URL
    local url="${API_BASE}${endpoint}"

    # 准备curl命令
    local curl_cmd=(
        curl -s -w "%{http_code}" -o "$response_file"
        -X "$method"
        -H "Content-Type: application/json"
        -H "Accept: application/json"
        --connect-timeout "$API_CONNECT_TIMEOUT"
        --max-time "$API_TIMEOUT"
        --dump-header "$headers_file"
    )

    # 添加认证头
    if [[ "$auth_type" != "none" ]]; then
        local auth_header=$(get_auth_header "$auth_type")
        if [[ $? -ne 0 ]]; then
            rm -f "$headers_file" "$response_file"
            return 1
        fi
        curl_cmd+=(-H "$auth_header")
    fi

    # 添加请求数据
    if [[ -n "$data" ]]; then
        curl_cmd+=(-d "$data")
    fi

    # 添加URL
    curl_cmd+=("$url")

    # 执行请求
    log_debug "执行请求: $method $url"
    local http_code=$("${curl_cmd[@]}")
    local curl_exit_code=$?

    # 读取响应头和响应体
    local headers=$(cat "$headers_file")
    local response=$(cat "$response_file")

    # 清理临时文件
    rm -f "$headers_file" "$response_file"

    # 返回结果
    echo "$http_code|$curl_exit_code|$headers|$response"
}

# 解析HTTP响应
parse_response() {
    local response_string=$1
    IFS='|' read -r http_code curl_exit_code headers response <<< "$response_string"

    echo "$http_code|$curl_exit_code|$response"
}

# 提取响应状态
get_response_status() {
    local response_string=$1
    parse_response "$response_string" | cut -d'|' -f1
}

# 提取curl退出码
get_curl_exit_code() {
    local response_string=$1
    parse_response "$response_string" | cut -d'|' -f2
}

# 提取响应体
get_response_body() {
    local response_string=$1
    parse_response "$response_string" | cut -d'|' -f3
}

# 格式化JSON输出
format_json() {
    local json_string=$1
    if command -v jq &> /dev/null; then
        echo "$json_string" | jq '.' 2>/dev/null || echo "$json_string"
    else
        echo "$json_string"
    fi
}

# =============================================================================
# 测试结果管理
# =============================================================================

# 测试结果统计
declare -i TESTS_PASSED=0
declare -i TESTS_FAILED=0
declare -i TESTS_TOTAL=0

# 测试结果数组
declare -a TEST_RESULTS=()

# 记录测试结果
record_test_result() {
    local test_name=$1
    local status=$2
    local expected_code=$3
    local actual_code=$4
    local response=$5
    local error_msg=${6:-""}

    ((TESTS_TOTAL++))

    local result="{
  \"name\": \"$test_name\",
  \"status\": \"$status\",
  \"expected_code\": $expected_code,
  \"actual_code\": $actual_code,
  \"response\": $(echo "$response" | jq -c '.' 2>/dev/null || echo "\"$response\""),
  \"error_message\": \"$error_msg\",
  \"timestamp\": \"$(date -Iseconds)\"
}"

    TEST_RESULTS+=("$result")

    case $status in
        "PASSED")
            ((TESTS_PASSED++))
            log_success "✓ $test_name (HTTP $actual_code)"
            ;;
        "FAILED")
            ((TESTS_FAILED++))
            log_error "✗ $test_name (期望: $expected_code, 实际: $actual_code)"
            if [[ -n "$error_msg" ]]; then
                log_error "  错误: $error_msg"
            fi
            ;;
        "ERROR")
            ((TESTS_FAILED++))
            log_error "✗ $test_name - 请求错误: $error_msg"
            ;;
    esac
}

# 重置测试统计
reset_test_stats() {
    TESTS_PASSED=0
    TESTS_FAILED=0
    TESTS_TOTAL=0
    TEST_RESULTS=()
    log_debug "测试统计已重置"
}

# 打印测试摘要
print_test_summary() {
    print_separator
    log_info "测试摘要"
    print_separator

    if ((TESTS_TOTAL == 0)); then
        log_warning "没有执行任何测试"
        return 0
    fi

    local success_rate=$((TESTS_PASSED * 100 / TESTS_TOTAL))

    echo -e "${GREEN}通过: $TESTS_PASSED${NC}"
    echo -e "${RED}失败: $TESTS_FAILED${NC}"
    echo -e "${BLUE}总计: $TESTS_TOTAL${NC}"
    echo -e "${CYAN}成功率: $success_rate%${NC}"

    if ((TESTS_FAILED == 0)); then
        log_success "所有测试通过！"
        return 0
    else
        log_error "有 $TESTS_FAILED 个测试失败"
        return 1
    fi
}

# 生成测试报告
generate_test_report() {
    local report_file="${1:-${REPORT_DIR:-./reports}/api-test-$(date +%Y%m%d_%H%M%S).json}"

    ensure_directory "$(dirname "$report_file")"

    local report_content="{
  \"test_suite\": \"Order Management System API Tests\",
  \"generated_at\": \"$(date -Iseconds)\",
  \"environment\": {
    \"api_base_url\": \"$API_BASE\",
    \"backend_port\": ${BACKEND_PORT:-8081},
    \"test_user\": \"$TEST_USER_USERNAME\"
  },
  \"summary\": {
    \"total\": $TESTS_TOTAL,
    \"passed\": $TESTS_PASSED,
    \"failed\": $TESTS_FAILED,
    \"success_rate\": $((TESTS_PASSED * 100 / TESTS_TOTAL))
  },
  \"results\": [
"

    # 添加测试结果
    for ((i=0; i<${#TEST_RESULTS[@]}; i++)); do
        report_content+="${TEST_RESULTS[i]}"
        if ((i < ${#TEST_RESULTS[@]} - 1)); then
            report_content+=","
        fi
    done

    report_content+="  ]
}"

    echo "$report_content" > "$report_file"
    log_success "测试报告已生成: $report_file"
    echo "$report_file"
}

# =============================================================================
# 具体API测试函数
# =============================================================================

# 测试获取所有菜品
test_get_all_menus() {
    local test_name="获取所有菜品"
    local expected_code=200

    local response=$(make_request "GET" "/api/menu")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
        return 0
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# 测试获取推荐菜品
test_get_recommended_menus() {
    local test_name="获取推荐菜品"
    local expected_code=200

    local response=$(make_request "GET" "/api/menu/recommended")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
        return 0
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# 测试获取热销菜品
test_get_hot_sales_menus() {
    local test_name="获取热销菜品"
    local expected_code=200

    local response=$(make_request "GET" "/api/menu/hot-sales")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
        return 0
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# 测试获取所有分类
test_get_all_categories() {
    local test_name="获取所有分类"
    local expected_code=200

    local response=$(make_request "GET" "/api/categories")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
        return 0
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# 测试用户登录
test_user_login() {
    local test_name="用户登录"
    local expected_code=200

    local login_data="{\"username\":\"$TEST_USER_USERNAME\",\"password\":\"$TEST_USER_PASSWORD\"}"
    local response=$(make_request "POST" "/api/user/login" "$login_data")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        # 提取并保存token
        local token=$(echo "$response_body" | jq -r '.data.token' 2>/dev/null)
        if [[ -n "$token" && "$token" != "null" ]]; then
            set_auth_token "user" "$token"
            record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
            return 0
        else
            record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "无法提取认证令牌"
            return 1
        fi
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# 测试管理员登录
test_admin_login() {
    local test_name="管理员登录"
    local expected_code=200

    local login_data="{\"username\":\"$TEST_ADMIN_USERNAME\",\"password\":\"$TEST_ADMIN_PASSWORD\"}"
    local response=$(make_request "POST" "/api/admin/login" "$login_data")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        # 提取并保存token
        local token=$(echo "$response_body" | jq -r '.data.token' 2>/dev/null)
        if [[ -n "$token" && "$token" != "null" ]]; then
            set_auth_token "admin" "$token"
            record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
            return 0
        else
            record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "无法提取认证令牌"
            return 1
        fi
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# 测试用户注册
test_user_register() {
    local test_name="用户注册"
    local expected_code=200

    # 生成随机用户名避免冲突
    local random_username="testuser_$(date +%s)"
    local register_data="{\"username\":\"$random_username\",\"password\":\"testpass123\",\"email\":\"test@example.com\"}"

    local response=$(make_request "POST" "/api/user/register" "$register_data")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
        return 0
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# 测试获取用户信息（需要认证）
test_get_user_info() {
    local test_name="获取用户信息"
    local expected_code=200

    if [[ -z "$USER_TOKEN" ]]; then
        record_test_result "$test_name" "ERROR" "$expected_code" "0" "" "用户未登录"
        return 1
    fi

    local response=$(make_request "GET" "/api/user/info" "" "user")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
        return 0
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# 测试获取管理员菜单列表（需要管理员认证）
test_admin_get_menus() {
    local test_name="管理员获取菜单列表"
    local expected_code=200

    if [[ -z "$ADMIN_TOKEN" ]]; then
        record_test_result "$test_name" "ERROR" "$expected_code" "0" "" "管理员未登录"
        return 1
    fi

    local response=$(make_request "GET" "/admin/menu/items" "" "admin")
    local http_code=$(get_response_status "$response")
    local response_body=$(get_response_body "$response")

    if [[ "$http_code" == "$expected_code" ]]; then
        record_test_result "$test_name" "PASSED" "$expected_code" "$http_code" "$response_body"
        return 0
    else
        record_test_result "$test_name" "FAILED" "$expected_code" "$http_code" "$response_body" "HTTP状态码不匹配"
        return 1
    fi
}

# =============================================================================
# 测试套件
# =============================================================================

# 运行公开API测试
run_public_api_tests() {
    log_info "运行公开API测试..."
    print_separator

    test_get_all_menus
    test_get_recommended_menus
    test_get_hot_sales_menus
    test_get_all_categories

    print_separator
}

# 运行认证API测试
run_auth_api_tests() {
    log_info "运行认证API测试..."
    print_separator

    test_user_register
    test_user_login
    test_admin_login

    print_separator
}

# 运行需要认证的API测试
run_authenticated_api_tests() {
    log_info "运行需要认证的API测试..."
    print_separator

    test_get_user_info
    test_admin_get_menus

    print_separator
}

# 运行完整API测试套件
run_full_api_test_suite() {
    log_info "运行完整API测试套件..."
    print_separator

    reset_test_stats

    # 检查必要命令
    check_required_commands curl

    # 检查API服务是否可用
    if ! check_api_service_available; then
        log_error "API服务不可用，请先启动后端服务"
        return 1
    fi

    # 运行测试
    run_public_api_tests
    run_auth_api_tests
    run_authenticated_api_tests

    # 打印摘要
    print_test_summary
    local summary_result=$?

    # 生成报告
    generate_test_report

    return $summary_result
}

# 检查API服务可用性
check_api_service_available() {
    log_info "检查API服务可用性..."

    local response=$(make_request "GET" "/api/menu")
    local http_code=$(get_response_status "$response")

    if [[ "$http_code" == "200" ]]; then
        log_success "API服务可用"
        return 0
    else
        log_error "API服务不可用 (HTTP $http_code)"
        return 1
    fi
}

# =============================================================================
# 初始化和主函数
# =============================================================================

# 显示API测试帮助
show_api_test_help() {
    cat << EOF
API测试函数库使用方法:

基本测试:
  test_get_all_menus              # 测试获取所有菜品
  test_get_recommended_menus      # 测试获取推荐菜品
  test_get_hot_sales_menus        # 测试获取热销菜品
  test_get_all_categories         # 测试获取所有分类
  test_user_login                 # 测试用户登录
  test_admin_login                # 测试管理员登录
  test_user_register              # 测试用户注册
  test_get_user_info              # 测试获取用户信息
  test_admin_get_menus            # 测试管理员获取菜单

测试套件:
  run_public_api_tests            # 运行公开API测试
  run_auth_api_tests              # 运行认证API测试
  run_authenticated_api_tests    # 运行认证API测试
  run_full_api_test_suite         # 运行完整测试套件

工具函数:
  check_api_service_available     # 检查API服务可用性
  print_test_summary              # 打印测试摘要
  generate_test_report           # 生成测试报告

环境变量:
  BACKEND_PORT=8081             # 后端服务端口
  TEST_USER_USERNAME=testuser   # 测试用户名
  TEST_USER_PASSWORD=testpass123 # 测试用户密码
  TEST_ADMIN_USERNAME=admin     # 测试管理员用户名
  TEST_ADMIN_PASSWORD=admin     # 测试管理员密码
EOF
}

# 初始化API测试
init_api_test() {
    log_debug "初始化API测试函数库"
    log_debug "API基础URL: $API_BASE"

    # 检查必要命令
    if ! command -v curl &> /dev/null; then
        log_error "curl 命令未找到，请安装后再试"
        exit 1
    fi

    # 检查jq是否安装（用于JSON处理）
    if ! command -v jq &> /dev/null; then
        log_warning "jq 命令未找到，JSON格式化功能将不可用"
    fi
}

# 如果脚本被直接执行，则显示帮助
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    init_script
    init_api_test
    show_api_test_help
fi