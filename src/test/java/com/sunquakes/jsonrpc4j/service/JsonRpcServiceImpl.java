package com.sunquakes.jsonrpc4j.service;

public class JsonRpcServiceImpl implements IJsonRpcService {

    @Override
    public Integer add(int a, int b) {
        return a + b;
    }

    @Override
    public Integer sub(int a, int b) {
        return a - b;
    }

    @Override
    public String splice(String a, String b) {
        return a + b;
    }
}
