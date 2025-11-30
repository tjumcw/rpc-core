package com.mcw.distributed.config;

// 暂时没用，只有一个注册中心，写了xml也不会读取对应的配置
public class RegistryConfig {

    private String address;
    private int port;
    private String protocol = "socket";

    // getter 和 setter
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
