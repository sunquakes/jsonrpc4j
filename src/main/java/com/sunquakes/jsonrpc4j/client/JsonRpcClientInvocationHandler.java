package com.sunquakes.jsonrpc4j.client;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
@Slf4j
public class JsonRpcClientInvocationHandler<T> implements InvocationHandler {

    private JsonRpcClientHandlerInterface jsonRpcClientHandler;

    private String service;

    public JsonRpcClientInvocationHandler(JsonRpcClientHandlerInterface jsonRpcClientHandler, String service) {
        this.jsonRpcClientHandler = jsonRpcClientHandler;
        this.service = service;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodPath = String.format("/%s/%s", service, method.getName());
        return jsonRpcClientHandler.handle(methodPath, args);
    }
}
