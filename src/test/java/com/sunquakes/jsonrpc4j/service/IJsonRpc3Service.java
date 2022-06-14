package com.sunquakes.jsonrpc4j.service;

import com.sunquakes.jsonrpc4j.JsonRpcService;

@JsonRpcService("json_rpc")
public interface IJsonRpc3Service {

    Integer add(int a, int b);
}
