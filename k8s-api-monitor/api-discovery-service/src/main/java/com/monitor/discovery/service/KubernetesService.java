package com.monitor.discovery.service;

import com.monitor.discovery.model.ServiceInfo;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kubernetes资源监听服务
 * 负责监听Kubernetes集群中的服务变化
 */
@Service
public class KubernetesService {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesService.class);

    @Autowired
    private KubernetesClient kubernetesClient;

    @Autowired
    private ApiDiscoveryService apiDiscoveryService;

    private final Map<String, Watch> watches = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        startWatchingServices();
    }

    @PreDestroy
    public void cleanup() {
        stopWatchingServices();
    }

    /**
     * 开始监听服务变化
     */
    public void startWatchingServices() {
        logger.info("开始监听Kubernetes服务变化");
        Watch watch = kubernetesClient.services().inAnyNamespace().watch(new Watcher<Service>() {
            @Override
            public void eventReceived(Action action, Service service) {
                handleServiceEvent(action, service);
            }

            @Override
            public void onClose(WatcherException e) {
                if (e != null) {
                    logger.error("服务监听异常关闭: {}", e.getMessage());
                    restartWatching();
                }
            }
        });
        watches.put("services", watch);
    }

    /**
     * 停止监听服务变化
     */
    public void stopWatchingServices() {
        logger.info("停止监听Kubernetes服务变化");
        watches.values().forEach(Watch::close);
        watches.clear();
    }

    /**
     * 重启监听
     */
    private void restartWatching() {
        logger.info("重启服务监听");
        stopWatchingServices();
        startWatchingServices();
    }

    /**
     * 处理服务事件
     */
    private void handleServiceEvent(Watcher.Action action, Service service) {
        String serviceName = service.getMetadata().getName();
        String namespace = service.getMetadata().getNamespace();
        logger.info("接收到服务事件: {} {}/{}", action, namespace, serviceName);

        ServiceInfo serviceInfo = convertToServiceInfo(service);

        switch (action) {
            case ADDED:
            case MODIFIED:
                apiDiscoveryService.processService(serviceInfo);
                break;
            case DELETED:
                apiDiscoveryService.removeService(serviceInfo.getId());
                break;
            default:
                logger.warn("未处理的服务事件类型: {}", action);
        }
    }

    /**
     * 将Kubernetes Service转换为ServiceInfo
     */
    private ServiceInfo convertToServiceInfo(Service service) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(service.getMetadata().getUid());
        serviceInfo.setName(service.getMetadata().getName());
        serviceInfo.setNamespace(service.getMetadata().getNamespace());
        serviceInfo.setType(service.getSpec().getType());
        serviceInfo.setClusterIP(service.getSpec().getClusterIP());
        serviceInfo.setSelector(service.getSpec().getSelector());
        serviceInfo.setLabels(service.getMetadata().getLabels());
        serviceInfo.setAnnotations(service.getMetadata().getAnnotations());
        serviceInfo.setCreatedAt(LocalDateTime.now());
        serviceInfo.setLastUpdatedAt(LocalDateTime.now());
        serviceInfo.setStatus("Active");

        // 转换端口信息
        List<ServiceInfo.ServicePort> ports = new ArrayList<>();
        if (service.getSpec().getPorts() != null) {
            service.getSpec().getPorts().forEach(port -> {
                ServiceInfo.ServicePort servicePort = new ServiceInfo.ServicePort();
                servicePort.setName(port.getName());
                servicePort.setPort(port.getPort());
                servicePort.setTargetPort(port.getTargetPort().getIntVal());
                servicePort.setProtocol(port.getProtocol());
                servicePort.setNodePort(port.getNodePort());
                ports.add(servicePort);
            });
        }
        serviceInfo.setPorts(ports);

        return serviceInfo;
    }

    /**
     * 获取所有服务
     */
    public List<Service> getAllServices() {
        return kubernetesClient.services().inAnyNamespace().list().getItems();
    }
}