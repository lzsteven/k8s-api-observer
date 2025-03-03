package com.monitor.discovery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * 应用全局配置类
 * 用于配置应用的基本参数和功能
 */
@Configuration
public class AppConfig {

    @Value("${api.discovery.thread-pool.core-size:5}")
    private int corePoolSize;

    @Value("${api.discovery.thread-pool.max-size:10}")
    private int maxPoolSize;

    @Value("${api.discovery.thread-pool.queue-capacity:25}")
    private int queueCapacity;

    /**
     * 配置RestTemplate用于HTTP请求
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 配置线程池用于异步处理API发现任务
     */
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("api-discovery-");
        return executor;
    }
}