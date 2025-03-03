package com.monitor.gateway.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiRegistryService {

    @Value("${api.discovery.service.url}")
    private String apiDiscoveryServiceUrl;

    private final RestTemplate restTemplate;
    private final Map<String, List<String>> apiRegistry = new ConcurrentHashMap<>();

    public ApiRegistryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedDelay = 60000) // 每分钟更新一次API注册信息
    public void updateApiRegistry() {
        try {
            Map<String, List<String>> updatedRegistry = restTemplate.getForObject(apiDiscoveryServiceUrl + "/apis", Map.class);
            if (updatedRegistry != null) {
                apiRegistry.clear();
                apiRegistry.putAll(updatedRegistry);
            }
        } catch (Exception e) {
            // 记录错误，但不中断服务
            e.printStackTrace();
        }
    }

    public List<String> getApisByService(String serviceName) {
        return apiRegistry.get(serviceName);
    }

    public Map<String, List<String>> getAllApis() {
        return apiRegistry;
    }
}