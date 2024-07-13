package com.cgxdgfcd.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.config.RpcConfig;
import com.cgxdgfcd.rpc.constant.RpcConstant;
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
import java.util.List;
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
            // 暂时先取第一个
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfos.get(0);

            // 发送 tcp 请求
            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            return rpcResponse.getData();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
