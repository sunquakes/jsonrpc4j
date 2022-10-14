package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.JsonRpcService;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ArgsDto;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ResultDto;

@JsonRpcService("JsonRpc2")
public interface IJsonRpc2Service {

    int add(int a, int b);

    int sub(int a, int b);

    ResultDto add2(ArgsDto args);
}
