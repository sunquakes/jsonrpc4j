package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;

@JsonRpcClient(value = "JsonRpc", protocol = JsonRpcProtocol.HTTPS, url = "localhost:3205")
public interface IJsonRpcHttpsClient {
    
    int add(int a, int b);

    String splice(String a, String b);
}
