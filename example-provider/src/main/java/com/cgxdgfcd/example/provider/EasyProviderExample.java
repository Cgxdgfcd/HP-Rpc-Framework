package com.cgxdgfcd.example.provider;

import com.cgxdgfcd.example.common.service.UserService;
import com.cgxdgfcd.rpc.registry.LocalRegistry;
import com.cgxdgfcd.rpc.server.HttpServer;
import com.cgxdgfcd.rpc.server.VertxHttpServer;

/**
 * 建议服务提供者实例
 */
public class EasyProviderExample {

    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        HttpServer server = new VertxHttpServer();
        server.doStart(8080);
    }
}
