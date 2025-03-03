package com.monitor.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * 日志记录过滤器
 * 记录API调用的详细信息，便于问题追踪和性能分析
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        String remoteAddress = request.getRemoteAddress().getAddress().getHostAddress();
        
        // 记录请求开始时间
        Instant startTime = Instant.now();
        
        // 记录请求信息
        logger.info("收到请求: {} {} 来自 {}", method, path, remoteAddress);
        
        // 添加请求ID用于追踪
        String requestId = java.util.UUID.randomUUID().toString();
        exchange.getAttributes().put("requestId", requestId);
        
        // 继续处理请求，并在响应返回时记录响应信息
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 计算请求处理时间
            Duration duration = Duration.between(startTime, Instant.now());
            
            // 记录响应状态和处理时间
            logger.info("请求完成: {} {} 状态: {} 耗时: {}ms 请求ID: {}", 
                    method, 
                    path, 
                    exchange.getResponse().getStatusCode(), 
                    duration.toMillis(),
                    requestId);
        }));
    }

    @Override
    public int getOrder() {
        return -110; // 在所有过滤器之前执行，以便记录完整的请求处理过程
    }
}