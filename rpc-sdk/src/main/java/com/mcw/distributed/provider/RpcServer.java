package com.mcw.distributed.provider;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public static final ProviderImplFactory LOCAL_SERVICE_FACTORY = new ProviderImplFactory();

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private ServerSocket server;

    public RpcServer() {
        try {
            this.server = new ServerSocket(0);
            threadPool.execute(this::runServer);
        } catch (IOException e) {
            logger.error("创建服务端Socket失败", e);
        }
    }

    private void runServer() {
        while (true) {
            try {
                Socket client = server.accept();
                threadPool.execute(new RpcTask(client));
                logger.debug("接收到客户端连接: {}", client.getRemoteSocketAddress());
            } catch (IOException e) {
                if (!server.isClosed()) {
                    logger.error("接受连接时发生错误", e);
                }
            }
        }
    }

    public String getHost() {
        return this.server.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return this.server.getLocalPort();
    }
}
