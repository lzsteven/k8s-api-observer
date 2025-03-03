package com.monitor.portal.service;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 容器管理服务
 * 提供容器状态查看和管理功能
 */
@Service
public class ContainerService {

    private static final Logger logger = LoggerFactory.getLogger(ContainerService.class);

    @Autowired
    private KubernetesClient kubernetesClient;

    /**
     * 获取所有容器信息
     */
    public List<Map<String, Object>> getAllContainers() {
        List<Map<String, Object>> containers = new ArrayList<>();
        try {
            List<Pod> pods = kubernetesClient.pods().inAnyNamespace().list().getItems();
            for (Pod pod : pods) {
                String podName = pod.getMetadata().getName();
                String namespace = pod.getMetadata().getNamespace();
                String podStatus = pod.getStatus().getPhase();
                String nodeName = pod.getSpec().getNodeName();

                for (Container container : pod.getSpec().getContainers()) {
                    Map<String, Object> containerInfo = new HashMap<>();
                    containerInfo.put("name", container.getName());
                    containerInfo.put("image", container.getImage());
                    containerInfo.put("podName", podName);
                    containerInfo.put("namespace", namespace);
                    containerInfo.put("podStatus", podStatus);
                    containerInfo.put("nodeName", nodeName);

                    // 获取容器状态
                    pod.getStatus().getContainerStatuses().stream()
                            .filter(status -> status.getName().equals(container.getName()))
                            .findFirst()
                            .ifPresent(status -> {
                                containerInfo.put("ready", status.getReady());
                                containerInfo.put("restartCount", status.getRestartCount());
                                containerInfo.put("started", status.getStarted());
                                
                                if (status.getState().getRunning() != null) {
                                    containerInfo.put("state", "Running");
                                    containerInfo.put("startedAt", status.getState().getRunning().getStartedAt());
                                } else if (status.getState().getWaiting() != null) {
                                    containerInfo.put("state", "Waiting");
                                    containerInfo.put("reason", status.getState().getWaiting().getReason());
                                } else if (status.getState().getTerminated() != null) {
                                    containerInfo.put("state", "Terminated");
                                    containerInfo.put("exitCode", status.getState().getTerminated().getExitCode());
                                    containerInfo.put("reason", status.getState().getTerminated().getReason());
                                }
                            });

                    containers.add(containerInfo);
                }
            }
        } catch (Exception e) {
            logger.error("获取容器信息失败", e);
        }
        return containers;
    }

    /**
     * 获取容器日志
     */
    public String getContainerLogs(String namespace, String podName, String containerName) {
        try {
            return kubernetesClient.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .inContainer(containerName)
                    .getLog();
        } catch (Exception e) {
            logger.error("获取容器日志失败: {}/{}/{}", namespace, podName, containerName, e);
            return "获取日志失败: " + e.getMessage();
        }
    }

    /**
     * 重启容器
     */
    public boolean restartContainer(String namespace, String podName) {
        try {
            // Kubernetes没有直接重启容器的API，需要删除Pod让它重新创建
            // 注意：这只适用于由控制器管理的Pod（如Deployment）
            kubernetesClient.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .delete();
            logger.info("已发送重启命令: {}/{}", namespace, podName);
            return true;
        } catch (Exception e) {
            logger.error("重启容器失败: {}/{}", namespace, podName, e);
            return false;
        }
    }

    /**
     * 获取容器资源使用情况
     */
    public Map<String, Object> getContainerResources(String namespace, String podName, String containerName) {
        Map<String, Object> resources = new HashMap<>();
        try {
            Pod pod = kubernetesClient.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .get();

            if (pod != null) {
                pod.getSpec().getContainers().stream()
                        .filter(c -> c.getName().equals(containerName))
                        .findFirst()
                        .ifPresent(container -> {
                            if (container.getResources() != null) {
                                if (container.getResources().getLimits() != null) {
                                    resources.put("cpuLimit", container.getResources().getLimits().get("cpu"));
                                    resources.put("memoryLimit", container.getResources().getLimits().get("memory"));
                                }
                                if (container.getResources().getRequests() != null) {
                                    resources.put("cpuRequest", container.getResources().getRequests().get("cpu"));
                                    resources.put("memoryRequest", container.getResources().getRequests().get("memory"));
                                }
                            }
                        });
            }
        } catch (Exception e) {
            logger.error("获取容器资源信息失败: {}/{}/{}", namespace, podName, containerName, e);
        }
        return resources;
    }
}