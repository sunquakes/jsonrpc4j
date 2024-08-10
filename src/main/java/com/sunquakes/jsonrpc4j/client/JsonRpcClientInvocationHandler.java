package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
public class JsonRpcClientInvocationHandler implements InvocationHandler {

    private final JsonRpcClientInterface jsonRpcClient;

    private final String service;

    public JsonRpcClientInvocationHandler(JsonRpcClientInterface jsonRpcClient, String service) {
        this.jsonRpcClient = jsonRpcClient;
        this.service = service;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodPath = String.format("/%s/%s", service, method.getName());
        return JSON.to(method.getReturnType(), jsonRpcClient.handle(methodPath, args));
    }
}
