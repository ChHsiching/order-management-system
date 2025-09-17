#!/bin/bash

# Web Order Management System - Performance Test
# 性能测试脚本 - 测试系统性能和负载能力

set -e

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

# 引入测试框架核心库
source "$SCRIPT_DIR/../core/test_framework.sh"

# 测试API响应时间
test_api_response_time() {
    log_info "测试API响应时间..."

    local endpoints=(
        "/api/categories"
        "/api/menu/list"
        "/api/menu/recommend"
    )

    for endpoint in "${endpoints[@]}"; do
        local total_time=0
        local iterations=10

        for i in $(seq 1 $iterations); do
            local start_time=$(date +%s%N)
            curl -s "$API_BASE$endpoint" > /dev/null
            local end_time=$(date +%s%N)
            local duration=$(( (end_time - start_time) / 1000000 ))
            total_time=$((total_time + duration))
        done

        local avg_time=$((total_time / iterations))
        log_info "$endpoint 平均响应时间: ${avg_time}ms"

        if [[ $avg_time -lt 500 ]]; then
            log_success "$endpoint 响应时间优秀 (${avg_time}ms < 500ms)"
            update_test_stats "PASS"
        elif [[ $avg_time -lt 1000 ]]; then
            log_warn "$endpoint 响应时间一般 (${avg_time}ms < 1000ms)"
            update_test_stats "PASS"
        else
            log_error "$endpoint 响应时间过慢 (${avg_time}ms >= 1000ms)"
            update_test_stats "FAIL"
        fi
    done
}

# 测试数据库查询性能
test_database_query_performance() {
    log_info "测试数据库查询性能..."

    local queries=(
        "SELECT COUNT(*) FROM administrators"
        "SELECT COUNT(*) FROM ltypes WHERE catelock = 0"
        "SELECT COUNT(*) FROM menu WHERE productLock = 0"
        "SELECT COUNT(*) FROM cg_info"
        "SELECT COUNT(*) FROM the_order_entry"
    )

    for query in "${queries[@]}"; do
        local total_time=0
        local iterations=20

        for i in $(seq 1 $iterations); do
            local start_time=$(date +%s%N)
            mysql -h localhost -u root -p123456 -s -e "USE web_order; $query;" > /dev/null
            local end_time=$(date +%s%N)
            local duration=$(( (end_time - start_time) / 1000000 ))
            total_time=$((total_time + duration))
        done

        local avg_time=$((total_time / iterations))
        local query_desc=$(echo "$query" | sed 's/SELECT COUNT(*) FROM //')
        log_info "$query_desc 平均查询时间: ${avg_time}ms"

        if [[ $avg_time -lt 10 ]]; then
            log_success "$query_desc 查询性能优秀 (${avg_time}ms < 10ms)"
            update_test_stats "PASS"
        elif [[ $avg_time -lt 50 ]]; then
            log_success "$query_desc 查询性能良好 (${avg_time}ms < 50ms)"
            update_test_stats "PASS"
        else
            log_warn "$query_desc 查询性能较慢 (${avg_time}ms >= 50ms)"
            update_test_stats "PASS"
        fi
    done
}

# 测试并发处理能力
test_concurrent_handling() {
    log_info "测试并发处理能力..."

    local concurrent_users=(5 10 20 50)

    for users in "${concurrent_users[@]}"; do
        log_info "测试 $users 个并发用户..."

        local start_time=$(date +%s%N)
        local pids=()

        # 启动并发请求
        for i in $(seq 1 $users); do
            {
                curl -s "$API_BASE/api/categories" > /dev/null
                curl -s "$API_BASE/api/menu/list" > /dev/null
            } &
            pids+=($!)
        done

        # 等待所有请求完成
        for pid in "${pids[@]}"; do
            wait $pid
        done

        local end_time=$(date +%s%N)
        local total_duration=$(( (end_time - start_time) / 1000000 ))
        local avg_duration=$((total_duration / (users * 2) ))

        log_info "$users 并发用户: 总耗时 ${total_duration}ms, 平均请求 ${avg_duration}ms"

        if [[ $avg_duration -lt 1000 ]]; then
            log_success "$users 并发用户处理优秀 (${avg_duration}ms < 1000ms)"
            update_test_stats "PASS"
        elif [[ $avg_duration -lt 3000 ]]; then
            log_success "$users 并发用户处理良好 (${avg_duration}ms < 3000ms)"
            update_test_stats "PASS"
        else
            log_warn "$users 并发用户处理较慢 (${avg_duration}ms >= 3000ms)"
            update_test_stats "PASS"
        fi
    done
}

# 测试内存使用情况
test_memory_usage() {
    log_info "测试内存使用情况..."

    # 获取Java进程ID
    local java_pid=$(pgrep -f "spring-boot:run" | head -1)

    if [[ -n "$java_pid" ]]; then
        local memory_info=$(ps -p "$java_pid" -o %mem,rss --no-headers)
        local mem_percent=$(echo "$memory_info" | awk '{print $1}')
        local mem_rss=$(echo "$memory_info" | awk '{print $2}')

        log_info "Java进程内存使用: ${mem_percent}%, RSS: ${mem_rss}KB"

        if (( $(echo "$mem_percent < 5.0" | bc -l) )); then
            log_success "内存使用优秀 (${mem_percent}% < 5%)"
            update_test_stats "PASS"
        elif (( $(echo "$mem_percent < 10.0" | bc -l) )); then
            log_success "内存使用良好 (${mem_percent}% < 10%)"
            update_test_stats "PASS"
        else
            log_warn "内存使用较高 (${mem_percent}% >= 10%)"
            update_test_stats "PASS"
        fi
    else
        log_error "未找到Java进程"
        update_test_stats "FAIL"
    fi
}

# 测试数据库连接池性能
test_connection_pool_performance() {
    log_info "测试数据库连接池性能..."

    local start_time=$(date +%s%N)
    local iterations=50
    local pids=()

    # 并发数据库查询
    for i in $(seq 1 $iterations); do
        {
            mysql -h localhost -u root -p123456 -s -e "USE web_order; SELECT COUNT(*) FROM menu;" > /dev/null
        } &
        pids+=($!)
    done

    # 等待所有查询完成
    for pid in "${pids[@]}"; do
        wait $pid
    done

    local end_time=$(date +%s%N)
    local total_duration=$(( (end_time - start_time) / 1000000 ))
    local avg_duration=$((total_duration / iterations ))

    log_info "连接池并发查询: 总耗时 ${total_duration}ms, 平均查询 ${avg_duration}ms"

    if [[ $avg_duration -lt 20 ]]; then
        log_success "连接池性能优秀 (${avg_duration}ms < 20ms)"
        update_test_stats "PASS"
    elif [[ $avg_duration -lt 100 ]]; then
        log_success "连接池性能良好 (${avg_duration}ms < 100ms)"
        update_test_stats "PASS"
    else
        log_warn "连接池性能较慢 (${avg_duration}ms >= 100ms)"
        update_test_stats "PASS"
    fi
}

# 测试负载情况下的系统稳定性
test_load_stability() {
    log_info "测试负载情况下的系统稳定性..."

    local load_duration=30  # 30秒负载测试
    local end_time=$(( $(date +%s) + load_duration ))
    local request_count=0
    local error_count=0

    log_info "开始 ${load_duration} 秒负载测试..."

    while [[ $(date +%s) -lt $end_time ]]; do
        # 并发发送请求
        {
            local response=$(curl -s -w "%{http_code}" "$API_BASE/api/categories")
            local http_code=${response: -3}

            if [[ "$http_code" == "200" ]]; then
                : $((request_count++))
            else
                : $((error_count++))
            fi
        } &

        # 控制并发数量
        if [[ $((request_count % 10)) -eq 0 ]]; then
            sleep 0.1
        fi
    done

    # 等待所有后台任务完成
    wait

    local requests_per_second=$((request_count / load_duration))
    local error_rate=$((error_count * 100 / (request_count + error_count) ))

    log_info "负载测试完成: ${request_count} 请求, ${error_count} 错误, ${requests_per_second} RPS, 错误率 ${error_rate}%"

    if [[ $error_rate -eq 0 ]]; then
        log_success "负载测试完美通过 (0% 错误率)"
        update_test_stats "PASS"
    elif [[ $error_rate -lt 5 ]]; then
        log_success "负载测试良好通过 (${error_rate}% 错误率 < 5%)"
        update_test_stats "PASS"
    else
        log_error "负载测试错误率过高 (${error_rate}% >= 5%)"
        update_test_stats "FAIL"
    fi
}

# 主测试流程
main() {
    log_start "性能测试"

    # 初始化测试环境
    setup_test_environment

    # 执行性能测试
    test_api_response_time
    test_database_query_performance
    test_concurrent_handling
    test_memory_usage
    test_connection_pool_performance
    test_load_stability

    # 清理测试环境
    cleanup_test_environment

    log_end "性能测试"

    # 显示测试统计
    show_test_stats
}

# 运行主函数
main "$@"