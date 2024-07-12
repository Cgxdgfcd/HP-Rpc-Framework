package com.cgxdgfcd.rpc.registry;

import com.cgxdgfcd.rpc.model.ServiceMetaInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册中心本地缓存
 */
public class RegisterServiceCache {

    /**
     * 服务缓存
     */
    private Map<String, List<ServiceMetaInfo>> serviceCache;

    public RegisterServiceCache() {
        this.serviceCache = new HashMap<>();
    }

    /**
     * 写缓存
     *
     * @param newServiceCache
     */
    void writeCache(String serviceKey, List<ServiceMetaInfo> newServiceCache) {
        this.serviceCache.put(serviceKey, newServiceCache);
    }

    /**
     * 读缓存
     *
     * @return
     */
    List<ServiceMetaInfo> readCache(String serviceKey) {
        return this.serviceCache.get(serviceKey);
    }

    /**
     * 清理缓存
     */
    void removeCache(String serviceKey) {
        this.serviceCache.remove(serviceKey);
    }
}
