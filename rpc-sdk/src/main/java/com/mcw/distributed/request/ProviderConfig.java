package com.mcw.distributed.request;

/**
 * 服务提供者配置
 */
public class ProviderConfig {
    private String interfaceName;
    private String ref;
    private String version = "1.0.0";

    // getter 和 setter
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
