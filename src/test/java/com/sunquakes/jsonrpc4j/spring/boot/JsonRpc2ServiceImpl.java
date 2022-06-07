package com.sunquakes.jsonrpc4j.spring.boot;

public class JsonRpc2ServiceImpl implements IJsonRpc2Service {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        return a - b;
    }
}
