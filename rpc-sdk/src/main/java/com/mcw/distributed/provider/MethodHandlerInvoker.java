package com.mcw.distributed.provider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodHandlerInvoker {

    private static final Map<String, MethodHandle> HANDLE_CACHE_MAP = new ConcurrentHashMap<>();

    public static Object invoke(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Throwable {
        Class<?> clazz = target.getClass();
        String key = clazz.getName() + "#" + methodName + Arrays.toString(paramTypes);

        MethodHandle handle = HANDLE_CACHE_MAP.computeIfAbsent(key, k -> {
            try {
                Method method = clazz.getMethod(methodName, paramTypes);
                return MethodHandles.lookup().unreflect(method);
            } catch (Exception e) {
                throw new RuntimeException("创建 MethodHandle失败", e);
            }
        });

        return handle.bindTo(target).invokeWithArguments(args);
    }
}
