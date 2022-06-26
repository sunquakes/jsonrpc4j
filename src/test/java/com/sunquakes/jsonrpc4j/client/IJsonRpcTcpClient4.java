package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;

@JsonRpcClient(value = "JsonRpc", protocol = "tcp", url = "localhost:3204")
public interface IJsonRpcTcpClient4 {
    
    int add(int a, int b);

    String splice(String a, String b);
}
