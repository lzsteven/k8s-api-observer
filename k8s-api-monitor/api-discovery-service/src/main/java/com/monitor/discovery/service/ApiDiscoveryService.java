package com.monitor.discovery.service;

import com.monitor.discovery.model.ApiInfo;
import com.monitor.discovery.model.ServiceInfo;
import com.monitor.discovery.repository.ApiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API发现服务
 * 负责处理服务信息和发现API
 */
@Service
public class ApiDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(ApiDiscoveryService.class);

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private SwaggerParserService swaggerParserService;

    // 缓存已处理的服务，避免重复处理
    private final Map<String, LocalDateTime> processedServices = new ConcurrentHashMap<>();

    /**
     * 处理服务信息
     * @param serviceInfo 服务信息
     */
    public void processService(ServiceInfo serviceInfo) {
        logger.info("处理服务: {}/{}", serviceInfo.getNamespace(), serviceInfo.getName());

        // 保存服务信息
        ServiceInfo savedService = apiRepository.saveService(serviceInfo);

        // 检查服务是否有API文档注解
        if (hasApiDocAnnotation(serviceInfo)) {
            // 异步发现API
            discoverApis(savedService);
        } else {
            logger.info("服务 {}/{} 没有API文档注解，跳过API发现", serviceInfo.getNamespace(), serviceInfo.getName());
        }

        // 更新处理时间
        processedServices.put(serviceInfo.getId(), LocalDateTime.now());
    }

    /**
     * 检查服务是否有API文档注解
     */
    private boolean hasApiDocAnnotation(ServiceInfo serviceInfo) {
        Map<String, String> annotations = serviceInfo.getAnnotations();
        if (annotations == null) {
            return false;
        }

        // 检查常见的API文档注解
        return annotations.containsKey("swagger.io/path") || 
               annotations.containsKey("api.monitor/doc-path") ||
               annotations.containsKey("springdoc.openapi") ||
               annotations.containsKey("openapi.path");
    }

    /**
     * 发现服务的API
     */
    private void discoverApis(ServiceInfo serviceInfo) {
        try {
            logger.info("开始发现服务 {}/{} 的API", serviceInfo.getNamespace(), serviceInfo.getName());

            // 获取API文档路径
            String apiDocPath = getApiDocPath(serviceInfo);
            if (apiDocPath == null) {
                logger.warn("无法确定服务 {}/{} 的API文档路径", serviceInfo.getNamespace(), serviceInfo.getName());
                return;
            }

            // 构建API文档URL
            String apiDocUrl = buildApiDocUrl(serviceInfo, apiDocPath);

            // 解析API文档
            List<ApiInfo> apis = swaggerParserService.parseApiDoc(apiDocUrl, serviceInfo);
            if (apis.isEmpty()) {
                logger.warn("服务 {}/{} 未发现API", serviceInfo.getNamespace(), serviceInfo.getName());
                return;
            }

            // 保存API信息
            List<ApiInfo> savedApis = apiRepository.saveApis(apis);
            logger.info("服务 {}/{} 发现并保存了 {} 个API", serviceInfo.getNamespace(), serviceInfo.getName(), savedApis.size());

            // 更新服务的API列表
            serviceInfo.setApis(savedApis);
            apiRepository.saveService(serviceInfo);

        } catch (Exception e) {
            logger.error("发现服务 {}/{} 的API时出错: {}", serviceInfo.getNamespace(), serviceInfo.getName(), e.getMessage(), e);
        }
    }

    /**
     * 获取API文档路径
     */
    private String getApiDocPath(ServiceInfo serviceInfo) {
        Map<String, String> annotations = serviceInfo.getAnnotations();
        if (annotations == null) {
            return "/v3/api-docs"; // 默认Spring Doc路径
        }

        // 按优先级检查注解
        if (annotations.containsKey("api.monitor/doc-path")) {
            return annotations.get("api.monitor/doc-path");
        }
        if (annotations.containsKey("swagger.io/path")) {
            return annotations.get("swagger.io/path");
        }
        if (annotations.containsKey("springdoc.openapi")) {
            return annotations.get("springdoc.openapi");
        }
        if (annotations.containsKey("openapi.path")) {
            return annotations.get("openapi.path");
        }

        // 默认路径
        return "/v3/api-docs";
    }

    /**
     * 构建API文档URL
     */
    private String buildApiDocUrl(ServiceInfo serviceInfo, String apiDocPath) {
        // 构建服务URL
        String serviceUrl = String.format("http://%s.%s.svc:8080", 
                serviceInfo.getName(), 
                serviceInfo.getNamespace());

        // 如果服务有特定端口，使用第一个端口
        if (serviceInfo.getPorts() != null && !serviceInfo.getPorts().isEmpty()) {
            ServiceInfo.ServicePort port = serviceInfo.getPorts().get(0);
            serviceUrl = String.format("http://%s.%s.svc:%d", 
                    serviceInfo.getName(), 
                    serviceInfo.getNamespace(),
                    port.getPort());
        }

        // 确保路径以/开头
        if (!apiDocPath.startsWith("/")) {
            apiDocPath = "/" + apiDocPath;
        }

        return serviceUrl + apiDocPath;
    }

    /**
     * 移除服务及其API
     */
    public void removeService(String serviceId) {
        logger.info("移除服务: {}", serviceId);

        // 删除服务的所有API
        apiRepository.deleteApisByServiceId(serviceId);

        // 删除服务
        apiRepository.deleteService(serviceId);

        // 从处理缓存中移除
        processedServices.remove(serviceId);
    }

    /**
     * 定期重新扫描服务的API
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000)
    public void scheduledApiDiscovery() {
        logger.info("开始定期API发现任务");

        List<ServiceInfo> services = apiRepository.findAllServices();
        for (ServiceInfo service : services) {
            if (hasApiDocAnnotation(service)) {
                discoverApis(service);
            }
        }

        logger.info("定期API发现任务完成，处理了 {} 个服务", services.size());
    }

    /**
     * 获取服务的API列表
     */
    public List<ApiInfo> getServiceApis(String serviceId) {
        return apiRepository.findApisByServiceId(serviceId);
    }

    /**
     * 获取命名空间的所有API
     */
    public List<ApiInfo> getNamespaceApis(String namespace) {
        return apiRepository.findApisByNamespace(namespace);
    }

    /**
     * 获取所有API
     */
    public List<ApiInfo> getAllApis() {
        return apiRepository.findAllApis();
    }

    /**
     * 获取所有服务
     */
    public List<ServiceInfo> getAllServices() {
        return apiRepository.findAllServices();
    }

    /**
     * 获取服务信息
     */
    public Optional<ServiceInfo> getServiceInfo(String serviceId) {
        return apiRepository.findServiceById(serviceId);
    }
}