#!/bin/bash
# ============================================================
# 智慧渔业管理系统 - 一键环境恢复脚本
# 使用方法: source /workspace/env-restore/setup.sh
# 说明: 环境重置后执行此脚本，自动恢复JDK、Maven、Maven仓库，
#       编译后端，安装前端依赖，并启动前后端服务。
# ============================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

WORKSPACE="/workspace"
ENV_RESTORE_DIR="$WORKSPACE/env-restore"
TOOLS_DIR="$ENV_RESTORE_DIR/tools"
JDK_HOME="$TOOLS_DIR/zulu17.58.21-ca-jdk17.0.15-linux_x64"
MAVEN_HOME="$TOOLS_DIR/apache-maven-3.9.14"
MAVEN_REPO="$TOOLS_DIR/maven-repo"
BACKEND_DIR="$WORKSPACE/backend"
FRONTEND_DIR="$WORKSPACE/frontend"

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ============================================================
# 步骤1: 设置环境变量
# ============================================================
log_info "===== 步骤1/6: 设置环境变量 ====="
export JAVA_HOME="$JDK_HOME"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
export MAVEN_REPO="$MAVEN_REPO"

# 配置Maven使用本地仓库
mkdir -p ~/.m2
if [ ! -f ~/.m2/settings.xml ]; then
    # 使用workspace中的Maven配置（已含阿里云镜像）
    cp "$MAVEN_HOME/conf/settings.xml" ~/.m2/settings.xml
fi

# 创建或更新Maven settings指向本地仓库
cat > ~/.m2/settings.xml << 'MAVEN_SETTINGS'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
  <localRepository>WORKSPACE_MAVEN_REPO_PLACEHOLDER</localRepository>
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
  <profiles>
    <profile>
      <id>jdk17</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
      </properties>
    </profile>
  </profiles>
</settings>
MAVEN_SETTINGS

# 替换本地仓库路径
sed -i "s|WORKSPACE_MAVEN_REPO_PLACEHOLDER|$MAVEN_REPO|g" ~/.m2/settings.xml

java --version 2>&1 | head -1
mvn --version 2>&1 | head -1

# ============================================================
# 步骤2: 链接Maven仓库
# ============================================================
log_info "===== 步骤2/6: 链接Maven本地仓库 ====="
if [ ! -d ~/.m2/repository ] || [ -L ~/.m2/repository ]; then
    rm -rf ~/.m2/repository 2>/dev/null || true
    ln -sf "$MAVEN_REPO" ~/.m2/repository
    log_info "Maven仓库已链接: ~/.m2/repository -> $MAVEN_REPO"
else
    log_warn "~/.m2/repository 已存在且非符号链接，跳过"
fi

# ============================================================
# 步骤3: 编译后端
# ============================================================
log_info "===== 步骤3/6: 编译后端 ====="
cd "$BACKEND_DIR"

# 清理旧的class文件
rm -rf target/classes 2>/dev/null || true
mkdir -p target/classes

# 复制资源文件（application.yml、mapper XML、init.sql等）
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* target/classes/ 2>/dev/null || true
    log_info "资源文件已复制到 target/classes"
fi

# 构建classpath
CLASSPATH=""
for jar in $(find "$MAVEN_REPO" -name "*.jar" -not -name "*sources*" -not -name "*javadoc*" 2>/dev/null); do
    case "$jar" in
        *org.eclipse.sisu.equinox*|*commons-io-2.6*|*plexus-container-default-2.1.1*) continue ;;
        *3.5.3.1*) continue ;;
    esac
    CLASSPATH="$CLASSPATH:$jar"
done
CLASSPATH="$CLASSPATH:target/classes"

# 编译所有Java源文件
find src/main/java -name "*.java" > /tmp/sources.txt
CLASS_COUNT=$(wc -l < /tmp/sources.txt)
log_info "编译 $CLASS_COUNT 个Java源文件..."

javac -processor lombok.launch.AnnotationProcessorHider\$AnnotationProcessor \
      -cp "$CLASSPATH" -d target/classes -encoding UTF-8 @/tmp/sources.txt 2>&1

COMPILED=$(find target/classes -name "*.class" | wc -l)
log_info "编译完成: $COMPILED 个class文件"

# ============================================================
# 步骤4: 安装前端依赖
# ============================================================
log_info "===== 步骤4/6: 安装前端依赖 ====="
cd "$FRONTEND_DIR"
if [ ! -d "node_modules" ] || [ ! -d "node_modules/vue-cli-service" ]; then
    npm install --prefer-offline 2>&1 | tail -3
    log_info "前端依赖安装完成"
else
    log_info "node_modules 已存在，跳过安装"
fi

# ============================================================
# 步骤5: 启动后端
# ============================================================
log_info "===== 步骤5/6: 启动后端 ====="
cd "$BACKEND_DIR"

# 检查8080端口是否被占用
if lsof -i :8080 > /dev/null 2>&1 || ss -tlnp | grep -q ':8080 '; then
    log_warn "端口8080已被占用，跳过后端启动"
else
    # 确保数据库目录存在
    mkdir -p "$BACKEND_DIR/data"

    # 后台启动后端（使用子shell确保工作目录正确）
    (cd "$BACKEND_DIR" && nohup java -cp "$CLASSPATH" com.aquaculture.AquacultureApplication > /tmp/backend.log 2>&1 &)
    BACKEND_PID=$!
    log_info "后端启动中... PID=$BACKEND_PID (工作目录: $BACKEND_DIR)"

    # 等待后端启动
    for i in $(seq 1 20); do
        sleep 2
        if curl -s http://localhost:8080/api/auth/login > /dev/null 2>&1; then
            log_info "后端启动成功! (端口8080)"
            break
        fi
        if [ $i -eq 20 ]; then
            log_error "后端启动超时，请检查 /tmp/backend.log"
        fi
    done
fi

# ============================================================
# 步骤6: 启动前端
# ============================================================
log_info "===== 步骤6/6: 启动前端 ====="
cd "$FRONTEND_DIR"

if lsof -i :8081 > /dev/null 2>&1 || ss -tlnp | grep -q ':8081 '; then
    log_warn "端口8081已被占用，跳过前端启动"
else
    nohup npx vue-cli-service serve > /tmp/frontend.log 2>&1 &
    FRONTEND_PID=$!
    log_info "前端启动中... PID=$FRONTEND_PID"

    # 等待前端启动
    for i in $(seq 1 20); do
        sleep 2
        if curl -s http://localhost:8081 > /dev/null 2>&1; then
            log_info "前端启动成功! (端口8081)"
            break
        fi
        if [ $i -eq 20 ]; then
            log_error "前端启动超时，请检查 /tmp/frontend.log"
        fi
    done
fi

# ============================================================
# 完成
# ============================================================
echo ""
log_info "=========================================="
log_info "  环境恢复完成!"
log_info "  后端: http://localhost:8080"
log_info "  前端: http://localhost:8081"
log_info "  账号: admin / admin123"
log_info "=========================================="
echo ""
log_info "提示: 后续新会话中只需执行:"
log_info "  source /workspace/env-restore/setup.sh"
