package com.mcw.distributed.consumer;


import com.mcw.distributed.request.RequestInfo;
import com.mcw.distributed.request.ServiceInfo;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConsumerInvocationHandler implements InvocationHandler {

    private final Class<?> interfaceClass;
    private final ServiceInfo serviceInfo;

    public ConsumerInvocationHandler(Class<?> interfaceClass, ServiceInfo serviceInfo) {
        this.interfaceClass = interfaceClass;
        this.serviceInfo = serviceInfo;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setClassName(interfaceClass.getName());
        requestInfo.setMethodName(method.getName());
        requestInfo.setParams(args);
        requestInfo.setParamsType(method.getParameterTypes());

        Object result = null;
        try(Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serviceInfo.getHost(), serviceInfo.getPort()));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(requestInfo);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            result = objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
