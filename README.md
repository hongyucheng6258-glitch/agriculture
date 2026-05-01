# 智慧渔业管理系统

一个功能完整的智能水产养殖管理系统，提供网箱管理、水质监测、饲喂管理、病害防治、数据溯源、AI辅助决策等全方位功能。

## ✨ 功能特性

### 🐟 核心功能
- **网箱管理**: 养殖网箱的增删改查和状态监控
- **水质监测**: 实时监测水温、pH值、溶解氧、氨氮等指标
- **天气监测**: 集成天气预报数据，辅助养殖决策
- **饲喂管理**: 记录和管理饲喂计划与执行情况
- **病害防治**: 病害信息管理和防治方案推荐
- **饲料库存**: 饲料出入库管理和库存预警
- **员工管理**: 养殖人员信息管理和权限分配
- **溯源管理**: 养殖全流程溯源，支持二维码追踪
- **告警管理**: 基于阈值的智能告警和通知
- **数据备份**: 数据库备份和恢复功能
- **统计分析**: 多维度数据统计和可视化图表
- **AI助手**: 智能问答和养殖建议推荐

### 🛠️ 技术特性
- 前后端分离架构
- 响应式设计，支持多种屏幕尺寸
- 数据可视化（ECharts）
- 二维码生成与识别
- Excel数据导出
- 智能告警引擎

## 🚀 快速开始

### 前置要求
- **Java**: 17+
- **Node.js**: 14+
- **Maven**: 3.6+

### 本地运行

#### 1. 启动后端
```bash
cd backend
mvn spring-boot:run
```

后端服务将运行在 `http://localhost:8080`

#### 2. 启动前端
```bash
cd frontend
npm install
npm run serve
```

前端服务将运行在 `http://localhost:8081`

#### 3. 访问系统
打开浏览器访问 `http://localhost:8081`

默认账号：
- 管理员: `admin` / `666666`
- 操作员: `operator` / `666666`

## 📁 项目结构

```
aquaculture-system/
├── backend/                 # 后端项目
│   ├── src/main/java/com/aquaculture/
│   │   ├── controller/     # 控制器层
│   │   ├── service/        # 服务层
│   │   ├── mapper/         # 数据访问层
│   │   ├── entity/         # 实体类
│   │   ├── dto/            # 数据传输对象
│   │   ├── config/         # 配置类
│   │   ├── util/           # 工具类
│   │   └── exception/      # 异常处理
│   └── src/main/resources/
│       ├── application.yml # 配置文件
│       └── db/init.sql     # 数据库初始化
├── frontend/               # 前端项目
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   ├── components/     # 公共组件
│   │   ├── api/            # API接口
│   │   ├── router/         # 路由配置
│   │   └── store/          # 状态管理
│   └── package.json
└── deploy/                 # 部署相关
    ├── scripts/            # 部署脚本
    ├── config/             # 配置文件
    └── db/                 # 数据库脚本
```

## 🔧 技术栈

### 后端
- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.3
- **数据库**: SQLite 3.42
- **工具库**: Lombok, Apache POI, ZXing, OkHttp, Gson

### 前端
- **框架**: Vue 2.6.14
- **UI组件**: Element UI 2.15
- **路由**: Vue Router 3.5
- **状态管理**: Vuex 3.6
- **HTTP客户端**: Axios 0.27
- **图表**: ECharts 5.4

## 📦 部署

详细的部署说明请参考 [deploy/README.md](file:///workspace/deploy/README.md) 和 [deploy/部署说明.md](file:///workspace/deploy/部署说明.md)。

### 快速部署
```bash
cd deploy/scripts
python deploy_full.py
```

## 📝 配置说明

### 后端配置
主要配置文件: [backend/src/main/resources/application.yml](file:///workspace/backend/src/main/resources/application.yml)

- 服务端口: 8080
- 数据库路径: ./data/aquaculture.db
- AI配置: 智谱AI API

### 前端配置
前端配置文件: [frontend/vue.config.js](file:///workspace/frontend/vue.config.js)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目仅供学习和研究使用。

## 📞 联系方式

如有问题，请查看部署文档或联系项目维护者。
