package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;

@JsonRpcClient(value = "JsonRpc", protocol = "https", url = "localhost:443")
public interface IJsonRpcHttpsClient {
    
    int add(int a, int b);

    String splice(String a, String b);
}
