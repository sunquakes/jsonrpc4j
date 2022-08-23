package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.JsonRpcClient;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;

@JsonRpcClient(value = "IntRpc", protocol = JsonRpcProtocol.http, url = "localhost:3232")
public interface IJsonRpcTcpClient3 {
    
    int add(int a, int b);
}
