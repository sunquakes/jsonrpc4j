package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.annotation.Import;

@Import(JsonRpcClientImportBeanDefinitionRegistrar.class)
public @interface JsonRpcScan {
}
