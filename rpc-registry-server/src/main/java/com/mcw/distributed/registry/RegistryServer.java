package com.mcw.distributed.registry;
import com.mcw.distributed.enums.RegistryOperationEnum;
import com.mcw.distributed.request.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryServer {

    private static final Logger logger = LoggerFactory.getLogger(RegistryServer.class);

    private static final Map<String, ServiceInfo> registry = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9090);
        logger.info("注册中心启动在9090端口");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("注册中心正在关闭...");
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("关闭注册中心时发生错误",  e);
            }
            logger.info("注册中心已关闭");
        }));
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(new RegistryHandler(socket)).start();
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    logger.error("接受连接时发生错误", e);
                }
                break;
            }
        }
    }

    static class RegistryHandler implements Runnable {
        private final Socket socket;

        public RegistryHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                // 读取操作类型：register、discover
                String operation = ois.readUTF();
                if (RegistryOperationEnum.REGISTER.getOperation().equals(operation)) {
                    // 注册服务
                    ServiceInfo serviceInfo = (ServiceInfo) ois.readObject();
                    String serviceKey = serviceInfo.getInterfaceName() + ":" + serviceInfo.getVersion();
                    registry.put(serviceKey, serviceInfo);
                    oos.writeObject("注册成功");
                    oos.flush();
                    logger.info("注册服务: {}", serviceInfo);
                } else if (RegistryOperationEnum.DISCOVER.getOperation().equals(operation)) {
                    // 发现服务
                    String serviceName = ois.readUTF();
                    String version = ois.readUTF();
                    String serviceKey = serviceName + ":" + version;
                    ServiceInfo serviceInfo = registry.get(serviceKey);
                    oos.writeObject(serviceInfo);
                    oos.flush();
                }
            } catch (Exception e) {
                logger.error("处理注册请求时发生错误", e);
            }
        }
    }
}
