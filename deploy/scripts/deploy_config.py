# ============================================
# 部署配置文件
# ============================================

# 服务器配置
SERVER_CONFIG = {
    # 服务器IP地址
    "host": "8.147.71.153",
    
    # SSH用户名
    "username": "root",
    
    # SSH端口（默认22）
    "port": 22,
    
    # SSH密码（可选，如果使用密钥可留空）
    "password": "",
    
    # SSH私钥路径（可选）
    "private_key_path": ""
}

# 路径配置
PATH_CONFIG = {
    # 本地项目根目录
    "local_project_root": "e:\\作业\\智能渔业系统",
    
    # 本地前端目录
    "local_frontend_dir": "e:\\作业\\智能渔业系统\\frontend",
    
    # 本地后端目录
    "local_backend_dir": "e:\\作业\\智能渔业系统\\backend",
    
    # 服务器前端部署目录
    "server_frontend_dir": "/www/wwwroot/default",
    
    # 服务器后端部署目录
    "server_backend_dir": "/root/aquaculture",
    
    # 后端JAR文件名
    "backend_jar_name": "aquaculture-system-1.0.0.jar"
}

# 构建配置
BUILD_CONFIG = {
    # 是否跳过前端构建
    "skip_frontend_build": False,
    
    # 是否跳过后端构建
    "skip_backend_build": False,
    
    # 是否跳过测试
    "skip_tests": True,
    
    # Maven打包额外参数
    "maven_args": []
}

# 部署选项
DEPLOY_OPTIONS = {
    # 是否备份当前版本
    "backup_before_deploy": True,
    
    # 备份保留天数
    "backup_retention_days": 7,
    
    # 是否自动启动服务
    "auto_start_service": True,
    
    # 部署后等待时间（秒）
    "wait_after_deploy": 30,
    
    # 是否验证部署
    "verify_after_deploy": True
}

# ============================================
# 自定义配置（在此处修改）
# ============================================
# 如果您的服务器信息不同，请修改上面的 SERVER_CONFIG
# 例如：
# SERVER_CONFIG["host"] = "your-server-ip"
# SERVER_CONFIG["username"] = "your-username"
# SERVER_CONFIG["password"] = "your-password"
