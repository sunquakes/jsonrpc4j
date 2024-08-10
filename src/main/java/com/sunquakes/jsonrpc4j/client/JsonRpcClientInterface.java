package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.exception.JsonRpcException;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 1.0.0
 * @since 1.0.0
 */
public interface JsonRpcClientInterface {

    Object handle(String method, Object[] args) throws JsonRpcException;
}
