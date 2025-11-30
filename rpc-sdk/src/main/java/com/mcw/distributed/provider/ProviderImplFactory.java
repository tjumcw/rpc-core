package com.mcw.distributed.provider;


import java.util.HashMap;
import java.util.Map;

public class ProviderImplFactory {

    private static final Map<Class<?>, Object> PROVIDER_IMPL_MAP = new HashMap<>();


    public static void registerService(Class<?> interfaceClass, Object instance) {
        PROVIDER_IMPL_MAP.put(interfaceClass, instance);
    }

    public Object getServiceInstance(Class<?> clazz) {
        return PROVIDER_IMPL_MAP.get(clazz);
    }
}
