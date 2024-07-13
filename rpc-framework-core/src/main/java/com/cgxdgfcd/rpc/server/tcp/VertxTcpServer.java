package com.cgxdgfcd.rpc.server.tcp;

import com.cgxdgfcd.rpc.RpcApplication;
import com.cgxdgfcd.rpc.model.RpcRequest;
import com.cgxdgfcd.rpc.model.RpcResponse;
import com.cgxdgfcd.rpc.model.ServiceMetaInfo;
import com.cgxdgfcd.rpc.protocol.ProtocolConstant;
import com.cgxdgfcd.rpc.protocol.ProtocolMessage;
import com.cgxdgfcd.rpc.protocol.ProtocolMessageSerializerEnum;
import com.cgxdgfcd.rpc.protocol.ProtocolMessageTypeEnum;
import com.cgxdgfcd.rpc.server.HttpServer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VertxTcpServer {

    /**
     * 启动服务器
     *
     * @param port
     */
    public void doStart(int port) {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        // 创建 TCP 服务器
        NetServer server = vertx.createNetServer();

        // 处理请求
        server.connectHandler(new TpcServerHandler());

        // 启动 TCP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP server started on port " + port);
            } else {
                System.out.println("Failed to start TCP server: " + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
