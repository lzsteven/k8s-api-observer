# Kubernetes 集群监控与 API 管理平台编译指南

本文档提供了 Kubernetes 集群监控与 API 管理平台的详细编译指南，包括环境准备、代码编译和镜像构建的全过程。

## 目录

- [开发环境准备](#开发环境准备)
- [源码获取](#源码获取)
- [项目结构说明](#项目结构说明)
- [编译流程](#编译流程)
- [镜像构建](#镜像构建)
- [本地测试](#本地测试)
- [常见编译问题](#常见编译问题)

## 开发环境准备

### 1. JDK 环境

本项目使用 Java 11 进行开发，请确保您的开发环境已安装 JDK 11 或更高版本。

**Windows 环境安装步骤：**

1. 从 Oracle 官网下载 JDK 11：https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
2. 运行安装程序，按照向导完成安装
3. 配置环境变量：
   ```
   set JAVA_HOME=C:\Program Files\Java\jdk-11
   set PATH=%PATH%;%JAVA_HOME%\bin
   ```
4. 验证安装：
   ```
   java -version
   ```
   应显示类似以下内容：
   ```
   java version "11.0.12" 2021-07-20 LTS
   Java(TM) SE Runtime Environment 18.9 (build 11.0.12+8-LTS-237)
   Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.12+8-LTS-237, mixed mode)
   ```

### 2. Maven 环境

本项目使用 Maven 进行依赖管理和构建，需要安装 Maven 3.6 或更高版本。

**Windows 环境安装步骤：**

1. 从 Maven 官网下载最新版本：https://maven.apache.org/download.cgi
2. 解压到本地目录，如 `C:\apache-maven-3.8.6`
3. 配置环境变量：
   ```
   set MAVEN_HOME=C:\apache-maven-3.8.6
   set PATH=%PATH%;%MAVEN_HOME%\bin
   ```
4. 验证安装：
   ```
   mvn -version
   ```
   应显示 Maven 版本信息

### 3. Docker 环境

用于构建和测试容器镜像，需要安装 Docker 20.10 或更高版本。

**Windows 环境安装步骤：**

1. 下载并安装 Docker Desktop：https://www.docker.com/products/docker-desktop
2. 启动 Docker Desktop 并等待服务就绪
3. 验证安装：
   ```
   docker --version
   ```

## 源码获取

### 1. 克隆代码仓库

```bash
# 使用 Git 克隆项目代码
git clone <project-url>
cd k8s-api-monitor
```

### 2. 分支说明

- `main`: 主分支，包含稳定版本的代码
- `develop`: 开发分支，包含最新的开发特性
- `feature/*`: 特性分支，用于开发新功能

```bash
# 切换到开发分支（如果需要）
git checkout develop
```

## 项目结构说明

本项目采用 Maven 多模块结构，主要包含以下模块：

```
k8s-api-monitor/                # 项目根目录
├── api-discovery-service/      # API 发现服务模块
├── api-gateway-service/        # API 网关服务模块
├── management-portal/          # 管理门户模块
├── monitoring-core/            # 监控核心配置
│   ├── prometheus/             # Prometheus 配置
│   └── alertmanager/           # Alertmanager 配置
└── pom.xml                     # 父 POM 文件
```

### 模块说明

1. **api-discovery-service**: 负责发现和收集集群中的 API 信息
   - 基于 Spring Boot 开发
   - 使用 Kubernetes Java 客户端监听服务变化
   - 支持解析 Swagger/OpenAPI 文档

2. **api-gateway-service**: 提供统一的 API 访问入口
   - 基于 Spring Cloud Gateway 开发
   - 支持动态路由配置
   - 提供 API 调用监控和限流功能

3. **management-portal**: 提供 Web 管理界面
   - 基于 Spring Boot 和 Thymeleaf 开发
   - 集成 Bootstrap 和 Vue.js 前端框架
   - 提供监控数据可视化和 API 管理功能

4. **monitoring-core**: 包含监控组件的配置文件
   - Prometheus 配置和告警规则
   - Alertmanager 配置

## 编译流程

### 1. 编译整个项目

在项目根目录下执行以下命令，编译所有模块：

```bash
# 清理并安装所有模块（跳过测试）
mvn clean install -DskipTests
```

如果需要运行测试，可以去掉 `-DskipTests` 参数：

```bash
# 清理并安装所有模块（包含测试）
mvn clean install
```

### 2. 编译单个模块

如果只需要编译特定模块，可以进入对应模块目录执行编译命令：

```bash
# 编译 API 发现服务
cd api-discovery-service
mvn clean package
```

### 3. 编译选项说明

- `-DskipTests`: 跳过测试执行
- `-Dmaven.test.skip=true`: 跳过测试编译和执行
- `-Pprod`: 使用生产环境配置文件（如果已定义）
- `-Dspring.profiles.active=prod`: 指定 Spring 配置文件

### 4. 编译输出

编译成功后，各模块的输出文件位于各自的 `target` 目录下：

- API 发现服务: `api-discovery-service/target/api-discovery-service-1.0.0.jar`
- API 网关服务: `api-gateway-service/target/api-gateway-service-1.0.0.jar`
- 管理门户: `management-portal/target/management-portal-1.0.0.jar`

## 镜像构建

### 1. API 发现服务镜像

```bash
# 进入 API 发现服务目录
cd api-discovery-service

# 构建 Docker 镜像
docker build -t api-discovery-service:latest .

# 查看构建的镜像
docker images | grep api-discovery-service
```

### 2. API 网关服务镜像

```bash
# 进入 API 网关服务目录
cd ../api-gateway-service

# 构建 Docker 镜像
docker build -t api-gateway-service:latest .

# 查看构建的镜像
docker images | grep api-gateway-service
```

### 3. 管理门户镜像

```bash
# 进入管理门户目录
cd ../management-portal

# 构建 Docker 镜像
docker build -t management-portal:latest .

# 查看构建的镜像
docker images | grep management-portal
```

### 4. 推送镜像到仓库

如果需要将镜像推送到私有或公共镜像仓库，可以执行以下命令：

```bash
# 标记镜像
docker tag api-discovery-service:latest <your-registry>/api-discovery-service:latest
docker tag api-gateway-service:latest <your-registry>/api-gateway-service:latest
docker tag management-portal:latest <your-registry>/management-portal:latest

# 登录镜像仓库
docker login <your-registry> -u <username> -p <password>

# 推送镜像
docker push <your-registry>/api-discovery-service:latest
docker push <your-registry>/api-gateway-service:latest
docker push <your-registry>/management-portal:latest
```

## 本地测试

### 1. 运行单元测试

```bash
# 运行所有测试
mvn test

# 运行特定模块的测试
cd api-discovery-service
mvn test
```

### 2. 本地启动服务

可以使用 Spring Boot Maven 插件在本地启动服务进行测试：

```bash
# 启动 API 发现服务
cd api-discovery-service
mvn spring-boot:run

# 启动 API 网关服务
cd ../api-gateway-service
mvn spring-boot:run

# 启动管理门户
cd ../management-portal
mvn spring-boot:run
```

### 3. 使用 Docker Compose 测试

如果项目包含 Docker Compose 配置，可以使用以下命令启动所有服务：

```bash
# 在项目根目录下
docker-compose up -d
```

## 常见编译问题

### 1. 依赖下载失败

**问题**: Maven 依赖下载失败或超时

**解决方案**:
- 检查网络连接
- 配置国内 Maven 镜像源，在 `~/.m2/settings.xml` 中添加：
  ```xml
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven Repository</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
  ```

### 2. 编译错误

**问题**: 编译时出现语法错误或依赖冲突

**解决方案**:
- 确保使用正确的 JDK 版本
- 检查 Maven 依赖是否有冲突
- 尝试清理 Maven 本地仓库缓存：
  ```bash
  mvn dependency:purge-local-repository
  ```

### 3. 测试失败

**问题**: 单元测试或集成测试失败

**解决方案**:
- 检查测试环境配置
- 查看测试日志，定位具体错误
- 如果是环境问题，可以临时跳过测试：
  ```bash
  mvn install -DskipTests
  ```

### 4. Docker 构建问题

**问题**: Docker 镜像构建失败

**解决方案**:
- 确保 Dockerfile 正确配置
- 检查 Docker 服务是否正常运行
- 确保有足够的磁盘空间
- 查看 Docker 构建日志，定位具体错误

## 参考资源

- [Spring Boot 文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Maven 文档](https://maven.apache.org/guides/index.html)
- [Docker 文档](https://docs.docker.com/)
- [Kubernetes Java 客户端](https://github.com/kubernetes-client/java)