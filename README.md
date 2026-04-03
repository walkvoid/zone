# Zone 项目

## 项目简介

Zone 为多模块 Java 工程，构建方式与 **wv-framework** 对齐：**Gradle Wrapper**、**JDK 21**、**Spring Boot 3.2.5**、**Spring Cloud 2023.0.1**。

## 模块结构

- **zone-ai**: AI 相关
  - zone-ai-api / zone-ai-business / zone-ai-model
- **zone-common**: 通用能力
  - zone-common-api / zone-common-business / zone-common-model
- **zone-finance**: 金融相关
  - zone-finance-api / zone-finance-business / zone-finance-model
- **zone-gateway**: Spring Boot 可执行网关（`bootJar`）
- **zone-tools**: 工具模块
  - zone-tools-api / zone-tools-business / zone-tools-model
- **zone-user**: 用户模块
  - zone-user-api / zone-user-business / zone-user-model

## 技术栈与版本（与 wv-framework 一致）

| 项 | 版本 |
|----|------|
| JDK（工具链） | 21 |
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.1 |
| MyBatis-Plus（Spring Boot 3） | 3.5.5 |
| JUnit | 5.x（`junit-jupiter`） |

依赖版本目录：`gradle/libs.versions.toml`（与 wv-framework 同源策略，可按需同步升级）。

## 开发环境要求

- **JDK 21**（运行 Gradle 与编译均需 17+；工程工具链指定为 21）
- 无需单独安装 Gradle，使用项目内 **Wrapper**

## 依赖仓库与 JDK

`settings.gradle.kts` 中直接配置本地目录 **`file:///D:/apache-maven-3.5.3/repo`**，顺序为：该目录 → **`mavenLocal()`** → **`mavenCentral()`**（插件另加 **`gradlePluginPortal()`**）。换路径时改 `settings.gradle.kts` 即可。

`gradle.properties` 里 **`org.gradle.java.home`** 指定 Gradle 使用的 **JDK 21**（可按本机在 **`gradle-local.properties`** 覆盖）。

各子模块通过 **`build.gradle.kts`** 的 **Java 工具链 21** 编译。

## 编译与运行

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
set PATH=%JAVA_HOME%\bin;%PATH%
gradlew.bat clean build
```

运行网关（示例）：

```bat
gradlew.bat :zone-gateway:bootRun
```

打包网关：

```bat
gradlew.bat :zone-gateway:bootJar
```

产物：`zone-gateway/build/libs/zone-gateway-*.jar`

## 项目维护

- 作者: walkvoid
- 邮箱: 2916147177@qq.com
