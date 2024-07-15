package com.cgxdgfcd.rpc.fault.tolerant;

import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.config.RpcConfig;
import com.cgxdgfcd.rpc.model.RpcResponse;

import java.util.Map;

/**
 * 降级到其他服务 - 容错策略
 */
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 获取降级的服务并调用
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        MockService mockService = MockServiceFactory.getInstance(rpcConfig.getMockService());
        Object mock = mockService.mock();
        return RpcResponse.builder().data(mock).message("ok").build();
    }
}
