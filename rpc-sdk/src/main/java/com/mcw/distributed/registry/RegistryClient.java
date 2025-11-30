package com.mcw.distributed.registry;

import com.mcw.distributed.enums.RegistryOperationEnum;
import com.mcw.distributed.request.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class RegistryClient {
    private static final Logger logger = LoggerFactory.getLogger(RegistryClient.class);

    private static final String registryAddress = RegistryCenterConfig.getString("registry.address", "127.0.0.1");
    private static final int registryPort = RegistryCenterConfig.getInt("registry.port", 9090);

    private RegistryClient() {

    }

    // 注册服务
    public static void register(ServiceInfo serviceInfo) {
        try (Socket socket = new Socket(registryAddress, registryPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            oos.writeUTF(RegistryOperationEnum.REGISTER.getOperation());
            oos.writeObject(serviceInfo);
            oos.flush();
            ois.readObject();
        } catch (Exception e) {
            logger.error("注册服务失败", e);
        }
    }

    // 发现服务
    public static ServiceInfo discover(String serviceName, String version) {
        try (Socket socket = new Socket(registryAddress, registryPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            oos.writeUTF(RegistryOperationEnum.DISCOVER.getOperation());
            oos.writeUTF(serviceName);
            oos.writeUTF(version);
            oos.flush();

            @SuppressWarnings("unchecked")
            ServiceInfo service = (ServiceInfo) ois.readObject();

            logger.info("发现服务成功: {}:{}", serviceName, version);
            return service;
        } catch (Exception e) {
            logger.error("发现服务失败", e);
        }
        return null;
    }
}
