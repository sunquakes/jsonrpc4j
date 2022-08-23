package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;

@JsonRpcClient(value = "JsonRpc", protocol = JsonRpcProtocol.tcp, url = "localhost:3201")
public interface IJsonRpcTcpClient {
    
    int add(int a, int b);

    String splice(String a, String b);
}
