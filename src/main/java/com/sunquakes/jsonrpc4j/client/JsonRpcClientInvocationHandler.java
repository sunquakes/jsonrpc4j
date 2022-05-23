package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.client
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
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
        System.out.println(methodPath);
        return jsonRpcClientHandler.handle(methodPath, args);
    }
}
