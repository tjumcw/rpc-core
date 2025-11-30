package com.mcw.distributed.config;

import java.util.ArrayList;
import java.util.List;

/**
 * RPC 配置总览
 */
public class RpcConfig {
    private RegistryConfig registryConfig;
    private List<ProviderConfig> providerConfigs = new ArrayList<>();
    private List<ConsumerConfig> consumerConfigs = new ArrayList<>();
    private ScanConfig scanConfig;

    // getter 和 setter 方法

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    public List<ProviderConfig> getProviderConfigs() {
        return providerConfigs;
    }

    public void setProviderConfigs(List<ProviderConfig> providerConfigs) {
        this.providerConfigs = providerConfigs;
    }

    public List<ConsumerConfig> getConsumerConfigs() {
        return consumerConfigs;
    }

    public void setConsumerConfigs(List<ConsumerConfig> consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
    }

    public ScanConfig getScanConfig() {
        return scanConfig;
    }

    public void setScanConfig(ScanConfig scanConfig) {
        this.scanConfig = scanConfig;
    }
}

