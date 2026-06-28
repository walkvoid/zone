# Zone 项目

## 项目简介

Zone 为多模块 Java 工程，使用 **Maven** 构建，**JDK 21**、**Spring Boot 3.2.11**、**Spring Cloud 2023.0.1**。

## 模块结构

- **zone-ai**: AI 相关
  - zone-ai-api / zone-ai-business / zone-ai-model
- **zone-common**: 通用能力
  - zone-common-api / zone-common-business / zone-common-model
- **zone-finance**: 金融相关
  - zone-finance-api / zone-finance-business / zone-finance-model
- **zone-gateway**: Spring Boot 可执行网关
- **zone-tools**: 工具模块
  - zone-tools-api / zone-tools-business / zone-tools-model
- **zone-user**: 用户模块
  - zone-user-api / zone-user-business / zone-user-model

## 技术栈与版本

| 项 | 版本 |
|----|------|
| JDK | 21 |
| Spring Boot | 3.2.11 |
| Spring Cloud | 2023.0.1 |
| MyBatis-Plus（Spring Boot 3） | 3.5.5 |
| JUnit | 5.x（`junit-jupiter`） |

依赖版本由根 `pom.xml` 及 **wv-framework** BOM 统一管理。

## 开发环境要求

- **JDK 21**
- **Maven 3.6+**

## 编译与运行

```bash
mvn clean install
```

运行网关（示例）：

```bash
mvn -pl zone-gateway spring-boot:run
```

打包网关：

```bash
mvn -pl zone-gateway package
```

产物：`zone-gateway/target/zone-gateway-*.jar`

## 项目维护

- 作者: walkvoid
- 邮箱: 2916147177@qq.com
