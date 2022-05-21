package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
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
public class JsonRpcClientInvocationHandler<T> implements InvocationHandler {

    private Class<T> interfaceType;

    private JsonRpcClientHandlerInterface jsonRpcClientHandler;

    public JsonRpcClientInvocationHandler(Class<T> intefaceType, JsonRpcClientHandlerInterface jsonRpcClientHandler) {
        this.interfaceType = interfaceType;
        this.jsonRpcClientHandler = jsonRpcClientHandler;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return jsonRpcClientHandler.handle("test", args);
    }
}
