package com.cgxdgfcd.rpc.bootstrap;

import cn.hutool.core.date.DateTime;
import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.config.RegistryConfig;
import com.cgxdgfcd.rpc.config.RpcConfig;
import com.cgxdgfcd.rpc.model.ServiceMetaInfo;
import com.cgxdgfcd.rpc.model.ServiceRegisterInfo;
import com.cgxdgfcd.rpc.registry.LocalRegistry;
import com.cgxdgfcd.rpc.registry.Registry;
import com.cgxdgfcd.rpc.registry.RegistryFactory;
import com.cgxdgfcd.rpc.server.tcp.VertxTcpServer;

import java.util.List;

/**
 * 服务提供者启动类
 */
public class ProviderBootstrap {

    /**
     * 初始化
     */
    public static void init(List<ServiceRegisterInfo> serviceRegisterInfoList) {
        // RPC 框架初始化
        RpcApplication.init();
        // 获取配置信息
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            // 获取服务名称
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心

            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            serviceMetaInfo.setRegisterTime(DateTime.now());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败" + e);
            }
        }

        VertxTcpServer server = new VertxTcpServer();
        server.doStart(rpcConfig.getServerPort());
    }
}
