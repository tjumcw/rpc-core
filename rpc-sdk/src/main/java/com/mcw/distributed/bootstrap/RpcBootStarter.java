package com.mcw.distributed.bootstrap;

public interface RpcBootStarter {

    void start();
    void shutdown();
    boolean isStarted();
}
