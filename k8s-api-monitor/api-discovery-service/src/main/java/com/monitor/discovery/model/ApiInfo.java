package com.monitor.discovery.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API信息模型类
 * 用于存储API的基本信息，包括路径、方法、参数等
 */
public class ApiInfo {

    private String id;
    private String name;
    private String path;
    private String method; // GET, POST, PUT, DELETE等
    private String description;
    private String serviceId; // 所属服务ID
    private String serviceName; // 所属服务名称
    private String namespace; // 所属命名空间
    private List<ApiParameter> parameters; // API参数列表
    private ApiResponse response; // API响应信息
    private Map<String, String> metadata; // 元数据，如版本、标签等
    private LocalDateTime discoveredAt; // 发现时间
    private LocalDateTime lastUpdatedAt; // 最后更新时间
    private boolean deprecated; // 是否已废弃

    // 内部类：API参数
    public static class ApiParameter {
        private String name;
        private String type;
        private String description;
        private boolean required;
        private String defaultValue;
        private String location; // path, query, header, body等

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    // 内部类：API响应
    public static class ApiResponse {
        private String type;
        private String description;
        private Map<String, Object> schema;

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getSchema() {
            return schema;
        }

        public void setSchema(Map<String, Object> schema) {
            this.schema = schema;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<ApiParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ApiParameter> parameters) {
        this.parameters = parameters;
    }

    public ApiResponse getResponse() {
        return response;
    }

    public void setResponse(ApiResponse response) {
        this.response = response;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getDiscoveredAt() {
        return discoveredAt;
    }

    public void setDiscoveredAt(LocalDateTime discoveredAt) {
        this.discoveredAt = discoveredAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
}