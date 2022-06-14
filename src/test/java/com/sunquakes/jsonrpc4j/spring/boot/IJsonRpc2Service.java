package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.JsonRpcService;

@JsonRpcService("JsonRpc2")
public interface IJsonRpc2Service {

    int add(int a, int b);

    int sub(int a, int b);
}
