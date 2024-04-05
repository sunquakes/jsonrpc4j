package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;

@JsonRpcClient(value = "JsonRpc", protocol = JsonRpcProtocol.TCP, url = "localhost:3203")
public interface IJsonRpcTcpClient2 {
    
    int add(int a, int b);

    String splice(String a, String b);
}
