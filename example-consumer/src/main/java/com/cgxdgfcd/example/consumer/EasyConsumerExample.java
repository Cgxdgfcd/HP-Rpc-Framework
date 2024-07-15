package com.cgxdgfcd.example.consumer;

import com.cgxdgfcd.example.common.model.User;
import com.cgxdgfcd.example.common.service.UserService;
import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.bootstrap.ConsumerBootstrap;
import com.cgxdgfcd.rpc.config.RpcConfig;
import com.cgxdgfcd.rpc.proxy.ServiceProxyFactory;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        // 服务消费者初始化
        ConsumerBootstrap.init();

        // 通过动态代理获取代理对象
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("cgxdgfcd");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
