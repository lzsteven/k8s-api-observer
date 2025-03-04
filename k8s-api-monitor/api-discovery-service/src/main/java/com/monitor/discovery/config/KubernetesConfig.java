package com.monitor.discovery.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kubernetes客户端配置类
 * 用于配置与Kubernetes集群的连接和交互
 */
@Configuration
public class KubernetesConfig {

    @Value("${kubernetes.master:https://kubernetes.default.svc}")
    private String masterUrl;

    @Value("${kubernetes.namespace:default}")
    private String namespace;

    @Bean
    public KubernetesClient kubernetesClient() {
        Config config = new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withNamespace(namespace)
                // 使用服务账号的默认配置
                .build();

        return new DefaultKubernetesClient(config);
    }
}