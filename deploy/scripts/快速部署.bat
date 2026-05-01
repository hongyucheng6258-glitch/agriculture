@echo off
chcp 65001 >nul
echo ========================================
echo   智慧渔业管理系统 - 快速部署脚本
echo ========================================
echo.

REM 配置
set SERVER_HOST=8.147.71.153
set SERVER_USER=root
set PROJECT_ROOT=e:\作业\智能渔业系统
set FRONTEND_DIR=%PROJECT_ROOT%\frontend
set BACKEND_DIR=%PROJECT_ROOT%\backend
set JAR_NAME=aquaculture-system-1.0.0.jar
set SERVER_FRONTEND=/www/wwwroot/default
set SERVER_BACKEND=/root/aquaculture

echo [1/5] 构建前端...
cd /d %FRONTEND_DIR%
if not exist node_modules (
    echo 安装前端依赖...
    call npm install
)
call npm run build
if errorlevel 1 (
    echo ❌ 前端构建失败
    pause
    exit /b 1
)

echo [2/5] 打包前端...
cd dist
tar -czf %PROJECT_ROOT%\frontend_dist.tar.gz .
cd ..

echo [3/5] 构建后端...
cd /d %BACKEND_DIR%
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ❌ 后端构建失败
    pause
    exit /b 1
)

REM 复制JAR文件
for %%f in (target\*.jar) do (
    echo %%f | findstr /i "original" >nul || (
        copy "%%f" "%PROJECT_ROOT%\%JAR_NAME%" /Y
    )
)

echo [4/5] 上传文件...
cd /d %PROJECT_ROOT%

REM 上传前端
scp -o StrictHostKeyChecking=no frontend_dist.tar.gz %SERVER_USER%@%SERVER_HOST%:%SERVER_FRONTEND%/
if errorlevel 1 (
    echo ❌ 前端上传失败
    pause
    exit /b 1
)

REM 上传后端
scp -o StrictHostKeyChecking=no %JAR_NAME% %SERVER_USER%@%SERVER_HOST%:%SERVER_BACKEND%/
if errorlevel 1 (
    echo ❌ 后端上传失败
    pause
    exit /b 1
)

echo [5/5] 服务器部署...
REM 部署前端
ssh -o StrictHostKeyChecking=no %SERVER_USER%@%SERVER_HOST% "cd %SERVER_FRONTEND% && tar -czf backup_$(date +%%Y%%m%%d_%%H%%M%%S).tar.gz index.html css js img fonts 2>/dev/null || true && rm -rf index.html css js img fonts && tar -xzf frontend_dist.tar.gz && rm frontend_dist.tar.gz"

REM 部署后端
ssh -o StrictHostKeyChecking=no %SERVER_USER%@%SERVER_HOST% "cd %SERVER_BACKEND% && pkill -f '%JAR_NAME%' 2>/dev/null || true && mkdir -p data data/export && nohup java -jar %JAR_NAME% > app.log 2>&1 &"

echo.
echo ========================================
echo   ✅ 部署完成！
echo ========================================
echo.
echo 访问地址: http://%SERVER_HOST%
echo 默认账号: admin / 666666
echo.
pause
