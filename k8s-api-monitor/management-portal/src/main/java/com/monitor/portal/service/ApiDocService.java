package com.monitor.portal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API文档服务
 * 负责从API发现服务获取API信息，提供API文档展示和测试功能
 */
@Service
public class ApiDocService {

    private static final Logger logger = LoggerFactory.getLogger(ApiDocService.class);

    @Value("${api.discovery.service.url:http://api-discovery-service:8080}")
    private String apiDiscoveryServiceUrl;

    private final RestTemplate restTemplate;

    public ApiDocService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取所有服务的API信息
     */
    public List<Map<String, Object>> getAllApiDocs() {
        try {
            return restTemplate.getForObject(apiDiscoveryServiceUrl + "/apis/docs", List.class);
        } catch (Exception e) {
            logger.error("获取API文档信息失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取指定服务的API信息
     */
    public Map<String, Object> getServiceApiDocs(String serviceName) {
        try {
            return restTemplate.getForObject(apiDiscoveryServiceUrl + "/apis/docs/" + serviceName, Map.class);
        } catch (Exception e) {
            logger.error("获取服务[{}]的API文档信息失败", serviceName, e);
            return new HashMap<>();
        }
    }

    /**
     * 获取API调用历史记录
     */
    public List<Map<String, Object>> getApiCallHistory(String serviceName, String apiPath, Integer limit) {
        try {
            String url = apiDiscoveryServiceUrl + "/apis/history";
            if (serviceName != null && !serviceName.isEmpty()) {
                url += "?serviceName=" + serviceName;
                if (apiPath != null && !apiPath.isEmpty()) {
                    url += "&apiPath=" + apiPath;
                }
                if (limit != null) {
                    url += "&limit=" + limit;
                }
            }
            return restTemplate.getForObject(url, List.class);
        } catch (Exception e) {
            logger.error("获取API调用历史记录失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 测试API调用
     */
    public Map<String, Object> testApiCall(String serviceName, String apiPath, String method, 
                                          Map<String, String> headers, String requestBody) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("serviceName", serviceName);
            request.put("apiPath", apiPath);
            request.put("method", method);
            request.put("headers", headers);
            request.put("requestBody", requestBody);
            
            return restTemplate.postForObject(apiDiscoveryServiceUrl + "/apis/test", request, Map.class);
        } catch (Exception e) {
            logger.error("测试API调用失败: {}/{}", serviceName, apiPath, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", e.getMessage());
            return error;
        }
    }

    /**
     * 获取API统计信息
     */
    public Map<String, Object> getApiStats() {
        try {
            return restTemplate.getForObject(apiDiscoveryServiceUrl + "/apis/stats", Map.class);
        } catch (Exception e) {
            logger.error("获取API统计信息失败", e);
            return new HashMap<>();
        }
    }
}