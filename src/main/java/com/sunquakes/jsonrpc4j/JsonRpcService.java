package com.sunquakes.jsonrpc4j;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface JsonRpcService {
    /**
     * The name of the server;
     * @return the server name
     */
    String value();
}
