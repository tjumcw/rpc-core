package com.mcw.distributed.provider;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);


    private static final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private ServerSocket server;

    public RpcServer() {
        try {
            this.server = new ServerSocket(0);
        } catch (IOException e) {
            logger.error("创建服务端Socket失败", e);
        }
    }

    public void start() {
        threadPool.execute(this::runServer);
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

    public void stop() {
        try {
            // 先关闭ServerSocket，防止新的连接进入
            this.server.close();

            // 关闭线程池，不再接受新任务，并等待已提交任务完成
            threadPool.shutdown();
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                // 如果等待超时，则强制关闭
                threadPool.shutdownNow();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("关闭服务端Socket失败", e);
            // 强制关闭线程池
            threadPool.shutdownNow();
        }
    }

    public String getHost() {
        return this.server.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return this.server.getLocalPort();
    }
}
