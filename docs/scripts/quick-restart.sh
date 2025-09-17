#!/bin/bash

# ============================================================================
# Web订餐管理系统 - 快速重启脚本
# 功能：快速重启后端服务（跳过编译）
# 使用方法：./quick-restart.sh [dev|prod] 默认dev环境
# ============================================================================

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
PORT=8080
PROFILE=${1:-dev}

echo "🚀 快速重启后端服务..."

# 停止旧服务
echo "🛑 停止旧服务..."
pkill -f "spring-boot.*BackendApplication" || true
sleep 2

# 强制停止占用端口的进程
if command -v lsof >/dev/null 2>&1; then
    lsof -i :$PORT | awk 'NR>1{print $2}' | xargs -r kill -9 || true
fi

sleep 1

# 清理日志
if [ -f "$BACKEND_DIR/app-test.log" ]; then
    rm -f "$BACKEND_DIR/app-test.log"
fi

# 启动服务
echo "🔄 启动后端服务（环境：$PROFILE）..."
cd "$BACKEND_DIR"
nohup mvn spring-boot:run -Dspring-boot.run.profiles="$PROFILE" -q > app-test.log 2>&1 &

# 等待启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
if curl -s -f "http://localhost:$PORT/WebOrderSystem/actuator/health" >/dev/null 2>&1; then
    echo "✅ 后端服务启动成功！"
    echo "📝 服务地址: http://localhost:$PORT/WebOrderSystem"
    echo "📚 Swagger文档: http://localhost:$PORT/WebOrderSystem/swagger-ui.html"
    echo "📊 健康检查: http://localhost:$PORT/WebOrderSystem/actuator/health"
    echo "📋 测试命令:"
    echo "   公开接口: curl -s http://localhost:$PORT/WebOrderSystem/api/categories"
    echo "   后台接口: curl -u admin:admin -s http://localhost:$PORT/WebOrderSystem/admin/menu/categories"
else
    echo "❌ 后端服务启动失败！"
    echo "📄 请检查日志: tail -f $BACKEND_DIR/app-test.log"
    exit 1
fi