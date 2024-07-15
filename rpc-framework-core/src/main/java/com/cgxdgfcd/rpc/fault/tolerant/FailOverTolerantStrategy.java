package com.cgxdgfcd.rpc.fault.tolerant;

import cn.hutool.core.collection.CollUtil;
import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.loadbalancer.LoadBalancer;
import com.cgxdgfcd.rpc.loadbalancer.LoadBalancerFactory;
import com.cgxdgfcd.rpc.model.RpcRequest;
import com.cgxdgfcd.rpc.model.RpcResponse;
import com.cgxdgfcd.rpc.model.ServiceMetaInfo;
import com.cgxdgfcd.rpc.server.tcp.VertxTcpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 转移到其他服务节点 - 容错策略
 */
public class FailOverTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) throws ExecutionException, InterruptedException {
        // 获取其他服务节点并调用
        List<ServiceMetaInfo> serviceMetaInfos = (List<ServiceMetaInfo>) context.get("serviceList");
        ServiceMetaInfo errorService = (ServiceMetaInfo) context.get("errorService");
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        // 从服务列表中删除失败的服务信息
        serviceMetaInfos.remove(errorService);
        if (!CollUtil.isEmpty(serviceMetaInfos)) {
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(RpcApplication.getRpcConfig().getLoadBalancer());
            // 将调用方法名（请求路径）作为负载均衡器参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName", rpcRequest.getMethodName());
            ServiceMetaInfo serviceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfos);
            return VertxTcpClient.doRequest(rpcRequest, serviceMetaInfo);
        }
        return null;
    }
}
