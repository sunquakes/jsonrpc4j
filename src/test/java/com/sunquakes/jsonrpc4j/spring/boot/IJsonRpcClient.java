package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.JsonRpcClient;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ArgsDto;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ResultDto;

@JsonRpcClient(value = "JsonRpc2", protocol = JsonRpcProtocol.TCP)
public interface IJsonRpcClient {
    
    int add(int a, int b);

    int sub(int a, int b);

    ResultDto add2(ArgsDto args);

    ResultDto add3(ArgsDto args);
}
