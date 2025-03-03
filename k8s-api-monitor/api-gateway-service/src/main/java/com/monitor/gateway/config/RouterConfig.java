package com.monitor.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 路由配置类
 * 定义API网关的路由规则，包括路由断言和过滤器链
 */
@Configuration
public class RouterConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 示例路由规则，实际路由将由RouteService动态管理
                .route("example-route", r -> r.path("/example/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Gateway-Request-Time", String.valueOf(System.currentTimeMillis())))
                        .uri("lb://example-service"))
                .build();
    }
}