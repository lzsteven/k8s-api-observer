# Kubernetes 集群监控与 API 管理平台

## 项目介绍
本项目是一个基于 Kubernetes 的集群监控与 API 管理平台，旨在提供全面的集群资源监控、容器管理和 API 服务发现功能。平台整合了 Prometheus 监控系统和自研 API 管理组件，为云原生应用提供完整的运维管理解决方案。

### 核心功能
- 集群资源监控：CPU、内存、磁盘、网络等指标实时监控
- 容器生命周期管理：状态查看、启停、日志查看等
- API 自动发现：自动收集和管理集群中的 API 信息
- 统一 API 网关：提供集中的 API 访问和管理入口
- 告警管理：支持自定义告警规则和多渠道通知

## 系统架构

### 核心组件
1. 监控中心 (Monitoring Core)
   - 基于 Prometheus Operator
   - 提供指标采集和告警功能
   - 包含 Grafana 可视化界面

2. API 发现服务 (API Discovery Service)
   - 监听集群服务变化
   - 采集 API 元数据
   - 提供 API 注册中心

3. API 网关服务 (API Gateway Service)
   - 统一访问入口
   - 动态路由配置
   - 负载均衡

4. 管理门户 (Management Portal)
   - Web 控制台
   - 监控数据展示
   - API 文档管理

## 环境要求

### 开发环境
- JDK 11 或更高版本
- Maven 3.6+
- Docker 20.10+
- Kubernetes 1.18+
- Helm 3.0+

### 运行环境
- Kubernetes 集群（1.18+）
- 至少 2 个工作节点
- 每个节点至少 4GB 内存
- 持久化存储支持（用于 Prometheus 和告警数据）

## 编译指南

### 1. 准备开发环境
```bash
# 克隆项目
git clone <project-url>
cd k8s-api-monitor

# 安装依赖
mvn clean install -DskipTests
```

### 2. 编译各个模块
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

### 3. 构建 Docker 镜像
```bash
# API 发现服务
cd api-discovery-service
docker build -t api-discovery-service:latest .

# API 网关服务
cd ../api-gateway-service
docker build -t api-gateway-service:latest .

# 管理门户
cd ../management-portal
docker build -t management-portal:latest .
```

## 部署指南

### 1. 准备 Kubernetes 集群
- 确保 kubectl 已正确配置
- 创建专用命名空间
```bash
kubectl create namespace k8s-monitor
```

### 2. 部署 Prometheus Operator
```bash
# 添加 Helm 仓库
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# 安装 Prometheus Operator
helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace k8s-monitor \
  --values monitoring-core/prometheus/values.yaml
```

### 3. 部署平台组件
```bash
# 部署 API 发现服务
kubectl apply -f kubernetes/api-discovery/ -n k8s-monitor

# 部署 API 网关服务
kubectl apply -f kubernetes/api-gateway/ -n k8s-monitor

# 部署管理门户
kubectl apply -f kubernetes/management-portal/ -n k8s-monitor
```

### 4. 验证部署
```bash
# 检查 Pod 状态
kubectl get pods -n k8s-monitor

# 检查服务状态
kubectl get svc -n k8s-monitor
```

### 5. 访问管理门户
```bash
# 获取管理门户访问地址
kubectl get svc management-portal -n k8s-monitor
```
访问管理门户地址（默认为 http://<cluster-ip>:30000）

## 配置说明

### 1. 告警配置
告警规则配置文件位于 `monitoring-core/prometheus/rules/` 目录下：
- cpu-alerts.yml：CPU 相关告警规则
- memory-alerts.yml：内存相关告警规则
- disk-alerts.yml：磁盘相关告警规则

### 2. API 网关配置
网关配置文件位于 `api-gateway-service/src/main/resources/application.yml`：
- 路由规则配置
- 负载均衡策略
- 限流规则

### 3. 监控指标配置
Prometheus 配置文件位于 `monitoring-core/prometheus/prometheus.yml`：
- 指标采集配置
- 数据保留策略
- 采集目标配置

## 常见问题

### 1. 组件无法启动
检查：
- Pod 日志是否有错误信息
- 资源配额是否充足
- 配置文件是否正确

### 2. 无法采集监控数据
检查：
- Prometheus 配置是否正确
- 服务发现是否正常工作
- 网络策略是否允许采集

### 3. API 发现异常
检查：
- API 发现服务日志
- RBAC 权限配置
- 服务注解是否正确

## 维护指南

### 1. 日志查看
```bash
# 查看各组件日志
kubectl logs -f <pod-name> -n k8s-monitor
```

### 2. 配置更新
```bash
# 更新配置后重新应用
kubectl apply -f <config-file> -n k8s-monitor
```

### 3. 版本升级
```bash
# 升级 Prometheus Operator
helm upgrade monitoring prometheus-community/kube-prometheus-stack \
  --namespace k8s-monitor \
  --values monitoring-core/prometheus/values.yaml

# 升级平台组件
kubectl apply -f kubernetes/ -n k8s-monitor
```

## 贡献指南
1. Fork 项目
2. 创建功能分支
3. 提交变更
4. 发起 Pull Request

## 许可证
[Apache License 2.0](LICENSE)