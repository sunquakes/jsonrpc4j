package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;

@JsonRpcClient(value = "JsonRpc", url = "localhost:3200")
public interface IJsonRpcHttpClient {
    
    int add(int a, int b);

    String splice(String a, String b);

    int methodNotFount();
}
