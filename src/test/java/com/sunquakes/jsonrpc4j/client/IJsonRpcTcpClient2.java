package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;

@JsonRpcClient(value = "JsonRpc", protocol = "tcp", url = "localhost:3203")
public interface IJsonRpcTcpClient2 {
    
    int add(int a, int b);

    String splice(String a, String b);
}
