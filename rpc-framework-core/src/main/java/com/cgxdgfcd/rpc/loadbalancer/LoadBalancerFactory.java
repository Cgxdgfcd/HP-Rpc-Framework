package com.cgxdgfcd.rpc.loadbalancer;

import com.cgxdgfcd.rpc.registry.EtcdRegistry;
import com.cgxdgfcd.rpc.registry.Registry;
import com.cgxdgfcd.rpc.spi.SpiLoader;

/**
 * 负载均衡器工厂类
 */
public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    /**
     * 获取负载均衡器实例
     *
     * @param key
     * @return
     */
    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
