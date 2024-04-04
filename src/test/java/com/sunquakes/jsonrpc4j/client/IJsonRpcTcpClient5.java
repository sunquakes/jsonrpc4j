package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;

@JsonRpcClient(value = "JsonRpc", protocol = JsonRpcProtocol.TCP, url = "localhost:3206", packageEof = "aaaa", packageMaxLength = 4096)
public interface IJsonRpcTcpClient5 {
    
    int add(int a, int b);

    String splice(String a, String b);
}
