package com.cgxdgfcd.rpc.bootstrap;

import com.cgxdgfcd.rpc.RpcApplication;

/**
 * 服务消费者启动类
 */
public class ConsumerBootstrap {

    public static void init() {
        // RPC 框架初始化
        RpcApplication.init();
    }
}
