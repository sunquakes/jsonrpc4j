package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;

@JsonRpcClient(value = "IntRpc", protocol = "http", url = "http://localhost:3232")
public interface IJsonRpcTcpClient3 {
    
    int add(int a, int b);
}
