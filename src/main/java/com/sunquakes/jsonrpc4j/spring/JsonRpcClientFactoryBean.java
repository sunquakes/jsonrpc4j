package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.client.JsonRpcClientHandlerInterface;
import com.sunquakes.jsonrpc4j.client.JsonRpcClientInvocationHandler;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
public class JsonRpcClientFactoryBean<T> implements FactoryBean<T> {

    private Class<T> interfaceType;

    private JsonRpcClientHandlerInterface jsonRpcClientHandler;

    public JsonRpcClientFactoryBean(Class<T> interfaceType, JsonRpcClientHandlerInterface jsonRpcClientHandler) {
        this.interfaceType = interfaceType;
        this.jsonRpcClientHandler = jsonRpcClientHandler;
    }

    @Override
    public T getObject() {
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new JsonRpcClientInvocationHandler(interfaceType, jsonRpcClientHandler));
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
