package com.monitor.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * API发现服务应用入口
 * 负责发现和收集Kubernetes集群中各容器提供的API信息
 */
@SpringBootApplication
@EnableScheduling
public class ApiDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiDiscoveryApplication.class, args);
    }
}