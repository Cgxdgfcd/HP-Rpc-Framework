package com.cgxdgfcd.rpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.model.RpcRequest;
import com.cgxdgfcd.rpc.model.RpcResponse;
import com.cgxdgfcd.rpc.model.ServiceMetaInfo;
import com.cgxdgfcd.rpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VertxTcpClient {

    /**
     * 发送请求
     *
     * @param rpcRequest
     * @param serviceMetaInfo
     * @return
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        Vertx vertx = Vertx.vertx();

        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        vertx.createNetClient().connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(), result -> {
            if (result.succeeded()) {
                System.out.println("Connected to TCP server");
                NetSocket socket = result.result();
                // 消息序列化
                ProtocolMessage<RpcRequest> rpcRequestProtocolMessage = new ProtocolMessage<>();
                ProtocolMessage.Header header = new ProtocolMessage.Header();
                header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
                header.setRequestId(IdUtil.getSnowflakeNextId());
                rpcRequestProtocolMessage.setHeader(header);
                rpcRequestProtocolMessage.setBody(rpcRequest);

                try {
                    Buffer encode = ProtocolMessageEncoder.encode(rpcRequestProtocolMessage);
                    socket.write(encode);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // 接收响应
                TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                    try {
                        ProtocolMessage<RpcResponse> decode = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                        responseFuture.complete(decode.getBody());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                socket.handler(tcpBufferHandlerWrapper);
            } else {
                System.err.println("Failed to connect TCP server");
            }
        });

        RpcResponse rpcResponse = responseFuture.get();
        return rpcResponse;
    }
}
