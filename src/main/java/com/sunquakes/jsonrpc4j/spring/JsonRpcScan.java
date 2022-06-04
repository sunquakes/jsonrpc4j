package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.annotation.Import;

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
@Import({JsonRpcClientImportBeanDefinitionRegistrar.class})
public @interface JsonRpcScan {
}
