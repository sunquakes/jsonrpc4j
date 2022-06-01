package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.JsonRpcService;

@JsonRpcService
public interface IJsonRpc2Service {

    Integer add(int a, int b);

    Integer sub(int a, int b);
}
