package com.sunquakes.jsonrpc4j.spring.server;

import com.sunquakes.jsonrpc4j.JsonRpcService;

@JsonRpcService
public interface IJsonRpcService {

    Integer add(int a, int b);
}
