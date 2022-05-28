package com.sunquakes.jsonrpc4j.client;

import java.io.IOException;

public interface JsonRpcClientHandlerInterface {

    Object handle(String method, Object[] args) throws Exception;
}
