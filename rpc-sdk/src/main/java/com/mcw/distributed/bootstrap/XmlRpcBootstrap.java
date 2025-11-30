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

public class XmlRpcBootstrap extends AbstractRpcBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(XmlRpcBootstrap.class);

    private final RpcConfig config;

    public XmlRpcBootstrap(String configFile) {
        super();
        this.config = XmlConfigParser.parse(configFile);
    }

    @Override
    protected void registerProviderServices() {
        logger.info("注册XML配置的服务...");

        for (ProviderConfig providerConfig : config.getProviderConfigs()) {
            try {
                Class<?> implClass = Class.forName(providerConfig.getRef());
                Object instance = implClass.getDeclaredConstructor().newInstance();

                ServiceInfo serviceInfo = super.createServiceInfo(providerConfig.getInterfaceName(), providerConfig.getVersion());
                RegistryClient.register(serviceInfo);
                ProviderImplFactory.registerService(Class.forName(providerConfig.getInterfaceName()), instance);

                logger.info("注册服务成功: {} -> {}", providerConfig.getInterfaceName(), providerConfig.getRef());

            } catch (Exception e) {
                logger.error("注册服务失败: {}", providerConfig.getInterfaceName(), e);
            }
        }
    }

    @Override
    protected void discoverConsumerServices() {
        logger.info("发现XML配置的消费者服务引用...");

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
