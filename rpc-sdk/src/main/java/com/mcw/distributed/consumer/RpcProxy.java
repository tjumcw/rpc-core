package com.mcw.distributed.consumer;

import com.mcw.distributed.request.ServiceInfo;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcProxy {

    private static final Map<Class<?>, Object> PROXY_CACHE_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<?> interfaceClass) throws ClassNotFoundException {
        ServiceInfo serviceInfo = ConsumerRefMapping.getServiceInfo(interfaceClass);
        return (T) PROXY_CACHE_MAP.computeIfAbsent(interfaceClass, clazz ->
                Proxy.newProxyInstance(
                        clazz.getClassLoader(),
                        new Class[]{clazz},
                        new ConsumerInvocationHandler(interfaceClass, serviceInfo)
                ));
    }
}
