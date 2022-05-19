package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;

@JsonRpcClient
public interface IJsonRpcClient {
    
    int add(int a, int b);
}
