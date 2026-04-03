#!/bin/bash
# ====================================================
# 提示词词库管理系统 - 启动脚本 (Mac/Linux)
# 双击 start.command 或在终端执行 bash start.sh
# ====================================================

cd "$(dirname "$0")"

# 检查端口占用
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  端口 8080 已被占用，请先关闭占用该端口的程序"
    echo "   或执行: kill \$(lsof -t -i:8080)"
    read -p "按回车退出..."
    exit 1
fi

echo "🚀 正在启动提示词词库管理系统..."
echo "   数据库文件将保存在 ./data/ 目录"
echo ""

# 使用内嵌JRE启动
./jre/bin/java -jar prompt-manage-1.0.0.jar &
SERVER_PID=$!

# 等待服务启动
echo "⏳ 等待服务启动..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
        echo "✅ 服务已启动！"
        echo "📖 浏览器访问: http://localhost:8080"
        echo ""
        echo "按 Ctrl+C 停止服务"

        # 自动打开浏览器
        if command -v open &> /dev/null; then
            open http://localhost:8080
        elif command -v xdg-open &> /dev/null; then
            xdg-open http://localhost:8080
        fi

        # 等待进程结束
        wait $SERVER_PID
        exit 0
    fi
    sleep 1
done

echo "❌ 服务启动超时，请检查日志"
kill $SERVER_PID 2>/dev/null
read -p "按回车退出..."
exit 1
