package com.mcw.distributed.consumer;

import com.mcw.distributed.request.ServiceInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerRefMapping {

    private static final Map<Class<?>, ServiceInfo> CONSUMER_REF_MAP = new HashMap<>();

    public static void putServiceInfo(Class<?> clazz, ServiceInfo serviceInfo) {
        CONSUMER_REF_MAP.put(clazz, serviceInfo);
    }

    public static ServiceInfo getServiceInfo(Class<?> clazz) {
        return CONSUMER_REF_MAP.get(clazz);
    }
}
