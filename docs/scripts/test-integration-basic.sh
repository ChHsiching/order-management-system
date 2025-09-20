#!/bin/bash

# =============================================================================
# 订餐管理系统 - 前后端联调测试脚本
# Frontend-Backend Integration Test Script
# =============================================================================

# 加载通用工具函数
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

# 初始化脚本
init_script

# =============================================================================
# 测试配置
# =============================================================================

# API配置
readonly API_BASE_URL="http://localhost:8080/WebOrderSystem/api"
readonly FRONTEND_URL="http://localhost:5173"

# 测试用户数据
readonly TEST_USER="testuser_$(date +%s)"
readonly TEST_PASSWORD="testpass123"
readonly TEST_EMAIL="test@example.com"

# =============================================================================
# 测试函数
# =============================================================================

# 测试公开API访问
test_public_apis() {
    log_info "测试公开API访问..."

    # 测试菜单API
    local menu_response=$(curl -s "$API_BASE_URL/menu")
    if [[ $? -eq 0 && "$menu_response" == *"\"code\":0"* ]]; then
        log_success "✓ 菜单API访问正常"
    else
        log_error "✗ 菜单API访问失败"
        return 1
    fi

    # 测试分类API
    local categories_response=$(curl -s "$API_BASE_URL/categories")
    if [[ $? -eq 0 && "$categories_response" == *"["* ]]; then
        log_success "✓ 分类API访问正常"
    else
        log_error "✗ 分类API访问失败"
        return 1
    fi

    return 0
}

# 测试前端页面访问
test_frontend_access() {
    log_info "测试前端页面访问..."

    # 测试前台页面
    local frontdesk_response=$(curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_URL")
    if [[ "$frontdesk_response" == "200" ]]; then
        log_success "✓ 前台页面访问正常"
    else
        log_error "✗ 前台页面访问失败 (HTTP $frontdesk_response)"
        return 1
    fi

    # 测试后台页面
    local backdesk_response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:5174")
    if [[ "$backdesk_response" == "200" ]]; then
        log_success "✓ 后台页面访问正常"
    else
        log_error "✗ 后台页面访问失败 (HTTP $backdesk_response)"
        return 1
    fi

    return 0
}

# 测试CORS配置
test_cors_config() {
    log_info "测试CORS配置..."

    # 测试OPTIONS预检请求
    local options_response=$(curl -s -X OPTIONS "$API_BASE_URL/menu" \
        -H "Origin: $FRONTEND_URL" \
        -H "Access-Control-Request-Method: GET" \
        -H "Access-Control-Request-Headers: Content-Type" \
        -o /dev/null -w "%{http_code}")

    if [[ "$options_response" == "200" || "$options_response" == "204" ]]; then
        log_success "✓ CORS预检请求正常"
    else
        log_error "✗ CORS预检请求失败 (HTTP $options_response)"
        return 1
    fi

    # 测试实际请求带Origin头
    local cors_response=$(curl -s "$API_BASE_URL/menu" \
        -H "Origin: $FRONTEND_URL" \
        -o /dev/null -w "%{http_code}")

    if [[ "$cors_response" == "200" ]]; then
        log_success "✓ CORS跨域请求正常"
    else
        log_error "✗ CORS跨域请求失败 (HTTP $cors_response)"
        return 1
    fi

    return 0
}

# 测试数据格式
test_data_format() {
    log_info "测试数据格式..."

    # 测试菜单数据格式
    local menu_data=$(curl -s "$API_BASE_URL/menu")
    local menu_count=$(echo "$menu_data" | jq '.data | length' 2>/dev/null || echo "0")

    if [[ "$menu_count" -gt 0 ]]; then
        log_success "✓ 菜单数据格式正确，共 $menu_count 个菜品"

        # 检查必要字段
        local has_id=$(echo "$menu_data" | jq '.data[0] | has("id")' 2>/dev/null)
        local has_name=$(echo "$menu_data" | jq '.data[0] | has("name")' 2>/dev/null)
        local has_price=$(echo "$menu_data" | jq '.data[0] | has("price")' 2>/dev/null)

        if [[ "$has_id" == "true" && "$has_name" == "true" ]]; then
            log_success "✓ 菜单数据字段完整"
        else
            log_warning "⚠ 菜单数据字段可能不完整"
        fi
    else
        log_error "✗ 菜单数据格式错误或为空"
        return 1
    fi

    return 0
}

# =============================================================================
# 测试套件
# =============================================================================

# 运行基础集成测试
run_basic_integration_test() {
    log_info "运行基础前后端集成测试..."
    print_separator

    local tests_passed=0
    local tests_failed=0

    # 测试公开API
    if test_public_apis; then
        ((tests_passed++))
    else
        ((tests_failed++))
    fi

    # 测试前端访问
    if test_frontend_access; then
        ((tests_passed++))
    else
        ((tests_failed++))
    fi

    # 测试CORS配置
    if test_cors_config; then
        ((tests_passed++))
    else
        ((tests_failed++))
    fi

    # 测试数据格式
    if test_data_format; then
        ((tests_passed++))
    else
        ((tests_failed++))
    fi

    # 输出测试结果
    print_separator
    log_info "基础集成测试结果:"
    log_info "通过: $tests_passed, 失败: $tests_failed"

    if [[ $tests_failed -eq 0 ]]; then
        log_success "✓ 所有基础集成测试通过"
        return 0
    else
        log_error "✗ 有 $tests_failed 个测试失败"
        return 1
    fi
}

# =============================================================================
# 主函数
# =============================================================================

# 显示帮助信息
show_help() {
    cat << EOF
订餐管理系统 - 前后端联调测试脚本

使用方法:
  $0 [选项]

选项:
  --basic               运行基础集成测试
  --help                显示此帮助信息

示例:
  $0 --basic            # 运行基础集成测试
EOF
}

# 解析命令行参数
case "${1:-}" in
    --basic)
        run_basic_integration_test
        ;;
    --help|"")
        show_help
        ;;
    *)
        log_error "未知选项: $1"
        show_help
        exit 1
        ;;
esac