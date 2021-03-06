package com.sunquakes.jsonrpc4j.service;

import com.sunquakes.jsonrpc4j.JsonRpcService;

@JsonRpcService("JsonRpc")
public interface IJsonRpcService {

    Integer add(int a, int b);

    Integer sub(int a, int b);

    String splice(String a, String b);
}
