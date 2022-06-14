package com.sunquakes.jsonrpc4j.service;

public class JsonRpc3ServiceImpl implements IJsonRpc3Service {

    @Override
    public Integer add(int a, int b) {
        return a + b;
    }
}
