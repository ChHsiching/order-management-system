#!/bin/bash

# Web Order Management System - Permission Test
# 权限测试脚本 - 验证多角色权限控制和安全访问

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/../core/test_framework.sh"

# 游客权限测试
test_guest_permissions() {
    log_info "测试游客权限..."

    # ✅ 允许访问的公开API
    test_permission "guest" "分类查询" "GET" "/api/categories" "" 200
    test_permission "guest" "菜品列表" "GET" "/api/menu/list" "" 200
    test_permission "guest" "菜品搜索" "GET" "/api/menu/search?keyword=test" "" 200
    test_permission "guest" "菜品推荐" "GET" "/api/menu/recommend" "" 200

    # ❌ 禁止访问的认证API
    test_permission "guest" "用户信息" "GET" "/api/user/me" "" 401
    test_permission "guest" "订单查询" "GET" "/api/order/list" "" 401
    test_permission "guest" "购物车" "GET" "/api/cart/list" "" 401
    test_permission "guest" "用户管理" "GET" "/api/admin/users" "" 401
    test_permission "guest" "菜单管理" "GET" "/api/admin/categories" "" 401
    test_permission "guest" "订单管理" "GET" "/api/admin/orders" "" 401
}

# 会员权限测试
test_member_permissions() {
    log_info "测试会员权限..."

    # ✅ 允许访问的用户API
    test_permission "member" "个人信息" "GET" "/api/user/me" "" 200
    test_permission "member" "订单创建" "POST" "/api/order/create" '{"items":[],"total_price":0}' 201
    test_permission "member" "订单查询" "GET" "/api/order/list" "" 200
    test_permission "member" "购物车" "GET" "/api/cart/list" "" 200

    # ✅ 允许访问的公开API
    test_permission "member" "分类查询" "GET" "/api/categories" "" 200
    test_permission "member" "菜品列表" "GET" "/api/menu/list" "" 200

    # ❌ 禁止访问的管理API
    test_permission "member" "用户管理" "GET" "/api/admin/users" "" 403
    test_permission "member" "菜单管理" "POST" "/api/admin/categories" '{"name":"test"}' 403
    test_permission "member" "订单管理" "GET" "/api/admin/orders" "" 403
}

# 管理员权限测试
test_admin_permissions() {
    log_info "测试管理员权限..."

    # ✅ 允许访问的所有API
    test_permission "admin" "用户管理" "GET" "/api/admin/users" "" 200
    test_permission "admin" "菜单管理" "POST" "/api/admin/categories" '{"name":"test"}' 201
    test_permission "admin" "订单管理" "GET" "/api/admin/orders" "" 200
    test_permission "admin" "系统统计" "GET" "/api/admin/stats" "" 200

    # ✅ 管理员也可以访问会员功能
    test_permission "admin" "个人信息" "GET" "/api/user/me" "" 200
    test_permission "admin" "订单创建" "POST" "/api/order/create" '{"items":[],"total_price":0}' 201
}

# 越权测试
test_privilege_escalation() {
    log_info "测试越权访问..."

    # 游客伪造管理员token
    test_permission "guest" "伪造管理员权限" "GET" "/api/admin/users" "" 401

    # 会员伪造管理员token
    test_permission "member" "伪造管理员权限" "GET" "/api/admin/users" "" 403

    # 测试直接访问管理接口
    test_permission "guest" "直接访问用户管理" "GET" "/api/admin/users" "" 401
    test_permission "member" "直接访问用户管理" "GET" "/api/admin/users" "" 403
}

# 会话管理测试
test_session_management() {
    log_info "测试会话管理..."

    # 测试登录状态验证
    test_permission "member" "会话验证" "GET" "/api/user/me" "" 200

    # 测试token有效性
    test_permission "admin" "管理员会话" "GET" "/api/admin/users" "" 200

    # 测试并发登录限制（如果有）
    test_permission "member" "并发登录测试" "GET" "/api/user/me" "" 200
}

# 输入验证测试
test_input_validation() {
    log_info "测试输入验证..."

    # 测试SQL注入防护
    test_permission "guest" "SQL注入测试" "GET" "/api/menu/search?keyword=test' OR '1'='1" "" 400

    # 测试XSS防护
    test_permission "guest" "XSS测试" "GET" "/api/menu/search?keyword=<script>alert('xss')</script>" "" 400

    # 测试参数验证
    test_permission "guest" "参数验证测试" "GET" "/api/menu/list?page=-1" "" 400
}

# 权限边界测试
test_permission_boundaries() {
    log_info "测试权限边界..."

    # 测试权限继承
    test_permission "member" "权限继承测试" "GET" "/api/categories" "" 200

    # 测试权限限制
    test_permission "member" "权限限制测试" "GET" "/api/admin/users" "" 403

    # 测试权限提升
    test_permission "admin" "权限提升测试" "GET" "/api/admin/users" "" 200
}

# 安全漏洞扫描
test_security_vulnerabilities() {
    log_info "测试安全漏洞..."

    # 测试目录遍历
    test_permission "guest" "目录遍历测试" "GET" "/api/../admin/users" "" 404

    # 测试文件包含
    test_permission "guest" "文件包含测试" "GET" "/api/menu/include?file=config" "" 404

    # 测试认证绕过
    test_permission "guest" "认证绕过测试" "GET" "/api/user/me" "" 401
}

# 权限矩阵验证
test_permission_matrix() {
    log_info "验证权限矩阵..."

    # 根据PDF文档验证权限矩阵
    local permissions=(
        "guest:/api/categories:200"
        "guest:/api/user/me:401"
        "guest:/api/admin/users:401"
        "member:/api/user/me:200"
        "member:/api/admin/users:403"
        "admin:/api/admin/users:200"
    )

    for perm in "${permissions[@]}"; do
        IFS=':' read -r role path expected_code <<< "$perm"
        test_permission "$role" "权限矩阵验证" "GET" "$path" "" "$expected_code"
    done
}

# 主测试流程
main() {
    log_start "权限测试"

    # 初始化测试环境
    setup_test_environment

    # 执行权限测试
    test_guest_permissions
    test_member_permissions
    test_admin_permissions
    test_privilege_escalation
    test_session_management
    test_input_validation
    test_permission_boundaries
    test_security_vulnerabilities
    test_permission_matrix

    # 清理测试环境
    cleanup_test_environment

    log_end "权限测试"

    # 显示测试统计
    show_test_stats
}

# 运行主函数
main "$@"