package com.monitor.discovery.service;

import com.monitor.discovery.model.ApiInfo;
import com.monitor.discovery.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Swagger文档解析服务
 * 负责解析OpenAPI/Swagger文档，提取API信息
 */
@Service
public class SwaggerParserService {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerParserService.class);

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 解析API文档
     * @param apiDocUrl API文档URL
     * @param serviceInfo 服务信息
     * @return API信息列表
     */
    public List<ApiInfo> parseApiDoc(String apiDocUrl, ServiceInfo serviceInfo) {
        try {
            logger.info("开始解析API文档: {}", apiDocUrl);

            // 获取API文档
            ResponseEntity<Map> response = restTemplate.getForEntity(apiDocUrl, Map.class);
            Map<String, Object> apiDoc = response.getBody();
            if (apiDoc == null) {
                logger.warn("无法获取API文档: {}", apiDocUrl);
                return Collections.emptyList();
            }

            List<ApiInfo> apis = new ArrayList<>();

            // 解析路径
            Map<String, Object> paths = (Map<String, Object>) apiDoc.get("paths");
            if (paths != null) {
                paths.forEach((path, methodsObj) -> {
                    Map<String, Object> methods = (Map<String, Object>) methodsObj;
                    methods.forEach((method, operationObj) -> {
                        Map<String, Object> operation = (Map<String, Object>) operationObj;
                        ApiInfo apiInfo = parseOperation(path, method, operation, serviceInfo);
                        apis.add(apiInfo);
                    });
                });
            }

            logger.info("API文档解析完成，发现 {} 个API", apis.size());
            return apis;

        } catch (Exception e) {
            logger.error("解析API文档时出错: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析API操作信息
     */
    private ApiInfo parseOperation(String path, String method, Map<String, Object> operation, ServiceInfo serviceInfo) {
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setId(UUID.randomUUID().toString());
        apiInfo.setPath(path);
        apiInfo.setMethod(method.toUpperCase());
        apiInfo.setName((String) operation.get("summary"));
        apiInfo.setDescription((String) operation.get("description"));
        apiInfo.setServiceId(serviceInfo.getId());
        apiInfo.setServiceName(serviceInfo.getName());
        apiInfo.setNamespace(serviceInfo.getNamespace());
        apiInfo.setDiscoveredAt(LocalDateTime.now());
        apiInfo.setLastUpdatedAt(LocalDateTime.now());
        apiInfo.setDeprecated(Boolean.TRUE.equals(operation.get("deprecated")));

        // 解析参数
        List<ApiInfo.ApiParameter> parameters = parseParameters(operation);
        apiInfo.setParameters(parameters);

        // 解析响应
        ApiInfo.ApiResponse response = parseResponse(operation);
        apiInfo.setResponse(response);

        // 设置元数据
        Map<String, String> metadata = new HashMap<>();
        metadata.put("operationId", (String) operation.get("operationId"));
        metadata.put("tags", String.join(",", (List<String>) operation.getOrDefault("tags", Collections.emptyList())));
        apiInfo.setMetadata(metadata);

        return apiInfo;
    }

    /**
     * 解析API参数
     */
    private List<ApiInfo.ApiParameter> parseParameters(Map<String, Object> operation) {
        List<ApiInfo.ApiParameter> parameters = new ArrayList<>();
        List<Map<String, Object>> parametersList = (List<Map<String, Object>>) operation.getOrDefault("parameters", Collections.emptyList());

        for (Map<String, Object> parameterMap : parametersList) {
            ApiInfo.ApiParameter parameter = new ApiInfo.ApiParameter();
            parameter.setName((String) parameterMap.get("name"));
            parameter.setDescription((String) parameterMap.get("description"));
            parameter.setRequired(Boolean.TRUE.equals(parameterMap.get("required")));
            parameter.setLocation((String) parameterMap.get("in"));

            Map<String, Object> schema = (Map<String, Object>) parameterMap.get("schema");
            if (schema != null) {
                parameter.setType((String) schema.get("type"));
                parameter.setDefaultValue(String.valueOf(schema.get("default")));
            }

            parameters.add(parameter);
        }

        return parameters;
    }

    /**
     * 解析API响应
     */
    private ApiInfo.ApiResponse parseResponse(Map<String, Object> operation) {
        ApiInfo.ApiResponse response = new ApiInfo.ApiResponse();
        Map<String, Object> responses = (Map<String, Object>) operation.get("responses");

        if (responses != null && !responses.isEmpty()) {
            // 获取默认响应或200响应
            Map<String, Object> defaultResponse = (Map<String, Object>) responses.getOrDefault("200", responses.get("default"));
            if (defaultResponse != null) {
                response.setDescription((String) defaultResponse.get("description"));
                Map<String, Object> content = (Map<String, Object>) defaultResponse.get("content");
                if (content != null) {
                    // 获取第一个内容类型的schema
                    String firstContentType = content.keySet().iterator().next();
                    Map<String, Object> mediaType = (Map<String, Object>) content.get(firstContentType);
                    response.setType(firstContentType);
                    response.setSchema((Map<String, Object>) mediaType.get("schema"));
                }
            }
        }

        return response;
    }
}