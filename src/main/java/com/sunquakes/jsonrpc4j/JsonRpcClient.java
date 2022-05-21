package com.sunquakes.jsonrpc4j;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface JsonRpcClient {

    /**
     * The name of the client
     * @return
     */
    String value() default "";
}
