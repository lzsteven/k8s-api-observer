package com.monitor.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * 限流过滤器
 * 使用Redis实现API调用频率限制
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> redisScript;

    public RateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate, 
                          RedisScript<Long> redisScript) {
        this.redisTemplate = redisTemplate;
        this.redisScript = redisScript;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // 如果是需要限流的路径
        if (isRateLimitRequired(path)) {
            String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            String key = "rate_limit:" + clientIp + ":" + path;
            
            // 使用Redis Lua脚本进行原子性限流检查
            return redisTemplate.execute(redisScript, 
                    Arrays.asList(key), 
                    Arrays.asList(String.valueOf(10), // 最大请求数
                                 String.valueOf(60), // 时间窗口（秒）
                                 String.valueOf(Instant.now().getEpochSecond()))) // 当前时间
                    .flatMap(response -> {
                        if (response == 0L) {
                            // 超过限流阈值
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            return exchange.getResponse().setComplete();
                        }
                        // 未超过限流阈值，继续处理请求
                        return chain.filter(exchange);
                    });
        }
        
        // 不需要限流的路径，直接放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -90; // 在认证过滤器之后执行
    }

    private boolean isRateLimitRequired(String path) {
        // 需要限流的路径
        return path.startsWith("/api/") || 
               path.contains("/high-load/");
    }
}