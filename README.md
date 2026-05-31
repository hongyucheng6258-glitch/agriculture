# 🐟 智能渔业系统 (Smart Aquaculture System)

基于 Spring Boot + Vue.js 的智慧水产养殖管理平台，实现网箱管理、水质监测、投喂管理、病害追踪、告警预警等全流程数字化管理。

## 📋 项目简介

本系统面向现代水产养殖企业，提供从养殖环境监测到生产管理的完整解决方案。通过实时数据采集与智能分析，帮助养殖户科学决策，提升养殖效率和产品质量。

## ✨ 功能特性

### 🏠 网箱管理
- 网箱信息的增删改查
- 网箱状态实时监控（使用中/空闲/维修）
- 网箱容量与位置管理

### 💧 水质监测
- 实时监测溶解氧、pH值、水温、盐度等关键指标
- 水质数据趋势分析
- 异常数据自动触发告警

### 🍽️ 投喂管理
- 投喂记录管理
- 饲料类型与库存联动（自动扣减库存）
- 投喂数据统计分析

### 🦠 病害管理
- 病害发现与记录
- 病害处理流程跟踪
- 病害统计分析

### 🔔 告警系统
- 水质异常自动告警
- 饲料库存不足告警
- 告警阈值自定义配置
- 告警处理与记录

### 📊 数据统计
- 综合仪表盘展示
- 多维度数据聚合分析
- 投喂汇总与病害统计

### 🌤️ 天气管理
- 天气数据记录
- 天气对养殖影响分析

### 🤖 AI 智能助手
- 基于大语言模型的养殖咨询
- 智能问答与建议

### 📱 溯源系统
- 批次溯源码生成
- 二维码扫码查询
- 全链条养殖追溯

### 👥 员工管理
- 员工信息管理
- 角色权限控制

### 💾 数据备份
- 数据库备份与恢复
- 数据导出功能

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.x | 应用框架 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| SQLite | - | 嵌入式数据库 |
| Java | 17 | 编程语言 |

### 前端
| 技术 | 说明 |
|------|------|
| Vue.js 2 | 前端框架 |
| Element UI | UI组件库 |
| Axios | HTTP请求 |
| Vue Router | 路由管理 |
| Vuex | 状态管理 |

### 部署
| 技术 | 说明 |
|------|------|
| Docker | 容器化部署 |
| Nginx | 反向代理 & 静态资源服务 |
| Docker Compose | 多容器编排 |

## 📁 项目结构

```
agriculture/
├── backend/                    # 后端服务
│   ├── src/main/java/
│   │   └── com/aquaculture/
│   │       ├── controller/     # 控制器层
│   │       ├── service/        # 业务逻辑层
│   │       ├── mapper/         # 数据访问层
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── config/         # 配置类
│   │       ├── common/         # 公共类
│   │       ├── exception/      # 异常处理
│   │       └── util/           # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml     # 应用配置
│   │   └── db/init.sql         # 数据库初始化脚本
│   ├── pom.xml                 # Maven配置
│   └── Dockerfile              # Docker构建文件
├── frontend/                   # 前端应用
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   ├── store/              # 状态管理
│   │   └── utils/              # 工具函数
│   ├── public/                 # 静态资源
│   ├── package.json            # 依赖配置
│   └── vue.config.js           # Vue配置
├── docker-compose.yml          # Docker编排
├── nginx.conf                  # Nginx配置
├── start.sh                    # 启动脚本
└── restart.sh                  # 重启脚本
```

## 🚀 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- Maven 3.6+
- Docker & Docker Compose（可选）

### 后端启动

```bash
cd backend
mvn clean package -DskipTests
java -jar target/aquaculture-system-1.0.0.jar
```

### 前端启动

```bash
cd frontend
npm install
npm run serve
```

### Docker 部署

```bash
# 构建前端
cd frontend && npm run build

# 启动服务
docker-compose up -d
```

## 📡 API 接口

| 模块 | 路径 | 说明 |
|------|------|------|
| 网箱管理 | `/api/cage/*` | 网箱CRUD |
| 水质监测 | `/api/water-quality/*` | 水质数据管理 |
| 投喂管理 | `/api/feeding/*` | 投喂记录管理 |
| 病害管理 | `/api/disease/*` | 病害记录管理 |
| 告警系统 | `/api/alert/*` | 告警管理 |
| 饲料库存 | `/api/feed-stock/*` | 库存管理 |
| 数据统计 | `/api/statistics/*` | 统计分析 |
| 员工管理 | `/api/staff/*` | 员工管理 |
| 天气管理 | `/api/weather/*` | 天气数据 |
| AI助手 | `/api/ai/*` | 智能问答 |
| 溯源管理 | `/api/trace/*` | 溯源查询 |
| 数据备份 | `/api/backup/*` | 备份恢复 |

## 🔗 系统联动

```
网箱管理 ─────► 水质监测 ─────► 告警系统
    │                │                │
    │                ▼                ▼
    ├──► 投喂管理 ──► 饲料库存 ──► 库存告警
    │                │
    ├──► 病害管理 ──► 告警系统
    │
    └──► 数据统计仪表盘（聚合所有模块数据）
```

## 📄 License

MIT License
