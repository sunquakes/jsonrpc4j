package com.sunquakes.jsonrpc4j;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface JsonRpcClient {

    /**
     * The name of the client;
     * @return the client name
     */
    String value() default "";

    /**
     * The protocol of the client request to server;
     * @return the client protocol
     */
    JsonRpcProtocol protocol() default JsonRpcProtocol.HTTP;

    /**
     * The server url of the client request to server;
     * Include IP and port;
     * The format of url is 127.0.0.1:3200 or www.sunquakes.com
     * @return the server url
     */
    String url() default "";

    /**
     * The client package end of in tcp protocol;
     * @return the client package end of in tcp protocol
     */
    String packageEof() default "";

    /**
     * The client package max length in tcp protocol;
     * @return the client package max length in tcp protocol
     */
    int packageMaxLength() default 0;
}
