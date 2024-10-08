package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.client.JsonRpcClientInterface;
import com.sunquakes.jsonrpc4j.client.JsonRpcClientInvocationHandler;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 1.0.0
 * @since 1.0.0
 **/
public class JsonRpcClientFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> interfaceType;

    private JsonRpcClientInterface jsonRpcClient;

    private String service;

    public JsonRpcClientFactoryBean(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public T getObject() {
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new JsonRpcClientInvocationHandler(jsonRpcClient, service));
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setJsonRpcClient(JsonRpcClientInterface jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;
    }

    public void setService(String service) {
        this.service = service;
    }
}
