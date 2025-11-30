package com.mcw.distributed.bootstrap;

import com.mcw.distributed.config.ConsumerConfig;
import com.mcw.distributed.config.ProviderConfig;
import com.mcw.distributed.config.RpcConfig;
import com.mcw.distributed.config.ScanConfig;
import com.mcw.distributed.consumer.ConsumerRefMapping;
import com.mcw.distributed.provider.ProviderImplFactory;
import com.mcw.distributed.provider.RpcServer;
import com.mcw.distributed.registry.RegistryClient;
import com.mcw.distributed.request.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlRpcBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(XmlRpcBootstrap.class);

    private final RpcConfig config;
    private final RpcServer rpcServer;

    public XmlRpcBootstrap(String configFile) {
        this.config = XmlConfigParser.parse(configFile);
        this.rpcServer = new RpcServer();
        this.start();
    }

    public void start() {
        try {
            logger.info("开始启动 RPC 服务 (XML配置模式)...");

            // 2. 注册服务提供者
            registerServices();

            // 3. 扫描注解方式的服务（如果配置了包扫描）
            scanAnnotationServices();

            // 4. 注入服务消费者
            mappingConsumerReferences();

            logger.info("RPC 服务启动完成");

        } catch (Exception e) {
            logger.error("RPC 服务启动失败", e);
            throw new RuntimeException("RPC服务启动失败", e);
        }
    }

    private void registerServices() {
        logger.info("注册XML配置的服务...");

        for (ProviderConfig providerConfig : config.getProviderConfigs()) {
            try {
                Class<?> implClass = Class.forName(providerConfig.getRef());
                Object instance = implClass.getDeclaredConstructor().newInstance();

                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.setInterfaceName(providerConfig.getInterfaceName());
                serviceInfo.setVersion(providerConfig.getVersion());
                serviceInfo.setHost(rpcServer.getHost());
                serviceInfo.setPort(rpcServer.getPort());

                RegistryClient.register(serviceInfo);
                ProviderImplFactory.registerService(Class.forName(providerConfig.getInterfaceName()), instance);

                logger.info("注册服务成功: {} -> {}", providerConfig.getInterfaceName(), providerConfig.getRef());

            } catch (Exception e) {
                logger.error("注册服务失败: {}", providerConfig.getInterfaceName(), e);
            }
        }
    }

    private void scanAnnotationServices() {
        ScanConfig scanConfig = config.getScanConfig();
        if (scanConfig == null || scanConfig.getBasePackage() == null) {
            return;
        }

        logger.info("扫描注解服务: {}", scanConfig.getBasePackage());

        // 复用之前的扫描逻辑
        // 这里可以调用之前 RpcBootstrap 中的扫描方法
    }

    private void mappingConsumerReferences() {
        logger.info("映射XML配置的服务引用...");

        for (ConsumerConfig refConfig : config.getConsumerConfigs()) {
            try {
                // 服务发现
                ServiceInfo serviceInfo = RegistryClient.discover(
                        refConfig.getInterfaceName(),
                        refConfig.getVersion()
                );

                if (serviceInfo == null ) {
                    logger.warn("未找到可用的服务: {}:{}", refConfig.getInterfaceName(), refConfig.getVersion());
                    continue;
                }
                Class<?> clazz = Class.forName(refConfig.getInterfaceName());
                ConsumerRefMapping.putServiceInfo(clazz, serviceInfo);

            } catch (Exception e) {
                logger.error("创建服务引用失败: {}", refConfig.getInterfaceName(), e);
            }
        }
    }

}
