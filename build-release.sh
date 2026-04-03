#!/bin/bash
# ====================================================
# 提示词词库管理系统 - Mac 打包脚本
# 用法: bash build-release.sh
# ====================================================

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
BACKEND_DIR="$PROJECT_ROOT/backend"
RELEASE_DIR="$PROJECT_ROOT/release"
SCRIPTS_DIR="$PROJECT_ROOT/scripts"
STATIC_DIR="$BACKEND_DIR/src/main/resources/static"
MAC_RELEASE="$RELEASE_DIR/prompt-manage-mac"
JAR_NAME="prompt-manage-1.0.0.jar"

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info()  { echo -e "[INFO]  $1"; }
log_ok()    { echo -e "${GREEN}[OK]${NC}    $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

echo ""
echo "=============================="
echo "  提示词词库管理系统 - 打包"
echo "=============================="
echo ""

# 检查必要工具
command -v node >/dev/null 2>&1 || { log_error "需要安装 Node.js"; exit 1; }
command -v npm  >/dev/null 2>&1 || { log_error "需要安装 npm"; exit 1; }
command -v mvn  >/dev/null 2>&1 || { log_error "需要安装 Maven"; exit 1; }
command -v java >/dev/null 2>&1 || { log_error "需要安装 Java 17+"; exit 1; }

# ===================== 1. 前端构建 =====================
log_info "======= 第 1 步：构建前端 ======="

cd "$FRONTEND_DIR"

if [ ! -d "node_modules" ]; then
    log_info "安装前端依赖..."
    npm install
fi

log_info "执行 npm run build..."
npm run build

if [ ! -f "dist/index.html" ]; then
    log_error "前端构建失败：dist/index.html 不存在"
    exit 1
fi
log_ok "前端构建完成"

# ===================== 2. 复制前端到后端 static =====================
log_info "======= 第 2 步：复制前端到后端 static/ ======="

rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -r "$FRONTEND_DIR/dist/"* "$STATIC_DIR/"

log_ok "前端资源已复制到 backend/src/main/resources/static/"

# ===================== 3. 后端构建 =====================
log_info "======= 第 3 步：构建后端 JAR ======="

cd "$BACKEND_DIR"
log_info "执行 mvn clean package -DskipTests..."
mvn clean package -DskipTests -q

if [ ! -f "target/$JAR_NAME" ]; then
    log_error "后端构建失败：target/$JAR_NAME 不存在"
    exit 1
fi
log_ok "后端构建完成：$JAR_NAME ($(du -h "target/$JAR_NAME" | cut -f1))"

# ===================== 4. 组装 release =====================
log_info "======= 第 4 步：组装 Mac 发行包 ======="

# 保留已有的 jre 和 data 目录，只替换 JAR 和脚本
mkdir -p "$MAC_RELEASE/data"

# 复制新 JAR（覆盖旧的）
cp "$BACKEND_DIR/target/$JAR_NAME" "$MAC_RELEASE/"
log_ok "JAR 已更新"

# 如果没有 JRE 则用 jlink 裁剪
if [ ! -d "$MAC_RELEASE/jre" ]; then
    log_info "使用 jlink 裁剪 JRE..."
    local_java_home="${JAVA_HOME:-$(/usr/libexec/java_home 2>/dev/null || echo "")}"
    if [ -z "$local_java_home" ]; then
        log_error "未找到 JAVA_HOME，无法裁剪 JRE"
        exit 1
    fi
    "$local_java_home/bin/jlink" \
        --add-modules java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.logging,java.xml,java.compiler,jdk.unsupported,jdk.crypto.ec \
        --strip-debug --no-man-pages --no-header-files --compress=2 \
        --output "$MAC_RELEASE/jre"
    log_ok "JRE 裁剪完成 ($(du -sh "$MAC_RELEASE/jre" | cut -f1))"
else
    log_ok "JRE 已存在，跳过裁剪"
fi

# 复制启动脚本
cp "$SCRIPTS_DIR/start.sh" "$MAC_RELEASE/"
cp "$SCRIPTS_DIR/start.command" "$MAC_RELEASE/" 2>/dev/null || true
chmod +x "$MAC_RELEASE/start.sh"
chmod +x "$MAC_RELEASE/start.command" 2>/dev/null || true
chmod +x "$MAC_RELEASE/jre/bin/java"

log_ok "启动脚本已更新"

echo ""
echo "=============================="
log_ok "打包完成！"
echo ""
echo "  位置: $MAC_RELEASE"
echo "  启动: cd $MAC_RELEASE && bash start.sh"
echo ""
echo "=============================="
