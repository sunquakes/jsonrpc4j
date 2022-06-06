package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({JsonRpcImportBeanDefinitionRegistrar.class})
public @interface JsonRpcScan {

    /**
     * Base packages to scan for annotated jsonrpc4j components.
     * @return
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for annotated jsonrpc4j components.
     * @return
     */
    @AliasFor("value")
    String[] basePackages() default {};
}
