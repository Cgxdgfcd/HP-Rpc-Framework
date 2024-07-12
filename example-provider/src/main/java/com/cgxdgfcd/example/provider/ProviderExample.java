package com.cgxdgfcd.example.provider;

import com.cgxdgfcd.example.common.service.UserService;
import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.config.RegistryConfig;
import com.cgxdgfcd.rpc.config.RpcConfig;
import com.cgxdgfcd.rpc.model.ServiceMetaInfo;
import com.cgxdgfcd.rpc.registry.LocalRegistry;
import com.cgxdgfcd.rpc.registry.Registry;
import com.cgxdgfcd.rpc.registry.RegistryFactory;
import com.cgxdgfcd.rpc.server.HttpServer;
import com.cgxdgfcd.rpc.server.VertxHttpServer;

public class ProviderExample {
    public static void main(String[] args) {
        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(rpcConfig.getServerPort());
    }
}
