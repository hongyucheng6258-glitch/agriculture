# 智慧渔业管理系统 - 环境恢复工具包

## 📁 文件夹结构

```
env-restore/
├── setup.sh                    # 一键环境恢复脚本
├── README.md                   # 本说明文件
└── tools/                      # 工具和依赖
    ├── zulu17.58.21-ca-jdk17.0.15-linux_x64/   # JDK 17 (325MB)
    ├── apache-maven-3.9.14/                    # Maven 3.9.14 (11MB)
    └── maven-repo/                             # Maven本地仓库 (89MB)
```

## 🚀 使用方法

每次环境重置后，执行一条命令即可恢复完整开发环境：

```bash
source /workspace/env-restore/setup.sh
```

## ✅ 脚本功能

| 步骤 | 功能 | 说明 |
|------|------|------|
| 1 | 设置环境变量 | JAVA_HOME、PATH、Maven配置 |
| 2 | 链接Maven仓库 | ~/.m2/repository → env-restore/tools/maven-repo |
| 3 | 编译后端 | 74个Java文件 + 资源文件 |
| 4 | 安装前端依赖 | npm install（如需要） |
| 5 | 启动后端 | 端口8080 |
| 6 | 启动前端 | 端口8081 |

## 📊 系统信息

- **后端**: http://localhost:8080
- **前端**: http://localhost:8081
- **账号**: admin / admin123

## ⚠️ 注意事项

1. 执行前请确保 8080 和 8081 端口未被占用
2. 如遇问题，检查日志文件：
   - 后端日志: `/tmp/backend.log`
   - 前端日志: `/tmp/frontend.log`

## 📦 总大小

约 425MB（JDK 325MB + Maven 11MB + Maven仓库 89MB）
