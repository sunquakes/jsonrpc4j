package com.sunquakes.jsonrpc4j.service;

import com.sunquakes.jsonrpc4j.JsonRpcService;

@JsonRpcService
public interface IJsonRpcService {

    Integer add(int a, int b);

    Integer sub(int a, int b);
}