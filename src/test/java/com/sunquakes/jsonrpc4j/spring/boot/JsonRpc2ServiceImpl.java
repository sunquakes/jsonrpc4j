package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.JsonRpcService;
import org.springframework.stereotype.Service;

public class JsonRpc2ServiceImpl implements IJsonRpc2Service {

    @Override
    public Integer add(int a, int b) {
        return a + b;
    }

    @Override
    public Integer sub(int a, int b) {
        return a - b;
    }
}
