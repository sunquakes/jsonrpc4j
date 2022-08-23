package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.JsonRpcClient;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;

@JsonRpcClient(value = "JsonRpc2", protocol = JsonRpcProtocol.tcp, url = "localhost:3202")
public interface IJsonRpcClient {
    
    int add(int a, int b);

    int sub(int a, int b);
}
