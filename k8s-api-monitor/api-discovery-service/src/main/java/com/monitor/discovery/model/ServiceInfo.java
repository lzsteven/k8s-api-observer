package com.monitor.discovery.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 服务信息模型类
 * 用于存储Kubernetes服务的基本信息
 */
public class ServiceInfo {

    private String id;
    private String name;
    private String namespace;
    private String type; // ClusterIP, NodePort, LoadBalancer等
    private String clusterIP;
    private Map<String, String> selector; // 服务选择器
    private Map<String, String> labels; // 服务标签
    private List<ServicePort> ports; // 服务端口列表
    private Map<String, String> annotations; // 服务注解
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime lastUpdatedAt; // 最后更新时间
    private String status; // 服务状态
    private List<ApiInfo> apis; // 服务提供的API列表

    // 内部类：服务端口
    public static class ServicePort {
        private String name;
        private int port;
        private int targetPort;
        private String protocol; // TCP, UDP等
        private Integer nodePort; // NodePort类型服务特有

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getTargetPort() {
            return targetPort;
        }

        public void setTargetPort(int targetPort) {
            this.targetPort = targetPort;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public Integer getNodePort() {
            return nodePort;
        }

        public void setNodePort(Integer nodePort) {
            this.nodePort = nodePort;
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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClusterIP() {
        return clusterIP;
    }

    public void setClusterIP(String clusterIP) {
        this.clusterIP = clusterIP;
    }

    public Map<String, String> getSelector() {
        return selector;
    }

    public void setSelector(Map<String, String> selector) {
        this.selector = selector;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public List<ServicePort> getPorts() {
        return ports;
    }

    public void setPorts(List<ServicePort> ports) {
        this.ports = ports;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ApiInfo> getApis() {
        return apis;
    }

    public void setApis(List<ApiInfo> apis) {
        this.apis = apis;
    }
}