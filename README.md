# Zone 项目

## 项目简介

Zone 是一个基于 Spring Cloud 的微服务架构多模块 Java 工程，采用 Maven 构建，集成了 AI、金融、用户管理等核心业务模块，提供统一的 API 网关入口。

**核心特性：**
- 微服务架构设计，模块职责清晰
- 统一的技术栈和依赖管理（基于 wv-framework BOM）
- JWT 认证与授权机制
- Spring Security 安全框架集成
- OpenAPI 3.0 + Knife4j API 文档
- 支持服务注册与发现
- 集成网关统一入口
- XXL-JOB 定时任务支持

## 工作区概览

Zone 项目采用多仓库协作的开发模式，由以下三个子项目组成：

```
zone-workspace/
├── zone/              # 后端服务（本仓库）—— Spring Cloud 微服务
├── zone-front/        # 前端应用 —— 基于 Vue Vben Admin 5.x 的中后台
└── wv-framework/      # 基础框架 —— 提供通用组件、工具类和依赖管理（BOM）
```

| 子项目 | 技术栈 | 说明 |
|--------|--------|------|
| **zone** | Spring Boot 3.2 + Spring Cloud 2023 + JDK 21 | 后端微服务，包含业务模块和网关 |
| **zone-front** | Vue 3 + Vite + TypeScript + pnpm monorepo | 前端中后台应用（`zone-admin`） |
| **wv-framework** | Gradle + JDK 21 | 底层框架，提供 annotations、components、utils、starters 等 |

> **依赖关系**：`zone` 通过 `wvframework-dependencies` BOM 统一管理依赖版本；`zone-front` 通过 API 与 `zone` 后端交互。

## 技术栈与版本

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 21 | 编程语言 |
| Spring Boot | 3.2.11 | 应用框架 |
| Spring Cloud | 2023.0.1 | 微服务框架 |
| Spring Security | 6.2.x | 安全框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| JWT | 0.12.x | 令牌认证 |
| Knife4j | 4.4.x | API 文档工具 |
| JUnit | 5.x | 测试框架 |

依赖版本由根 `pom.xml` 及 **wv-framework** BOM 统一管理。

## 模块结构

### 三层模块架构

每个业务模块均采用 **api / model / business** 三层分包设计，职责分离清晰：

| 子模块 | 职责 | 说明 |
|--------|------|------|
| `*-api` | 接口定义层 | 定义 Service 接口，供跨模块调用 |
| `*-model` | 数据模型层 | 定义 Entity、DTO、VO 等数据对象 |
| `*-business` | 业务逻辑层 | 实现 Service、Controller、DAO、Mapper |

### 目录结构

```
zone/
├── zone-system/          # 系统模块（菜单管理、股票事件等）
│   ├── zone-system-api       # 系统接口定义
│   ├── zone-system-business  # 系统业务逻辑（AuthController、MenuController）
│   └── zone-system-model     # 系统数据模型（Menu、StockEvent）
├── zone-user/            # 用户模块
│   ├── zone-user-api
│   ├── zone-user-business
│   └── zone-user-model
├── zone-ai/              # AI 相关模块
│   ├── zone-ai-api
│   ├── zone-ai-business
│   └── zone-ai-model
├── zone-finance/         # 金融相关模块
│   ├── zone-finance-api
│   ├── zone-finance-business
│   └── zone-finance-model
├── zone-tools/           # 工具模块
│   ├── zone-tools-api
│   ├── zone-tools-business
│   └── zone-tools-model
├── zone-gateway/         # Spring Boot 可执行网关（统一入口）
│   ├── src/main/java/
│   │   └── com/github/walkvoid/zone/gateway/
│   │       ├── config/    # 配置类（JWT、Security、MyBatis等）
│   │       └── ZoneGatewayApplication.java
│   └── src/main/resources/
│       ├── certs/        # SSL 证书
│       ├── template/     # 前端模板
│       └── application.properties
├── docs/                 # 文档与SQL脚本
└── pom.xml               # 根 pom
```

**模块职责说明：**

| 模块 | 职责 |
|------|------|
| zone-system | 系统管理（菜单管理、股票事件、认证控制器等） |
| zone-user | 用户管理与认证 |
| zone-ai | AI 能力封装与调用（Prompt 模板管理等） |
| zone-finance | 金融业务处理（指标、菜单等） |
| zone-tools | 通用工具服务 |
| zone-gateway | API 网关，路由转发，统一认证入口 |

## 开发环境要求

- **JDK 21**
- **Maven 3.6+**
- **MySQL 8.0+**（如需数据库支持）
- **Git**（版本控制）

## 快速开始

### 1. 环境准备

确保已安装 JDK 21 和 Maven，并配置好环境变量。

### 2. 编译项目

```bash
cd zone
mvn clean install -DskipTests
```

### 3. 配置数据库连接

在运行前，请确保数据库已创建并配置正确。可通过环境变量或修改配置文件设置：

```bash
# Linux/Mac 环境变量方式
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/zone_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=your_password
```

### 4. 运行网关

```bash
# 开发模式运行
mvn -pl zone-gateway spring-boot:run

# 或运行打包后的 Jar
java -jar zone-gateway/target/zone-gateway-*.jar
```

### 5. 打包部署

```bash
# 打包所有模块
mvn clean package -DskipTests

# 仅打包网关
mvn -pl zone-gateway package -DskipTests
```

产物：`zone-gateway/target/zone-gateway-*.jar`

## 配置说明

### 主要配置文件

配置文件位于 `zone-gateway/src/main/resources/application.properties`

### 默认配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 服务端口 | 8080 | 网关监听端口 |
| 上下文路径 | /api | API 基础路径 |
| JWT 密钥 | 内置密钥 | 生产环境请更换 |
| Access Token 有效期 | 7天 | 604800000ms |
| Refresh Token 有效期 | 30天 | 2592000000ms |

### 环境变量配置

| 环境变量 | 说明 |
|----------|------|
| `SPRING_DATASOURCE_URL` | 数据库连接URL |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 |
| `JWT_SECRET` | JWT 签名密钥 |

## API 文档

启动服务后，可通过以下地址访问 API 文档：

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Knife4j**: http://localhost:8080/api/doc.html

## SQL 脚本

初始化脚本位于 `docs/` 目录：

| 文件 | 说明 |
|------|------|
| `phase1-init-all.sql` | 全量初始化脚本 |
| `init-finance-tables.sql` | 金融模块表结构 |
| `init-finance-indicator.sql` | 金融指标数据 |
| `init-finance-menu.sql` | 金融菜单数据 |
| `init-user-data.sql` | 用户初始数据 |
| `init-menu-data.sql` | 系统菜单数据 |
| `menu-merge-status-to-visible.sql` | 菜单状态合并脚本 |
| `fix-chinese-encoding.sql` | 中文编码修复脚本 |

### 数据库初始化

```bash
mysql -u root -p < docs/phase1-init-all.sql
```

## 项目结构详解

### 网关模块核心配置

| 配置类 | 职责 |
|--------|------|
| `SecurityConfig` | Spring Security 安全配置 |
| `JwtAuthFilter` | JWT 认证过滤器 |
| `JwtProperties` | JWT 配置属性 |
| `MybatisPlusConfig` | MyBatis-Plus 配置 |
| `XxlJobConfig` | XXL-JOB 定时任务配置 |
| `DataInitializer` | 数据初始化器 |

## 前端开发

前端项目位于 `zone-front/`，基于 Vue Vben Admin 5.x 构建，采用 pnpm monorepo 管理。

### 环境要求

- **Node.js** >= 22.18.0 或 >= 24.0.0
- **pnpm** >= 11.0.0

### 快速启动

```bash
cd zone-front

# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 构建生产版本
pnpm build
```

启动后访问前端应用，默认通过后端 API 地址 `http://localhost:8080/api` 进行数据交互。

## 开发约定

### Git 提交规范

采用 Conventional Commits 规范：

```
feat(module): 新增某功能
fix(module): 修复某问题
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具变动
```

### 分支策略

- `main` / `master` — 稳定发布分支
- `develop` — 开发集成分支
- `feature/*` — 功能开发分支
- `fix/*` — 缺陷修复分支

### 模块开发流程

新增业务模块时，需创建三个子模块（api / model / business）并在根 `pom.xml` 中注册：

```xml
<module>zone-xxx/zone-xxx-api</module>
<module>zone-xxx/zone-xxx-model</module>
<module>zone-xxx/zone-xxx-business</module>
```

## 安全说明

1. **JWT 密钥**: 生产环境务必更换默认密钥，可通过环境变量 `JWT_SECRET` 设置
2. **数据库密码**: 不要在配置文件中硬编码密码，使用环境变量
3. **SSL 证书**: 生产环境建议启用 HTTPS

## 项目维护

- 作者: walkvoid
- 邮箱: 2916147177@qq.com

## 许可证

MIT License