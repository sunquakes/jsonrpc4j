package com.sunquakes.jsonrpc4j.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JsonRpcClientInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(proxy, args);
    }
}
