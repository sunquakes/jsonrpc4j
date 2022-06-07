package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.JsonRpcService;

@JsonRpcService
public interface IJsonRpc2Service {

    int add(int a, int b);

    int sub(int a, int b);
}
