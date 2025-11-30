package com.mcw.distributed.request;

import java.io.Serial;
import java.io.Serializable;

public class ServiceInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 8087682028823032391L;

    private String interfaceName;
    private String version;
    private String host;
    private int port;

    public ServiceInfo() {
    }

    public ServiceInfo(String name, String s, String host, int port) {
        this.interfaceName = name;
        this.version = s;
        this.host = host;
        this.port = port;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "interfaceName='" + interfaceName + '\'' +
                ", version='" + version + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
