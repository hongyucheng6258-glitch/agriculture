# 智慧渔业管理系统 - 部署包

## 📁 目录结构

```
deploy/
├── README.md                          # 本文件（快速开始）
├── 部署说明.md                        # 详细部署文档
├── scripts/                           # 部署脚本
│   ├── deploy_config.py              # Python部署配置
│   ├── deploy_full.py                # Python完整部署脚本
│   ├── deploy.ps1                    # PowerShell部署脚本
│   └── 快速部署.bat                  # Windows批处理脚本
├── config/                           # 配置文件
│   ├── application.yml               # 后端配置
│   └── nginx.conf                    # Nginx配置
└── db/                               # 数据库
    └── init.sql                      # 数据库初始化脚本
```

## 🚀 快速开始

### 方式一：Windows批处理（最简单）

双击运行：
```
scripts\快速部署.bat
```

### 方式二：Python脚本（推荐）

```bash
cd deploy/scripts
python deploy_full.py
```

### 方式三：PowerShell脚本

```powershell
cd deploy/scripts
.\deploy.ps1
```

## 📝 前置要求

- Node.js 14+
- Maven 3.6+
- Java 11+
- SSH访问权限
- 服务器已安装Nginx和Java

## 🔧 配置修改

如果您的服务器信息不同，请修改：

1. **Python**: 编辑 `deploy_config.py`
2. **PowerShell**: 编辑 `deploy.ps1`
3. **批处理**: 编辑 `快速部署.bat`

修改以下变量：
- `SERVER_HOST` - 服务器IP
- `SERVER_USER` - SSH用户名

## 🌐 访问信息

部署成功后：

- **访问地址**: http://8.147.71.153
- **管理员账号**: admin / 666666
- **操作员账号**: operator / 666666

## 📖 详细文档

请查看 [部署说明.md](./部署说明.md) 了解：
- 完整部署流程
- 手动部署步骤
- 故障排查
- 常用命令
- 安全建议

## 💡 常见问题

**Q: 部署脚本执行失败怎么办？**  
A: 查看详细文档的手动部署步骤，一步步操作。

**Q: 部署后无法访问？**  
A: 检查防火墙是否开放80端口，检查Nginx和Java服务是否运行。

**Q: 如何修改默认密码？**  
A: 登录系统后，在用户管理页面修改。

## 📞 获取帮助

如有问题，请查看：
- 后端日志: /root/aquaculture/app.log
- Nginx日志: /var/log/nginx/
- 详细文档: 部署说明.md
