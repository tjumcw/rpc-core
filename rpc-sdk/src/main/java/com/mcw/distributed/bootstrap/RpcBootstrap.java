package com.mcw.distributed.bootstrap;

import com.mcw.distributed.annotation.RpcService;
import com.mcw.distributed.provider.RpcServer;
import com.mcw.distributed.provider.ProviderImplFactory;
import com.mcw.distributed.registry.RegistryClient;
import com.mcw.distributed.request.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class RpcBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(RpcBootstrap.class);


    private final String basePackage;
    private final RpcServer rpcServer;

    public RpcBootstrap(String basePackage, String registryAddress, int registryPort) {
        this.rpcServer = new RpcServer();
        this.basePackage = basePackage;
    }

    public void start() {
        try {
            logger.info("开始启动 RPC 服务...");

            // 1. 扫描并注册服务提供者
            this.scanAndRegisterServices();

        } catch (Exception e) {
            logger.error("RPC 服务启动失败", e);
            throw new RuntimeException("RPC服务启动失败", e);
        }
    }

    private void scanAndRegisterServices() {
        logger.info("开始扫描服务包: {}", basePackage);

        try {
            String path = basePackage.replace(".", "/");
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url == null) {
                logger.warn("未找到要扫描的包: {}", basePackage);
                return;
            }

            File dir = new File(url.getFile());
            int serviceCount = scanDirectory(dir, basePackage);

            logger.info("服务扫描完成，共注册 {} 个服务", serviceCount);

        } catch (Exception e) {
            logger.error("扫描服务包失败: {}", basePackage, e);
            throw new RuntimeException("扫描服务包失败: " + basePackage, e);
        }
    }

    private int scanDirectory(File dir, String currentPackage) {
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        int count = 0;
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                count += scanDirectory(file, currentPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackage + "." + file.getName().replace(".class", "");
                if (registerServiceIfAnnotated(className)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean registerServiceIfAnnotated(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(RpcService.class)) {
                Class<?> interfaceClass = clazz.getAnnotation(RpcService.class).value();
                Object instance = clazz.getDeclaredConstructor().newInstance();

                // 注册到注册中心
                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.setInterfaceName(interfaceClass.getName());
                serviceInfo.setVersion("1.0.0");
                serviceInfo.setHost(rpcServer.getHost());
                serviceInfo.setPort(rpcServer.getPort());

                RegistryClient.register(serviceInfo);
                ProviderImplFactory.registerService(interfaceClass, instance);

                logger.info("注册服务成功: {} -> {}", interfaceClass.getName(), clazz.getName());
                return true;
            }
        } catch (Exception e) {
            logger.debug("跳过类 {}: {}", className, e.getMessage());
        }
        return false;
    }
}
