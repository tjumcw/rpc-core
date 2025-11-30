package com.mcw.distributed.bootstrap;

import com.mcw.distributed.provider.RpcServer;
import com.mcw.distributed.request.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractRpcBootstrap implements RpcBootStarter{

    private static final Logger logger = LoggerFactory.getLogger(AbstractRpcBootstrap.class);

    protected final RpcServer rpcServer;
    protected final AtomicBoolean started = new AtomicBoolean(false);
    protected final List<ServiceInfo> registeredServices = new ArrayList<>();

    public AbstractRpcBootstrap() {
        this.rpcServer = new RpcServer();
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            logger.warn("RPC服务已经启动，忽略重复启动");
            return;
        }

        try {
            logger.info("开始启动 RPC 服务...");

            // 模板方法：定义启动流程
            rpcServer.start();
            this.registerProviderServices();
            this.discoverConsumerServices();
            this.doCustomStart();

            started.set(true);
            logger.info("RPC 服务启动完成");

        } catch (Exception e) {
            started.set(false);
            logger.error("RPC 服务启动失败", e);
            throw new RuntimeException("RPC服务启动失败", e);
        }
    }

    @Override
    public void shutdown() {
        if (!started.get()) {
            return;
        }

        logger.info("开始关闭 RPC 服务...");

        // 模板方法：定义关闭流程
        this.doCustomShutdown();
        this.unregisterServices();
        rpcServer.stop();

        started.set(false);
        logger.info("RPC 服务关闭完成");
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    // 模板方法 - 由子类实现
    protected abstract void registerProviderServices();
    protected abstract void discoverConsumerServices();
    protected void doCustomStart() {} // 钩子方法
    protected void doCustomShutdown() {} // 钩子方法

    private void unregisterServices() {
        // 这里可以实现服务注销逻辑
    }

    protected ServiceInfo createServiceInfo(String interfaceName, String version) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setInterfaceName(interfaceName);
        serviceInfo.setVersion(version);
        serviceInfo.setHost(rpcServer.getHost());
        serviceInfo.setPort(rpcServer.getPort());
        return serviceInfo;
    }

}
