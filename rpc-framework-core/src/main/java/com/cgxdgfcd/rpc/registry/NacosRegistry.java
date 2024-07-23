package com.cgxdgfcd.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.cgxdgfcd.rpc.config.RegistryConfig;
import com.cgxdgfcd.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Nacos 注册中心
 */
public class NacosRegistry implements Registry {

    private NamingService namingService;

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心本地缓存（消费端）
     */
    private final RegisterServiceCache registerServiceCache = new RegisterServiceCache();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();


    @Override
    public void init(RegistryConfig registryConfig) {
        try {
            namingService = NamingFactory.createNamingService(registryConfig.getAddress());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) {
        // 注册服务
        try {
            namingService.registerInstance(serviceMetaInfo.getServiceKey(), serviceMetaInfo.getServiceHost(), serviceMetaInfo.getServicePort());
            watch(serviceMetaInfo.getServiceKey());
        } catch (NacosException e) {
            throw new RuntimeException("服务注册时有错误发生: ", e);
        }

        // 添加节点到本地缓存
        localRegisterNodeKeySet.add(serviceMetaInfo.getServiceKey());
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        // 注销服务
        try {
            namingService.deregisterInstance(serviceMetaInfo.getServiceKey(), serviceMetaInfo.getServiceHost(), serviceMetaInfo.getServicePort());
        } catch (NacosException e) {
            throw new RuntimeException("注销服务时有错误发生: ", e);
        }

        // 从本地缓存删除节点
        localRegisterNodeKeySet.remove(serviceMetaInfo.getServiceKey());
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存获取服务
        List<ServiceMetaInfo> serviceMetaInfos = registerServiceCache.readCache(serviceKey);
        if (serviceMetaInfos != null) {
            return serviceMetaInfos;
        }

        try {
            List<Instance> allInstances = namingService.getAllInstances(serviceKey);
            List<ServiceMetaInfo> serviceMetaInfoList = allInstances.stream().map(instance -> {
                ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
                serviceMetaInfo.setServiceHost(instance.getIp());
                serviceMetaInfo.setServicePort(instance.getPort());

                return serviceMetaInfo;
            }).collect(Collectors.toList());

            // 写入服务缓存
            registerServiceCache.writeCache(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (NacosException e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");

        /*// 遍历所有本地节点
        for (String key : localRegisterNodeKeySet) {

        }*/
        localRegisterNodeKeySet.clear();
    }

    /**
     * 心跳续签
     */
    @Override
    public void heartBeat() {
        // 注册服务时会自动添加心跳检测
    }

    @Override
    public void watch(String serviceNodeKey) throws NacosException {
        // 之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            namingService.subscribe(serviceNodeKey, event -> {
                // 当服务实例发生变化时移除本地缓存
                if (event instanceof NamingEvent) {
                    NamingEvent namingEvent = (NamingEvent) event;
                    removeCache(namingEvent.getServiceName());
                }
            });
        }
    }

    /**
     * 清理指定 serviceKey 的缓存
     *
     * @param serviceKey
     */
    @Override
    public void removeCache(String serviceKey) {
        registerServiceCache.removeCache(serviceKey);
    }
}
