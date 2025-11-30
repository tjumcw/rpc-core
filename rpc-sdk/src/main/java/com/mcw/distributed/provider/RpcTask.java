package com.mcw.distributed.provider;


import com.mcw.distributed.request.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RpcTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RpcTask.class);

    private final Socket client;

    public RpcTask(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream())) {

            RequestInfo requestInfo = (RequestInfo) objectInputStream.readObject();
            logger.info("RpcTask -> requestInfo: {}", requestInfo);

            Class<?> clazz = Class.forName(requestInfo.getClassName());
            Object service = ProviderImplFactory.getServiceInstance(clazz);

            Object result = MethodHandlerInvoker.invoke(
                    service,
                    requestInfo.getMethodName(),
                    requestInfo.getParamsType(),
                    requestInfo.getParams()
            );

            objectOutputStream.writeObject(result);

        } catch (Throwable e) {
            logger.error("RpcTask 处理请求失败", e);
        }
    }
}
