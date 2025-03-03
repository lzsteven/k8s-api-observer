package com.monitor.portal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控服务
 * 负责从Prometheus获取监控数据，提供集群和容器的性能指标
 */
@Service
public class MonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);

    @Value("${prometheus.url:http://prometheus:9090}")
    private String prometheusUrl;

    private final RestTemplate restTemplate;

    public MonitoringService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取节点CPU使用率
     */
    public Map<String, Object> getNodeCpuUsage(String nodeName, String timeRange) {
        String query = String.format("100 - (avg by (instance) (irate(node_cpu_seconds_total{mode=\"idle\",instance=\"%s\"}[5m])) * 100)", nodeName);
        return executePrometheusQuery(query, timeRange);
    }

    /**
     * 获取节点内存使用率
     */
    public Map<String, Object> getNodeMemoryUsage(String nodeName, String timeRange) {
        String query = String.format("(node_memory_MemTotal_bytes{instance=\"%s\"} - node_memory_MemAvailable_bytes{instance=\"%s\"}) / node_memory_MemTotal_bytes{instance=\"%s\"} * 100", 
                                    nodeName, nodeName, nodeName);
        return executePrometheusQuery(query, timeRange);
    }

    /**
     * 获取节点磁盘使用率
     */
    public Map<String, Object> getNodeDiskUsage(String nodeName, String timeRange) {
        String query = String.format("(node_filesystem_size_bytes{instance=\"%s\",mountpoint=\"/\"} - node_filesystem_free_bytes{instance=\"%s\",mountpoint=\"/\"}) / node_filesystem_size_bytes{instance=\"%s\",mountpoint=\"/\"} * 100", 
                                    nodeName, nodeName, nodeName);
        return executePrometheusQuery(query, timeRange);
    }

    /**
     * 获取节点网络I/O
     */
    public Map<String, Object> getNodeNetworkIO(String nodeName, String timeRange) {
        Map<String, Object> result = new HashMap<>();
        
        // 网络接收速率
        String receiveQuery = String.format("rate(node_network_receive_bytes_total{instance=\"%s\"}[5m])", nodeName);
        result.put("receive", executePrometheusQuery(receiveQuery, timeRange));
        
        // 网络发送速率
        String transmitQuery = String.format("rate(node_network_transmit_bytes_total{instance=\"%s\"}[5m])", nodeName);
        result.put("transmit", executePrometheusQuery(transmitQuery, timeRange));
        
        return result;
    }

    /**
     * 获取容器CPU使用率
     */
    public Map<String, Object> getContainerCpuUsage(String namespace, String podName, String containerName, String timeRange) {
        String query = String.format("sum(rate(container_cpu_usage_seconds_total{namespace=\"%s\",pod=\"%s\",container=\"%s\"}[5m])) / sum(container_spec_cpu_quota{namespace=\"%s\",pod=\"%s\",container=\"%s\"} / container_spec_cpu_period{namespace=\"%s\",pod=\"%s\",container=\"%s\"}) * 100", 
                                    namespace, podName, containerName, namespace, podName, containerName, namespace, podName, containerName);
        return executePrometheusQuery(query, timeRange);
    }

    /**
     * 获取容器内存使用率
     */
    public Map<String, Object> getContainerMemoryUsage(String namespace, String podName, String containerName, String timeRange) {
        String query = String.format("sum(container_memory_working_set_bytes{namespace=\"%s\",pod=\"%s\",container=\"%s\"}) / sum(container_spec_memory_limit_bytes{namespace=\"%s\",pod=\"%s\",container=\"%s\"}) * 100", 
                                    namespace, podName, containerName, namespace, podName, containerName);
        return executePrometheusQuery(query, timeRange);
    }

    /**
     * 获取集群节点状态概览
     */
    public Map<String, Object> getClusterOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // 获取节点总数
            String nodeCountQuery = "count(kube_node_info)";
            Map<String, Object> nodeCountResult = executePrometheusQuery(nodeCountQuery, "5m");
            overview.put("nodeCount", extractLatestValue(nodeCountResult));
            
            // 获取Pod总数
            String podCountQuery = "count(kube_pod_info)";
            Map<String, Object> podCountResult = executePrometheusQuery(podCountQuery, "5m");
            overview.put("podCount", extractLatestValue(podCountResult));
            
            // 获取容器总数
            String containerCountQuery = "count(kube_pod_container_info)";
            Map<String, Object> containerCountResult = executePrometheusQuery(containerCountQuery, "5m");
            overview.put("containerCount", extractLatestValue(containerCountResult));
            
            // 获取集群CPU使用率
            String cpuUsageQuery = "sum(rate(node_cpu_seconds_total{mode!=\"idle\"}[5m])) / sum(machine_cpu_cores) * 100";
            Map<String, Object> cpuUsageResult = executePrometheusQuery(cpuUsageQuery, "5m");
            overview.put("cpuUsage", extractLatestValue(cpuUsageResult));
            
            // 获取集群内存使用率
            String memoryUsageQuery = "sum(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / sum(node_memory_MemTotal_bytes) * 100";
            Map<String, Object> memoryUsageResult = executePrometheusQuery(memoryUsageQuery, "5m");
            overview.put("memoryUsage", extractLatestValue(memoryUsageResult));
            
            // 获取集群磁盘使用率
            String diskUsageQuery = "sum(node_filesystem_size_bytes{mountpoint=\"/\"} - node_filesystem_free_bytes{mountpoint=\"/\"}) / sum(node_filesystem_size_bytes{mountpoint=\"/\"}) * 100";
            Map<String, Object> diskUsageResult = executePrometheusQuery(diskUsageQuery, "5m");
            overview.put("diskUsage", extractLatestValue(diskUsageResult));
            
        } catch (Exception e) {
            logger.error("获取集群概览信息失败", e);
        }
        
        return overview;
    }

    /**
     * 获取告警信息
     */
    public List<Map<String, Object>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        try {
            String url = prometheusUrl + "/api/v1/alerts";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "success".equals(response.get("status"))) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) ((Map<String, Object>) response.get("data")).get("alerts");
                
                for (Map<String, Object> alert : data) {
                    Map<String, Object> alertInfo = new HashMap<>();
                    Map<String, String> labels = (Map<String, String>) alert.get("labels");
                    Map<String, String> annotations = (Map<String, String>) alert.get("annotations");
                    
                    alertInfo.put("name", labels.get("alertname"));
                    alertInfo.put("severity", labels.get("severity"));
                    alertInfo.put("summary", annotations.get("summary"));
                    alertInfo.put("description", annotations.get("description"));
                    alertInfo.put("state", alert.get("state"));
                    alertInfo.put("activeAt", alert.get("activeAt"));
                    
                    alerts.add(alertInfo);
                }
            }
        } catch (Exception e) {
            logger.error("获取告警信息失败", e);
        }
        
        return alerts;
    }

    /**
     * 执行Prometheus查询
     */
    private Map<String, Object> executePrometheusQuery(String query, String timeRange) {
        Map<String, Object> result = new HashMap<>();
        List<List<Object>> dataPoints = new ArrayList<>();
        
        try {
            // 计算时间范围
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(parseTimeRange(timeRange), ChronoUnit.SECONDS);
            
            // 构建查询URL
            String url = String.format("%s/api/v1/query_range?query=%s&start=%d&end=%d&step=15s", 
                                      prometheusUrl, 
                                      query, 
                                      startTime.getEpochSecond(), 
                                      endTime.getEpochSecond());
            
            // 执行查询
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "success".equals(response.get("status"))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("result");
                
                if (results != null && !results.isEmpty()) {
                    for (Map<String, Object> metricData : results) {
                        Map<String, String> metric = (Map<String, String>) metricData.get("metric");
                        List<List<Object>> values = (List<List<Object>>) metricData.get("values");
                        
                        // 添加指标标签信息
                        result.put("metric", metric);
                        
                        // 转换数据点
                        for (List<Object> value : values) {
                            Double timestamp = Double.parseDouble(value.get(0).toString()) * 1000; // 转换为毫秒
                            Double metricValue = Double.parseDouble(value.get(1).toString());
                            List<Object> point = new ArrayList<>();
                            point.add(timestamp);
                            point.add(metricValue);
                            dataPoints.add(point);
                        }
                    }
                }
            }
            
            result.put("dataPoints", dataPoints);
            
        } catch (Exception e) {
            logger.error("执行Prometheus查询失败: {}", query, e);
        }
        
        return result;
    }

    /**
     * 解析时间范围字符串为秒数
     */
    private long parseTimeRange(String timeRange) {
        if (timeRange == null || timeRange.isEmpty()) {
            return 3600; // 默认1小时
        }
        
        try {
            if (timeRange.endsWith("m")) {
                return Long.parseLong(timeRange.substring(0, timeRange.length() - 1)) * 60;
            } else if (timeRange.endsWith("h")) {
                return Long.parseLong(timeRange.substring(0, timeRange.length() - 1)) * 3600;
            } else if (timeRange.endsWith("d")) {
                return Long.parseLong(timeRange.substring(0, timeRange.length() - 1)) * 86400;
            } else {
                return Long.parseLong(timeRange);
            }
        } catch (NumberFormatException e) {
            logger.warn("无法解析时间范围: {}, 使用默认值1小时", timeRange);
            return 3600;
        }
    }

    /**
     * 从查询结果中提取最新的值
     */
    private Double extractLatestValue(Map<String, Object> queryResult) {
        List<List<Object>> dataPoints = (List<List<Object>>) queryResult.get("dataPoints");
        if (dataPoints != null && !dataPoints.isEmpty()) {
            List<Object> latestPoint = dataPoints.get(dataPoints.size() - 1);
            return Double.parseDouble(latestPoint.get(1).toString());
        }
        return 0.0;
    }
}