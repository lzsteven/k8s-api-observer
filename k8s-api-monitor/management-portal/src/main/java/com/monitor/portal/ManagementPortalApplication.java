package com.monitor.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 管理门户应用入口
 * 整合容器管理、监控视图和API管理功能，提供统一的Web界面
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class ManagementPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManagementPortalApplication.class, args);
    }
}