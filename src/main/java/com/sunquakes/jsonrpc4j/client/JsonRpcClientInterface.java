package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.exception.JsonRpcException;

public interface JsonRpcClientInterface {

    Object handle(String method, Object[] args) throws JsonRpcException;
}
