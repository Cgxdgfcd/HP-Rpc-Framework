package com.cgxdgfcd.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.config.RpcConfig;
import com.cgxdgfcd.rpc.constant.RpcConstant;
import com.cgxdgfcd.rpc.fault.retry.FixedIntervalRetryStrategy;
import com.cgxdgfcd.rpc.fault.retry.RetryStrategy;
import com.cgxdgfcd.rpc.fault.retry.RetryStrategyFactory;
import com.cgxdgfcd.rpc.fault.tolerant.TolerantStrategy;
import com.cgxdgfcd.rpc.fault.tolerant.TolerantStrategyFactory;
import com.cgxdgfcd.rpc.loadbalancer.LoadBalancer;
import com.cgxdgfcd.rpc.loadbalancer.LoadBalancerFactory;
import com.cgxdgfcd.rpc.model.RpcRequest;
import com.cgxdgfcd.rpc.model.RpcResponse;
import com.cgxdgfcd.rpc.model.ServiceMetaInfo;
import com.cgxdgfcd.rpc.protocol.*;
import com.cgxdgfcd.rpc.registry.Registry;
import com.cgxdgfcd.rpc.registry.RegistryFactory;
import com.cgxdgfcd.rpc.serializer.JdkSerializer;
import com.cgxdgfcd.rpc.serializer.Serializer;
import com.cgxdgfcd.rpc.serializer.SerializerFactory;
import com.cgxdgfcd.rpc.server.tcp.VertxTcpClient;
import io.netty.util.concurrent.CompleteFuture;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 服务代理（JDK动态代理）
 */
public class ServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 构造服务信息
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(rpcRequest.getServiceName());
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);

            // 从注册中心获取服务提供者请求地址
            List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfos)) {
                throw new RuntimeException("暂无服务地址");
            }
            // 负载均衡
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            // 将调用方法名（请求路径）作为负载均衡器参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName", rpcRequest.getMethodName());
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfos);
//            System.out.println(selectedServiceMetaInfo.getServiceAddress());

            // 使用重试机制
            RpcResponse rpcResponse;
            try {
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(RpcApplication.getRpcConfig().getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(() ->
                        VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
                );
            } catch (Exception e) {
                // 容错机制
                HashMap<String, Object> map = new HashMap<>();
                map.put("serviceList", serviceMetaInfos);
                map.put("errorService", selectedServiceMetaInfo);
                map.put("rpcRequest", rpcRequest);
                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(RpcApplication.getRpcConfig().getTolerantStrategy());
                rpcResponse = tolerantStrategy.doTolerant(map, e);
            }

            return rpcResponse.getData();
        } catch (Exception e) {
            throw new RuntimeException("调用失败, " + e);
        }
    }
}
