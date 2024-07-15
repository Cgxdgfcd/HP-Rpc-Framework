package com.cgxdgfcd.rpc.config;

import com.cgxdgfcd.rpc.fault.retry.RetryStrategyKeys;
import com.cgxdgfcd.rpc.fault.tolerant.MockServiceKeys;
import com.cgxdgfcd.rpc.fault.tolerant.TolerantStrategyKeys;
import com.cgxdgfcd.rpc.loadbalancer.LoadBalancer;
import com.cgxdgfcd.rpc.loadbalancer.LoadBalancerKeys;
import com.cgxdgfcd.rpc.loadbalancer.RoundRobinLoadBalancer;
import com.cgxdgfcd.rpc.serializer.Serializer;
import com.cgxdgfcd.rpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    /**
     * 模拟调用
     */
    private boolean mock = false;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;

    /**
     * 服务降级（模拟服务）
     */
    private String mockService = MockServiceKeys.DEFAULT_MOCK;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;
}
