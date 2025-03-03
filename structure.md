
k8s-api-monitor/                # 项目根目录
│
├── api-discovery-service/      # API 发现服务模块
│   ├── src/main/java/com/monitor/discovery/
│   │   ├── ApiDiscoveryApplication.java        # 应用主类
│   │   ├── config/                             # 配置类目录
│   │   │   ├── KubernetesConfig.java           # K8s客户端配置
│   │   │   └── AppConfig.java                  # 应用全局配置
│   │   ├── model/                              # 数据模型
│   │   │   ├── ApiInfo.java                    # API信息模型类
│   │   │   └── ServiceInfo.java                # 服务信息模型类
│   │   ├── repository/                         # 数据访问层
│   │   │   └── ApiRepository.java              # API信息存储接口
│   │   ├── service/                            # 业务逻辑层
│   │   │   ├── KubernetesService.java          # K8s资源监听服务
│   │   │   ├── ApiDiscoveryService.java        # API发现服务
│   │   │   ├── SwaggerParserService.java       # Swagger文档解析服务
│   │   │   └── ApiStorageService.java          # API信息存储服务
│   │   ├── controller/                         # 控制器层
│   │   │   └── ApiDiscoveryController.java     # 对外提供API发现接口
│   │   └── util/                               # 工具类
│   │       └── HttpUtils.java                  # HTTP请求工具
│   ├── src/main/resources/
│   │   ├── application.yml                     # 应用配置文件
│   │   └── logback.xml                         # 日志配置
│   ├── Dockerfile                              # Docker构建文件
│   └── pom.xml                                 # Maven配置文件
│
├── api-gateway-service/        # API网关服务模块
│   ├── src/main/java/com/monitor/gateway/
│   │   ├── ApiGatewayApplication.java          # 应用主类
│   │   ├── config/                             # 配置类目录
│   │   │   ├── RouterConfig.java               # 路由配置
│   │   │   └── SecurityConfig.java             # 安全配置
│   │   ├── filter/                             # 网关过滤器
│   │   │   ├── AuthenticationFilter.java       # 认证过滤器
│   │   │   ├── RateLimitFilter.java            # 限流过滤器
│   │   │   └── LoggingFilter.java              # 日志记录过滤器
│   │   ├── service/                            # 业务逻辑层
│   │   │   ├── RouteService.java               # 路由服务
│   │   │   └── ApiRegistryService.java         # API注册服务
│   │   └── controller/                         # 控制器层
│   │       └── ApiGatewayController.java       # 网关管理接口
│   ├── src/main/resources/
│   │   ├── application.yml                     # 应用配置文件
│   │   └── logback.xml                         # 日志配置
│   ├── Dockerfile                              # Docker构建文件
│   └── pom.xml                                 # Maven配置文件
│
├── monitoring-core/            # 监控核心模块
│   ├── prometheus/                             # Prometheus相关配置
│   │   ├── prometheus.yml                      # Prometheus配置文件
│   │   └── alert-rules.yml                     # 告警规则配置
│   ├── grafana/                                # Grafana相关配置
│   │   ├── dashboards/                         # 仪表盘配置
│   │   │   ├── cluster-dashboard.json          # 集群监控仪表盘
│   │   │   ├── node-dashboard.json             # 节点监控仪表盘
│   │   │   └── api-dashboard.json              # API监控仪表盘
│   │   └── datasources/                        # 数据源配置
│   │       └── prometheus.yml                  # Prometheus数据源
│   └── alertmanager/                           # 告警管理器配置
│       └── alertmanager.yml                    # 告警配置文件
│
├── management-portal/          # 管理门户模块
│   ├── src/main/java/com/monitor/portal/
│   │   ├── ManagementPortalApplication.java    # 应用主类
│   │   ├── config/                             # 配置类目录
│   │   │   ├── WebConfig.java                  # Web配置
│   │   │   └── SecurityConfig.java             # 安全配置
│   │   ├── model/                              # 数据模型
│   │   │   ├── ContainerInfo.java              # 容器信息模型
│   │   │   └── DashboardData.java              # 仪表盘数据模型
│   │   ├── service/                            # 业务逻辑层
│   │   │   ├── ContainerService.java           # 容器管理服务
│   │   │   ├── MonitoringService.java          # 监控数据服务
│   │   │   ├── ApiDocService.java              # API文档服务
│   │   │   └── UserService.java                # 用户管理服务
│   │   ├── controller/                         # 控制器层
│   │   │   ├── DashboardController.java        # 仪表盘控制器
│   │   │   ├── ContainerController.java        # 容器管理控制器
│   │   │   ├── ApiDocController.java           # API文档控制器
│   │   │   └── UserController.java             # 用户管理控制器
│   │   └── util/                               # 工具类
│   │       └── ChartDataUtil.java              # 图表数据处理工具
│   ├── src/main/resources/
│   │   ├── application.yml                     # 应用配置文件
│   │   ├── templates/                          # 前端模板文件
│   │   │   ├── index.html                      # 主页模板
│   │   │   ├── dashboard/                      # 仪表盘页面
│   │   │   ├── containers/                     # 容器管理页面
│   │   │   └── apis/                           # API管理页面
│   │   └── static/                             # 静态资源
│   │       ├── css/                            # 样式文件
│   │       ├── js/                             # JavaScript文件
│   │       └── img/                            # 图片资源
│   ├── Dockerfile                              # Docker构建文件
│   └── pom.xml                                 # Maven配置文件
│
├── kubernetes/                 # Kubernetes部署配置
│   ├── api-discovery/
│   │   ├── deployment.yaml                     # 部署配置
│   │   ├── service.yaml                        # 服务配置
│   │   └── configmap.yaml                      # 配置项
│   ├── api-gateway/
│   │   ├── deployment.yaml                     # 部署配置
│   │   ├── service.yaml                        # 服务配置
│   │   └── configmap.yaml                      # 配置项
│   ├── monitoring-core/
│   │   ├── prometheus-operator/                # Prometheus Operator配置
│   │   └── custom-resources/                   # 自定义资源配置
│   ├── management-portal/
│   │   ├── deployment.yaml                     # 部署配置
│   │   ├── service.yaml                        # 服务配置
│   │   └── configmap.yaml                      # 配置项
│   └── common/
│       ├── namespace.yaml                      # 命名空间配置
│       ├── rbac/                               # 权限配置
│       │   ├── service-account.yaml            # 服务账号
│       │   ├── role.yaml                       # 角色定义
│       │   └── role-binding.yaml               # 角色绑定
│       └── secrets/                            # 密钥配置
│           └── api-keys.yaml                   # API密钥
│
├── scripts/                    # 脚本目录
│   ├── deploy.sh                               # 部署脚本
│   ├── cleanup.sh                              # 清理脚本
│   ├── update.sh                               # 更新脚本
│   └── monitoring/
│       ├── backup-prometheus.sh                # Prometheus数据备份脚本
│       └── check-system.sh                     # 系统检查脚本
│
└── docs/                       # 文档目录
    ├── architecture.md                         # 架构设计文档
    ├── api-reference.md                        # API参考文档
    ├── user-guide.md                           # 用户指南
    └── images/                                 # 文档图片资源
        └── architecture-diagram.png            # 架构图
文件结构说明
1. API 发现服务 (api-discovery-service)
这个模块负责发现和收集 Kubernetes 集群中各容器提供的 API 信息。

ApiDiscoveryApplication.java: 应用入口点，初始化 Spring Boot 应用
config/: 配置类，包含 Kubernetes 客户端和应用全局配置
model/: 数据模型类，定义 API 和服务信息的结构
repository/: 数据访问层，定义存储 API 信息的接口
service/:
KubernetesService.java: 监听 Kubernetes 资源变化
ApiDiscoveryService.java: API 发现的核心逻辑
SwaggerParserService.java: 解析 Swagger/OpenAPI 文档
ApiStorageService.java: 存储和管理 API 信息
controller/: REST API 控制器，提供 API 信息查询接口
util/: 工具类，包含 HTTP 请求工具等
2. API 网关服务 (api-gateway-service)
这个模块提供统一的 API 访问入口，路由请求到相应的后端服务。

ApiGatewayApplication.java: 应用入口点
config/:
RouterConfig.java: 定义路由规则
SecurityConfig.java: 安全配置，如认证授权策略
filter/: 网关过滤器，处理请求和响应
AuthenticationFilter.java: 身份验证
RateLimitFilter.java: API 调用限流
LoggingFilter.java: 请求日志记录
service/:
RouteService.java: 动态路由管理
ApiRegistryService.java: 连接 API 发现服务，获取 API 信息
controller/: 网关管理接口，提供路由配置等功能
3. 监控核心 (monitoring-core)
这个模块包含 Prometheus、Grafana 和 Alertmanager 的配置，提供集群监控能力。

prometheus/: Prometheus 配置文件和告警规则
grafana/:
dashboards/: 预定义的监控仪表盘
datasources/: 数据源配置
alertmanager/: 告警管理器配置，定义告警通知渠道和规则
4. 管理门户 (management-portal)
这个模块提供统一的 Web 界面，整合容器管理、监控视图和 API 管理功能。

ManagementPortalApplication.java: 应用入口点
config/: Web 和安全配置
model/: 数据模型，如容器信息、仪表盘数据
service/:
ContainerService.java: 容器管理服务
MonitoringService.java: 从 Prometheus 获取监控数据
ApiDocService.java: API 文档管理
UserService.java: 用户管理
controller/: Web 控制器，处理前端请求
templates/: Thymeleaf 模板文件，定义 Web 页面结构
static/: 静态资源，如 CSS、JavaScript 和图片
5. Kubernetes 部署配置 (kubernetes)
这个目录包含 Kubernetes 资源定义文件，用于部署平台各组件。

api-discovery/: API 发现服务的部署配置
api-gateway/: API 网关服务的部署配置
monitoring-core/: 监控组件的部署配置
management-portal/: 管理门户的部署配置
common/: 公共资源，如命名空间、RBAC 配置和密钥
6. 脚本 (scripts)
包含部署、更新和维护平台的脚本。

deploy.sh: 部署整个平台的脚本
cleanup.sh: 清理资源的脚本
update.sh: 更新组件的脚本
monitoring/: 监控相关的维护脚本
7. 文档 (docs)
项目文档，包含架构设计、API 参考和用户指南。

architecture.md: 架构设计文档
api-reference.md: API 参考文档
user-guide.md: 用户指南
images/: 文档中使用的图片资源
文件之间的关系与交互
API 发现服务 监听 Kubernetes 集群中的服务变化，收集 API 信息并存储。

API 网关服务 从 API 发现服务获取 API 路由信息，配置动态路由规则，并处理外部 API 调用请求。

监控核心 收集集群和应用指标，提供给管理门户和告警系统使用。

管理门户 整合以上所有组件的功能，提供统一的用户界面，包括：

调用 API 发现服务获取 API 信息
通过 API 网关代理用户的 API 调用
从监控核心获取性能指标和健康状态
管理容器的生命周期
Kubernetes 部署配置 定义了所有组件如何在 Kubernetes 集群中部署和运行，包括资源分配、网络配置和权限设置。

脚本 提供自动化部署和维护功能，简化运维工作。