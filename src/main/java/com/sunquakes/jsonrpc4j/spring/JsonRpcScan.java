package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.annotation.Import;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
@Import(JsonRpcClientImportBeanDefinitionRegistrar.class)
public @interface JsonRpcScan {
}
