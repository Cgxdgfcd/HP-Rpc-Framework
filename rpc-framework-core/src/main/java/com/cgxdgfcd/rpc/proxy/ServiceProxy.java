package com.cgxdgfcd.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.config.RpcConfig;
import com.cgxdgfcd.rpc.constant.RpcConstant;
import com.cgxdgfcd.rpc.model.RpcRequest;
import com.cgxdgfcd.rpc.model.RpcResponse;
import com.cgxdgfcd.rpc.model.ServiceMetaInfo;
import com.cgxdgfcd.rpc.registry.Registry;
import com.cgxdgfcd.rpc.registry.RegistryFactory;
import com.cgxdgfcd.rpc.serializer.JdkSerializer;
import com.cgxdgfcd.rpc.serializer.Serializer;
import com.cgxdgfcd.rpc.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务代理（JDK动态代理）
 */
public class ServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 构造服务信息
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(rpcRequest.getServiceName());
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);

        try {
            return invokeHandle(rpcRequest, registry, serviceMetaInfo, serializer);
        } catch (IORuntimeException e) {
            // 服务注册信息失效时，强制从服务中心拉取信息更新本地缓存（清理本地缓存即可）
            registry.removeCache(serviceMetaInfo.getServiceKey());
            return invokeHandle(rpcRequest, registry, serviceMetaInfo, serializer);
        }
    }

    /**
     * 执行发送请求操作
     *
     * @param rpcRequest
     * @param registry
     * @param serializer
     * @return
     */
    private Object invokeHandle(RpcRequest rpcRequest, Registry registry, ServiceMetaInfo serviceMetaInfo, Serializer serializer) {
        // 从注册中心获取服务提供者请求地址
        List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfos)) {
            throw new RuntimeException("暂无服务地址");
        }
        // 暂时先取第一个
        ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfos.get(0);

        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
