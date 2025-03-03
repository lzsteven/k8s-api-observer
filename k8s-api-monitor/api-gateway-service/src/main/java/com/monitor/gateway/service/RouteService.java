package com.monitor.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态路由管理服务
 * 负责动态添加、更新和删除路由规则
 */
@Service
public class RouteService implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    private ApiRegistryService apiRegistryService;

    private ApplicationEventPublisher publisher;

    private final List<String> routeIds = new ArrayList<>();

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @PostConstruct
    public void init() {
        // 初始化时从API注册中心获取路由信息并配置
        refreshRoutes();
    }

    /**
     * 刷新路由配置
     */
    public void refreshRoutes() {
        logger.info("开始刷新路由配置");
        try {
            // 获取API信息
            List<RouteDefinition> routes = apiRegistryService.getRouteDefinitions();
            
            // 删除旧路由
            deleteAllRoutes();
            
            // 添加新路由
            routes.forEach(this::addRoute);
            
            // 发布路由刷新事件
            publisher.publishEvent(new RefreshRoutesEvent(this));
            
            logger.info("路由刷新完成，共配置{}个路由", routes.size());
        } catch (Exception e) {
            logger.error("路由刷新失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 添加路由
     */
    public void addRoute(RouteDefinition definition) {
        try {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            routeIds.add(definition.getId());
            logger.info("添加路由成功: {}", definition.getId());
        } catch (Exception e) {
            logger.error("添加路由失败: {}, 错误: {}", definition.getId(), e.getMessage(), e);
        }
    }

    /**
     * 更新路由
     */
    public void updateRoute(RouteDefinition definition) {
        try {
            deleteRoute(definition.getId());
            addRoute(definition);
            logger.info("更新路由成功: {}", definition.getId());
        } catch (Exception e) {
            logger.error("更新路由失败: {}, 错误: {}", definition.getId(), e.getMessage(), e);
        }
    }

    /**
     * 删除路由
     */
    public void deleteRoute(String id) {
        try {
            routeDefinitionWriter.delete(Mono.just(id)).subscribe();
            routeIds.remove(id);
            logger.info("删除路由成功: {}", id);
        } catch (Exception e) {
            logger.error("删除路由失败: {}, 错误: {}", id, e.getMessage(), e);
        }
    }

    /**
     * 删除所有路由
     */
    private void deleteAllRoutes() {
        List<String> ids = new ArrayList<>(routeIds);
        ids.forEach(this::deleteRoute);
    }

    /**
     * 获取所有路由定义
     */
    public List<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> definitions = new ArrayList<>();
        routeDefinitionLocator.getRouteDefinitions().subscribe(definitions::add);
        return definitions;
    }
}