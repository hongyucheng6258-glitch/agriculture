# ============================================
# 智慧渔业管理系统 - PowerShell部署脚本
# ============================================

# 配置
$ServerHost = "8.147.71.153"
$ServerUser = "root"
$ProjectRoot = "e:\作业\智能渔业系统"
$FrontendDir = "$ProjectRoot\frontend"
$BackendDir = "$ProjectRoot\backend"
$FrontendDistTar = "$ProjectRoot\frontend_dist.tar.gz"
$BackendJarName = "aquaculture-system-1.0.0.jar"
$ServerFrontendDir = "/www/wwwroot/default"
$ServerBackendDir = "/root/aquaculture"

# 颜色输出
function Write-Header($text) {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
}

function Write-Success($text) {
    Write-Host "✅ $text" -ForegroundColor Green
}

function Write-Error($text) {
    Write-Host "❌ $text" -ForegroundColor Red
}

function Write-Info($text) {
    Write-Host "ℹ️ $text" -ForegroundColor Yellow
}

# 步骤1: 构建前端
function Build-Frontend {
    Write-Header "步骤 1: 构建前端"
    
    Set-Location $FrontendDir
    
    # 检查node_modules
    if (-not (Test-Path "node_modules")) {
        Write-Info "安装前端依赖..."
        npm install
        if ($LASTEXITCODE -ne 0) {
            Write-Error "依赖安装失败"
            return $false
        }
        Write-Success "依赖安装成功"
    }
    
    # 构建
    Write-Info "构建前端..."
    npm run build
    if ($LASTEXITCODE -ne 0) {
        Write-Error "前端构建失败"
        return $false
    }
    
    # 打包
    Write-Info "打包前端文件..."
    if (Test-Path $FrontendDistTar) {
        Remove-Item $FrontendDistTar -Force
    }
    
    Set-Location "dist"
    tar -czf $FrontendDistTar .
    Set-Location ..
    
    Write-Success "前端构建完成"
    return $true
}

# 步骤2: 构建后端
function Build-Backend {
    Write-Header "步骤 2: 构建后端"
    
    Set-Location $BackendDir
    
    Write-Info "构建后端..."
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "后端构建失败"
        return $false
    }
    
    # 查找jar文件
    $jarFile = Get-ChildItem "target" -Filter "*.jar" | Where-Object { $_.Name -notlike "*original*" } | Select-Object -First 1
    if (-not $jarFile) {
        Write-Error "未找到JAR文件"
        return $false
    }
    
    # 复制
    Copy-Item $jarFile.FullName "$ProjectRoot\$BackendJarName" -Force
    
    Write-Success "后端构建完成"
    return $true
}

# 步骤3: 上传文件
function Upload-Files {
    Write-Header "步骤 3: 上传文件"
    
    # 上传前端
    Write-Info "上传前端..."
    scp -o StrictHostKeyChecking=no $FrontendDistTar "${ServerUser}@${ServerHost}:${ServerFrontendDir}/"
    if ($LASTEXITCODE -ne 0) {
        Write-Error "上传前端失败"
        return $false
    }
    
    # 上传后端
    Write-Info "上传后端..."
    scp -o StrictHostKeyChecking=no "$ProjectRoot\$BackendJarName" "${ServerUser}@${ServerHost}:${ServerBackendDir}/"
    if ($LASTEXITCODE -ne 0) {
        Write-Error "上传后端失败"
        return $false
    }
    
    Write-Success "文件上传完成"
    return $true
}

# 步骤4: 服务器部署
function Deploy-OnServer {
    Write-Header "步骤 4: 服务器部署"
    
    # 部署前端命令
    $deployFrontendCmd = @"
cd $ServerFrontendDir && \
tar -czf backup_\$(date +%Y%m%d_%H%M%S).tar.gz index.html css js img fonts 2>/dev/null || true && \
rm -rf index.html css js img fonts && \
tar -xzf frontend_dist.tar.gz && \
rm frontend_dist.tar.gz
"@
    
    Write-Info "部署前端..."
    ssh -o StrictHostKeyChecking=no ${ServerUser}@${ServerHost} $deployFrontendCmd
    if ($LASTEXITCODE -ne 0) {
        Write-Error "前端部署失败"
        return $false
    }
    
    # 部署后端命令
    $deployBackendCmd = @"
cd $ServerBackendDir && \
pkill -f '$BackendJarName' 2>/dev/null || true && \
mkdir -p data data/export && \
nohup java -jar $BackendJarName > app.log 2>&1 &
"@
    
    Write-Info "部署后端..."
    ssh -o StrictHostKeyChecking=no ${ServerUser}@${ServerHost} $deployBackendCmd
    if ($LASTEXITCODE -ne 0) {
        Write-Error "后端部署失败"
        return $false
    }
    
    Write-Success "服务器部署完成"
    return $true
}

# 主函数
function Main {
    Write-Header "智慧渔业管理系统 - PowerShell部署"
    Write-Info "服务器: $ServerHost"
    
    # 构建前端
    if (-not (Build-Frontend)) {
        return 1
    }
    
    # 构建后端
    if (-not (Build-Backend)) {
        return 1
    }
    
    # 上传文件
    if (-not (Upload-Files)) {
        return 1
    }
    
    # 服务器部署
    if (-not (Deploy-OnServer)) {
        return 1
    }
    
    # 完成
    Write-Header "部署完成"
    Write-Success "🎉 部署成功！"
    Write-Info "访问地址: http://${ServerHost}"
    Write-Info "默认账号: admin / 666666"
    
    return 0
}

# 执行
Set-Location $ProjectRoot
Main
