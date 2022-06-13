package com.sunquakes.jsonrpc4j.client;

public interface JsonRpcClientHandlerInterface {

    Object handle(String method, Object[] args) throws Exception;
}
