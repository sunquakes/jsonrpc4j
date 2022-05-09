package com.sunquakes.jsonrpc4j.spring.server;

public class JsonRpcServiceImpl implements IJsonRpcService {

    @Override
    public String add(int a, int b) {
        return (a + b) + "";
    }
}
