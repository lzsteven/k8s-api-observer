package com.monitor.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证过滤器
 * 处理请求的身份验证
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 检查认证头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        // 如果是需要认证的路径但没有认证信息，返回未授权错误
        if (isAuthRequired(request) && (authHeader == null || !authHeader.startsWith("Bearer "))) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 验证通过，继续处理请求
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100; // 确保认证过滤器最先执行
    }

    private boolean isAuthRequired(ServerHttpRequest request) {
        String path = request.getPath().value();
        // 需要认证的路径
        return path.startsWith("/admin/") || 
               path.startsWith("/api/protected/");
    }
}