package com.monitor.gateway.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URI;

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

    public List<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routes = new ArrayList<>();
        apiRegistry.forEach((serviceName, apis) -> {
            apis.forEach(api -> {
                RouteDefinition route = new RouteDefinition();
                route.setId(serviceName + "-" + api.hashCode());
                
                // 设置断言
                PredicateDefinition predicate = new PredicateDefinition();
                predicate.setName("Path");
                predicate.addArg("pattern", api);
                route.setPredicates(Collections.singletonList(predicate));
                
                // 设置过滤器
                FilterDefinition filter = new FilterDefinition();
                filter.setName("StripPrefix");
                filter.addArg("parts", "1");
                route.setFilters(Collections.singletonList(filter));
                
                // 设置URI
                route.setUri(URI.create("lb://" + serviceName));
                
                routes.add(route);
            });
        });
        return routes;
    }
}