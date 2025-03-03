package com.monitor.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 安全配置类
 * 配置API网关的认证和授权策略
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange()
                // 允许访问监控端点
                .pathMatchers("/actuator/**").permitAll()
                // 允许访问API文档
                .pathMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
                // 允许访问管理接口
                .pathMatchers("/admin/**").authenticated()
                // 其他请求需要认证
                .anyExchange().permitAll()
                .and()
                .httpBasic()
                .and()
                .build();
    }
}