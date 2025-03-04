# Kubernetes 集群监控与 API 管理平台部署指南

本文档提供了 Kubernetes 集群监控与 API 管理平台的详细部署指南，从环境准备到系统配置的全过程。

## 目录

- [环境准备](#环境准备)
- [代码编译](#代码编译)
- [镜像构建](#镜像构建)
- [Kubernetes 部署](#kubernetes-部署)
- [配置说明](#配置说明)
- [验证部署](#验证部署)
- [常见问题排查](#常见问题排查)

## 环境准备

### 开发环境要求

1. **JDK 环境**
   ```bash
   # 安装 JDK 11
   # Windows 环境下载安装包：https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
   # 配置环境变量
   set JAVA_HOME=C:\Program Files\Java\jdk-11
   set PATH=%PATH%;%JAVA_HOME%\bin
   
   # 验证安装
   java -version
   ```

2. **Maven 环境**
   ```bash
   # 下载 Maven: https://maven.apache.org/download.cgi
   # 解压并配置环境变量
   set MAVEN_HOME=C:\apache-maven-3.8.6
   set PATH=%PATH%;%MAVEN_HOME%\bin
   
   # 验证安装
   mvn -version
   ```

3. **Docker 环境**
   ```bash
   # Windows 安装 Docker Desktop: https://www.docker.com/products/docker-desktop
   # 验证安装
   docker --version
   ```

4. **Kubernetes 工具**
   ```bash
   # 安装 kubectl
   # Windows: https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/
   
   # 验证安装
   kubectl version --client
   ```

5. **Helm 工具**
   ```bash
   # 安装 Helm
   # Windows: https://helm.sh/docs/intro/install/
   
   # 验证安装
   helm version
   ```

### Kubernetes 集群准备

1. **创建或连接到 Kubernetes 集群**
   - 本地开发可使用 Minikube 或 Docker Desktop 内置的 Kubernetes
   - 生产环境推荐使用云服务商提供的 Kubernetes 服务或自建集群

2. **配置 kubectl**
   ```bash
   # 确保 kubectl 已正确配置
   kubectl config use-context <your-context>
   kubectl cluster-info
   ```

3. **创建专用命名空间**
   ```bash
   kubectl create namespace k8s-monitor
   ```

4. **配置存储类**
   ```bash
   # 确保集群有可用的存储类用于持久化数据
   kubectl get storageclass
   ```

## 代码编译

### 1. 获取源代码

```bash
# 克隆代码仓库
git clone <project-url>
cd k8s-api-monitor
```

### 2. 编译父项目

```bash
# 编译整个项目
mvn clean install -DskipTests
```

### 3. 编译各个子模块

```bash
# 编译 API 发现服务
cd api-discovery-service
mvn clean package

# 编译 API 网关服务
cd ../api-gateway-service
mvn clean package

# 编译管理门户
cd ../management-portal
mvn clean package
```

## 镜像构建

### 1. API 发现服务镜像

```bash
cd api-discovery-service
docker build -t api-discovery-service:latest .

# 如果需要推送到镜像仓库
docker tag api-discovery-service:latest <your-registry>/api-discovery-service:latest
docker push <your-registry>/api-discovery-service:latest
```

### 2. API 网关服务镜像

```bash
cd ../api-gateway-service
docker build -t api-gateway-service:latest .

# 如果需要推送到镜像仓库
docker tag api-gateway-service:latest <your-registry>/api-gateway-service:latest
docker push <your-registry>/api-gateway-service:latest
```

### 3. 管理门户镜像

```bash
cd ../management-portal
docker build -t management-portal:latest .

# 如果需要推送到镜像仓库
docker tag management-portal:latest <your-registry>/management-portal:latest
docker push <your-registry>/management-portal:latest
```

## Kubernetes 部署

### 1. 部署 Prometheus Operator

```bash
# 安装 Prometheus Operator
# 添加 Helm 仓库（如果尚未添加）
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# 安装 Prometheus Operator 及相关组件
helm install monitoring prometheus-community/kube-prometheus-stack --namespace k8s-monitor --create-namespace --values monitoring-core/prometheus/values.yaml --timeout 10m --wait --atomic --description "Kubernetes监控系统 - Prometheus Operator及相关组件"

# 验证安装状态
helm ls -n k8s-monitor
```

> **注意**：在 Docker Desktop 环境中，node-exporter 可能会因为挂载问题而无法启动，错误信息为 "path / is mounted on / but it is not a shared or slave mount"。如果遇到这个问题，请确保在 `monitoring-core/prometheus/values.yaml` 文件中添加以下配置：
> 
> ```yaml
> nodeExporter:
>   enabled: true
>   hostRootFsMount:
>     enabled: false
>   extraArgs:
>     - --collector.filesystem.mount-points-exclude=^/(dev|proc|sys|var/lib/docker/.+|var/lib/kubelet/.+)($|/)
>     - --collector.filesystem.fs-types-exclude=^(autofs|binfmt_misc|bpf|cgroup2?|configfs|debugfs|devpts|devtmpfs|fusectl|hugetlbfs|iso9660|mqueue|nsfs|overlay|proc|procfs|pstore|rpc_pipefs|securityfs|selinuxfs|squashfs|sysfs|tracefs)$
>     - --no-collector.filesystem
>   securityContext:
>     runAsUser: 65534
>     runAsNonRoot: true
>   volumes: []
>   volumeMounts: []
> ```
> 
> 然后使用以下命令更新部署：
> 
> ```bash
> helm upgrade monitoring prometheus-community/kube-prometheus-stack --namespace k8s-monitor --values monitoring-core/prometheus/values.yaml
> ```

### 2. 检查 Prometheus 组件

```bash
# 检查 Prometheus 相关 Pod
kubectl get pods -n k8s-monitor | grep prometheus

# 检查 Alertmanager
kubectl get pods -n k8s-monitor | grep alertmanager

# 检查 Grafana
kubectl get pods -n k8s-monitor | grep grafana
```

### 3. 部署自定义告警规则

```bash
# 部署告警规则
kubectl apply -f monitoring-core/prometheus/rules/ -n k8s-monitor
```

### 4. 部署 API 发现服务

```bash
# 部署配置文件
kubectl apply -f kubernetes/api-discovery/configmap.yaml -n k8s-monitor

# 部署服务
kubectl apply -f kubernetes/api-discovery/deployment.yaml -n k8s-monitor
kubectl apply -f kubernetes/api-discovery/service.yaml -n k8s-monitor
```

### 5. 部署 API 网关服务

```bash
# 部署配置文件
kubectl apply -f kubernetes/api-gateway/configmap.yaml -n k8s-monitor

# 部署服务
kubectl apply -f kubernetes/api-gateway/deployment.yaml -n k8s-monitor
kubectl apply -f kubernetes/api-gateway/service.yaml -n k8s-monitor
```

### 6. 部署管理门户

```bash
# 部署配置文件
kubectl apply -f kubernetes/management-portal/configmap.yaml -n k8s-monitor

# 部署服务
kubectl apply -f kubernetes/management-portal/deployment.yaml -n k8s-monitor
kubectl apply -f kubernetes/management-portal/service.yaml -n k8s-monitor
```

### 7. 部署 Ingress（可选）

```bash
# 如果需要通过 Ingress 暴露服务
kubectl apply -f kubernetes/ingress.yaml -n k8s-monitor
```

## 配置说明

### 1. Prometheus 配置

主要配置文件：`monitoring-core/prometheus/prometheus.yml`

```yaml
# 关键配置项说明
global:
  scrape_interval: 15s     # 采集间隔，可根据需要调整
  evaluation_interval: 15s  # 规则评估间隔

# 告警管理器配置
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

# 采集配置
scrape_configs:
  # 各种采集目标配置
```

### 2. API 发现服务配置

主要配置文件：`api-discovery-service/src/main/resources/application.yml`

```yaml
# 关键配置项
spring:
  application:
    name: api-discovery-service

# Kubernetes 客户端配置
kubernetes:
  master-url: https://kubernetes.default.svc  # K8s API 服务器地址
  trust-certificates: true                    # 是否信任证书
  namespace: default                          # 默认监听的命名空间

# API 发现配置
api-discovery:
  scan-interval: 60000                        # 扫描间隔（毫秒）
  swagger-paths:                              # Swagger 文档路径
    - /v3/api-docs
    - /swagger-resources
    - /v2/api-docs
```

### 3. API 网关配置

主要配置文件：`api-gateway-service/src/main/resources/application.yml`

```yaml
# 关键配置项
spring:
  application:
    name: api-gateway-service
  cloud:
    gateway:
      routes:                                 # 路由配置
      - id: api-discovery-route
        uri: lb://api-discovery-service
        predicates:
        - Path=/api/discovery/**

# 安全配置
security:
  enabled: true                               # 是否启用安全认证
  auth-header: X-API-Key                      # 认证头
```

### 4. 管理门户配置

主要配置文件：`management-portal/src/main/resources/application.yml`

```yaml
# 关键配置项
spring:
  application:
    name: management-portal

# 监控配置
monitoring:
  prometheus-url: http://prometheus:9090      # Prometheus 地址
  grafana-url: http://grafana:3000            # Grafana 地址

# API 服务配置
api:
  discovery-url: http://api-discovery-service:8080  # API 发现服务地址
  gateway-url: http://api-gateway-service:8080      # API 网关服务地址
```

## 验证部署

### 1. 检查所有 Pod 状态

```bash
# 检查所有 Pod 是否正常运行
kubectl get pods -n k8s-monitor
```

所有 Pod 应该处于 `Running` 状态，并且 `READY` 列应该显示所有容器都已就绪。

### 2. 检查服务状态

```bash
# 检查所有服务
kubectl get svc -n k8s-monitor
```

### 3. 访问管理门户

```bash
# 获取管理门户服务地址
kubectl get svc management-portal -n k8s-monitor
```

如果使用 NodePort 类型的服务，可以通过 `http://<node-ip>:<node-port>` 访问管理门户。

如果使用 Ingress，可以通过配置的域名访问。

### 4. 验证监控功能

1. 登录管理门户
2. 导航到监控仪表板
3. 确认可以看到集群节点和容器的监控指标
4. 检查告警规则是否生效

### 5. 验证 API 发现功能

1. 导航到 API 管理页面
2. 确认系统能够发现集群中的 API 服务
3. 查看 API 文档是否正确显示

## 常见问题排查

### 1. Pod 启动失败

```bash
# 查看 Pod 详细信息
kubectl describe pod <pod-name> -n k8s-monitor

# 查看 Pod 日志
kubectl logs <pod-name> -n k8s-monitor
```

常见原因：
- 镜像拉取失败：检查镜像名称和仓库配置
- 资源不足：检查节点资源使用情况
- 配置错误：检查 ConfigMap 和环境变量

### 2. 服务无法访问

```bash
# 检查服务端点
kubectl get endpoints <service-name> -n k8s-monitor

# 测试服务连通性
kubectl run -it --rm debug --image=busybox -- wget -O- <service-name>:<port>
```

常见原因：
- 服务选择器配置错误
- Pod 健康检查失败
- 网络策略限制

### 3. Prometheus 无法采集数据

```bash
# 检查 Prometheus 配置
kubectl get configmap -n k8s-monitor | grep prometheus

# 查看 Prometheus 日志
kubectl logs -l app=prometheus -n k8s-monitor
```

常见原因：
- 采集目标配置错误
- 服务发现配置问题
- 权限不足

### 4. API 发现服务异常

```bash
# 查看 API 发现服务日志
kubectl logs -l app=api-discovery-service -n k8s-monitor
```

常见原因：
- Kubernetes API 访问权限不足
- 服务注解配置错