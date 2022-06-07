package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.JsonRpcClient;

@JsonRpcClient(value = "JsonRpc2", protocol = "tcp", url = "localhost:3202")
public interface IJsonRpcClient {
    
    int add(int a, int b);

    int sub(int a, int b);
}
