package com.monitor.discovery.repository;

import com.monitor.discovery.model.ApiInfo;
import com.monitor.discovery.model.ServiceInfo;

import java.util.List;
import java.util.Optional;

/**
 * API信息存储接口
 * 用于存储和管理API信息
 */
public interface ApiRepository {

    /**
     * 保存API信息
     * @param apiInfo API信息
     * @return 保存后的API信息
     */
    ApiInfo saveApi(ApiInfo apiInfo);

    /**
     * 批量保存API信息
     * @param apiInfos API信息列表
     * @return 保存后的API信息列表
     */
    List<ApiInfo> saveApis(List<ApiInfo> apiInfos);

    /**
     * 根据ID查询API信息
     * @param id API ID
     * @return API信息
     */
    Optional<ApiInfo> findApiById(String id);

    /**
     * 根据服务ID查询API信息列表
     * @param serviceId 服务ID
     * @return API信息列表
     */
    List<ApiInfo> findApisByServiceId(String serviceId);

    /**
     * 根据命名空间查询API信息列表
     * @param namespace 命名空间
     * @return API信息列表
     */
    List<ApiInfo> findApisByNamespace(String namespace);

    /**
     * 查询所有API信息
     * @return 所有API信息列表
     */
    List<ApiInfo> findAllApis();

    /**
     * 删除API信息
     * @param id API ID
     */
    void deleteApi(String id);

    /**
     * 删除服务的所有API信息
     * @param serviceId 服务ID
     */
    void deleteApisByServiceId(String serviceId);

    /**
     * 保存服务信息
     * @param serviceInfo 服务信息
     * @return 保存后的服务信息
     */
    ServiceInfo saveService(ServiceInfo serviceInfo);

    /**
     * 根据ID查询服务信息
     * @param id 服务ID
     * @return 服务信息
     */
    Optional<ServiceInfo> findServiceById(String id);

    /**
     * 根据名称和命名空间查询服务信息
     * @param name 服务名称
     * @param namespace 命名空间
     * @return 服务信息
     */
    Optional<ServiceInfo> findServiceByNameAndNamespace(String name, String namespace);

    /**
     * 根据命名空间查询服务信息列表
     * @param namespace 命名空间
     * @return 服务信息列表
     */
    List<ServiceInfo> findServicesByNamespace(String namespace);

    /**
     * 查询所有服务信息
     * @return 所有服务信息列表
     */
    List<ServiceInfo> findAllServices();

    /**
     * 删除服务信息
     * @param id 服务ID
     */
    void deleteService(String id);
}