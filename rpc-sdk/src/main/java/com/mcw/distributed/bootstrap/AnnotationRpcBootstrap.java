package com.mcw.distributed.bootstrap;

import com.mcw.distributed.annotation.RpcConsumer;
import com.mcw.distributed.annotation.RpcService;
import com.mcw.distributed.consumer.ConsumerRefMapping;
import com.mcw.distributed.provider.ProviderImplFactory;
import com.mcw.distributed.registry.RegistryClient;
import com.mcw.distributed.request.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Objects;

public class AnnotationRpcBootstrap extends AbstractRpcBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationRpcBootstrap.class);

    private final String basePackage;

    public AnnotationRpcBootstrap(String basePackage) {
        super();
        this.basePackage = basePackage;
    }

    @Override
    protected void registerProviderServices() {
        logger.info("开始扫描注解服务: {}", basePackage);

        try {
            String path = basePackage.replace(".", "/");
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url == null) {
                logger.warn("未找到要扫描的包: {}", basePackage);
                return;
            }

            File dir = new File(url.getFile());
            int serviceCount = scanDirectoryForProviders(dir, basePackage);

            logger.info("服务扫描完成，共注册 {} 个服务", serviceCount);

        } catch (Exception e) {
            logger.error("扫描服务包失败: {}", basePackage, e);
            throw new RuntimeException("扫描服务包失败: " + basePackage, e);
        }
    }

    @Override
    protected void discoverConsumerServices() {
        logger.info("开始扫描注解消费者: {}", basePackage);

        try {
            String path = basePackage.replace(".", "/");
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url == null) {
                return;
            }

            File dir = new File(url.getFile());
            scanDirectoryForConsumers(dir, basePackage);

            logger.info("消费者扫描完成");

        } catch (Exception e) {
            logger.error("扫描消费者失败", e);
            throw new RuntimeException("扫描消费者失败", e);
        }
    }

    private int scanDirectoryForProviders(File dir, String currentPackage) {
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        int count = 0;
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                count += scanDirectoryForProviders(file, currentPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackage + "." + file.getName().replace(".class", "");
                if (registerProviderIfAnnotated(className)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void scanDirectoryForConsumers(File dir, String currentPackage) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                scanDirectoryForConsumers(file, currentPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackage + "." + file.getName().replace(".class", "");
                scanClassForConsumers(className);
            }
        }
    }

    private boolean registerProviderIfAnnotated(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(RpcService.class)) {
                Class<?> interfaceClass = clazz.getAnnotation(RpcService.class).value();
                Object instance = clazz.getDeclaredConstructor().newInstance();

                ServiceInfo serviceInfo = super.createServiceInfo(interfaceClass.getName(), "1.0.0");
                RegistryClient.register(serviceInfo);
                ProviderImplFactory.registerService(interfaceClass, instance);
                return true;
            }
        } catch (Exception e) {
            logger.error("跳过类 {}: {}", className, e.getMessage());
        }
        return false;
    }

    private void scanClassForConsumers(String className) {
        try {
            Class<?> clazz = Class.forName(className);

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(RpcConsumer.class)) {
                    RpcConsumer consumer = field.getAnnotation(RpcConsumer.class);
                    Class<?> serviceInterface = field.getType();

                    // 服务发现
                    ServiceInfo serviceInfo = RegistryClient.discover(
                            serviceInterface.getName(),
                            consumer.version()
                    );

                    if (serviceInfo != null) {
                        ConsumerRefMapping.putServiceInfo(serviceInterface, serviceInfo);
                        logger.info("发现消费者服务: {} -> {}",
                                serviceInterface.getName(), field.getName());
                    } else {
                        logger.warn("未找到消费者服务: {}:{}",
                                serviceInterface.getName(), consumer.version());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("扫描类 {} 的消费者失败: {}", className, e.getMessage());
        }
    }
}
