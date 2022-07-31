package com.sunquakes.jsonrpc4j.client;

public interface JsonRpcClientInterface {

    Object handle(String method, Object[] args) throws Exception;
}
